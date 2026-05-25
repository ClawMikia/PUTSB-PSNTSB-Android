package com.cyberpunk.debttracker.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cyberpunk.debttracker.R
import com.cyberpunk.debttracker.databinding.ActivitySplashBinding
import com.cyberpunk.debttracker.ui.dashboard.MainActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runSplashSequence()
    }

    private fun runSplashSequence() {
        // Step 1: Logo pops in
        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()

        // Step 2: App name fades in
        binding.tvAppName.animate()
            .alpha(1f)
            .setStartDelay(400)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Step 3: Tagline
        binding.tvTagline.animate()
            .alpha(1f)
            .setStartDelay(700)
            .setDuration(400)
            .start()

        // Step 4: Divider line expands
        binding.dividerLine.animate()
            .alpha(1f)
            .setStartDelay(900)
            .setDuration(400)
            .start()

        // Step 5: Status text + progress bar
        binding.tvStatus.animate()
            .alpha(1f)
            .setStartDelay(1100)
            .setDuration(300)
            .start()

        binding.progressLoading.animate()
            .alpha(1f)
            .setStartDelay(1100)
            .setDuration(300)
            .start()

        // Step 6: Cycle status messages then navigate
        CoroutineScope(Dispatchers.Main).launch {
            delay(1300)
            binding.tvStatus.text = getString(R.string.splash_scanning)
            delay(700)
            binding.tvStatus.text = getString(R.string.splash_ready)
            delay(500)

            // Fade out everything
            binding.root.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                }
                .start()
        }
    }
}
