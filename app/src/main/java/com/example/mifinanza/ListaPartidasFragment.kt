package com.example.mifinanza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AlertDialog
import android.content.ContentValues

import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment

class ListaPartidasFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: SimpleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_partidas, container, false)

        dbHelper = DatabaseHelper(requireContext())
        listView = view.findViewById(R.id.list_partidas)
        val btnAgregarPartida = view.findViewById<Button>(R.id.btn_agregar_partida)

        btnAgregarPartida.setOnClickListener {
            // Obtenemos el NavController desde el NavHostFragment en activity_main.xml
            val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val action = ListaPartidasFragmentDirections.actionListaPartidasFragmentToAgregarPartidaFragment()
            navController.navigate(action)
        }

        loadPartidas()

        listView.setOnItemLongClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as Map<String, String>
            val partidaId = selectedItem["id"]?.toInt()
            val partidaNombre = selectedItem["nombre"]
            val partidaTipo = selectedItem["tipo"]?.toInt()

            AlertDialog.Builder(requireContext())
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

        return view
    }

    private fun showEditDialog(partidaId: Int?, partidaNombre: String?, partidaTipo: Int?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_partida, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.et_nombre)
        val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinner_tipo)
        val tipos = arrayOf("Ingreso", "Egreso")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = spinnerAdapter

        etNombre.setText(partidaNombre)
        partidaTipo?.let {
            spinnerTipo.setSelection(it)
        }

        AlertDialog.Builder(requireContext())
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
                    Toast.makeText(requireContext(), "Partida actualizada", Toast.LENGTH_SHORT).show()
                    loadPartidas()
                } else {
                    Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadPartidas() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PARTIDAS, null, null, null, null, null, null)
        val partidas = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_ID)).toString()
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_NOMBRE))
            val tipo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_TIPO))
            val tipoTexto = when (tipo) {
                0 -> "Egreso"
                1 -> "Ingreso"
                2 -> "Préstamo"
                else -> "Desconocido"
            }
            partidas.add(mapOf("id" to id,"nombre" to nombre, "tipo" to tipoTexto))
        }
        cursor.close()
        db.close()

        adapter = SimpleAdapter(
            requireContext(),
            partidas,
            android.R.layout.simple_list_item_2,
            arrayOf("nombre", "tipo"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = adapter
    }

    private fun deletePartida(partidaId: Int?) {
        if (partidaId != null) {
            val db = dbHelper.writableDatabase
            db.delete(DatabaseHelper.TABLE_PARTIDAS, "${DatabaseHelper.COLUMN_PARTIDA_ID}=?", arrayOf(partidaId.toString()))
            db.close()
            Toast.makeText(requireContext(), "Partida eliminada", Toast.LENGTH_SHORT).show()
            loadPartidas()
        }
    }
}
