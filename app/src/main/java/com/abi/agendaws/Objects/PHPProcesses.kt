package com.abi.agendaws.Objects

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.ArrayList

class PHPProcesses : Response.Listener<JSONObject>, Response.ErrorListener {
    private var request: RequestQueue? = null
    private var jsonObjectRequest: JsonObjectRequest? = null
    private val contactos = ArrayList<Contacts>()
    private val serverip = "http://3.145.146.100/WebService/"

    fun setContext(context: Context) {
        request = Volley.newRequestQueue(context)
    }

    fun insertarContactoWebService(c: Contacts) {
        var url = "$serverip/wsRegistro.php?nombre=${c.nombre}&telefono1=${c.telefono1}&telefono2=${c.telefono2}&direccion=${c.direccion}&notas=${c.notas}&favorite=${c.favorite}&idMovil=${c.idMovil}"
        url = url.replace(" ", "%20")
        Log.d("PHPProcesses", "Insert URL: $url")
        jsonObjectRequest = CustomJsonObjectRequest(Request.Method.GET, url, null, this, this)
        request?.add(jsonObjectRequest)
    }

    fun actualizarContactoWebService(c: Contacts, id: Int) {
        var url = "$serverip/wsActualizar.php?_ID=$id&nombre=${c.nombre}&direccion=${c.direccion}&telefono1=${c.telefono1}&telefono2=${c.telefono2}&notas=${c.notas}&favorite=${c.favorite}"
        url = url.replace(" ", "%20")
        Log.d("PHPProcesses", "Update URL: $url")
        jsonObjectRequest = CustomJsonObjectRequest(Request.Method.GET, url, null, this, this)
        request?.add(jsonObjectRequest)
    }

    fun borrarContactoWebService(id: Int) {
        val url = "$serverip/wsEliminar.php?_ID=$id"
        Log.d("PHPProcesses", "Delete URL: $url")
        jsonObjectRequest = CustomJsonObjectRequest(Request.Method.GET, url, null, this, this)
        request?.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e("PHPProcesses", "Error: ${error.message}", error)
        if (error.networkResponse != null) {
            Log.e("PHPProcesses", "HTTP Status Code: ${error.networkResponse.statusCode}")
            Log.e("PHPProcesses", "Response Data: ${String(error.networkResponse.data)}")
        }
    }

    override fun onResponse(response: JSONObject) {
        Log.d("PHPProcesses", "Response: $response")
        try {
            val success = response.getBoolean("success")
            if (success) {
                Log.d("PHPProcesses", "Operation successful")
            } else {
                Log.d("PHPProcesses", "Operation failed: ${response.getString("message")}")
            }
        } catch (e: Exception) {
            Log.e("PHPProcesses", "Response handling error: ${e.message}", e)
        }
    }

    private class CustomJsonObjectRequest(
        method: Int,
        url: String,
        jsonRequest: JSONObject?,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ) : JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
        override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
            return try {
                val jsonString = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers, "utf-8")))
                Log.d("PHPProcesses", "Raw Response: $jsonString")
                Response.success(JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response))
            } catch (e: UnsupportedEncodingException) {
                Response.error(ParseError(e))
            } catch (je: Exception) {
                Response.error(ParseError(je))
            }
        }
    }
}
