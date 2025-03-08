package com.example.mifinanza

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class activityVerPrestamos : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_prestamos)
        dbHelper = DatabaseHelper(this)

        val lvPrestamos = findViewById<ListView>(R.id.lv_prestamos)
        val prestamos = dbHelper.obtenerPrestamosActivos()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, prestamos)
        lvPrestamos.adapter = adapter
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
    }
}