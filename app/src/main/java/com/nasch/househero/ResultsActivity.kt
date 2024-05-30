package com.nasch.househero

import ServicesAdapter
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nasch.househero.databinding.ActivityResultsBinding

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServicesAdapter // Adaptador personalizado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvServices

        val userLocation = intent.getStringExtra("userLocation")
        // Establecer la ubicación como texto en tvTitle
        binding.tvTitle.text = "Resultados en $userLocation"

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar el adaptador
        adapter = ServicesAdapter()

        // Establecer el adaptador en el RecyclerView
        recyclerView.adapter = adapter

        // Obtener los resultados de la actividad anterior
        val resultados = intent.getStringArrayListExtra("resultados")
        // Actualizar el adaptador con los resultados
        if (resultados != null) {
            adapter.actualizarResultados(resultados)
        }
    }
}

