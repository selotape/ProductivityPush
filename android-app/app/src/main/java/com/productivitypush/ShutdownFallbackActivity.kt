package com.productivitypush

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.productivitypush.databinding.ActivityShutdownFallbackBinding

class ShutdownFallbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShutdownFallbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShutdownFallbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreenMode()
        setupUI()
    }

    private fun setupFullscreenMode() {
        // Make activity fullscreen and always on top
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Hide system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Disable back button and recent apps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAW_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    private fun setupUI() {
        binding.apply {
            // Set motivational messages
            textShutdownTitle.text = "📵 Digital Detox Mode"
            textShutdownMessage.text = "Your phone is now in shutdown mode. Time to disconnect and focus on what truly matters."
            textMotivationalMessage.text = "Use this time to:\n\n• Read a book\n• Exercise or go for a walk\n• Have meaningful conversations\n• Practice mindfulness\n• Work on personal projects\n• Get quality sleep"

            // Emergency exit button (hidden by default)
            btnEmergencyExit.setOnClickListener {
                finish()
            }

            // Long press to reveal emergency exit
            root.setOnLongClickListener {
                btnEmergencyExit.visibility = if (btnEmergencyExit.visibility == View.VISIBLE) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
                true
            }
        }
    }

    override fun onBackPressed() {
        // Disable back button
        // Do nothing
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            // Try to regain focus when user tries to leave
            val intent = intent
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NO_HISTORY
            )
            startActivity(intent)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }
}