package com.example.mifinanza

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrestamosActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prestamos)
        dbHelper = DatabaseHelper(this)

        // Configurar el botón para registrar préstamos
        val btnRegistrarPrestamo = findViewById<Button>(R.id.btn_registrar_prestamo)

        btnRegistrarPrestamo.setOnClickListener {
            val intent = Intent(this, RegistrarPrestamoActivity::class.java)
            startActivity(intent)
        }

        // Configurar la lista de préstamos
        val lvPrestamos = findViewById<ListView>(R.id.lv_prestamos)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        lvPrestamos.adapter = adapter

        // Cargar préstamos
        cargarPrestamos()

        // Manejar clics en la lista
        lvPrestamos.setOnItemClickListener { _, _, position, _ ->
            val prestamoId = obtenerIdPrestamoDesdePosicion(position)
            mostrarOpcionesPrestamo(prestamoId)
        }
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
    }
    override fun onResume() {
        super.onResume()
        cargarPrestamos() // Recargar préstamos al volver a la actividad
    }

    private fun cargarPrestamos() {
        val prestamos = dbHelper.obtenerTodosLosPrestamos()
        adapter.clear()
        adapter.addAll(prestamos)
    }

    private fun obtenerIdPrestamoDesdePosicion(position: Int): Int {
        val item = adapter.getItem(position)
        return item?.split(" - ")?.get(0)?.replace("ID: ", "")?.toInt() ?: -1
    }
    private fun mostrarOpcionesPrestamo(prestamoId: Int) {
        val opciones = arrayOf("Asociar pago", "Eliminar préstamo")
        AlertDialog.Builder(this)
            .setTitle("Opciones")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> asociarPago(prestamoId)
                    1 -> eliminarPrestamo(prestamoId)
                }
            }
            .show()
    }

    private fun asociarPago(prestamoId: Int) {
        val intent = Intent(this, RegistrarPagoPrestamoActivity::class.java).apply {
            putExtra("prestamo_id", prestamoId)
        }
        startActivity(intent)
    }

    private fun eliminarPrestamo(prestamoId: Int) {
        if (dbHelper.eliminarPrestamo(prestamoId)) {
            Toast.makeText(this, "Préstamo eliminado", Toast.LENGTH_SHORT).show()
            cargarPrestamos()
        } else {
            Toast.makeText(this, "No se puede eliminar el préstamo (tiene pagos asociados)", Toast.LENGTH_SHORT).show()
        }
    }
}