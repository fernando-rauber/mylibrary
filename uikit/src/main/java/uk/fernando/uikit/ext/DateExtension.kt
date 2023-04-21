package uk.fernando.uikit.ext


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

fun Date.isSameDay(date: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal1.time = this
    cal2.time = date
    return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
}