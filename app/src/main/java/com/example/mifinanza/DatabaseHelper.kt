package com.example.mifinanza


import android.content.ContentValues
import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla ingresos_egresos (sin cambios)
        val createTableIngresosEgresos = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_MONTO DECIMAL(10, 2), "
                + "$COLUMN_TASA DECIMAL(10, 2), "
                + "$COLUMN_DESCRIPCION TEXT, "
                + "$COLUMN_FECHA DATE, "
                + "$COLUMN_TIPO INTEGER, "
                + "$COLUMN_PARTIDA INTEGER, "
                + "FOREIGN KEY($COLUMN_PARTIDA) REFERENCES $TABLE_PARTIDAS($COLUMN_PARTIDA_ID))")

        // Crear tabla partidas (sin cambios)
        val createTablePartidas = ("CREATE TABLE $TABLE_PARTIDAS ("
                + "$COLUMN_PARTIDA_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_PARTIDA_NOMBRE TEXT, "
                + "$COLUMN_PARTIDA_TIPO INTEGER)")

        // Crear tabla prestamos
        val createTablePrestamos = ("CREATE TABLE $TABLE_PRESTAMOS ("
                + "$COLUMN_PRESTAMO_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_PRESTAMISTA TEXT, "
                + "$COLUMN_MONTO_PRESTAMO DECIMAL(10, 2), "
                + "$COLUMN_TASA_INTERES DECIMAL(5, 2), "
                + "$COLUMN_FECHA_PRESTAMO DATE, "
                + "$COLUMN_PLAZO_DIAS INTEGER, "
                + "$COLUMN_ESTADO TEXT)")

        db.execSQL(createTableIngresosEgresos)
        db.execSQL(createTablePartidas)
        db.execSQL(createTablePrestamos)
        // Insertar valores iniciales en la tabla partidas
        val insertPartidas = "INSERT INTO $TABLE_PARTIDAS ($COLUMN_PARTIDA_NOMBRE, $COLUMN_PARTIDA_TIPO) VALUES " +
                "('Salario', 1), " +
                "('Prestamo', 1), " +
                "('Bono', 1), " +
                "('Proteinas', 0), " +
                "('Carbohidratos', 0)"
        db.execSQL(insertPartidas)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTIDAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRESTAMOS")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 2 // Incrementa la versión de la base de datos
        private const val DATABASE_NAME = "finanzas.db"

        // Tabla ingresos_egresos (sin cambios)
        const val TABLE_NAME = "ingresos_egresos"
        const val COLUMN_ID = "id"
        const val COLUMN_MONTO = "monto"
        const val COLUMN_TASA = "tasa"
        const val COLUMN_DESCRIPCION = "descripcion"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_PARTIDA = "partida"

        // Tabla partidas (sin cambios)
        const val TABLE_PARTIDAS = "partidas"
        const val COLUMN_PARTIDA_ID = "id"
        const val COLUMN_PARTIDA_NOMBRE = "nombre"
        const val COLUMN_PARTIDA_TIPO = "tipo"

        // Tabla prestamos
        const val TABLE_PRESTAMOS = "prestamos"
        const val COLUMN_PRESTAMO_ID = "id"
        const val COLUMN_PRESTAMISTA = "prestamista"
        const val COLUMN_MONTO_PRESTAMO = "monto"
        const val COLUMN_TASA_INTERES = "tasa_interes"
        const val COLUMN_FECHA_PRESTAMO = "fecha_prestamo"
        const val COLUMN_PLAZO_DIAS = "plazo_dias"
        const val COLUMN_ESTADO = "estado"
    }
    fun registrarMovimiento(
        monto: Double,
        tasa: Double,
        descripcion: String,
        fecha: String,
        tipo: Int,
        partida: Int
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_MONTO, monto)
            put(DatabaseHelper.COLUMN_TASA, tasa)
            put(DatabaseHelper.COLUMN_DESCRIPCION, descripcion)
            put(DatabaseHelper.COLUMN_FECHA, fecha)
            put(DatabaseHelper.COLUMN_TIPO, tipo)
            put(DatabaseHelper.COLUMN_PARTIDA, partida)
        }
        db.insert(DatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }

    fun obtenerPrestamosActivos(): List<String> {
        val db = readableDatabase
        val prestamos = mutableListOf<String>()

        val cursor = db.query(
            TABLE_PRESTAMOS,
            arrayOf(COLUMN_PRESTAMO_ID, COLUMN_PRESTAMISTA, COLUMN_MONTO_PRESTAMO, COLUMN_ESTADO),
            "$COLUMN_ESTADO = ?",
            arrayOf("Activo"),
            null, null, null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val prestamista = cursor.getString(1)
            val monto = cursor.getDouble(2)
            val saldoPendiente = calcularSaldoPendientePorPagarPrestamo(id)
            prestamos.add("ID: $id - $prestamista - Monto: $monto - Saldo: $saldoPendiente")
        }
        cursor.close()
        db.close()

        return prestamos
    }
    fun registrarPrestamo(
        prestamista: String,
        monto: Double,
        tasaInteres: Double,
        fechaPrestamo: String,
        plazoDias: Int,
        context: Context
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRESTAMISTA, prestamista)
            put(COLUMN_MONTO_PRESTAMO, monto)
            put(COLUMN_TASA_INTERES, tasaInteres)
            put(COLUMN_FECHA_PRESTAMO, fechaPrestamo)
            put(COLUMN_PLAZO_DIAS, plazoDias)
            put(COLUMN_ESTADO, "Activo") // Estado inicial
        }
        val prestamoId = db.insert(TABLE_PRESTAMOS, null, values).toInt()
        db.close()

        // Programar notificación
        if (prestamoId != -1) {
            programarNotificacion(prestamoId, prestamista, monto, fechaPrestamo, plazoDias, context)
        }
    }

    private fun programarNotificacion(
        prestamoId: Int,
        prestamista: String,
        monto: Double,
        fechaPrestamo: String,
        plazoDias: Int,
        context: Context
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Convertir la fecha de préstamo a milisegundos
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val fecha = formatoFecha.parse(fechaPrestamo)
        val fechaVencimiento = fecha!!.time + plazoDias * 24 * 60 * 60 * 1000L

        // Crear un Intent para el BroadcastReceiver
        val intent = Intent(context, PrestamoNotificationReceiver::class.java).apply {
            putExtra("prestamo_id", prestamoId)
            putExtra("prestamista", prestamista)
            putExtra("monto", monto)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prestamoId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Programar la alarma
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            fechaVencimiento,
            pendingIntent
        )
    }
    fun obtenerTodosLosPrestamos(): List<String> {
        val db = readableDatabase
        val prestamos = mutableListOf<String>()

        val cursor = db.query(
            TABLE_PRESTAMOS,
            arrayOf(COLUMN_PRESTAMO_ID, COLUMN_PRESTAMISTA, COLUMN_MONTO_PRESTAMO, COLUMN_ESTADO),
            null, null, null, null, null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val prestamista = cursor.getString(1)
            val monto = cursor.getDouble(2)
            val estado = cursor.getString(3)
            prestamos.add("ID: $id - $prestamista - Monto: $monto - Estado: $estado")
        }
        cursor.close()
        db.close()

        return prestamos
    }
    fun eliminarPrestamo(prestamoId: Int): Boolean {
        val db = writableDatabase

        // Verificar si el préstamo tiene pagos asociados
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_PRESTAMO_ID = ?",
            arrayOf(prestamoId.toString()),
            null, null, null
        )
        val tienePagos = cursor.count > 0
        cursor.close()

        if (tienePagos) {
            return false // No se puede eliminar si tiene pagos asociados
        }

        // Eliminar el préstamo
        val filasEliminadas = db.delete(
            TABLE_PRESTAMOS,
            "$COLUMN_PRESTAMO_ID = ?",
            arrayOf(prestamoId.toString())
        )
        db.close()

        return filasEliminadas > 0
    }
    fun actualizarPrestamo(prestamoId: Int): Boolean{
        val db = writableDatabase
        val valuesPrestamo = ContentValues().apply {
            put(COLUMN_ESTADO, "Pagado")
        }
        val filasEliminadas = db.update(
            TABLE_PRESTAMOS,
            valuesPrestamo,
            "$COLUMN_PRESTAMO_ID = ?",
            arrayOf(prestamoId.toString())
        )
        db.close()

        return filasEliminadas > 0
    }
    fun calcularSaldoPendientePorPagarPrestamo(prestamoId: Int): Double {
        val db = readableDatabase

        // Obtener el monto original del préstamo
        val cursorPrestamo = db.query(
            TABLE_PRESTAMOS,
            arrayOf(COLUMN_MONTO_PRESTAMO),
            "$COLUMN_PRESTAMO_ID = ?",
            arrayOf(prestamoId.toString()),
            null, null, null
        )
        var montoPrestamo = 0.0
        if (cursorPrestamo.moveToFirst()) {
            montoPrestamo = cursorPrestamo.getDouble(0)
        }
        cursorPrestamo.close()

        // Sumar todos los pagos asociados al préstamo
        val cursorPagos = db.query(
            TABLE_NAME,
            arrayOf("SUM($COLUMN_MONTO) as total_pagos"),
            "$COLUMN_DESCRIPCION LIKE ? AND $COLUMN_TIPO = 2",
            arrayOf("%Pago préstamo ID: $prestamoId%"),
            null, null, null
        )
        var totalPagos = 0.0
        if (cursorPagos.moveToFirst()) {
            totalPagos = cursorPagos.getDouble(0)
        }
        cursorPagos.close()

        db.close()

        // Calcular el saldo pendiente
        return montoPrestamo - totalPagos
    }
}