package com.productivitypush.data

import java.util.*

data class ShutdownSchedule(
    val isEnabled: Boolean = false,
    val shutdownTime: Int = 22 * 60, // Default 22:00 (10 PM) in minutes from midnight
    val daysOfWeek: Set<Int> = setOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY
    ), // Default weekdays
    val warningMinutes: Int = 10, // Warning time before shutdown
    val allowCancel: Boolean = true, // Allow user to cancel shutdown
    val shutdownMessage: String = "Time to disconnect and focus on what matters most!"
)

enum class ShutdownDay(val calendarValue: Int, val displayName: String) {
    MONDAY(Calendar.MONDAY, "Monday"),
    TUESDAY(Calendar.TUESDAY, "Tuesday"),
    WEDNESDAY(Calendar.WEDNESDAY, "Wednesday"),
    THURSDAY(Calendar.THURSDAY, "Thursday"),
    FRIDAY(Calendar.FRIDAY, "Friday"),
    SATURDAY(Calendar.SATURDAY, "Saturday"),
    SUNDAY(Calendar.SUNDAY, "Sunday");

    companion object {
        fun fromCalendarValue(value: Int): ShutdownDay? {
            return values().find { it.calendarValue == value }
        }
    }
}