package com.cyberpunk.debttracker.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.databinding.ActivityMainBinding
import com.cyberpunk.debttracker.ui.adddebt.AddDebtActivity
import dagger.hilt.android.AndroidEntryPoint

import androidx.preference.PreferenceManager
import com.cyberpunk.debttracker.util.ReminderManager

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied. Reminders won't work.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupFab()
        setupBottomNavInsets()
        initReminders()
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initReminders() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val freq = prefs.getString("overdue_reminder_frequency", "daily") ?: "daily"
        ReminderManager.scheduleReminders(this, freq)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupBottomNavInsets() {
        val baseNavHeight = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val fabOffset = resources.getDimensionPixelSize(R.dimen.spacing_md)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.updatePadding(top = bars.top)
            if (bars.bottom > 0) {
                binding.bottomNavigation.updateLayoutParams<ViewGroup.LayoutParams> {
                    height = baseNavHeight + bars.bottom
                }
                binding.bottomNavigation.setPadding(0, 0, 0, bars.bottom)
                binding.navHostFragment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = baseNavHeight + bars.bottom
                }
                binding.fabAddDebt.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = baseNavHeight + fabOffset + bars.bottom
                }
            }
            insets
        }
    }

    private fun setupFab() {
        binding.fabAddDebt.setOnClickListener {
            startActivity(Intent(this, AddDebtActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
