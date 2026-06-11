package com.example.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se asocia la lógica a la vista xml del login
        setContentView(R.layout.activity_main)

        // Inicialización del Helper de la base de datos
        dbHelper = DbHelper(this)

        // Vinculación de los componentes de la UI
        val etUsuario = findViewById<EditText>(R.id.et_usuario)
        val etContrasena = findViewById<EditText>(R.id.et_contrasena)
        val btnIngresar = findViewById<Button>(R.id.btn_ingresar)

        btnIngresar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Se implementa validación de campos vacíos solicitada en la retroalimentación anterior, utilizando Toast.
            if (usuario.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Se verifica la existencia del usuario y su contraseña en la base de datos local SQLite.
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT RolUsu FROM Usuario WHERE NomUsuario = ? AND Pass = ? AND Activo = 1",
                arrayOf(usuario, contrasena)
            )

            if (cursor.moveToFirst()) {
                // Recuperamos el rol para futuras validaciones de permisos si fueran necesarias.
                val rol = cursor.getInt(0)
                cursor.close()

                // Credenciales correctas. Se implementa la navegación mediante Intent hacia el menú principal.
                Toast.makeText(this, "Ingreso exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MenuPrincipalActivity::class.java)

                // Se pasa el nombre de usuario a la siguiente pantalla para personalizar el saludo.
                intent.putExtra("USUARIO_LOGUEADO", usuario)
                startActivity(intent)

                // Se finaliza la actividad de Login para evitar que el usuario regrese a esta pantalla con el botón "Atrás" del dispositivo.
                finish()
            } else {
                cursor.close()
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}