package com.example.mifinanza

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.ContentValues
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.Calendar

class DeudasAgregarFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var spinnerPartida: Spinner
    private lateinit var etFecha: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_deudas_agregar, container, false)

        dbHelper = DatabaseHelper(requireContext())
        spinnerPartida = view.findViewById(R.id.spinner_partida)
        etFecha = view.findViewById(R.id.et_fecha)

        etFecha.setOnClickListener {
            showDatePicker()
        }
        val etMonto = view.findViewById<EditText>(R.id.et_monto)
        val etDescripcion = view.findViewById<EditText>(R.id.et_descripcion)
        val btnGuardar = view.findViewById<Button>(R.id.btn_guardar)
        val btnRegresar = view.findViewById<Button>(R.id.btn_regresar)

        addTextWatcher(etMonto)
        loadPartidasEgresos()

        btnGuardar.setOnClickListener {
            guardarDeuda(etMonto,etDescripcion)
        }

        btnRegresar.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }
    private fun guardarDeuda(etMonto: EditText, etDescripcion: EditText){
        val monto = parseFormattedValue(etMonto.text.toString())
        val descripcion = etDescripcion.text.toString()
        val fecha = etFecha.text.toString()
        val partidaSeleccionada = spinnerPartida.selectedItem as Partida
        val partida = partidaSeleccionada.id
        if (monto != null && partida != null && fecha.isNotEmpty() && descripcion.isNotEmpty()) {
            dbHelper.guardarDeuda(
                descripcion,
                monto,
                fecha,
                partida
            )
            Toast.makeText(requireContext(), "Deuda registrada con exito", Toast.LENGTH_SHORT).show()
            etMonto.text.clear()
            etDescripcion.text.clear()
            etFecha.text.clear()
        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
        }

    }
    private fun loadPartidasEgresos() {
        val partidas = dbHelper.loadPartidas(0)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partidas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPartida.adapter = adapter
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

}