package com.example.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuPrincipalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se asocia a la vista del menú
        setContentView(R.layout.activity_menu_principal)

        // Vinculación de los componentes de la interfaz
        val tvSaludo = findViewById<TextView>(R.id.tv_saludo)
        val tvCerrarSesion = findViewById<TextView>(R.id.tv_cerrar_sesion)
        val btnRegistrarSocio = findViewById<Button>(R.id.btn_registrar_socio)
        val btnAsignarActividad = findViewById<Button>(R.id.btn_asignar_actividad)
        val btnListarMorosos = findViewById<Button>(R.id.btn_listar_morosos)

        // Se recupera el dato enviado desde MainActivity mediante Intent para personalizar la experiencia.
        val usuario = intent.getStringExtra("USUARIO_LOGUEADO") ?: "Usuario"
        tvSaludo.text = "Hola, $usuario"

        // =========================================================================
        // PUNTO 8: Lógica de Cierre de Sesión
        // =========================================================================
        tvCerrarSesion.setOnClickListener {
            // Se implementa un cierre de sesión seguro.
            // Se invoca a MainActivity (Login) limpiando la pila de actividades (Back Stack).
            // Esto evita que el usuario pueda presionar el botón "Atrás" físico y regresar al menú sin estar autenticado.
            val intentLogin = Intent(this, MainActivity::class.java)
            intentLogin.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentLogin)
            finish()
        }

        // =========================================================================
        // PUNTO 7: Navegación Central (Intents)
        // =========================================================================
        btnRegistrarSocio.setOnClickListener {
            val intent = Intent(this, RegistrarSocioActivity::class.java)
            startActivity(intent)
        }

        btnAsignarActividad.setOnClickListener {
            val intent = Intent(this, AsignarActividadActivity::class.java)
            startActivity(intent)
        }

        btnListarMorosos.setOnClickListener {
            val intent = Intent(this, ListaMorososActivity::class.java)
            startActivity(intent)
        }
    }
}