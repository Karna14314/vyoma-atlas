package com.karnadigital.vyoma.atlas.core.math

import java.util.*
import kotlin.math.floor

const val MINUTES_PER_HOUR = 60.0
const val SECONDS_PER_HOUR = 3600.0
const val HOURS_TO_DEGREES = 360.0f / 24.0f

fun julianDay(date: Date): Double {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
    cal.time = date
    val hour = cal[Calendar.HOUR_OF_DAY] + cal[Calendar.MINUTE] / MINUTES_PER_HOUR + cal[Calendar.SECOND] / SECONDS_PER_HOUR
    val year = cal[Calendar.YEAR]
    val month = cal[Calendar.MONTH] + 1
    val day = cal[Calendar.DAY_OF_MONTH]
    return (367.0 * year - floor(
        7.0 * (year + floor((month + 9.0) / 12.0)) / 4.0
    ) + floor(275.0 * month / 9.0) + day + 1721013.5 + hour / 24.0)
}

fun meanSiderealTime(date: Date, longitude: Float): Float {
    val jd = julianDay(date)
    val delta = jd - 2451545.0
    val gst = 280.461 + 360.98564737 * delta
    val lst = positiveMod(gst + longitude, 360.0)
    return lst.toFloat()
}

fun calculateRADecOfZenith(utc: Date, location: LatLong): RaDec {
    val myRa = meanSiderealTime(utc, location.longitude)
    val myDec = location.latitude
    return RaDec(myRa, myDec)
}
