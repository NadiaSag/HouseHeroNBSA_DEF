package com.nasch.househero

import ServicesAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nasch.househero.SealedClasses.Services
import com.nasch.househero.databinding.ActivityProfessionalBinding
import com.nasch.househero.dataclasses.Profesionales

class ProfessionalActivity : AppCompatActivity() {
    private lateinit var servicesAdapter: ServicesAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityProfessionalBinding
    private val selectedServices: MutableSet<String> = mutableSetOf()

    private lateinit var profesionalesRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessionalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        profesionalesRef = database.getReference("Profesionales")

        // Configurar los CheckBox
        setupCheckBoxListeners()

        // Obtener los datos pasados desde CreateProfileActivity
        val userName = intent.getStringExtra("userName") ?: ""
        val userSurname = intent.getStringExtra("userSurname") ?: ""
        val selectedRole = intent.getStringExtra("selectedRole") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val selectedCity = intent.getStringExtra("selectedCity") ?: ""

        binding.btSave.setOnClickListener {
            saveDataToFirebase(userName, userSurname, selectedRole, email, phoneNumber, selectedCity, selectedServices)
        }
    }

    private val services = mutableListOf(
        Services.Fontaneria,
        Services.Cristaleria,
        Services.Piscinas,
        Services.PequeObras,
        Services.Jardineria,
        Services.Electricidad,
        Services.Construccion,
        Services.Climatizacion,
        Services.Ascensores,
        Services.Cerrajeria,
        Services.Limpieza,
        Services.Carpinteria,
        Services.Pintura
    )

    private fun onCategoriesSelected(position: Int) {
        services[position].isSelected = !services[position].isSelected
        servicesAdapter.notifyItemChanged(position)
    }

    private fun setupCheckBoxListeners() {
        // Obtener referencias a todos los CheckBox
        val checkBoxes = arrayOf(
            binding.cbFontaneria,
            binding.cbCristaleria,
            binding.cbPiscinas,
            binding.cbElectricidad,
            binding.cbJardineria,
            binding.cbConstruccion,
            binding.cbCarpinteria,
            binding.cbClimatizacion,
            binding.cbAscensores,
            binding.cbCerrajeria,
            binding.cbLimpieza,
            binding.cbPintura
        )

        // Configurar el Listener para cada CheckBox
        for (checkBox in checkBoxes) {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val serviceName = checkBox.text.toString()
                if (isChecked) {
                    selectedServices.add(serviceName)
                } else {
                    selectedServices.remove(serviceName)
                }
            }
        }
    }

    private fun saveDataToFirebase(
        username: String,
        userSurname: String,
        selectedRole: String,
        email: String,
        phoneNumber: String,
        selectedCity: String,
        selectedServices: MutableSet<String>
    ) {
        // Obtiene una referencia al nodo del usuario actual
        val userId = getUserId() ?: return
        val userRef = profesionalesRef.child(userId)

        // Crea un objeto Profesionales con los datos del usuario y la lista de servicios seleccionados
        val profesional = Profesionales(
            userId,
            username,
            userSurname,
            selectedRole,
            email,
            phoneNumber,
            selectedCity,
            selectedServices.toList() // Convierte el conjunto de servicios a una lista
        )

        // Guarda el objeto Profesionales en Firebase
        userRef.setValue(profesional).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Información del usuario guardada.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Se produjo un error al guardar la información del usuario.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
    }
}
