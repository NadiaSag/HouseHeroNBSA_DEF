package com.nasch.househero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nasch.househero.databinding.ActivitySearchServiceBinding
import com.nasch.househero.dataclasses.Municipio
import java.io.InputStreamReader

class SearchServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchServiceBinding
    private val selectedServices: MutableSet<String> = mutableSetOf()
    private val resultados: MutableList<String> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCheckBoxListeners()
        val cities = loadCitiesFromJson()
        val cityLabels = cities.map { it.label }
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityLabels)
        val autoCompleteCity = findViewById<AutoCompleteTextView>(R.id.autoCompleteCity)
        autoCompleteCity.setAdapter(cityAdapter)
        binding.btSave.setOnClickListener {
            val userLocation = autoCompleteCity.text.toString()
            if (userLocation.isNotEmpty()) {
                buscarUsuarios(userLocation)

            } else {
                Toast.makeText(
                    baseContext,
                    "Por favor, ingresa una ubicación.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        val userId = getUserId() // Obtener el ID del usuario actual
        userId?.let {
            FirebaseDatabase.getInstance().getReference("Clientes").child(
                it
            )
        }?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userName = snapshot.child("userName").value.toString()
                    val userSurname = snapshot.child("userSurname").value.toString()
                    val selectedRole = snapshot.child("selectedRole").value.toString()

                    // Actualizar la interfaz de usuario con los datos del usuario
                    updateUI(userName, userSurname, selectedRole)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        })

    }
    private fun loadCitiesFromJson(): List<Municipio> {
        val inputStream = resources.openRawResource(R.raw.municipios)
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<Municipio>>() {}.type
        return Gson().fromJson(reader, type)
    }

    private fun updateUI(userName: String, userSurname: String, selectedRole: String) {
        // Actualizar el TextView dentro del CardView con los datos del usuario
        binding.cvInfo.findViewById<TextView>(R.id.tvGreetings).text = "Hola, $userName $userSurname"
    }

    private fun getUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
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
                // Obtener el texto asociado al CheckBox
                val serviceName = checkBox.text.toString()

                // Actualizar selectedServices dependiendo del estado del CheckBox
                if (isChecked) {
                    selectedServices.add(serviceName)
                } else {
                    selectedServices.remove(serviceName)
                }

            }
        }
    }

    private fun buscarUsuarios(userLocation: String) {
        val query = FirebaseDatabase.getInstance().getReference("Profesionales")

        query.orderByChild("userLocation").equalTo(userLocation)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val resultados = mutableListOf<String>()

                    for (userSnapshot in snapshot.children) {
                        val servicesSnapshot = userSnapshot.child("selectedServices")
                        val userServices = mutableListOf<String>()
                        for (serviceSnapshot in servicesSnapshot.children) {
                            userServices.add(serviceSnapshot.value.toString())
                        }

                        // Verificar si al menos uno de los servicios seleccionados coincide
                        // con los servicios que realiza el profesional
                        if (userServices.any { it in selectedServices }) {
                            val userName = userSnapshot.child("userName").value.toString()
                            val userSurname = userSnapshot.child("userSurname").value.toString()
                            val servicios = userServices.joinToString(", ") // Unir servicios en un string separado por comas
                            val resultado =
                                "$userName $userSurname,\n Servicios que realiza: $servicios"
                            resultados.add(resultado)
                        }
                    }

                    iniciarResultsActivity(resultados.toList(), userLocation)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SearchServiceActivity", "Error al buscar usuarios: ${error.message}")
                }
            })
    }





    private fun iniciarResultsActivity(resultados: List<String>, userLocation: String) {
        // Crear un Intent para iniciar ResultsActivity
        val intent = Intent(this, ResultsActivity::class.java)
        // Pasar los resultados como extras en el Intent
        intent.putStringArrayListExtra("resultados", ArrayList(resultados))
        // Pasar la ubicación como extra en el Intent
        intent.putExtra("userLocation", userLocation)
        // Iniciar la actividad
        startActivity(intent)
    }

}
