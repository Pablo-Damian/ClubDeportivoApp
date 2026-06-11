package com.example.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FacturaCuotaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_factura_cuota)

        dbHelper = DbHelper(this)

        val tvSocio = findViewById<TextView>(R.id.tv_socio)
        val tvMonto = findViewById<TextView>(R.id.tv_monto)
        val tvFecha = findViewById<TextView>(R.id.tv_fecha)
        val tvMedioPago = findViewById<TextView>(R.id.tv_medio_pago)
        val tvIdPago = findViewById<TextView>(R.id.tv_id_pago)

        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val btnDescargarPdf = findViewById<Button>(R.id.btn_descargar_pdf)
        val btnVerCarnet = findViewById<Button>(R.id.btn_ver_carnet)

        // Se recupera el DNI enviado desde la pantalla de cobro
        val dniSocio = intent.getStringExtra("DNI_CLIENTE") ?: ""

        // Se ejecuta una consulta relacional (INNER JOIN) para obtener los datos cruzados del Cliente y su Cuota.
        val db = dbHelper.readableDatabase
        val query = """
            SELECT c.nombre, c.apellido, cu.monto, cu.fecha_pago, cu.forma_de_pago, cu.id 
            FROM Cliente c 
            INNER JOIN CuotaSocial cu ON c.dni = cu.dni_socio 
            WHERE c.dni = ? 
            ORDER BY cu.id DESC LIMIT 1
        """
        val cursor = db.rawQuery(query, arrayOf(dniSocio))

        if (cursor.moveToFirst()) {
            val nombreCompleto = "${cursor.getString(0)} ${cursor.getString(1)}"
            val monto = cursor.getDouble(2)
            val fecha = cursor.getString(3)
            val formaPago = cursor.getString(4)
            val idPago = cursor.getInt(5)

            // Población de la interfaz con los datos reales
            tvSocio.text = "Socio: $nombreCompleto"
            tvMonto.text = "Monto: $$monto"
            tvFecha.text = "Fecha: $fecha"
            tvMedioPago.text = "Medio de pago: $formaPago"
            tvIdPago.text = "ID de pago: $idPago"
        } else {
            Toast.makeText(this, "Error al cargar la factura", Toast.LENGTH_SHORT).show()
        }
        cursor.close()

        tvVolver.setOnClickListener {
            // Regreso seguro al menú principal limpiando la pila para evitar volver a la factura por error
            val intentMenu = Intent(this, MenuPrincipalActivity::class.java)
            intentMenu.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intentMenu)
            finish()
        }

        btnDescargarPdf.setOnClickListener {
            // Simulación de funcionalidad de exportación a PDF
            Toast.makeText(this, "Descargando comprobante en PDF...", Toast.LENGTH_SHORT).show()
        }

        btnVerCarnet.setOnClickListener {
            // Transición a la pantalla final del ciclo de Socio
            val intentCarnet = Intent(this, CarnetSocioActivity::class.java)
            intentCarnet.putExtra("DNI_CLIENTE", dniSocio)
            startActivity(intentCarnet)
        }
    }
}