package com.example.mifinanza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.ContentValues
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AgregarPartidaFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerTipo: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar_partida, container, false)

        dbHelper = DatabaseHelper(requireContext())
        spinnerTipo = view.findViewById(R.id.spinner_tipo)

        val etNombre = view.findViewById<EditText>(R.id.et_nombre)
        val btnGuardar = view.findViewById<Button>(R.id.btn_guardar)
        val btnRegresar = view.findViewById<Button>(R.id.btn_regresar)

        // Configurar el Spinner
        val tipos = arrayOf("Ingreso", "Egreso")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = spinnerAdapter

        btnGuardar.setOnClickListener {
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
                Toast.makeText(requireContext(), "Partida agregada", Toast.LENGTH_SHORT).show()
                // Limpiar campos después de guardar
                etNombre.text.clear()
                spinnerTipo.setSelection(0)
            } else {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegresar.setOnClickListener {
            findNavController().navigateUp()
            //findNavController().navigate(R.id.action_registrarPrestamoFragment_to_listaPrestamosFragment)
        }

        return view
    }
}

