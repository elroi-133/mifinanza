package com.example.mifinanza

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.Calendar

class DeudaRegistrarPagoFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etFecha: EditText
    private lateinit var spinnerDeuda: Spinner
    private lateinit var btnVolverListado: Button
    private var deudaIdRecibido: Int = -1 // Variable para almacenar el prestamoId recibido
    private lateinit var deudasMap: Map<String, Int> // Mapa para almacenar nombres y IDs de préstamos


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_deuda_registrar_pago, container, false)
        dbHelper = DatabaseHelper(requireContext())
        spinnerDeuda = view.findViewById(R.id.spinner_deuda)

        val etMonto = view.findViewById<EditText>(R.id.et_monto)
        val etTasa = view.findViewById<EditText>(R.id.et_tasa)
        etFecha = view.findViewById(R.id.et_fecha)
        etFecha.setOnClickListener {
            showDatePicker()
        }
        deudaIdRecibido = DeudaRegistrarPagoFragmentArgs.fromBundle(requireArguments()).deudaId

        // Agregar TextWatcher para formatear el monto y la tasa
        addTextWatcher(etMonto)
        addTextWatcher(etTasa)

        val btnRegistrarPago = view.findViewById<Button>(R.id.btn_registrar_pago_deuda)
        btnRegistrarPago.setOnClickListener {
            registrarPago(etMonto, etTasa)
        }
        btnVolverListado = view.findViewById<Button>(R.id.btn_volver_listadoDeuda)

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeuda()
        btnVolverListado.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    fun registrarPago(etMonto: EditText, etTasa: EditText) {
        val selectedNombre = spinnerDeuda.selectedItem.toString()
        val deudaId = deudasMap[selectedNombre] ?: -1 // Obtener el ID del mapa
        if(deudaId == -1){
            Toast.makeText(requireContext(), "Error al obtener la deuda", Toast.LENGTH_SHORT).show()
            return
        }
        val monto = parseFormattedValue(etMonto.text.toString())
        val tasa = parseFormattedValue(etTasa.text.toString())
        val descripcion = "Pago deuda ID: $deudaId"
        val fecha = etFecha.text.toString()
        val tipo = 0 // Tipo es 0 para egresos
        val partida = dbHelper.obtenerIdPartida( "Deuda", tipo)
        if (monto != null && tasa != null && partida != null && fecha.isNotEmpty() && descripcion.isNotEmpty()) {
            // Guardar en la base de datos
            dbHelper.registrarMovimiento((monto * -1), tasa, descripcion, fecha, tipo, partida, ((monto * -1)/tasa))
            dbHelper.actualizarEstadoDeuda(deudaId, monto/tasa)
            Toast.makeText(requireContext(), "Pago a Deuda registrado con exito", Toast.LENGTH_SHORT).show()
            etMonto.text.clear()
            etTasa.text.clear()
            etFecha.text.clear()

        } else {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
        }
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

    private fun loadDeuda() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_DEUDAS,
            arrayOf(DatabaseHelper.COLUMN_DEUDA_ID, DatabaseHelper.COLUMN_DEUDA_DESCRIPCION),
            "${DatabaseHelper.COLUMN_ESTADO} = ?",
            arrayOf("Activo"),
            null,
            null,
            null
        )
        val deudas = mutableListOf<String>()
        deudasMap = mutableMapOf() // Inicializar el mapa
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEUDA_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DEUDA_DESCRIPCION))
            deudas.add(nombre)
            deudasMap = deudasMap.plus(Pair(nombre, id)) // Agregar al mapa
        }
        cursor.close()
        db.close()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, deudas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeuda.adapter = adapter

        // Preseleccionar el préstamo si prestamoIdRecibido es válido
        if (deudaIdRecibido != -1) {
            val nombreDeuda = deudasMap.filter { it.value == deudaIdRecibido }.keys.firstOrNull()
            if (nombreDeuda != null) {
                val position = deudas.indexOf(nombreDeuda)
                spinnerDeuda.setSelection(position)
            }
        }
    }

    companion object {
        fun newInstance(position: Int): PrestamoRegistrarPagoFragment {
            val fragment = PrestamoRegistrarPagoFragment()
            val args = Bundle().apply {
                putInt("id", position)
            }
            fragment.arguments = args
            return fragment
        }
    }
}