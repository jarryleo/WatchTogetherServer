package ext

import java.text.SimpleDateFormat
import java.util.*

/**
 * 打印日志
 */
fun log(text:String){
    val s = System.currentTimeMillis().time()
    println("[$s] $text")
}

/**
 * 格式化时间
 */
private fun Long.time(): String = this.toDate("yyyy-MM-dd HH:mm:ss")

/**
 * 转换成指定日期格式
 */
private fun Long.toDate(format: String): String {
    return SimpleDateFormat(format, Locale.CHINA).apply {
        //timeZone = TimeZone.getTimeZone("GMT+8:00")
        timeZone = TimeZone.getDefault()
    }.format(Date(this))
}