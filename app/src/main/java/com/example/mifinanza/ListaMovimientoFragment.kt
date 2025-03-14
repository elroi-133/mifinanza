package com.example.mifinanza

import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.io.InputStream

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


class ListaMovimientoFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: SimpleAdapter
    private lateinit var registros: MutableList<Map<String, String>>
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1

    private val importFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { importRegistros(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lista_movimiento, container, false)

        dbHelper = DatabaseHelper(requireContext())
        listView = view.findViewById(R.id.list_registros)

        loadRegistros()

        listView.setOnItemLongClickListener { parent, _, position, _ ->
            if (position >= 0 && position < registros.size) {
                val selectedItem = registros[position]
                val registroId = selectedItem["id"]?.toInt()
                val registroDescripcion = selectedItem["descripcion"]

                if (registroId != null && registroDescripcion != null) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Editar/Eliminar Registro")
                        .setMessage("¿Qué deseas hacer con el registro '$registroDescripcion'?")
                        .setPositiveButton("Editar") { _, _ ->
                            showEditDialog(registroId, selectedItem)
                        }
                        .setNegativeButton("Eliminar") { _, _ ->
                            deleteRegistro(registroId)
                        }
                        .setNeutralButton("Cancelar", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Error: 'id' o 'descripcion' son nulos.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Error: selectedItem es nulo.", Toast.LENGTH_SHORT).show()
            }

            true
        }

        view.findViewById<Button>(R.id.btn_exportar).setOnClickListener {
            exportarRegistros()
        }

        view.findViewById<Button>(R.id.btn_importar).setOnClickListener {
            importFileLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }

        return view
    }

    private fun loadRegistros() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, "${DatabaseHelper.COLUMN_FECHA} DESC")
        registros = mutableListOf()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)).toString()
            val monto = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTO)).toString()
            val tasa = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASA)).toString()
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION))
            val fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA))
            val tipo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO)).toString()
            val partida = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA)).toString()
            val total = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL)).toString()
            registros.add(mapOf("id" to id, "monto" to monto, "tasa" to tasa, "descripcion" to descripcion, "fecha" to fecha, "tipo" to tipo, "partida" to partida, "total" to total))
        }
        cursor.close()
        db.close()

        setupListView(registros)
    }
    private fun setupListView(registros: List<Map<String, String>>) {
        val registrosConcatenados = registros.map { registro ->
            mapOf(
                "registroConcatenado" to "Descripción: ${registro["descripcion"]}\n" +
                        "Monto: ${registro["monto"]}\n" +
                        "Tasa: ${registro["tasa"]}\n" +
                        "Divisas: ${registro["total"]}\n" +
                        "Fecha: ${registro["fecha"]}\n" +
                        "Tipo: ${registro["tipo"]}\n" +
                        "Partida: ${registro["partida"]}"
            )
        }

        adapter = SimpleAdapter(
            requireContext(),
            registrosConcatenados,
            android.R.layout.simple_list_item_1, // Usamos simple_list_item_1
            arrayOf("registroConcatenado"),
            intArrayOf(android.R.id.text1)
        )
        listView.adapter = adapter
    }
    private fun exportarRegistros() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
                return
            }
        }

        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null)

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Registros de Finanzas")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Monto")
        headerRow.createCell(2).setCellValue("Tasa")
        headerRow.createCell(3).setCellValue("Descripción")
        headerRow.createCell(4).setCellValue("Fecha")
        headerRow.createCell(5).setCellValue("Tipo")
        headerRow.createCell(6).setCellValue("Partida")

        var rowIndex = 1
        while (cursor.moveToNext()) {
            val row = sheet.createRow(rowIndex++)
            row.createCell(0).setCellValue(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)).toDouble())
            row.createCell(1).setCellValue(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MONTO)))
            row.createCell(2).setCellValue(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASA)))
            row.createCell(3).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPCION)))
            row.createCell(4).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA)))
            row.createCell(5).setCellValue(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO)).toDouble())
            row.createCell(6).setCellValue(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA)).toDouble())
        }
        cursor.close()
        db.close()

        val fileName = "registros_finanzas.xlsx"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = requireContext().contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream = requireContext().contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    workbook.write(outputStream)
                    outputStream.close()
                }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val filePath = File(downloadsDir, fileName).absolutePath
            val fileOutputStream = FileOutputStream(filePath)
            workbook.write(fileOutputStream)
            fileOutputStream.close()
        }
        workbook.close()

        Toast.makeText(requireContext(), "Registros exportados a Descargas/$fileName", Toast.LENGTH_SHORT).show()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportarRegistros()
            } else {
                Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(registroId: Int?, registro: Map<String, String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_registro, null)
        val etMonto = dialogView.findViewById<EditText>(R.id.et_monto)
        val etTasa = dialogView.findViewById<EditText>(R.id.et_tasa)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.et_descripcion)
        val etFecha = dialogView.findViewById<EditText>(R.id.et_fecha)
        val spinnerPartida = dialogView.findViewById<Spinner>(R.id.spinner_partida)

        etMonto.setText(registro["monto"])
        etTasa.setText(registro["tasa"])
        etDescripcion.setText(registro["descripcion"])
        etFecha.setText(registro["fecha"])
        loadPartidas(spinnerPartida, registro["tipo"]?.toInt() ?: 0)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Registro")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoMonto = etMonto.text.toString().toDoubleOrNull()
                val nuevaTasa = etTasa.text.toString().toDoubleOrNull()
                val nuevaDescripcion = etDescripcion.text.toString()
                val nuevaFecha = etFecha.text.toString()
                val nuevaPartida = spinnerPartida.selectedItemId.toInt()

                if (registroId != null && nuevoMonto != null && nuevaTasa != null && nuevaDescripcion.isNotEmpty() && nuevaFecha.isNotEmpty()) {
                    val db = dbHelper.writableDatabase
                    val values = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_MONTO, nuevoMonto)
                        put(DatabaseHelper.COLUMN_TASA, nuevaTasa)
                        put(DatabaseHelper.COLUMN_DESCRIPCION, nuevaDescripcion)
                        put(DatabaseHelper.COLUMN_FECHA, nuevaFecha)
                        put(DatabaseHelper.COLUMN_PARTIDA, nuevaPartida)
                    }
                    db.update(DatabaseHelper.TABLE_NAME, values, "${DatabaseHelper.COLUMN_ID}=?", arrayOf(registroId.toString()))
                    db.close()
                    Toast.makeText(requireContext(), "Registro actualizado", Toast.LENGTH_SHORT).show()
                    loadRegistros()
                } else {
                    Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteRegistro(registroId: Int?) {
        if (registroId != null) {
            val db = dbHelper.writableDatabase
            db.delete(DatabaseHelper.TABLE_NAME, "${DatabaseHelper.COLUMN_ID}=?", arrayOf(registroId.toString()))
            db.close()
            Toast.makeText(requireContext(), "Registro eliminado", Toast.LENGTH_SHORT).show()
            loadRegistros()
        }
    }

    private fun loadPartidas(spinner: Spinner, tipo: Int) {
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
        spinner.adapter = adapter
    }

    private fun importRegistros(uri: Uri) {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (row in sheet) {
                if (row.rowNum == 0) {
                    continue // Skip header row
                }
                val id = row.getCell(0).numericCellValue.toInt()
                val monto = row.getCell(1).numericCellValue
                val tasa = row.getCell(2).numericCellValue
                val descripcion = row.getCell(3).stringCellValue
                val fecha = row.getCell(4).stringCellValue
                val tipo = row.getCell(5).numericCellValue.toInt()
                val partida = row.getCell(6).numericCellValue.toInt()

                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_ID, id)
                    put(DatabaseHelper.COLUMN_MONTO, monto)
                    put(DatabaseHelper.COLUMN_TASA, tasa)
                    put(DatabaseHelper.COLUMN_DESCRIPCION, descripcion)
                    put(DatabaseHelper.COLUMN_FECHA, fecha)
                    put(DatabaseHelper.COLUMN_TIPO, tipo)
                    put(DatabaseHelper.COLUMN_PARTIDA, partida)
                }
                db.insertWithOnConflict(DatabaseHelper.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }

        inputStream?.close()
        workbook.close()

        Toast.makeText(requireContext(), "Registros importados", Toast.LENGTH_SHORT).show()
        loadRegistros()
    }
}