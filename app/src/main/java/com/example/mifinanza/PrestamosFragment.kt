package com.example.mifinanza

import android.os.Bundle
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController

class PrestamosFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prestamos, container, false)
        dbHelper = DatabaseHelper(requireContext())
        listView = view.findViewById(R.id.lv_prestamos)

        loadPrestamos()
        view.findViewById<Button>(R.id.btn_registrar_prestamo).setOnClickListener{
            val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val action = PrestamosFragmentDirections.actionPrestamosFragmentToPrestamoRegistrarFragment()
            navController.navigate(action)
        }
        // Manejar clics en la lista
        listView.setOnItemClickListener { _, _, position, _ ->
            val prestamoId = obtenerIdPrestamoDesdePosicion(position)
            mostrarOpcionesPrestamo(prestamoId)
        }
        return view
    }

    private fun loadPrestamos() {
        val prestamos = dbHelper.obtenerPrestamosActivos()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, prestamos)
        listView.adapter = adapter
    }

    private fun obtenerIdPrestamoDesdePosicion(position: Int): Int {
        val item = adapter.getItem(position)
        return item?.split(" - ")?.get(0)?.replace("ID: ", "")?.toInt() ?: -1
    }

    private fun mostrarOpcionesPrestamo(prestamoId: Int) {
        val opciones = arrayOf("Eliminar", "Registrar Pago")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        eliminarPrestamo(prestamoId)
                    }
                    1 -> {
                        mostrarFragmentRegistrarPago(prestamoId)
                    }
                }
            }
            .show()
    }

    private fun mostrarFragmentRegistrarPago(prestamoId: Int) {
        findNavController().navigate(
            PrestamosFragmentDirections.actionPrestamosFragmentToPrestamoRegistrarPagoFragment(prestamoId)
        )
    }

    private fun eliminarPrestamo(prestamoId: Int) {
        if (dbHelper.eliminarPrestamo(prestamoId)) {
            Toast.makeText(requireContext(), "Préstamo eliminado", Toast.LENGTH_SHORT).show()
            cargarPrestamos()
        } else {
            Toast.makeText(requireContext(), "No se puede eliminar el préstamo (tiene pagos asociados)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarPrestamos() {
        val prestamos = dbHelper.obtenerPrestamosActivos()
        adapter.clear()
        adapter.addAll(prestamos)
    }
}
