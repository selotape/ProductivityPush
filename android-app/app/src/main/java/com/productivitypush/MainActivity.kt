package com.productivitypush

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.productivitypush.databinding.ActivityMainBinding
import com.productivitypush.service.AppMonitoringService
import com.productivitypush.service.ShutdownSchedulerService
import com.productivitypush.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var shutdownScheduler: ShutdownSchedulerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        shutdownScheduler = ShutdownSchedulerService(this)

        setupUI()
        observeViewModel()
        updateShutdownInfo()
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

            // Shutdown toggle
            switchShutdownEnabled.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleShutdown(isChecked)
                if (isChecked) {
                    val schedule = shutdownScheduler.getSchedule().copy(isEnabled = true)
                    shutdownScheduler.saveSchedule(schedule)
                    Toast.makeText(this@MainActivity, "Scheduled shutdown enabled", Toast.LENGTH_SHORT).show()
                } else {
                    val schedule = shutdownScheduler.getSchedule().copy(isEnabled = false)
                    shutdownScheduler.saveSchedule(schedule)
                    Toast.makeText(this@MainActivity, "Scheduled shutdown disabled", Toast.LENGTH_SHORT).show()
                }
                updateShutdownInfo()
            }

            // Configure shutdown button
            btnConfigureShutdown.setOnClickListener {
                startActivity(Intent(this@MainActivity, ShutdownSettingsActivity::class.java))
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

            isShutdownEnabled.observe(this@MainActivity) { isEnabled ->
                binding.switchShutdownEnabled.isChecked = isEnabled
                binding.textShutdownStatus.text = if (isEnabled) "Shutdown Scheduled" else "Shutdown Disabled"
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

    private fun updateShutdownInfo() {
        val schedule = shutdownScheduler.getSchedule()
        viewModel.setShutdownEnabled(schedule.isEnabled)

        if (schedule.isEnabled) {
            val nextShutdownTime = shutdownScheduler.getNextShutdownTime()
            if (nextShutdownTime != null) {
                val formatter = SimpleDateFormat("EEEE 'at' h:mm a", Locale.getDefault())
                val nextShutdownText = "Next shutdown: ${formatter.format(Date(nextShutdownTime))}"
                binding.textNextShutdown.text = nextShutdownText
            } else {
                binding.textNextShutdown.text = "No shutdown scheduled"
            }
        } else {
            binding.textNextShutdown.text = "Shutdown disabled"
        }
    }

    override fun onResume() {
        super.onResume()
        updateShutdownInfo() // Refresh shutdown info when returning to activity
    }
}