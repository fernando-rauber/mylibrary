package uk.fernando.util.ext


import java.text.SimpleDateFormat
import java.util.*

fun Date.formatToTime(format: String = "HH:mm"): String {
    val parser = SimpleDateFormat(format, Locale.getDefault())
    return parser.format(this)
}

fun Date.formatToDate(format: String = "dd/MM/yyyy"): String {
    val parser = SimpleDateFormat(format, Locale.getDefault())
    return parser.format(this)
}