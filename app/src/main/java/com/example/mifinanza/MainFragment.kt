package com.example.mifinanza

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.Calendar

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout del fragmento
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // Configurar el contenido principal
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre)
        val tvResumenIngresos: TextView = view.findViewById(R.id.tv_resumen_ingresos)
        val tvResumenEgresos: TextView = view.findViewById(R.id.tv_resumen_egresos)
        val tvPrestamoPendiente: TextView = view.findViewById(R.id.tv_prestamo_pendiente)

        val dbHelper = DatabaseHelper(requireContext()) // Inicializa tu DatabaseHelper

        val totalIngresos = calcularTotalIngresos(dbHelper)
        val totalEgresos = calcularTotalEgresos(dbHelper)
        val montoTotalPrestamos = calcularMontoTotalPrestamos(dbHelper)

        tvNombre.text = "RESUMEN DE MIS FINANZAS"
        tvResumenIngresos.text = "Ingresos: $totalIngresos"
        tvResumenEgresos.text = "Egresos: $totalEgresos"
        tvPrestamoPendiente.text = "Préstamo por Pagar: $montoTotalPrestamos"

        // Dentro de tu Fragment o Activity
        val barChartIngresos = view.findViewById<BarChart>(R.id.barChartIngresos) // Asegúrate de tener un BarChart para ingresos
        val barChartEgresos = view.findViewById<BarChart>(R.id.barChartEgresos) // Asegúrate de tener un BarChart para egresos

        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH) + 1 // Enero es 0
        val anioActual = calendario.get(Calendar.YEAR)
        calendario.add(Calendar.MONTH, -1)
        val mesAnterior = calendario.get(Calendar.MONTH) + 1
        val anioAnterior = calendario.get(Calendar.YEAR)

// Gráfico de ingresos
        val datosIngresos = obtenerDatosGrafica(dbHelper, 1, mesActual, anioActual, mesAnterior, anioAnterior)
        println("Datos Ingresos: $datosIngresos")
        val barDataIngresos = prepararDatosGraficaBarras(datosIngresos, barChartIngresos)
        barChartIngresos.data = barDataIngresos
        barChartIngresos.groupBars(-0.5f, 0.35f, 0f) // Agrupar las barras
        barChartIngresos.invalidate()
