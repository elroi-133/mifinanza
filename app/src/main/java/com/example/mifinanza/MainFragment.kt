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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet // Importar IBarDataSet

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.math.pow

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
        val tvDisponible: TextView = view.findViewById(R.id.tv_disponibilidad)
        val tvDeudaPendiente: TextView = view.findViewById(R.id.tv_deuda_pendiente)
        val dbHelper = DatabaseHelper(requireContext()) // Inicializa tu DatabaseHelper

        val totalIngresos = calcularTotalIngresos(dbHelper)
        val totalEgresos = calcularTotalEgresos(dbHelper)
        val montoTotalPrestamos = calcularMontoTotalPrestamos(dbHelper)
        val montoTotalDeuda = calcularTotalDeudaPendiente(dbHelper)

        tvNombre.text = "RESUMEN DE MIS FINANZAS"
        tvResumenIngresos.text = "Ingresos: $totalIngresos"
        tvResumenEgresos.text = "Egresos: $totalEgresos"
        tvPrestamoPendiente.text = "Préstamo por Pagar: $montoTotalPrestamos"
        tvDeudaPendiente.text = "Deuda por Pagar: $montoTotalDeuda"
        val m_disp = totalIngresos + totalEgresos
        val m_total = m_disp.roundTo(2)
       tvDisponible.text = "Disponible: $m_total"

        // declaracion de la graficas
        val horizontalBarChart: HorizontalBarChart = view.findViewById(R.id.horizontalBarChart)

        val barChartIngresos = view.findViewById<BarChart>(R.id.barChartIngresos) // Asegúrate de tener un BarChart para ingresos
        val barChartEgresos = view.findViewById<BarChart>(R.id.barChartEgresos) // Asegúrate de tener un BarChart para egresos

        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH) + 1 // Enero es 0
        val anioActual = calendario.get(Calendar.YEAR)
        calendario.add(Calendar.MONTH, -1)
        val mesAnterior = calendario.get(Calendar.MONTH) + 1
        val anioAnterior = calendario.get(Calendar.YEAR)

//grafica resumen
        val data = listOf(
            Pair("Ingresos", totalIngresos.toFloat()),
            Pair("Egresos", totalEgresos.toFloat()*-1),
            Pair("Deudas", montoTotalDeuda.toFloat()),
            Pair("Prestamos", montoTotalPrestamos.toFloat())
        )

        configurarGrafico(horizontalBarChart, data)

// Gráfico de ingresos
        val datosIngresos = obtenerDatosGrafica(dbHelper, 1, mesActual, anioActual, mesAnterior, anioAnterior)
        val barDataIngresos = prepararDatosGraficaBarras(datosIngresos, barChartIngresos)
        barChartIngresos.data = barDataIngresos
        barChartIngresos.description.text = "Ingresos"
        barChartIngresos.description.textColor = Color.BLUE
        barChartIngresos.description.textSize = 12f
        barChartIngresos.groupBars(-0.5f, 0.35f, 0f)
        barChartIngresos.invalidate()
