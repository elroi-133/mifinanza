package com.example.mifinanza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

class PrestamosFragment : Fragment() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prestamos, container, false)
        dbHelper = DatabaseHelper(requireContext())
        listView = view.findViewById(R.id.lv_prestamos)

        loadPrestamos()
        view.findViewById<Button>(R.id.btn_registrar_prestamo).setOnClickListener{
            //findNavController().navigate(R.id.action_prestamosFragment_to_registrarPrestamoFragment)
            val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val action = PrestamosFragmentDirections.actionPrestamosFragmentToRegistrarPrestamoFragment()
            navController.navigate(action)
        }
        return view
    }

    private fun loadPrestamos() {
        val prestamos = dbHelper.obtenerPrestamosActivos()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, prestamos)
        listView.adapter = adapter
    }
}
