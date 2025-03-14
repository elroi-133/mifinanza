package com.example.mifinanza

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

class DeudasFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_deudas, container, false)
        dbHelper = DatabaseHelper(requireContext())
        listView = view.findViewById(R.id.lv_deudas)

        loadDeudas()
        view.findViewById<Button>(R.id.btn_registrar_deuda).setOnClickListener{
            val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val action = DeudasFragmentDirections.actionDeudasFragmentToDeudasAgregarFragment()
            navController.navigate(action)
        }
        // Manejar clics en la lista
        listView.setOnItemClickListener { _, _, position, _ ->
            val deudaId = obtenerIdDeudaDesdePosicion(position)
            mostrarOpcionesDeuda(deudaId)
        }
        return view
    }

    private fun loadDeudas() {
        val deudas = dbHelper.obtenerDeudasActivas()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deudas)
        listView.adapter = adapter
    }

    private fun obtenerIdDeudaDesdePosicion(position: Int): Int {
        val item = adapter.getItem(position)
        return item?.split(" - ")?.get(0)?.replace("ID: ", "")?.toInt() ?: -1
    }

    private fun mostrarOpcionesDeuda(deudaId: Int) {
        val opciones = arrayOf("Eliminar", "Registrar Pago")
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        eliminarDeuda(deudaId)
                    }
                    1 -> {
                        mostrarFragmentRegistrarPago(deudaId)
                    }
                }
            }
        .show()
    }

    private fun mostrarFragmentRegistrarPago(deudaId: Int) {
        findNavController().navigate(
            DeudasFragmentDirections.actionDeudasFragmentToDeudasRegistrarPagoFragment(deudaId)
        )
    }

    private fun eliminarDeuda(deudaId: Int) {
        if (dbHelper.eliminarDeuda(deudaId)) {
            Toast.makeText(requireContext(), "Deuda eliminada", Toast.LENGTH_SHORT).show()
            cargarDeuda()
        } else {
            Toast.makeText(requireContext(), "No se puede eliminar la deuda (tiene pagos asociados)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDeuda() {
        val deudas = dbHelper.obtenerDeudasActivas()
        adapter.clear()
        adapter.addAll(deudas)
    }
}