// Gráfico de egresos
        val datosEgresos = obtenerDatosGrafica(dbHelper, 0, mesActual, anioActual, mesAnterior, anioAnterior)
        println("Datos Egresos: $datosEgresos")
        val barDataEgresos = prepararDatosGraficaBarras(datosEgresos, barChartEgresos) // Pasa barChartEgresos
        barChartEgresos.data = barDataEgresos
        barChartEgresos.groupBars(-0.5f, 0.35f, 0f) // Agrupar las barras
        barChartEgresos.invalidate()
        return view
    }
    fun calcularMontoTotalPrestamos(dbHelper: DatabaseHelper): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(${DatabaseHelper.COLUMN_MONTO_PRESTAMO}) FROM ${DatabaseHelper.TABLE_PRESTAMOS} WHERE ${DatabaseHelper.COLUMN_ESTADO} = 'Activo'", null)
        var totalPrestamos = 0.0
        if (cursor.moveToFirst()) {
            totalPrestamos = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return totalPrestamos
    }
    fun calcularTotalEgresos(dbHelper: DatabaseHelper): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(${DatabaseHelper.COLUMN_TOTAL}) FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_TIPO} = 0", null)
        var totalEgresos = 0.0
        if (cursor.moveToFirst()) {
            totalEgresos = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return totalEgresos
    }
    fun calcularTotalIngresos(dbHelper: DatabaseHelper): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(${DatabaseHelper.COLUMN_TOTAL}) FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_TIPO} = 1", null)
        var totalIngresos = 0.0
        if (cursor.moveToFirst()) {
            totalIngresos = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return totalIngresos
    }
    fun obtenerDatosGrafica(dbHelper: DatabaseHelper, tipo: Int, mesActual: Int, anioActual: Int, mesAnterior: Int, anioAnterior: Int): Map<String, Pair<Double, Double>> {

        val db = dbHelper.readableDatabase
        val datos = mutableMapOf<String, Pair<Double, Double>>()

        // Obtener nombres de partidas
        val cursorPartidas = db.query(DatabaseHelper.TABLE_PARTIDAS, arrayOf(DatabaseHelper.COLUMN_PARTIDA_ID, DatabaseHelper.COLUMN_PARTIDA_NOMBRE), "${DatabaseHelper.COLUMN_PARTIDA_TIPO} = ?", arrayOf(tipo.toString()), null, null, null)
        val partidas = mutableMapOf<Int, String>()
        while (cursorPartidas.moveToNext()) {
            partidas[cursorPartidas.getInt(0)] = cursorPartidas.getString(1)
        }
        cursorPartidas.close()
        println("Las Partidas: $partidas")
        // Obtener datos de ingresos/egresos por partida y mes
        val cursorDatos = db.query(DatabaseHelper.TABLE_NAME, arrayOf(DatabaseHelper.COLUMN_PARTIDA, "SUM(${DatabaseHelper.COLUMN_TOTAL})", "strftime('%m', ${DatabaseHelper.COLUMN_FECHA})", "strftime('%Y', ${DatabaseHelper.COLUMN_FECHA})"), "${DatabaseHelper.COLUMN_TIPO} = ? AND strftime('%m', ${DatabaseHelper.COLUMN_FECHA}) IN (?, ?) AND strftime('%Y', ${DatabaseHelper.COLUMN_FECHA}) IN (?, ?)", arrayOf(tipo.toString(), String.format("%02d", mesActual), String.format("%02d", mesAnterior), anioActual.toString(), anioAnterior.toString()), "${DatabaseHelper.COLUMN_PARTIDA}, strftime('%m', ${DatabaseHelper.COLUMN_FECHA})", null, null)

        while (cursorDatos.moveToNext()) {
            val partidaId = cursorDatos.getInt(cursorDatos.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA))
            val monto = cursorDatos.getDouble(cursorDatos.getColumnIndexOrThrow("SUM(${DatabaseHelper.COLUMN_TOTAL})"))
            val mesString = cursorDatos.getString(cursorDatos.getColumnIndexOrThrow("strftime('%m', ${DatabaseHelper.COLUMN_FECHA})"))
            val mes = mesString.toInt() // Convertir mes a Int
            val partidaNombre = partidas[partidaId] ?: "Desconocido"

            if (mes == mesActual) {
                datos[partidaNombre] = Pair(monto, datos[partidaNombre]?.second ?: 0.0)
            } else if (mes == mesAnterior) {
                datos[partidaNombre] = Pair(datos[partidaNombre]?.first ?: 0.0, monto)
            }
        }
        cursorDatos.close()
        db.close()

        return datos
    }

    fun prepararDatosGraficaBarras(datos: Map<String, Pair<Double, Double>>, barChart: BarChart): BarData {
        val entriesMesActual = ArrayList<BarEntry>()
        val entriesMesAnterior = ArrayList<BarEntry>()
        val nombresPartidas = ArrayList<String>()

        var index = 0f
        for ((nombrePartida, montos) in datos) {
            entriesMesActual.add(BarEntry(index, montos.first.toFloat()))
            entriesMesAnterior.add(BarEntry(index, montos.second.toFloat()))
            nombresPartidas.add(nombrePartida)
            index++
        }
        println("Nombres Partidas: $nombresPartidas")
        val dataSetMesActual = BarDataSet(entriesMesActual, "Mes Actual")
        val dataSetMesAnterior = BarDataSet(entriesMesAnterior, "Mes Anterior")

        // Personalizar dataSets (colores, etc.)
        dataSetMesActual.color = Color.BLUE
        dataSetMesAnterior.color = Color.RED

        val barData = BarData(dataSetMesActual, dataSetMesAnterior)
        barData.barWidth = 0.35f // Ajustar el ancho de las barras

        // Configurar el eje X
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(nombresPartidas)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)

        return barData
    }
}
