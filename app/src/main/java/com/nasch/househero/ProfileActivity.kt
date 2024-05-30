package com.nasch.househero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nasch.househero.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btLogOut.setOnClickListener{
            logOut()
        }

        binding.btSearch.setOnClickListener {
            buscarUsuarios()
        }

        val userId = getUserId()
        userId?.let {
            // Verificar primero en profesionales
            verificarProfesional(it)
        }
    }

    private fun verificarProfesional(userId: String) {
        val professionalRef = FirebaseDatabase.getInstance().getReference("Profesionales").child(userId)
        professionalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(professionalSnapshot: DataSnapshot) {
                if (professionalSnapshot.exists()) {
                    // El usuario es un profesional
                    val userName = professionalSnapshot.child("userName").value.toString()
                    val userSurname = professionalSnapshot.child("userSurname").value.toString()
                    val selectedRole = professionalSnapshot.child("selectedRole").value.toString()
                    val location = professionalSnapshot.child("userLocation").value.toString()
                    val phone = professionalSnapshot.child("phoneNumber").value.toString()
                    val email = professionalSnapshot.child("email").value.toString()
                    val services = professionalSnapshot.child("selectedServices").value.toString()

                    // Actualizar la interfaz de usuario con los datos del profesional
                    updateUIProfessional(userName, userSurname, selectedRole, location, phone, email, services)
                } else {
                    // Si no es profesional, buscar en clientes
                    verificarCliente(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        })
    }

    private fun verificarCliente(userId: String) {
        val clientRef = FirebaseDatabase.getInstance().getReference("Clientes")
        clientRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (clientSnapshot in dataSnapshot.children) {
                    // Obtener el ID del nodo (que coincide con el ID del usuario)
                    val nodeId = clientSnapshot.key

                    // Verificar si el nodo actual coincide con el ID del usuario
                    if (nodeId == userId) {
                        // El usuario es un cliente, puedes obtener sus datos
                        val userName = clientSnapshot.child("userName").value.toString()
                        val userSurname = clientSnapshot.child("userSurname").value.toString()
                        val selectedRole = clientSnapshot.child("selectedRole").value.toString()
                        val location = clientSnapshot.child("userLocation").value.toString()

                        // Actualizar la interfaz de usuario con los datos del cliente
                        updateUIClient(userName, userSurname, selectedRole, location)
                        return  // Salir del bucle una vez que se encuentre el cliente
                    }
                }
                // Si el usuario no se encuentra en Clientes
                binding.cvUser.findViewById<TextView>(R.id.tvData).text = "Usuario no encontrado"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores de Firebase Database
                Log.e("TAG", "Error al leer datos", databaseError.toException())
            }
        })
    }


    private fun buscarUsuarios() {
        val intent = Intent(this, SearchServiceActivity::class.java)
        startActivity(intent)
    }

    private fun logOut() {
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
    }

    private fun updateUIClient(userName: String, userSurname: String, selectedRole: String, location: String) {
        // Actualizar el TextView dentro del CardView con los datos del cliente
        binding.cvInfo.findViewById<TextView>(R.id.tvUserInfo).text = "Hola, $userName $userSurname"
        binding.cvUser.findViewById<TextView>(R.id.tvData).text = "Rol: $selectedRole\nUbicación: $location"
    }

    private fun updateUIProfessional(userName: String, userSurname: String, selectedRole: String, location: String, phone: String, email: String, services: String) {
        val servicesList = services.removeSurrounding("[", "]").split(",").map { it.trim() }
        val servicesString = servicesList.joinToString(", ")

        binding.cvInfo.findViewById<TextView>(R.id.tvUserInfo).text = "Hola, $userName $userSurname"
        binding.cvUser.findViewById<TextView>(R.id.tvData).text = "Rol: $selectedRole\nUbicación: $location\nTeléfono: $phone\nEmail: $email\nServicios: $servicesString"
    }


    private fun getUserId(): String? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid
    }
}
