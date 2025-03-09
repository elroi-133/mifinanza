package com.example.mifinanza

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.Calendar

class RegistrarPrestamoFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etFecha: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registrar_prestamo, container, false)

        dbHelper = DatabaseHelper(requireContext())
        val etPrestamista = view.findViewById<EditText>(R.id.et_prestamista)
        val etMonto = view.findViewById<EditText>(R.id.et_monto)
        val etTasa = view.findViewById<EditText>(R.id.et_tasa)
        val etTasaInteres = view.findViewById<EditText>(R.id.et_tasa_interes)
        val etPlazoDias = view.findViewById<EditText>(R.id.et_plazo_dias)
        val btnRegistrarPrestamo = view.findViewById<Button>(R.id.btn_registrar_prestamo)
        val btnVolverListado = view.findViewById<Button>(R.id.btn_volver_listado)
        etFecha = view.findViewById(R.id.et_fecha_prestamo)
        etFecha.setOnClickListener {
            showDatePicker()
        }
        // Agregar TextWatcher para formatear el monto y la tasa
        addTextWatcher(etMonto)
        addTextWatcher(etTasa)
        addTextWatcher(etTasaInteres)
        btnVolverListado.setOnClickListener {
            findNavController().navigateUp()
        }
        btnRegistrarPrestamo.setOnClickListener {
            val monto = parseFormattedValue(etMonto.text.toString())
            val tasa = parseFormattedValue(etTasa.text.toString())
            val tasaInteres = parseFormattedValue(etTasaInteres.text.toString())
            val fecha = etFecha.text.toString()
            val plazoDias = parseFormattedValue(etPlazoDias.text.toString())
            val prestamista = etPrestamista.text.toString()
            if (monto != null && tasa != null && tasaInteres != null && fecha.isNotEmpty() && prestamista.isNotEmpty() && plazoDias != null) {
                registrarPrestamo(
                    prestamista,
                    monto,
                    tasa,
                    tasaInteres,
                    fecha,
                    plazoDias.toInt()
                )
                // Limpiar campos después de guardar
                etMonto.text.clear()
                etTasa.text.clear()
                etPrestamista.text.clear()
                etTasaInteres.text.clear()
                etPlazoDias.text.clear()
                etFecha.text.clear()
            } else {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun registrarPrestamo(
        prestamista: String,
        monto: Double,
        tasa: Double,
        tasaInteres: Double,
        fechaPrestamo: String,
        plazoDias: Int
    ) {
        dbHelper.registrarPrestamo(prestamista, monto, tasaInteres, fechaPrestamo, plazoDias, requireContext())

        dbHelper.registrarMovimiento((monto*tasa), tasa, StringBuilder("PRESTAMO de ").append(prestamista).toString(), fechaPrestamo, 1, 2)
        Toast.makeText(requireContext(), "Préstamo registrado", Toast.LENGTH_SHORT).show()
    }

    private fun showDatePicker() {
        try {
            // Obtener la fecha actual
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Crear el DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(), // Usar el contexto del fragmento
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Formatear la fecha seleccionada como "YYYY-MM-DD"
                    val formattedDate = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1, // Los meses comienzan desde 0
                        selectedDay
                    )
                    // Asignar la fecha formateada al EditText
                    etFecha.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            // Mostrar el DatePickerDialog
            datePickerDialog.show()
        } catch (e: Exception) {
            // Capturar cualquier excepción y mostrar un mensaje de error
            Toast.makeText(requireContext(), "Error al seleccionar la fecha: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTextWatcher(editText: EditText) {
        // Establecer el valor predeterminado
        editText.setText("0,00")
        editText.gravity = android.view.Gravity.END // Alinear el texto a la derecha
        editText.setSelection(editText.text.length) // Mover el cursor al final

        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                // Obtener el texto actual
                val originalText = s.toString()

                // Eliminar caracteres no numéricos
                val cleanString = originalText.replace("[^0-9]".toRegex(), "")

                // Asegurarse de que el valor tenga al menos 3 dígitos (para los decimales)
                val paddedString = cleanString.padStart(3, '0')

                // Separar parte entera y decimal
                var parteEntera = paddedString.dropLast(2)
                val parteDecimal = paddedString.takeLast(2)

                // Eliminar ceros a la izquierda de la parte entera
                parteEntera = parteEntera.trimStart('0')
                if (parteEntera.isEmpty()) {
                    parteEntera = "0"
                }

                // Formatear el valor con separadores de miles
                val formattedEntera = parteEntera.reversed().chunked(3).joinToString(".").reversed()
                val formattedText = "$formattedEntera,$parteDecimal"

                // Actualizar el texto en el EditText
                editText.setText(formattedText)
                editText.setSelection(formattedText.length) // Mover el cursor al final

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
