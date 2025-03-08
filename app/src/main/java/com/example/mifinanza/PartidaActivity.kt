package com.example.mifinanza

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import android.content.ContentValues
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class PartidaActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: SimpleAdapter
    private lateinit var spinnerTipo: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partida)

        dbHelper = DatabaseHelper(this)
        listView = findViewById(R.id.list_partidas)
        spinnerTipo = findViewById(R.id.spinner_tipo)

        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val btnAgregar = findViewById<Button>(R.id.btn_agregar)

        // Configurar el Spinner
        val tipos = arrayOf("Ingreso", "Egreso")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = spinnerAdapter

        btnAgregar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val tipo = if (spinnerTipo.selectedItem.toString() == "Ingreso") 1 else 0

            if (nombre.isNotEmpty()) {
                // Agregar partida a la base de datos
                val db = dbHelper.writableDatabase
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_PARTIDA_NOMBRE, nombre)
                    put(DatabaseHelper.COLUMN_PARTIDA_TIPO, tipo)
                }
                db.insert(DatabaseHelper.TABLE_PARTIDAS, null, values)
                db.close()
                Toast.makeText(this, "Partida agregada", Toast.LENGTH_SHORT).show()
                // Limpiar campos después de guardar
                etNombre.text.clear()
                spinnerTipo.setSelection(0)
                // Actualizar lista
                loadPartidas()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar partidas al iniciar la actividad
        loadPartidas()

        // Configurar clic largo para editar/eliminar
        listView.setOnItemLongClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as Map<String, String>
            val partidaId = selectedItem["id"]?.toInt()
            val partidaNombre = selectedItem["nombre"]
            val partidaTipo = selectedItem["tipo"]?.toInt()

            AlertDialog.Builder(this)
                .setTitle("Editar/Eliminar Partida")
                .setMessage("¿Qué deseas hacer con la partida '$partidaNombre'?")
                .setPositiveButton("Editar") { dialog, which ->
                    showEditDialog(partidaId, partidaNombre, partidaTipo)
                }
                .setNegativeButton("Eliminar") { dialog, which ->
                    deletePartida(partidaId)
                }
                .setNeutralButton("Cancelar", null)
                .show()

            true
        }
    }

    private fun loadPartidas() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PARTIDAS, null, null, null, null, null, null)
        val partidas = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_ID)).toString()
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_NOMBRE))
            val tipo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_TIPO)).toString()
            partidas.add(mapOf("id" to id, "nombre" to nombre, "tipo" to tipo))
        }
        cursor.close()
        db.close()

        adapter = SimpleAdapter(
            this,
            partidas,
            android.R.layout.simple_list_item_2,
            arrayOf("nombre", "tipo"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = adapter
    }

    private fun showEditDialog(partidaId: Int?, partidaNombre: String?, partidaTipo: Int?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_partida, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.et_nombre)
        val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinner_tipo)
        val tipos = arrayOf("Ingreso", "Egreso")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = spinnerAdapter

        etNombre.setText(partidaNombre)
        spinnerTipo.setSelection(partidaTipo ?: 0)

        AlertDialog.Builder(this)
            .setTitle("Editar Partida")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, which ->
                val nuevoNombre = etNombre.text.toString()
                val nuevoTipo = if (spinnerTipo.selectedItem.toString() == "Ingreso") 1 else 0

                if (partidaId != null && nuevoNombre.isNotEmpty()) {
                    val db = dbHelper.writableDatabase
                    val values = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_PARTIDA_NOMBRE, nuevoNombre)
                        put(DatabaseHelper.COLUMN_PARTIDA_TIPO, nuevoTipo)
                    }
                    db.update(DatabaseHelper.TABLE_PARTIDAS, values, "${DatabaseHelper.COLUMN_PARTIDA_ID}=?", arrayOf(partidaId.toString()))
                    db.close()
                    Toast.makeText(this, "Partida actualizada", Toast.LENGTH_SHORT).show()
                    loadPartidas()
                } else {
                    Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deletePartida(partidaId: Int?) {
        if (partidaId != null) {
            val db = dbHelper.writableDatabase
            db.delete(DatabaseHelper.TABLE_PARTIDAS, "${DatabaseHelper.COLUMN_PARTIDA_ID}=?", arrayOf(partidaId.toString()))
            db.close()
            Toast.makeText(this, "Partida eliminada", Toast.LENGTH_SHORT).show()
            loadPartidas()
        }
    }
}
