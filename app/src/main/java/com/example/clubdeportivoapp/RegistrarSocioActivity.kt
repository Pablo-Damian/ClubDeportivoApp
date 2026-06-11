package com.example.clubdeportivoapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class RegistrarSocioActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_socio)

        dbHelper = DbHelper(this)

        // Vinculación de componentes UI
        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val etNombreApellido = findViewById<EditText>(R.id.et_nombre_apellido)
        val etDni = findViewById<EditText>(R.id.et_dni)
        val etFechaNac = findViewById<EditText>(R.id.et_fecha_nac)
        val etTelefono = findViewById<EditText>(R.id.et_telefono)
        val spinnerTipo = findViewById<Spinner>(R.id.spinner_tipo)
        val cbAptoFisico = findViewById<CheckBox>(R.id.cb_apto_fisico)
        val btnRegistrar = findViewById<Button>(R.id.btn_registrar)

        // Se carga el Spinner con las opciones de tipo de cliente compatibles con el CHECK de la BD.
        val opcionesTipo = arrayOf("SOCIO", "NO SOCIO")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcionesTipo)
        spinnerTipo.adapter = adapter

        tvVolver.setOnClickListener {
            finish() // Cierra la pantalla actual y vuelve a la anterior en la pila (Menú).
        }

        btnRegistrar.setOnClickListener {
            val nombreApellido = etNombreApellido.text.toString().trim()
            val dni = etDni.text.toString().trim()
            val fechaNac = etFechaNac.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val tipoCliente = spinnerTipo.selectedItem.toString()

            // Control estructural de campos obligatorios y regla de negocio (Apto Físico).
            if (nombreApellido.isEmpty() || dni.isEmpty() || fechaNac.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbAptoFisico.isChecked) {
                Toast.makeText(this, "Es obligatorio presentar el Apto Físico", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase

            // Se audita la unicidad del DNI en la tabla Cliente antes de insertar.
            val cursorDni = db.rawQuery("SELECT dni FROM Cliente WHERE dni = ?", arrayOf(dni))
            if (cursorDni.count > 0) {
                cursorDni.close()
                Toast.makeText(this, "El DNI ya se encuentra registrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            cursorDni.close()

            // Se separan lógicamente el nombre y apellido para normalización en BD.
            val partesNombre = nombreApellido.split(" ", limit = 2)
            val nombre = partesNombre[0]
            val apellido = if (partesNombre.size > 1) partesNombre[1] else ""

            val valoresCliente = ContentValues().apply {
                put("dni", dni)
                put("nombre", nombre)
                put("apellido", apellido)
                put("fecha_nacimiento", fechaNac)
                put("telefono", telefono)
                put("tipo_cliente", tipoCliente)
            }

            // Ejecución de la transacción DML principal.
            val idCliente = db.insert("Cliente", null, valoresCliente)

            if (idCliente != -1L) {
                val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (tipoCliente == "SOCIO") {
                    // Especialización del registro: Inserción en tabla Socio generando carnet automático.
                    val numeroCarnet = Random.nextInt(1000, 9999)

                    val valoresSocio = ContentValues().apply {
                        put("dni", dni)
                        put("fecha_ingreso", fechaActual)
                        put("numero_carnet", numeroCarnet)
                    }
                    db.insert("Socio", null, valoresSocio)

                    Toast.makeText(this, "Socio registrado con éxito", Toast.LENGTH_SHORT).show()

                    // Se delega el flujo al módulo de cobro pasando el DNI de referencia.
                    val intent = Intent(this, CobrarCuotaActivity::class.java)
                    intent.putExtra("DNI_CLIENTE", dni)
                    startActivity(intent)
                    finish()

                } else {
                    // Especialización del registro: Inserción en tabla NoSocio.
                    val valoresNoSocio = ContentValues().apply {
                        put("dni", dni)
                        put("ultima_visita", fechaActual)
                    }
                    db.insert("NoSocio", null, valoresNoSocio)

                    Toast.makeText(this, "No Socio registrado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Error al registrar en la base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}