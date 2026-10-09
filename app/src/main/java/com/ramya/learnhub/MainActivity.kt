package com.ramya.learnhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // targetSdk 36 draws edge-to-edge; keep content clear of system bars and the keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Skip login when a token is already stored.
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        val loggedIn = (application as LearnHubApp).container.authRepository.isLoggedIn()
        graph.setStartDestination(if (loggedIn) R.id.dashboardFragment else R.id.loginFragment)
        navController.graph = graph
    }
}
