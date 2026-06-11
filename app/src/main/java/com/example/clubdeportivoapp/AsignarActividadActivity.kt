package com.example.clubdeportivoapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AsignarActividadActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private val listaCheckBoxes = mutableListOf<CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignar_actividades)

        dbHelper = DbHelper(this)

        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val etDni = findViewById<EditText>(R.id.et_dni)
        val btnConfirmar = findViewById<Button>(R.id.btn_confirmar)
        val llContenedorActividades = findViewById<LinearLayout>(R.id.ll_contenedor_actividades)

        tvVolver.setOnClickListener { finish() }

        // Se leen las actividades disponibles desde SQLite y se inyectan dinámicamente como CheckBoxes.
        val db = dbHelper.readableDatabase
        val cursorActividades = db.rawQuery("SELECT nombre, precio, cupo FROM Actividad WHERE cupo > 0", null)

        while (cursorActividades.moveToNext()) {
            val nombreAct = cursorActividades.getString(0)
            val precio = cursorActividades.getDouble(1)
            val cupo = cursorActividades.getInt(2)

            val checkBox = CheckBox(this).apply {
                text = "$nombreAct - $$precio (Cupos: $cupo)"
                setTextColor(resources.getColor(android.R.color.white, theme))
                textSize = 16f
                tag = nombreAct // Se almacena el nombre original para consultas posteriores
            }
            llContenedorActividades.addView(checkBox)
            listaCheckBoxes.add(checkBox)
        }
        cursorActividades.close()

        btnConfirmar.setOnClickListener {
            val dniIngresado = etDni.text.toString().trim()

            if (dniIngresado.isEmpty()) {
                Toast.makeText(this, "Debe ingresar el DNI del cliente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seleccionados = listaCheckBoxes.filter { it.isChecked }

            if (seleccionados.isEmpty()) {
                Toast.makeText(this, "Seleccione al menos una actividad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (seleccionados.size > 1) {
                Toast.makeText(this, "Por favor, asigne de a una actividad por vez", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val actividadElegida = seleccionados.first().tag.toString()
            val dbWrite = dbHelper.writableDatabase

            val cursorCliente = dbWrite.rawQuery("SELECT tipo_cliente FROM Cliente WHERE dni = ?", arrayOf(dniIngresado))
            if (!cursorCliente.moveToFirst()) {
                Toast.makeText(this, "Error: El DNI no está registrado", Toast.LENGTH_SHORT).show()
                cursorCliente.close()
                return@setOnClickListener
            }

            val tipoCliente = cursorCliente.getString(0)
            cursorCliente.close()

            if (tipoCliente == "SOCIO") {
                // Lógica exclusiva para Socios: inscripción gratuita y actualización de cupo inmediata.
                val cursorAct = dbWrite.rawQuery("SELECT id FROM Actividad WHERE nombre = ?", arrayOf(actividadElegida))
                if (cursorAct.moveToFirst()) {
                    val idActividad = cursorAct.getInt(0)

                    val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val valoresInscripcion = ContentValues().apply {
                        put("dni_cliente", dniIngresado)
                        put("id_actividad", idActividad)
                        put("fecha_inscripcion", fechaActual)
                        put("estado_pago", "PAGO")
                    }

                    dbWrite.insert("Cliente_Actividad", null, valoresInscripcion)
                    dbWrite.execSQL("UPDATE Actividad SET cupo = cupo - 1 WHERE id = ?", arrayOf(idActividad))

                    Toast.makeText(this, "Asignación exitosa. Los Socios no abonan actividad.", Toast.LENGTH_LONG).show()
                }
                cursorAct.close()
                finish()
                return@setOnClickListener
            }

            // Si es NO SOCIO, se transfiere el flujo a la pantalla de cobro.
            val intentCobro = Intent(this, CobrarActividadActivity::class.java)
            intentCobro.putExtra("DNI_CLIENTE", dniIngresado)
            intentCobro.putExtra("NOMBRE_ACTIVIDAD", actividadElegida)
            startActivity(intentCobro)
            finish()
        }
    }
}