package ext

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 * json 转 对象
 */
inline fun <reified T> String?.jsonToBean(clazz: Class<T>): T? {
    return try {
        Gson().fromJson(this, clazz)
    } catch (e: Exception) {
        //e.printStackTrace()
        null
    }
}

/**
 * json 转 map
 */
fun String.jsonToMap(): HashMap<String, String> {
    val type = object : TypeToken<HashMap<String, String>>() {}.type
    return try {
        Gson().fromJson(this, type)
    } catch (e: Exception) {
        e.printStackTrace()
        hashMapOf()
    }
}

/**
 * 对象转json
 */
fun Any.toJson(): String {
    return GsonBuilder().disableHtmlEscaping().create().toJson(this)
}
