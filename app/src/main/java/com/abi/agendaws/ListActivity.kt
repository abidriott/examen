package com.abi.agendaws

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.abi.agendaws.Objects.Contacts
import com.abi.agendaws.Objects.Device
import com.abi.agendaws.Objects.PHPProcesses
import com.abi.agendaws.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class ListActivity : android.app.ListActivity(), Response.Listener<JSONObject>, Response.ErrorListener {
    private lateinit var btnNuevo: Button
    private val context: Context = this
    private val php = PHPProcesses()
    private lateinit var request: RequestQueue
    private lateinit var jsonObjectRequest: JsonObjectRequest
    private var listaContactos: ArrayList<Contacts> = ArrayList()
    private val serverip = "http://3.145.146.100/WebService/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        request = Volley.newRequestQueue(context)
        php.setContext(context)
        consultarTodosWebService()
        btnNuevo = findViewById(R.id.btnNew)
        btnNuevo.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun consultarTodosWebService() {
        val url = "$serverip/wsConsultarTodos.php?idMovil=${Device.getSecureId(this)}"
        jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, this, this)
        request.add(jsonObjectRequest)
    }

    override fun onErrorResponse(error: VolleyError) {
        // Manejo del error
    }

    override fun onResponse(response: JSONObject) {
        var contacto: Contacts?
        val json: JSONArray = response.optJSONArray("contactos") ?: return

        try {
            for (i in 0 until json.length()) {
                contacto = Contacts().apply {
                    val jsonObject: JSONObject = json.getJSONObject(i)
                    _ID = jsonObject.optInt("_ID")
                    nombre = jsonObject.optString("nombre")
                    telefono1 = jsonObject.optString("telefono1")
                    telefono2 = jsonObject.optString("telefono2")
                    direccion = jsonObject.optString("direccion")
                    notas = jsonObject.optString("notas")
                    favorite = jsonObject.optInt("favorite")
                    idMovil = jsonObject.optString("idMovil")
                }
                listaContactos.add(contacto)
            }
            val adapter = MyArrayAdapter(context, R.layout.layout_contact, listaContactos)
            listAdapter = adapter
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    inner class MyArrayAdapter(
        context: Context,
        private val textViewRecursoId: Int,
        private val objects: ArrayList<Contacts>
    ) : ArrayAdapter<Contacts>(context, textViewRecursoId, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(textViewRecursoId, null)

            val lblNombre: TextView = view.findViewById(R.id.lblNombreContacto)
            val lblTelefono: TextView = view.findViewById(R.id.lblTelefonoContacto)
            val btnModificar: Button = view.findViewById(R.id.btnModificar)
            val btnBorrar: Button = view.findViewById(R.id.btnBorrar)

            if (objects[position].favorite > 0) {
                lblNombre.setTextColor(Color.BLUE)
                lblTelefono.setTextColor(Color.BLUE)
            } else {
                lblNombre.setTextColor(Color.BLACK)
                lblTelefono.setTextColor(Color.BLACK)
            }

            lblNombre.text = objects[position].nombre
            lblTelefono.text = objects[position].telefono1

            btnBorrar.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Confirmar eliminación")
                builder.setMessage("¿Estás seguro de que quieres eliminar este contacto?")
                builder.setPositiveButton("Sí") { dialog, _ ->
                    php.borrarContactoWebService(objects[position]._ID)
                    objects.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(applicationContext, "Contacto eliminado con éxito", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val alert = builder.create()
                alert.show()
            }

            btnModificar.setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("contact", objects[position])
                }
                val intent = Intent().apply {
                    putExtras(bundle)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            return view
        }
    }
}
