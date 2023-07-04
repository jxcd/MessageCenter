package com.me.app.messagecenter.util

import java.time.LocalDate
import java.time.temporal.WeekFields

fun isSameWeek(date1: LocalDate, date2: LocalDate): Boolean {
    val weekFields = WeekFields.ISO
    val week1 = date1.get(weekFields.weekOfWeekBasedYear())
    val week2 = date2.get(weekFields.weekOfWeekBasedYear())
    val year1 = date1.get(weekFields.weekBasedYear())
    val year2 = date2.get(weekFields.weekBasedYear())

    return week1 == week2 && year1 == year2
}

fun isSameMonth(date1: LocalDate, date2: LocalDate): Boolean =
    date1.year == date2.year && date1.monthValue == date2.monthValue