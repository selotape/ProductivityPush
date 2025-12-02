package com.productivitypush

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.productivitypush.data.ShutdownDay
import com.productivitypush.data.ShutdownSchedule
import com.productivitypush.databinding.ActivityShutdownSettingsBinding
import com.productivitypush.service.ShutdownSchedulerService
import java.util.*

class ShutdownSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShutdownSettingsBinding
    private lateinit var shutdownScheduler: ShutdownSchedulerService
    private var currentSchedule = ShutdownSchedule()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShutdownSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shutdownScheduler = ShutdownSchedulerService(this)
        currentSchedule = shutdownScheduler.getSchedule()

        setupUI()
        loadCurrentSettings()
    }

    private fun setupUI() {
        binding.apply {
            // Enable back button
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Shutdown Settings"

            // Time picker
            btnSelectTime.setOnClickListener {
                showTimePicker()
            }

            // Warning time setup
            sliderWarningTime.addOnChangeListener { _, value, _ ->
                textWarningTime.text = "Warning: ${value.toInt()} minutes before"
            }

            // Days of week checkboxes
            checkMonday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }
            checkTuesday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }
            checkWednesday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }
            checkThursday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }
            checkFriday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }
            checkSaturday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }
            checkSunday.setOnCheckedChangeListener { _, _ -> updateSelectedDays() }

            // Allow cancel toggle
            switchAllowCancel.setOnCheckedChangeListener { _, isChecked ->
                currentSchedule = currentSchedule.copy(allowCancel = isChecked)
            }

            // Save button
            btnSave.setOnClickListener {
                saveSettings()
            }

            // Test shutdown button (for testing purposes)
            btnTestShutdown.setOnClickListener {
                testShutdown()
            }
        }
    }

    private fun loadCurrentSettings() {
        binding.apply {
            // Load shutdown time
            val shutdownHour = currentSchedule.shutdownTime / 60
            val shutdownMinute = currentSchedule.shutdownTime % 60
            val timeText = String.format("%02d:%02d", shutdownHour, shutdownMinute)
            textSelectedTime.text = "Shutdown Time: $timeText"

            // Load warning time
            sliderWarningTime.value = currentSchedule.warningMinutes.toFloat()
            textWarningTime.text = "Warning: ${currentSchedule.warningMinutes} minutes before"

            // Load selected days
            checkMonday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.MONDAY)
            checkTuesday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.TUESDAY)
            checkWednesday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.WEDNESDAY)
            checkThursday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.THURSDAY)
            checkFriday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.FRIDAY)
            checkSaturday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.SATURDAY)
            checkSunday.isChecked = currentSchedule.daysOfWeek.contains(Calendar.SUNDAY)

            // Load allow cancel setting
            switchAllowCancel.isChecked = currentSchedule.allowCancel

            // Load custom message
            editShutdownMessage.setText(currentSchedule.shutdownMessage)
        }
    }

    private fun showTimePicker() {
        val currentHour = currentSchedule.shutdownTime / 60
        val currentMinute = currentSchedule.shutdownTime % 60

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("Select Shutdown Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedTime = timePicker.hour * 60 + timePicker.minute
            currentSchedule = currentSchedule.copy(shutdownTime = selectedTime)

            val timeText = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            binding.textSelectedTime.text = "Shutdown Time: $timeText"
        }

        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun updateSelectedDays() {
        val selectedDays = mutableSetOf<Int>()

        binding.apply {
            if (checkMonday.isChecked) selectedDays.add(Calendar.MONDAY)
            if (checkTuesday.isChecked) selectedDays.add(Calendar.TUESDAY)
            if (checkWednesday.isChecked) selectedDays.add(Calendar.WEDNESDAY)
            if (checkThursday.isChecked) selectedDays.add(Calendar.THURSDAY)
            if (checkFriday.isChecked) selectedDays.add(Calendar.FRIDAY)
            if (checkSaturday.isChecked) selectedDays.add(Calendar.SATURDAY)
            if (checkSunday.isChecked) selectedDays.add(Calendar.SUNDAY)
        }

        currentSchedule = currentSchedule.copy(daysOfWeek = selectedDays)
    }

    private fun saveSettings() {
        binding.apply {
            // Update warning time from slider
            currentSchedule = currentSchedule.copy(
                warningMinutes = sliderWarningTime.value.toInt(),
                shutdownMessage = editShutdownMessage.text.toString().trim()
            )

            // Validate settings
            if (currentSchedule.daysOfWeek.isEmpty()) {
                Toast.makeText(this@ShutdownSettingsActivity, "Please select at least one day", Toast.LENGTH_SHORT).show()
                return
            }

            if (currentSchedule.shutdownMessage.isEmpty()) {
                currentSchedule = currentSchedule.copy(shutdownMessage = "Time to disconnect and focus on what matters most!")
            }

            // Save the schedule
            shutdownScheduler.saveSchedule(currentSchedule)

            Toast.makeText(this@ShutdownSettingsActivity, "Shutdown schedule saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun testShutdown() {
        Toast.makeText(this, "Testing shutdown in 5 seconds...", Toast.LENGTH_SHORT).show()

        // Create a test intent for immediate shutdown (for testing purposes)
        val testIntent = android.content.Intent(this, com.productivitypush.receiver.ShutdownReceiver::class.java).apply {
            action = com.productivitypush.receiver.ShutdownReceiver.ACTION_SHUTDOWN
            putExtra("shutdown_schedule", com.google.gson.Gson().toJson(currentSchedule))
        }

        // Delay the test by 5 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            sendBroadcast(testIntent)
        }, 5000)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}