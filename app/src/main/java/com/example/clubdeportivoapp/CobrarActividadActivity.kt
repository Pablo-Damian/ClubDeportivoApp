package com.example.clubdeportivoapp

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CobrarActividadActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private var pagoRealizado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobrar_actividad)

        dbHelper = DbHelper(this)

        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val rgModoPago = findViewById<RadioGroup>(R.id.rg_modo_pago)
        val etMonto = findViewById<EditText>(R.id.et_monto)
        val btnCobrar = findViewById<Button>(R.id.btn_cobrar)

        val dniCliente = intent.getStringExtra("DNI_CLIENTE") ?: ""
        val nombreActividad = intent.getStringExtra("NOMBRE_ACTIVIDAD") ?: ""
        var idActividad = -1

        // Se extrae el precio de la actividad seleccionada mediante una consulta a la base de datos local.
        val db = dbHelper.readableDatabase
        val cursorAct = db.rawQuery("SELECT id, precio FROM Actividad WHERE nombre = ?", arrayOf(nombreActividad))
        if (cursorAct.moveToFirst()) {
            idActividad = cursorAct.getInt(0)
            val precio = cursorAct.getDouble(1)
            etMonto.setText(precio.toString())
        }
        cursorAct.close()

        // Se bloquea el campo de monto para evitar alteraciones manuales.
        etMonto.isEnabled = false

        tvVolver.setOnClickListener { finish() }

        btnCobrar.setOnClickListener {
            if (pagoRealizado) {
                // Navegación hacia la pantalla de comprobante una vez que el pago fue procesado.
                val intentFactura = Intent(this, FacturaActividadActivity::class.java)
                intentFactura.putExtra("DNI_CLIENTE", dniCliente)
                intentFactura.putExtra("ID_ACTIVIDAD", idActividad)
                startActivity(intentFactura)
                finish()
                return@setOnClickListener
            }

            val formaPago = when (rgModoPago.checkedRadioButtonId) {
                R.id.rb_tarjeta -> "Tarjeta"
                R.id.rb_efectivo -> "Efectivo"
                else -> ""
            }

            if (formaPago.isEmpty()) {
                Toast.makeText(this, "Debe seleccionar un modo de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbWrite = dbHelper.writableDatabase
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val valoresInscripcion = ContentValues().apply {
                put("dni_cliente", dniCliente)
                put("id_actividad", idActividad)
                put("fecha_inscripcion", fechaActual)
                put("estado_pago", "PAGO")
            }

            // Se utiliza insertWithOnConflict con CONFLICT_REPLACE (UPSERT).
            // Si el cliente ya estaba preinscripto (PENDIENTE), se actualiza su estado a PAGO.
            // Si es una inscripción nueva, se inserta directamente. Esto evita excepciones de clave primaria.
            val resultado = dbWrite.insertWithOnConflict(
                "Cliente_Actividad",
                null,
                valoresInscripcion,
                SQLiteDatabase.CONFLICT_REPLACE
            )

            if (resultado != -1L) {
                // Se actualiza el cupo disponible de la actividad tras el cobro exitoso.
                dbWrite.execSQL("UPDATE Actividad SET cupo = cupo - 1 WHERE id = ?", arrayOf(idActividad))
                pagoRealizado = true
                Toast.makeText(this, "Cobro registrado correctamente", Toast.LENGTH_SHORT).show()

                // Actualización visual de la interfaz.
                btnCobrar.text = "Mostrar factura"
                rgModoPago.isEnabled = false
            } else {
                Toast.makeText(this, "Error al procesar el cobro", Toast.LENGTH_SHORT).show()
            }
        }
    }
}