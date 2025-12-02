package com.productivitypush

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.productivitypush.databinding.ActivityMainBinding
import com.productivitypush.service.AppMonitoringService
import com.productivitypush.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupUI()
        observeViewModel()
        checkPermissions()
    }

    private fun setupUI() {
        binding.apply {
            // Toggle blocking service
            switchBlockingEnabled.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleBlocking(isChecked)
                if (isChecked) {
                    startAppMonitoringService()
                } else {
                    stopAppMonitoringService()
                }
            }

            // Settings button
            btnSettings.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }

            // Add task button
            btnAddTask.setOnClickListener {
                val taskText = editTaskInput.text.toString().trim()
                if (taskText.isNotEmpty()) {
                    viewModel.addTask(taskText)
                    editTaskInput.setText("")
                } else {
                    Toast.makeText(this@MainActivity, "Please enter a task", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.apply {
            isBlocking.observe(this@MainActivity) { isEnabled ->
                binding.switchBlockingEnabled.isChecked = isEnabled
                binding.textBlockingStatus.text = if (isEnabled) "Blocking Active" else "Blocking Inactive"
            }

            dailyTasks.observe(this@MainActivity) { count ->
                binding.textDailyTasks.text = "Tasks Completed Today: $count"
            }

            motivationalMessage.observe(this@MainActivity) { message ->
                binding.textMotivation.text = message
            }
        }
    }

    private fun checkPermissions() {
        // Check if app has usage access permission
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        }

        // Check overlay permission for blocking screen
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        // Implementation to check usage stats permission
        return true // Simplified for boilerplate
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, "Please enable usage access for ProductivityPush", Toast.LENGTH_LONG).show()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivity(intent)
        Toast.makeText(this, "Please enable overlay permission for app blocking", Toast.LENGTH_LONG).show()
    }

    private fun startAppMonitoringService() {
        val intent = Intent(this, AppMonitoringService::class.java)
        startForegroundService(intent)
    }

    private fun stopAppMonitoringService() {
        val intent = Intent(this, AppMonitoringService::class.java)
        stopService(intent)
    }
}