package com.nasch.househero.AutocompleteTextView

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.nasch.househero.CreateProfileActivity
import com.nasch.househero.LoginActivity
import com.nasch.househero.MainActivity
import com.nasch.househero.R

class LocationActivity : AppCompatActivity() {
    private val startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                Log.i(MainActivity.TAG, "Place: ${place.name}, ${place.id}")
                val intent = Intent(this, CreateProfileActivity::class.java)
                startActivity(intent)
            }
        } else if (result.resultCode == RESULT_CANCELED) {
            // The user canceled the operation.
            Log.i(MainActivity.TAG, "User canceled autocomplete")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        Places.initialize(applicationContext, "AIzaSyC20gyGGjJs29RBOMep9wdKFKXG81CqwFY ")

// Set the fields to specify which types of place data to return after the user has made a selection.
        val fields = listOf(Place.Field.ID, Place.Field.NAME)

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)
        startAutocomplete.launch(intent)
    }
}