// Gráfico de egresos
        val datosEgresos = obtenerDatosGrafica(dbHelper, 0, mesActual, anioActual, mesAnterior, anioAnterior)
        val barDataEgresos = prepararDatosGraficaBarras(datosEgresos, barChartEgresos)
        barChartEgresos.data = barDataEgresos
        barChartEgresos.description.text = "Egresos"
        barChartEgresos.description.textColor = Color.BLUE
        barChartEgresos.description.textSize = 12f
        barChartEgresos.groupBars(-0.5f, 0.35f, 0f)
        barChartEgresos.invalidate()
        return view
    }
    fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return (this * factor).roundToInt() / factor
    }
    fun redondearADosDecimales(numero: Double): Double {
        return String.format("%.2f", numero).toDouble()
    }
    private fun configurarGrafico(chart: HorizontalBarChart, data: List<Pair<String, Float>>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val ordenDeseado = listOf("Ingresos", "Egresos", "Deudas", "Prestamos")
        val datosOrdenados = ordenDeseado.map { label ->
            data.find { it.first == label } ?: Pair(label, 0f)
        }
        val dataSets = ArrayList<BarDataSet>()
        datosOrdenados.forEachIndexed { index, pair ->
            entries.add(BarEntry(index.toFloat(), pair.second))
            labels.add(pair.first)

            // Crear un BarDataSet separado para cada categoría
            val dataSet = BarDataSet(listOf(BarEntry(index.toFloat(), pair.second)), pair.first) // Usar el nombre de la categoría como etiqueta
            dataSet.color = when (pair.first) {
                "Ingresos" -> Color.GREEN
                "Egresos" -> Color.RED
                "Deudas" -> Color.BLUE
                "Prestamos" -> Color.YELLOW
                else -> Color.GRAY
            }

            dataSets.add(dataSet)
        }
        val barData = BarData(dataSets as List<IBarDataSet>)
        barData.barWidth = 0.3f
        chart.data = barData

        chart.description.isEnabled = false
        chart.animateY(1000)
        val legend = chart.legend
        legend.isEnabled = true
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)

        chart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index >= 0 && index < labels.size) {
                    return labels[index]
                } else {
                    return ""
                }
            }
        }

        chart.invalidate()
    }
    fun calcularTotalDeudaPendiente(dbHelper: DatabaseHelper): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT ROUND(SUM(${DatabaseHelper.COLUMN_DEUDA_SALDO_PENDIENTE}),2) FROM ${DatabaseHelper.TABLE_DEUDAS}", null)

        var totalPendiente = 0.0
        if (cursor.moveToFirst()) {
            if (!cursor.isNull(0)) {
                totalPendiente = cursor.getDouble(0)
            }
        }
        cursor.close()
        db.close()

        return totalPendiente
    }
    fun calcularMontoTotalPrestamos(dbHelper: DatabaseHelper): Double {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT ROUND(SUM(${DatabaseHelper.COLUMN_MONTO_PRESTAMO}),2) FROM ${DatabaseHelper.TABLE_PRESTAMOS} WHERE ${DatabaseHelper.COLUMN_ESTADO} = 'Activo'", null)
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
        val cursor = db.rawQuery("SELECT ROUND(SUM(${DatabaseHelper.COLUMN_TOTAL}),2) FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_TIPO} = 0", null)
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
        val cursor = db.rawQuery("SELECT ROUND(SUM(${DatabaseHelper.COLUMN_TOTAL}),2) FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_TIPO} = 1", null)
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
        // Obtener datos de ingresos/egresos por partida y mes
        val cursorDatos = db.query(DatabaseHelper.TABLE_NAME, arrayOf(DatabaseHelper.COLUMN_PARTIDA, "SUM(${DatabaseHelper.COLUMN_TOTAL})", "strftime('%m', ${DatabaseHelper.COLUMN_FECHA})", "strftime('%Y', ${DatabaseHelper.COLUMN_FECHA})"), "${DatabaseHelper.COLUMN_TIPO} = ? AND strftime('%m', ${DatabaseHelper.COLUMN_FECHA}) IN (?, ?) AND strftime('%Y', ${DatabaseHelper.COLUMN_FECHA}) IN (?, ?)", arrayOf(tipo.toString(), String.format("%02d", mesActual), String.format("%02d", mesAnterior), anioActual.toString(), anioAnterior.toString()), "${DatabaseHelper.COLUMN_PARTIDA}, strftime('%m', ${DatabaseHelper.COLUMN_FECHA})", null, null)

        while (cursorDatos.moveToNext()) {
            val partidaId = cursorDatos.getInt(cursorDatos.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PARTIDA))
            val monto = cursorDatos.getDouble(cursorDatos.getColumnIndexOrThrow("SUM(${DatabaseHelper.COLUMN_TOTAL})"))
            val mesString = cursorDatos.getString(cursorDatos.getColumnIndexOrThrow("strftime('%m', ${DatabaseHelper.COLUMN_FECHA})"))
            val mes = mesString.toInt() // Convertir mes a Int
            val partidaNombre = partidas[partidaId] ?: "Desconocido"
            var montoModificado = monto // Crear una copia modificable de monto

            if (montoModificado < 0) {
                montoModificado *= -1 // Modificar la copia
            }

            if (mes == mesActual) {
                datos[partidaNombre] = Pair(montoModificado, datos[partidaNombre]?.second ?: 0.0) // Usar el valor modificado
            } else if (mes == mesAnterior) {
                datos[partidaNombre] = Pair(datos[partidaNombre]?.first ?: 0.0, montoModificado) // Usar el valor modificado
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
        val dataSetMesActual = BarDataSet(entriesMesActual, "Mes Actual")
        val dataSetMesAnterior = BarDataSet(entriesMesAnterior, "Mes Anterior")

        // Personalizar dataSets (colores, etc.)
        dataSetMesActual.color = Color.BLUE
        dataSetMesAnterior.color = Color.RED
        val groupSpace = 0.1f
        val barSpace = 0.02f
        val barWidth = 0.35f

        val groupCount = datos.size // Número de grupos de barras

        val barData = BarData(dataSetMesActual, dataSetMesAnterior)
        barData.barWidth = barWidth // Ajustar el ancho de las barras

        barChart.data = barData
        // Configurar el eje X
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(nombresPartidas)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.labelRotationAngle = -45f // Rota las etiquetas
        xAxis.textSize = 8f // Ajusta el tamaño de la fuente

        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 0f + barChart.barData.getGroupWidth(groupSpace, barSpace) * groupCount

        barChart.groupBars(0f, groupSpace, barSpace) // Agrupa las barras
        barChart.invalidate()

        //Ajuste de la descripcion
        barChart.description.textSize = 12f;
        return barData
    }
}
