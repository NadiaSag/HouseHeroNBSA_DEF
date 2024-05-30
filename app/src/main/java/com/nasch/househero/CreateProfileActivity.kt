package com.nasch.househero

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nasch.househero.databinding.ActivityCreateProfileBinding
import com.nasch.househero.dataclasses.Clientes
import com.nasch.househero.dataclasses.Municipio
import java.io.InputStreamReader

class CreateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProfileBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Firebase
        val cities = loadCitiesFromJson()
        val cityLabels = cities.map { it.label }
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityLabels)
        val autoCompleteCity = findViewById<AutoCompleteTextView>(R.id.autoCompleteCity)
        autoCompleteCity.setAdapter(cityAdapter)

        val rolesArray = resources.getStringArray(R.array.roles)
        val rolesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, rolesArray)
        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRoles.adapter = rolesAdapter

        binding.btNext.setOnClickListener {
            // Obtiene los valores del EditText y el Spinner
            val userName = binding.etUserName.text.toString()
            val userSurname = binding.etUserSurname.text.toString()
            val selectedRole = binding.spinnerRoles.selectedItem.toString()
            val email = binding.etEmail.text.toString()
            val phoneNumber = binding.etNumber.text.toString()
            val selectedCity = autoCompleteCity.text.toString()

            // Verifica si la opción seleccionada es "profesional"
            if (selectedRole.equals("soy profesional", ignoreCase = true)) {
                // Inicia ProfessionalActivity y pasa los datos
                val intent = Intent(this, ProfessionalActivity::class.java).apply {
                    putExtra("userName", userName)
                    putExtra("userSurname", userSurname)
                    putExtra("selectedRole", selectedRole)
                    putExtra("email", email)
                    putExtra("phoneNumber", phoneNumber)
                    putExtra("selectedCity", selectedCity)
                }
                startActivity(intent)
            } else {
                // Guarda los valores en la base de datos Firebase para un cliente
                saveUserInfo(userName, userSurname, selectedRole, email, phoneNumber, selectedCity)
            }
        }
    }

    private fun loadCitiesFromJson(): List<Municipio> {
        val inputStream = resources.openRawResource(R.raw.municipios)
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<Municipio>>() {}.type
        return Gson().fromJson(reader, type)
    }

    private fun saveUserInfo(
        userName: String,
        userSurname: String,
        selectedRole: String,
        email: String,
        phoneNumber: String,
        selectedCity: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                baseContext,
                "Usuario no autenticado.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (userName.isEmpty() || userSurname.isEmpty() || selectedRole.isEmpty() || email.isEmpty() || selectedCity.isEmpty()) {
            Toast.makeText(
                baseContext,
                "Por favor, complete todos los campos.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("Clientes").child(userId)
        val user = Clientes(userId, userName, userSurname, selectedRole, selectedCity)
        dbRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    baseContext,
                    "Información del usuario guardada.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    baseContext,
                    "Se produjo un error al guardar la información del usuario.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
