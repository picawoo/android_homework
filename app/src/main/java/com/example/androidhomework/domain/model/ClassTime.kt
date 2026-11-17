package com.example.androidhomework.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ClassTimeRegex = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

private val ClassTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

fun parseClassTime(text: String): LocalTime? =
    text.trim()
        .takeIf { ClassTimeRegex.matches(it) }
        ?.let { LocalTime.parse(it, ClassTimeFormatter) }

fun formatClassTime(time: LocalTime): String = time.format(ClassTimeFormatter)