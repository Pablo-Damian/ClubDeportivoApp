package com.example.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FacturaActividadActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_factura_actividad)

        dbHelper = DbHelper(this)

        // Vinculación de los componentes de la interfaz
        val tvTitulo = findViewById<TextView>(R.id.tv_titulo_comprobante)
        val tvMonto = findViewById<TextView>(R.id.tv_monto)
        val tvFecha = findViewById<TextView>(R.id.tv_fecha)
        val tvIdPago = findViewById<TextView>(R.id.tv_id_pago)
        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val btnDescargarPdf = findViewById<Button>(R.id.btn_descargar_pdf)

        // Recepción de parámetros desde la actividad de cobro
        val dniCliente = intent.getStringExtra("DNI_CLIENTE") ?: ""
        val idActividad = intent.getIntExtra("ID_ACTIVIDAD", -1)

        // Se ejecuta una consulta relacional (INNER JOIN) para extraer los datos combinados de la actividad y la inscripción.
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT a.nombre, a.precio, ca.fecha_inscripcion " +
                    "FROM Cliente_Actividad ca INNER JOIN Actividad a ON ca.id_actividad = a.id " +
                    "WHERE ca.dni_cliente = ? AND ca.id_actividad = ?",
            arrayOf(dniCliente, idActividad.toString())
        )

        if (cursor.moveToFirst()) {
            val nombreAct = cursor.getString(0)
            val precio = cursor.getDouble(1)
            val fecha = cursor.getString(2)

            // Asignación de datos a los componentes visuales
            tvTitulo.text = "Comprobante: $nombreAct"
            tvMonto.text = "Monto: $$precio"
            tvFecha.text = "Fecha: $fecha"
            tvIdPago.text = "ID de pago: ACT-$dniCliente-$idActividad"
        } else {
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show()
        }
        cursor.close()

        tvVolver.setOnClickListener {
            // Regreso seguro al menú principal limpiando la pila de actividades para evitar volver a la factura por error mediante el botón Atrás.
            val intentMenu = Intent(this, MenuPrincipalActivity::class.java)
            intentMenu.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intentMenu)
            finish()
        }

        btnDescargarPdf.setOnClickListener {
            // Se simula la funcionalidad de exportación a PDF mediante una notificación.
            Toast.makeText(this, "Descargando comprobante en PDF...", Toast.LENGTH_SHORT).show()
        }
    }
}