package com.nasch.househero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.nasch.househero.databinding.ActivityResetpasswordBinding

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityResetpasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetpasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.btResetPassword.setOnClickListener{
            sendEmailVerification()
        }
    }

    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                Toast.makeText(
                    baseContext,
                    "Email de verificación enviado. Consulte su correo electrónico",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        // [END send_email_verification]
    }
}