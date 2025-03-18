package com.example.mifinanza

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Manejar selecciones del menú de navegación
        navView.setNavigationItemSelectedListener { menuItem ->
            val navController = findNavController(R.id.nav_host_fragment)
            val currentDestination = navController.currentDestination?.id
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    when (currentDestination) {
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_mainFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_mainFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_mainFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_mainFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_mainFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_mainFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_mainFragment)
                    }
                }
                R.id.nav_ingresos -> {
                    when (currentDestination) {
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_ingresoFragment)
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_ingresoFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_ingresoFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_ingresoFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_ingresoFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_ingresoFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_ingresoFragment)
                    }
                }
                R.id.nav_egresos -> {
                    when (currentDestination) {
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_egresoFragment)
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_egresoFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_egresoFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_egresoFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_egresoFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_egresoFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_egresoFragment)
                    }
                }
                R.id.nav_partidas -> {
                    when (currentDestination) {
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_listaPartidasFragment)
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_listaPartidasFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_listaPartidasFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_listaPartidasFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_listaPartidasFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_listaPartidasFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_listaPartidasFragment)
                    }
                }
                R.id.nav_movimientos -> {
                    when (currentDestination) {
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_listaMovimientoFragment)
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_listaMovimientoFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_listaMovimientoFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_listaMovimientoFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_listaMovimientoFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_listaMovimientoFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_listaMovimientoFragment)
                    }
                }
                R.id.nav_prestamos -> {
                    when (currentDestination) {
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_prestamosFragment)
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_prestamosFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_prestamosFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_prestamosFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_prestamosFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_prestamosFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_prestamosFragment)
                    }
                }
                R.id.nav_deudas -> {
                    when (currentDestination) {
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_deudasFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_deudasFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_deudasFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_deudasFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_deudasFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_deudasFragment)
                        R.id.acercaDeFragment -> navController.navigate(R.id.action_acercaDeFragment_to_deudasFragment)
                    }
                }
                R.id.action_acerca_de -> {
                    when (currentDestination) {
                        R.id.mainFragment -> navController.navigate(R.id.action_mainFragment_to_acercaDeFragment)
                        R.id.ingresoFragment -> navController.navigate(R.id.action_ingresoFragment_to_acercaDeFragment)
                        R.id.egresoFragment -> navController.navigate(R.id.action_egresoFragment_to_acercaDeFragment)
                        R.id.listaPartidasFragment -> navController.navigate(R.id.action_listaPartidasFragment_to_acercaDeFragment)
                        R.id.listaMovimientoFragment -> navController.navigate(R.id.action_listaMovimientoFragment_to_acercaDeFragment)
                        R.id.prestamosFragment -> navController.navigate(R.id.action_prestamosFragment_to_acercaDeFragment)
                        R.id.deudasFragment -> navController.navigate(R.id.action_deudasFragment_to_acercaDeFragment)
                    }
                }
            }
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            true
        }

    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
