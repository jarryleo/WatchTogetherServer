package com.whitewolfx.module_base.ext

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @author : ling luo
 * @date : 2020/11/25
 * @description : json 相关类
 */
/**
 * json 转 对象
 */
inline fun <reified T> String?.jsonToBean(clazz: Class<T>): T? {
    return try {
        Gson().fromJson(this, clazz)
    } catch (e: Exception) {
        e.printStackTrace()
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

/**
 * 对象以json 形式提交 post 请求
 * 对象转RequestBody
 */
fun Any.toRequestBody(): RequestBody {
    val mediaType = "application/json; charset=utf-8".toMediaType()
    return this.toJson().toRequestBody(mediaType)
}