package com.example.clubdeportivoapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CarnetSocioActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet_socio)

        dbHelper = DbHelper(this)

        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val tvNombre = findViewById<TextView>(R.id.tv_nombre)
        val tvDni = findViewById<TextView>(R.id.tv_dni)
        val tvIngreso = findViewById<TextView>(R.id.tv_ingreso)
        val tvNroCarnet = findViewById<TextView>(R.id.tv_nro_carnet)
        val btnImprimir = findViewById<Button>(R.id.btn_imprimir)

        val dniSocio = intent.getStringExtra("DNI_CLIENTE") ?: ""

        // Se ejecuta consulta relacional para popular los datos del Carnet.
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT c.nombre, c.apellido, c.dni, s.fecha_ingreso, s.numero_carnet " +
                    "FROM Cliente c INNER JOIN Socio s ON c.dni = s.dni WHERE c.dni = ?",
            arrayOf(dniSocio)
        )

        if (cursor.moveToFirst()) {
            val nombreCompleto = "${cursor.getString(0)} ${cursor.getString(1)}"
            val dni = cursor.getString(2)
            val fechaIngreso = cursor.getString(3)
            val nroCarnet = cursor.getInt(4)

            tvNombre.text = nombreCompleto
            tvDni.text = "DNI: $dni"
            tvIngreso.text = "Socio desde: $fechaIngreso"
            tvNroCarnet.text = "Nº de Carnet: $nroCarnet"
        } else {
            Toast.makeText(this, "Error al cargar los datos del carnet", Toast.LENGTH_SHORT).show()
        }
        cursor.close()

        tvVolver.setOnClickListener {
            // Cierra el carnet y vuelve a la factura (la pantalla anterior en la pila)
            finish()
        }

        btnImprimir.setOnClickListener {
            // Se simula la acción requerida por la interfaz, dado que la generación nativa de PDF excede el alcance del prototipo.
            Toast.makeText(this, "Conectando con la impresora...", Toast.LENGTH_SHORT).show()
        }
    }
}