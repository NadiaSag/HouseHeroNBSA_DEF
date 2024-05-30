package com.nasch.househero

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.nasch.househero.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth //aquí hay dos imports, he seleccionado el acabado en ktx

        binding.btCreateAccount.setOnClickListener {
            createAccount()
        }
        binding.btCreateAccount.alpha = 0.5f
        binding.btCreateAccount.isEnabled = false
        binding.cbConditions.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Checkbox marcado, habilitar el CardView
                binding.btCreateAccount.alpha = 1.0f
                binding.btCreateAccount.isEnabled = true
            }else{
                binding.btCreateAccount.alpha = 0.5f
                binding.btCreateAccount.isEnabled = false
            }
        }


    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }
    private fun createAccount() {

        var email = binding.etUserEmail.text.toString()
        var password = binding.etPassword.text.toString()
        var confirmPass = binding.etConfirmPassword.text.toString()
        Log.d("PASSWORD_CHECK", "Password: $password")
        Log.d("PASSWORD_CHECK", "Confirm Password: $confirmPass")
        if(password == confirmPass) {
            // [START create_user_with_email]
            auth.createUserWithEmailAndPassword(email.toString(), password.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(
                            baseContext,
                            "Authentication success.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val user = auth.currentUser
                        updateUI(user)
                        val intent = Intent(this, CreateProfileActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null)
                    }
                }
       } else {
            Toast.makeText(baseContext, "La contraseña no coincide", Toast.LENGTH_SHORT).show()
            binding.etPassword.text.clear()
            binding.etConfirmPassword.text.clear()
        }
        // [END create_user_with_email]
    }



    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // Email Verification sent
            }
        // [END send_email_verification]
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    private fun reload() {
    }

    companion object {
        const val TAG = "EmailPassword"
    }
}