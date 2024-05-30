package com.nasch.househero

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nasch.househero.ReviewRecycler.Review
import com.nasch.househero.ReviewRecycler.ReviewAdapter


class ProfessionalInfoActivity : AppCompatActivity() {
    private lateinit var tvProfessionalInfo: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhoneNum: TextView
    private lateinit var tvLocation: TextView
    private var professionalId: String = "" // Initialize professionalId as an empty string

    private lateinit var professionalName: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviews = mutableListOf<Review>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professional_info)

        val resultado = intent.getStringExtra("resultado")
        professionalName = resultado?.split(",")?.get(0)?.trim() ?: ""

        val cardReview = findViewById<CardView>(R.id.btReview)
        cardReview.setOnClickListener {
            showReviewDialog(professionalName, professionalId)
        }

        tvProfessionalInfo = findViewById(R.id.tvProfessionalInfo)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhoneNum = findViewById(R.id.tvPhoneNum)
        tvLocation = findViewById(R.id.tvLocation)
        recyclerView = findViewById(R.id.recyclerView)

        tvProfessionalInfo.text = resultado

        if (professionalName.isNotBlank()) {
            val nameParts = professionalName.split(" ")
            if (nameParts.size >= 2) {
                val userName = nameParts[0]
                val userSurname = nameParts[1]

                fetchProfessionalDetails(userName, userSurname)
            } else {
                Log.e("ProfessionalInfoActivity", "Invalid professional name format: $professionalName")
            }
        }

        reviewAdapter = ReviewAdapter(reviews)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reviewAdapter
    }

    private fun fetchProfessionalDetails(userName: String, userSurname: String) {
        val query = FirebaseDatabase.getInstance().getReference("Profesionales")
            .orderByChild("userName")
            .equalTo(userName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (professionalSnapshot in snapshot.children) {
                        professionalId = professionalSnapshot.child("userId").value.toString()
                        val email = professionalSnapshot.child("email").value?.toString()
                        val phoneNum = professionalSnapshot.child("phoneNumber").value?.toString()
                        val location = professionalSnapshot.child("userLocation").value?.toString()

                        // Aquí puedes verificar si los valores son nulos antes de establecer los TextViews
                        email?.let { tvEmail.text = it }
                        phoneNum?.let { tvPhoneNum.text = it }
                        location?.let { tvLocation.text = it }

                        Log.d("ProfessionalInfoActivity", "Fetched professionalId: $professionalId")
                        fetchReviews(professionalId)
                        return
                    }
                } else {
                    Log.e("ProfessionalInfoActivity", "No professional found with the name: $userName $userSurname")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfessionalInfoActivity", "Error fetching professional details: ${error.message}")
            }
        })
    }



    private fun fetchReviews(professionalId: String) {
        val reviewQuery = FirebaseDatabase.getInstance().getReference("reviews")
            .orderByChild("userIdProf")
            .equalTo(professionalId)

        reviewQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    reviews.clear()
                    for (reviewSnapshot in snapshot.children) {
                        val rating = reviewSnapshot.child("rating").value.toString().toFloat()
                        val comment = reviewSnapshot.child("comment").value.toString()
                        val clientId = reviewSnapshot.child("userIdCli").value.toString()

                        Log.d("ProfessionalInfoActivity", "Review found with clientId: $clientId")

                        fetchClientDetails(clientId) { clientName, clientRole ->
                            val review = Review(clientName, clientRole, comment, rating)
                            reviews.add(review)
                            Log.d("ProfessionalInfoActivity", "Review added: $review")
                            reviewAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfessionalInfoActivity, "Error en mostrar reseñas", Toast.LENGTH_SHORT).show()
                Log.e("ProfessionalInfoActivity", "Error fetching reviews: ${error.message}")
            }
        })
    }

    private fun fetchClientDetails(clientId: String, callback: (String, String) -> Unit) {
        Log.d("ProfessionalInfoActivity", "Fetching client details for clientId: $clientId")
        val clientQuery = FirebaseDatabase.getInstance().getReference("Clientes")
            .child(clientId)

        clientQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val clientName = snapshot.child("userName").value.toString() + " " +
                            snapshot.child("userSurname").value.toString()
                    val clientRole = snapshot.child("selectedRole").value.toString()
                    Log.d("ProfessionalInfoActivity", "Client found: Name = $clientName, Role = $clientRole")
                    callback(clientName, clientRole)
                } else {
                    Log.e("ProfessionalInfoActivity", "No client found with the ID: $clientId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfessionalInfoActivity", "Error fetching client details: ${error.message}")
            }
        })
    }


    private fun showReviewDialog(professionalName: String, professionalId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Deja una reseña")

        val dialogLayout = layoutInflater.inflate(R.layout.rating_bar, null)
        val ratingBar = dialogLayout.findViewById<RatingBar>(R.id.ratingBar)
        val commentEditText = dialogLayout.findViewById<EditText>(R.id.commentEditText)

        builder.setView(dialogLayout)

        builder.setPositiveButton("Enviar") { dialog, which ->
            val rating = ratingBar.rating
            val comment = commentEditText.text.toString().trim()

            val clienteId = FirebaseAuth.getInstance().currentUser?.uid

            if (clienteId == null) {
                Toast.makeText(this, "No se pudo obtener la información del usuario.", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            Log.d("ProfessionalInfoActivity", "Sending review for professionalId: $professionalId with clientId: $clienteId")
            val reviewRef = FirebaseDatabase.getInstance().getReference("reviews").push()

            val reviewData = hashMapOf(
                "userIdProf" to professionalId,
                "userCompleteName" to professionalName,
                "userIdCli" to clienteId,
                "rating" to rating,
                "comment" to comment
            )

            reviewRef.setValue(reviewData)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Gracias por tu reseña!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al enviar la reseña. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
                }
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}