package com.example.mifinanza

import android.app.DatePickerDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegistrarPagoPrestamoActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etFecha: EditText
    private lateinit var spinnerPrestamo: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_pago_prestamo)
        dbHelper = DatabaseHelper(this)
        spinnerPrestamo = findViewById(R.id.spinner_prestamo)

        val etMonto = findViewById<EditText>(R.id.et_monto)
        val etTasa = findViewById<EditText>(R.id.et_tasa)
        etFecha = findViewById(R.id.et_fecha)
        etFecha.setOnClickListener {
            showDatePicker()
        }
        // Agregar TextWatcher para formatear el monto y la tasa
        addTextWatcher(etMonto)
        addTextWatcher(etTasa)
        loadPrestamo()

        val btnRegistrarPago = findViewById<Button>(R.id.btn_registrar_pago_prestamo)
        btnRegistrarPago.setOnClickListener {
            registrarPago(etMonto,etTasa)
        }
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
    }

    fun registrarPago(etMonto: EditText,etTasa: EditText) {
        val prestamoId = spinnerPrestamo.selectedItemId.toInt()
        val monto = parseFormattedValue(etMonto.text.toString())
        val tasa = parseFormattedValue(etTasa.text.toString())
        val descripcion = "Pago préstamo ID: $prestamoId"
        val fecha = etFecha.text.toString()
        val tipo = 0 // Tipo es 0 para egresos
        val partida = 2
        if (monto != null && tasa != null && fecha.isNotEmpty() && descripcion.isNotEmpty()) {
            // Guardar en la base de datos
            dbHelper.registrarMovimiento((monto*-1), tasa, descripcion, fecha, tipo, partida)
        }
        // Calcular el saldo pendiente del préstamo
        val saldoPendiente = calcularSaldoPendiente(prestamoId)
        if (saldoPendiente <= 0) {
            // Actualizar el estado del préstamo a "Pagado"
            dbHelper.actualizarPrestamo(prestamoId)
            // cancelar las notificaciones de ese prestamo
            cancelarNotificacion(prestamoId)
        }
    }

    fun calcularSaldoPendiente(prestamoId: Int): Double {
        // Calcular el saldo pendiente
        return dbHelper.calcularSaldoPendientePorPagarPrestamo(prestamoId)
    }

    fun cancelarNotificacion(prestamoId: Int) {
        val context = this // Obtén el contexto del fragmento
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PrestamoNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prestamoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
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
                this, // Usar el contexto de la actividad
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
            Toast.makeText(this, "Error al seleccionar la fecha: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun loadPrestamo() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PRESTAMOS, arrayOf(DatabaseHelper.COLUMN_PRESTAMO_ID, DatabaseHelper.COLUMN_PRESTAMISTA),
            "${DatabaseHelper.COLUMN_ESTADO} = ?", arrayOf("Activo"), null, null, null)
        val partidas = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRESTAMISTA))
            partidas.add(nombre)
        }
        cursor.close()
        db.close()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, partidas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrestamo.adapter = adapter
    }
}