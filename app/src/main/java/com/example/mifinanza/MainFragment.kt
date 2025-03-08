package com.example.mifinanza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

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

        return view
    }
}
