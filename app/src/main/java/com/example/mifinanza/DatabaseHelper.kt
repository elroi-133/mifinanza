package com.example.mifinanza


import android.content.ContentValues
import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.ArrayAdapter
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
                + "$COLUMN_TOTAL DECIMAL(10, 2), "
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

        // Crear tabla deudas
        val createTableDeudas = ("CREATE TABLE $TABLE_DEUDAS ("
                + "$COLUMN_DEUDA_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_DEUDA_DESCRIPCION TEXT, "
                + "$COLUMN_MONTO_DEUDA DECIMAL(10, 2), "
                + "$COLUMN_FECHA_DEUDA DATE, "
                + "$COLUMN_ESTADO TEXT, "
                + "$COLUMN_DEUDA_PARTIDA INTEGER, "
                + "$COLUMN_DEUDA_ABONO DECIMAL(10, 2), "
                + "$COLUMN_DEUDA_SALDO_PENDIENTE DECIMAL(10, 2), "
                + "FOREIGN KEY($COLUMN_DEUDA_PARTIDA) REFERENCES $TABLE_PARTIDAS($COLUMN_PARTIDA_ID))")

        db.execSQL(createTableDeudas)
        db.execSQL(createTableIngresosEgresos)
        db.execSQL(createTablePartidas)
        db.execSQL(createTablePrestamos)
        // Insertar valores iniciales en la tabla partidas
        val insertPartidas = "INSERT INTO $TABLE_PARTIDAS ($COLUMN_PARTIDA_NOMBRE, $COLUMN_PARTIDA_TIPO) VALUES " +
                "('Prestamos', 1), " +
                "('Sueldo', 1), " +
                "('Bono', 1), " +
                "('Liquidez_Banco', 1), " +
                "('Prestamos', 0), " +
                "('Hogar Carbohidrato', 0), " +
                "('Hogar Proteinas', 0), " +
                "('Hogar Articulos_Limpieza', 0), " +
                "('Hogar Aseo_Personal', 0), " +
                "('Hogar Articulos_Hogar', 0), " +
                "('Hogar Hortalizas', 0), " +
                "('Hogar Remodelacion', 0), " +
                "('Gastos_Padres', 0), " +
                "('Salidas', 0), " +
                "('Servicios Agua', 0), " +
                "('Servicios Energia_Electrica', 0), " +
                "('Servicios Gas', 0), " +
                "('Servicios Internet', 0), " +
                "('Servicios Telefono_Movil', 0), " +
                "('Servicios Colegio', 0), " +
                "('Servicios Transporte', 0), " +
                "('Salud Medicina', 0), " +
                "('Salud Consultas', 0), " +
                "('Salud Examenes', 0), " +
                "('Deuda', 0), " +
                "('Ahorro', 0), " +
                "('Comision_Banco', 0), " +
                "('Ropa y Calzado', 0)"
        db.execSQL(insertPartidas)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARTIDAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRESTAMOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DEUDAS")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_VERSION = 5 // Incrementa la versión de la base de datos
        private const val DATABASE_NAME = "finanzas.db"

        const val TABLE_NAME = "ingresos_egresos"
        const val COLUMN_ID = "id"
        const val COLUMN_MONTO = "monto"
        const val COLUMN_TASA = "tasa"
        const val COLUMN_DESCRIPCION = "descripcion"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_PARTIDA = "partida"
        const val COLUMN_TOTAL = "total"

        const val TABLE_PARTIDAS = "partidas"
        const val COLUMN_PARTIDA_ID = "id"
        const val COLUMN_PARTIDA_NOMBRE = "nombre"
        const val COLUMN_PARTIDA_TIPO = "tipo"

        const val TABLE_PRESTAMOS = "prestamos"
        const val COLUMN_PRESTAMO_ID = "id"
        const val COLUMN_PRESTAMISTA = "prestamista"
        const val COLUMN_MONTO_PRESTAMO = "monto"
        const val COLUMN_TASA_INTERES = "tasa_interes"
        const val COLUMN_FECHA_PRESTAMO = "fecha_prestamo"
        const val COLUMN_PLAZO_DIAS = "plazo_dias"
        const val COLUMN_ESTADO = "estado"

        const val TABLE_DEUDAS = "deudas"
        const val COLUMN_DEUDA_ID = "id"
        const val COLUMN_DEUDA_DESCRIPCION = "descripcion"
        const val COLUMN_MONTO_DEUDA = "monto_deuda"
        const val COLUMN_FECHA_DEUDA = "fecha_deuda"
        //const val COLUMN_ESTADO = "estado"
        const val COLUMN_DEUDA_PARTIDA = "partida"
        const val COLUMN_DEUDA_ABONO = "abono"
        const val COLUMN_DEUDA_SALDO_PENDIENTE = "saldo_pendiente"
    }
    //*****************************MOVIMIENTOS*************************************
    fun registrarMovimiento(monto: Double,tasa: Double,descripcion: String,fecha: String,tipo: Int,partida: Int, total: Double = 0.0) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MONTO, monto)
            put(COLUMN_TASA, tasa)
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_FECHA, fecha)
            put(COLUMN_TIPO, tipo)
            put(COLUMN_PARTIDA, partida)
            put(COLUMN_TOTAL, total)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
    fun actualizarMovimiento(id: Int,monto: Double,tasa: Double,descripcion: String,fecha: String,tipo: Int,partidaId: Int,montoTotal: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MONTO, monto)
            put(COLUMN_TASA, tasa)
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_FECHA, fecha)
            put(COLUMN_TIPO, tipo)
            put(COLUMN_PARTIDA, partidaId)
            put(COLUMN_TOTAL, montoTotal)
        }
        return db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }
    fun eliminarMovimiento(id: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    //******************************PRESTAMOS**************************************
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
            prestamos.add("ID: $id - $prestamista - Monto: $monto - Pendiente: $saldoPendiente")
        }
        cursor.close()
        db.close()

        return prestamos
    }
    fun registrarPrestamo(prestamista: String, monto: Double,tasaInteres: Double,fechaPrestamo: String,plazoDias: Int,context: Context) {
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
    private fun programarNotificacion(prestamoId: Int,prestamista: String,monto: Double,fechaPrestamo: String,plazoDias: Int,context: Context) {
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
    fun eliminarPrestamo(id: Int): Boolean {
        val db = writableDatabase

        // Verificar si el préstamo tiene pagos asociados
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_DESCRIPCION LIKE ? AND $COLUMN_TIPO = 0",
            arrayOf("%Pago prestamo ID: $id%"),
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
            arrayOf(id.toString())
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
            arrayOf("SUM($COLUMN_TOTAL) as total_pagos"),
            "$COLUMN_DESCRIPCION LIKE ? AND $COLUMN_TIPO = 0",
            arrayOf("%Pago prestamo ID: $prestamoId%"),
            null, null, null
        )
        var totalPagos = 0.0
        if (cursorPagos.moveToFirst()) {
            totalPagos = cursorPagos.getDouble(0)
        }
        cursorPagos.close()

        db.close()

        // Calcular el saldo pendiente
        return montoPrestamo + totalPagos
    }

    //*********************************PARTIDAS*************************************
    fun obtenerIdPartida( nombrePartida: String, tipoPartida: Int): Int? {
        val db = readableDatabase
        val cursor = db.query(TABLE_PARTIDAS,
            arrayOf("$COLUMN_PARTIDA_ID"),
            "$COLUMN_PARTIDA_NOMBRE = ? AND $COLUMN_PARTIDA_TIPO = ?",
            arrayOf(nombrePartida, tipoPartida.toString()),
            null,
            null,
            null
        )

        var idPartida: Int? = null

        if (cursor.moveToFirst()) {
            idPartida = cursor.getInt(cursor.getColumnIndexOrThrow("$COLUMN_PARTIDA_ID"))
        }

        cursor.close()
        return idPartida
    }
    fun loadPartidas(tipo: Int): MutableList<Partida> {
        val db = readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_PARTIDAS, arrayOf(DatabaseHelper.COLUMN_PARTIDA_ID, DatabaseHelper.COLUMN_PARTIDA_NOMBRE),
            "${DatabaseHelper.COLUMN_PARTIDA_TIPO} = ?", arrayOf(tipo.toString()), null, null, null)
        val partidas = mutableListOf<Partida>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA_NOMBRE))
            partidas.add(Partida(id, nombre))
        }
        cursor.close()
        db.close()
        return partidas
    }

    //********************************DEUDAS****************************************
    fun guardarDeuda(descripcion: String,monto: Double,fecha: String, partidaID: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DESCRIPCION, descripcion)
            put(COLUMN_MONTO_DEUDA, monto)
            put(COLUMN_FECHA_DEUDA, fecha)
            put(COLUMN_DEUDA_PARTIDA, partidaID)
            put(COLUMN_DEUDA_ABONO, 0)
            put(COLUMN_DEUDA_SALDO_PENDIENTE, monto)
            put(COLUMN_ESTADO, "Activo") // Estado inicial
        }
        db.insert(TABLE_DEUDAS, null, values)
        db.close()
    }
    fun obtenerDeudasActivas(): List<String> {
        val db = readableDatabase
        val deudas = mutableListOf<String>()

        val cursor = db.query(
            TABLE_DEUDAS,
            arrayOf(COLUMN_DEUDA_ID, COLUMN_DEUDA_DESCRIPCION, COLUMN_MONTO_DEUDA,COLUMN_DEUDA_SALDO_PENDIENTE, COLUMN_ESTADO),
            "$COLUMN_ESTADO = ?",
            arrayOf("Activo"),
            null, null, null
        )

        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val descrpcion = cursor.getString(1)
            val monto = cursor.getDouble(2)
            val deuda = cursor.getDouble(3)
            deudas.add("ID: $id - $descrpcion - Monto: $monto - Pendiente: $deuda")
        }
        cursor.close()
        db.close()

        return deudas
    }
    fun actualizarDeuda(id: Int,descripcion: String,monto: Double,pendiente: Double,fecha: String,estado: String,partidaId: Int ): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DEUDA_DESCRIPCION, descripcion)
            put(COLUMN_MONTO_DEUDA, monto)
            put(COLUMN_FECHA_DEUDA, fecha)
            put(COLUMN_ESTADO, estado)
            put(COLUMN_DEUDA_PARTIDA, partidaId)
            put(COLUMN_DEUDA_SALDO_PENDIENTE, pendiente)
        }
        return db.update(
            TABLE_DEUDAS,
            values,
            "$COLUMN_DEUDA_ID = ?",
            arrayOf(id.toString())
        )
    }
    fun eliminarDeuda(db: SQLiteDatabase, id: Int): Boolean {
        val db = readableDatabase
        // Verificar si la deuda tiene pagos asociados

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_DESCRIPCION LIKE ? AND $COLUMN_TIPO = 0",
            arrayOf("%Pago deuda ID: $id%"),
            null, null, null
        )
        val tienePagos = cursor.count > 0
        cursor.close()

        if (tienePagos) {
            return false // No se puede eliminar si tiene pagos asociados
        }

        // Eliminar la deuda
        val filasEliminadas = db.delete(
            TABLE_DEUDAS,
            "$COLUMN_DEUDA_ID = ?",
            arrayOf(id.toString())
        )

        db.close()

        return filasEliminadas > 0
    }
}