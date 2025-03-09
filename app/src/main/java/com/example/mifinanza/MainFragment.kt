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

        tvNombre.text = "Roissmer"
        tvResumenIngresos.text = "Ingresos: $1000"
        tvResumenEgresos.text = "Egresos: $500"
        tvPrestamoPendiente.text = "Préstamo por Pagar: $2000"
        // Dentro de tu Fragment o Activity
        val barChartIngresos = view.findViewById<BarChart>(R.id.barChartIngresos) // Asegúrate de tener un BarChart para ingresos
        val barChartEgresos = view.findViewById<BarChart>(R.id.barChartEgresos) // Asegúrate de tener un BarChart para egresos

        val dbHelper = DatabaseHelper(requireContext()) // Inicializa tu DatabaseHelper
        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH) + 1 // Enero es 0
        val anioActual = calendario.get(Calendar.YEAR)
        calendario.add(Calendar.MONTH, -1)
        val mesAnterior = calendario.get(Calendar.MONTH) + 1
        val anioAnterior = calendario.get(Calendar.YEAR)

// Gráfico de ingresos
        val datosIngresos = obtenerDatosGrafica(dbHelper, 1, mesActual, anioActual, mesAnterior, anioAnterior)
        val barDataIngresos = prepararDatosGraficaBarras(datosIngresos)

        barChartIngresos.data = barDataIngresos
        barChartIngresos.xAxis.valueFormatter = IndexAxisValueFormatter(datosIngresos.keys)
        barChartIngresos.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChartIngresos.description.text = "Ingresos por Partida"
        barChartIngresos.animateY(1000)
        barChartIngresos.groupBars(-0.5f, 0.35f, 0f) // Agrupar las barras
        barChartIngresos.invalidate()

// Gráfico de egresos
        val datosEgresos = obtenerDatosGrafica(dbHelper, 0, mesActual, anioActual, mesAnterior, anioAnterior)
        val barDataEgresos = prepararDatosGraficaBarras(datosEgresos)

        barChartEgresos.data = barDataEgresos
        barChartEgresos.xAxis.valueFormatter = IndexAxisValueFormatter(datosEgresos.keys)
        barChartEgresos.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChartEgresos.description.text = "Egresos por Partida"
        barChartEgresos.animateY(1000)
        barChartEgresos.groupBars(-0.5f, 0.35f, 0f) // Agrupar las barras
        barChartEgresos.invalidate()
        return view
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

        // Obtener datos de ingresos/egresos por partida y mes
        val cursorDatos = db.query(DatabaseHelper.TABLE_NAME, arrayOf(DatabaseHelper.COLUMN_PARTIDA, "SUM(${DatabaseHelper.COLUMN_MONTO})", "strftime('%m', ${DatabaseHelper.COLUMN_FECHA})", "strftime('%Y', ${DatabaseHelper.COLUMN_FECHA})"), "${DatabaseHelper.COLUMN_TIPO} = ? AND strftime('%m', ${DatabaseHelper.COLUMN_FECHA}) IN (?, ?) AND strftime('%Y', ${DatabaseHelper.COLUMN_FECHA}) IN (?, ?)", arrayOf(tipo.toString(), String.format("%02d", mesActual), String.format("%02d", mesAnterior), anioActual.toString(), anioAnterior.toString()), "${DatabaseHelper.COLUMN_PARTIDA}, strftime('%m', ${DatabaseHelper.COLUMN_FECHA})", null, null)

        while (cursorDatos.moveToNext()) {
            val partidaId = cursorDatos.getInt(0)
            val monto = cursorDatos.getDouble(1)
            val mes = cursorDatos.getString(2).toInt()
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

    fun prepararDatosGraficaBarras(datos: Map<String, Pair<Double, Double>>): BarData {
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

        val dataSetMesActual = BarDataSet(entriesMesActual, "Mes Actual")
        val dataSetMesAnterior = BarDataSet(entriesMesAnterior, "Mes Anterior")

        // Personalizar dataSets (colores, etc.)
        dataSetMesActual.color = Color.BLUE
        dataSetMesAnterior.color = Color.RED

        val barData = BarData(dataSetMesActual, dataSetMesAnterior)
        barData.barWidth = 0.35f // Ajustar el ancho de las barras
        return barData
    }
}
