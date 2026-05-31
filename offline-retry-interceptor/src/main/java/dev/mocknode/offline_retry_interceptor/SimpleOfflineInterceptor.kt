package dev.mocknode.offline_retry_interceptor

import android.content.Context
import android.content.SharedPreferences
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import okio.Buffer
import java.io.IOException
import kotlin.concurrent.thread

class SimpleOfflineInterceptor(context: Context) : Interceptor {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mocknode_queue", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return try {
            val response = chain.proceed(request)


            if (response.isSuccessful) {
                flushQueueInBackground()
            }
            response

        } catch (e: IOException) {
            if (request.method != "GET") {
                saveToQueue(request)
            }
            throw e
        }
    }

    private fun saveToQueue(request: Request) {
        val queueArray = JSONArray(prefs.getString("queue", "[]"))

        val requestObj = JSONObject().apply {
            put("url", request.url.toString())
            put("method", request.method)

            val headersMap = mutableMapOf<String, String>()
            request.headers.forEach { headersMap[it.first] = it.second }
            put("headers", JSONObject(headersMap as Map<*, *>))

            request.body?.let {
                val buffer = Buffer()
                it.writeTo(buffer)
                put("body", buffer.readUtf8())
            }
        }

        queueArray.put(requestObj)
        prefs.edit().putString("queue", queueArray.toString()).apply()
    }

    private fun flushQueueInBackground() {
        val queueString = prefs.getString("queue", "[]")
        if (queueString == "[]") return

        prefs.edit().putString("queue", "[]").apply()

        thread {
            val queueArray = JSONArray(queueString)
            val client = OkHttpClient()
            val failedAgainQueue = JSONArray()

            for (i in 0 until queueArray.length()) {
                try {
                    val item = queueArray.getJSONObject(i)
                    val builder = Request.Builder().url(item.getString("url"))

                    val headers = item.getJSONObject("headers")
                    headers.keys().forEach { key ->
                        builder.addHeader(key, headers.getString(key))
                    }

                    val bodyStr = if (item.has("body")) item.getString("body") else null
                    val body = bodyStr?.toRequestBody("application/json".toMediaTypeOrNull())

                    builder.method(item.getString("method"), body)

                    val response = client.newCall(builder.build()).execute()

                    if (!response.isSuccessful) failedAgainQueue.put(item)

                } catch (e: Exception) {
                    failedAgainQueue.put(queueArray.getJSONObject(i))
                }
            }

            if (failedAgainQueue.length() > 0) {
                prefs.edit().putString("queue", failedAgainQueue.toString()).apply()
            }
        }
    }
}