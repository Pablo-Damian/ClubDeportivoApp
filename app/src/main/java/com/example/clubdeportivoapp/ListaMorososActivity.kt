package com.example.clubdeportivoapp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaMorososActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_morosos)

        dbHelper = DbHelper(this)

        val tvVolver = findViewById<TextView>(R.id.tv_volver)
        val llContenedorMorosos = findViewById<LinearLayout>(R.id.ll_contenedor_morosos)

        tvVolver.setOnClickListener { finish() }

        val db = dbHelper.readableDatabase
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Consulta relacional: Se busca la última cuota pagada por cada socio y se verifica si su fecha de vencimiento es igual o menor a la fecha actual.
        val query = """
            SELECT c.nombre, c.apellido, cs.fecha_vencimiento 
            FROM Cliente c 
            INNER JOIN Socio s ON c.dni = s.dni 
            INNER JOIN CuotaSocial cs ON s.dni = cs.dni_socio 
            WHERE cs.id = (
                SELECT MAX(id) FROM CuotaSocial WHERE dni_socio = s.dni
            ) AND cs.fecha_vencimiento <= ?
        """

        val cursor = db.rawQuery(query, arrayOf(fechaActual))

        if (cursor.count == 0) {
            Toast.makeText(this, "No hay socios con cuotas vencidas al día de hoy.", Toast.LENGTH_LONG).show()
        }

        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val apellido = cursor.getString(1)
            val fechaVencimiento = cursor.getString(2)

            // Creación dinámica de la fila para cada moroso
            val filaLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.WHITE)
                setPadding(32, 32, 32, 32)
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvNombre = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "$nombre $apellido"
                setTextColor(Color.parseColor("#333333"))
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val tvVencimiento = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = "Venció: $fechaVencimiento"
                setTextColor(Color.parseColor("#D32F2F"))
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            filaLayout.addView(tvNombre)
            filaLayout.addView(tvVencimiento)
            llContenedorMorosos.addView(filaLayout)
        }
        cursor.close()
    }
}