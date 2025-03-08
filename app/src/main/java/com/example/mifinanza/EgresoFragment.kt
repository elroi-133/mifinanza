package com.example.mifinanza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.ArrayAdapter
import android.text.Editable
import android.text.TextWatcher
import android.app.DatePickerDialog
import androidx.fragment.app.Fragment
import java.util.Calendar

class EgresoFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerPartida: Spinner
    private lateinit var etFecha: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_egreso, container, false)

        dbHelper = DatabaseHelper(requireContext())
        spinnerPartida = view.findViewById(R.id.spinner_partida)

        val etMonto = view.findViewById<EditText>(R.id.et_monto)
        val etTasa = view.findViewById<EditText>(R.id.et_tasa)
        val etDescripcion = view.findViewById<EditText>(R.id.et_descripcion)
        val btnGuardar = view.findViewById<Button>(R.id.btn_guardar)
        etFecha = view.findViewById(R.id.et_fecha)

        etFecha.setOnClickListener {
            showDatePicker()
        }

        addTextWatcher(etMonto)
        addTextWatcher(etTasa)
        loadPartidas(0)

        btnGuardar.setOnClickListener {
            guardarEgreso(etMonto, etTasa, etDescripcion)
        }

        return view
    }

    private fun guardarEgreso(etMonto: EditText, etTasa: EditText, etDescripcion: EditText) {
        val monto = parseFormattedValue(etMonto.text.toString())
        val tasa = parseFormattedValue(etTasa.text.toString())
        val descripcion = etDescripcion.text.toString()
        val fecha = etFecha.text.toString()
        val tipo = 0 // Tipo es 0 para egresos
        val partida = spinnerPartida.selectedItemId.toInt()
        if (monto != null && tasa != null && fecha.isNotEmpty() && descripcion.isNotEmpty()) {
            dbHelper.registrarMovimiento((monto * -1), tasa, descripcion, fecha, tipo, partida)
            Toast.makeText(requireContext(), "Egreso guardado", Toast.LENGTH_SHORT).show()
            etMonto.text.clear()
            etTasa.text.clear()
            etDescripcion.text.clear()
            etFecha.text.clear()
        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        try {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                    etFecha.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al seleccionar la fecha: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTextWatcher(editText: EditText) {
        editText.setText("0,00")
        editText.gravity = android.view.Gravity.END
        editText.setSelection(editText.text.length)
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val originalText = s.toString()
                val cleanString = originalText.replace("[^0-9]".toRegex(), "")
                val paddedString = cleanString.padStart(3, '0')
                var parteEntera = paddedString.dropLast(2)
                val parteDecimal = paddedString.takeLast(2)
                parteEntera = parteEntera.trimStart('0')
                if (parteEntera.isEmpty()) {
                    parteEntera = "0"
                }
                val formattedEntera = parteEntera.reversed().chunked(3).joinToString(".").reversed()
                val formattedText = "$formattedEntera,$parteDecimal"
                editText.setText(formattedText)
                editText.setSelection(formattedText.length)
                isFormatting = false
            }
        })
    }

    private fun parseFormattedValue(value: String): Double? {
        return try {
            val normalized = value.replace(".", "").replace(",", ".")
            normalized.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun loadPartidas(tipo: Int) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PARTIDAS, arrayOf(DatabaseHelper.COLUMN_PARTIDA_ID, DatabaseHelper.COLUMN_PARTIDA_NOMBRE),
            "${DatabaseHelper.COLUMN_PARTIDA_TIPO} = ?", arrayOf(tipo.toString()), null, null, null)
        val partidas = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_NOMBRE))
            partidas.add(nombre)
        }
        cursor.close()
        db.close()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partidas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPartida.adapter = adapter
    }
}
