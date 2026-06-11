package com.example.clubdeportivoapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CobrarCuotaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private var pagoRealizado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cobrar_cuota)

        dbHelper = DbHelper(this)

        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val rgModoPago = findViewById<RadioGroup>(R.id.rg_modo_pago)
        val etMonto = findViewById<EditText>(R.id.et_monto)
        val btnCobrar = findViewById<Button>(R.id.btn_cobrar)

        // Se recupera el DNI del socio que proviene de la pantalla de Registro o del Menú
        val dniSocio = intent.getStringExtra("DNI_CLIENTE") ?: ""

        // Configuración inicial de la UI. Se obtiene el monto oficial desde SQLite.
        val db = dbHelper.readableDatabase
        val cursorMonto = db.rawQuery("SELECT valor FROM Configuracion WHERE clave = 'monto_cuota_social'", null)
        if (cursorMonto.moveToFirst()) {
            val montoOficial = cursorMonto.getString(0)
            etMonto.setText(montoOficial)
        }
        cursorMonto.close()

        // El monto no debe ser alterado manualmente para mantener consistencia contable.
        etMonto.isEnabled = false

        tvVolver.setOnClickListener {
            finish()
        }

        btnCobrar.setOnClickListener {
            if (pagoRealizado) {
                // Navegación hacia el comprobante si el pago ya fue procesado.
                val intentFactura = Intent(this, FacturaCuotaActivity::class.java)
                intentFactura.putExtra("DNI_CLIENTE", dniSocio)
                startActivity(intentFactura)
                finish()
                return@setOnClickListener
            }

            if (dniSocio.isEmpty()) {
                Toast.makeText(this, "Error: No se identificó al socio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Se determina la forma de pago mediante el RadioGroup
            val formaPago = when (rgModoPago.checkedRadioButtonId) {
                R.id.rb_tarjeta -> "Tarjeta"
                R.id.rb_efectivo -> "Efectivo"
                else -> ""
            }

            if (formaPago.isEmpty()) {
                Toast.makeText(this, "Debe seleccionar un modo de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0

            // Cálculo automático de fechas
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaPago = dateFormat.format(Date())

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1)
            val fechaVencimiento = dateFormat.format(calendar.time)

            // Registro contable en SQLite
            val dbWrite = dbHelper.writableDatabase
            val valoresCuota = ContentValues().apply {
                put("dni_socio", dniSocio)
                put("fecha_pago", fechaPago)
                put("fecha_vencimiento", fechaVencimiento)
                put("monto", monto)
                put("forma_de_pago", formaPago)
            }

            val idCuota = dbWrite.insert("CuotaSocial", null, valoresCuota)

            if (idCuota != -1L) {
                pagoRealizado = true
                Toast.makeText(this, "Cobro registrado correctamente", Toast.LENGTH_SHORT).show()

                // Modificación de UI tras el éxito del pago
                btnCobrar.text = "Mostrar factura"
                rgModoPago.isEnabled = false
            } else {
                Toast.makeText(this, "Error al procesar el cobro", Toast.LENGTH_SHORT).show()
            }
        }
    }
}