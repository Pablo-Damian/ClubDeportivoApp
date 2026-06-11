package com.example.clubdeportivoapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Clase encargada de validar la correcta inicialización de SQLite y la inserción de datos semilla.
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var dbHelper: DbHelper

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dbHelper = DbHelper(context)
    }

    @After
    fun closeDb() {
        dbHelper.close()
    }

    @Test
    fun testDatabaseCreationAndSeedData() {
        val db = dbHelper.readableDatabase
        assertNotNull("La base de datos es nula", db)

        // Validación de que los usuarios por defecto fueron insertados.
        val cursorUsuarios = db.rawQuery("SELECT COUNT(*) FROM Usuario", null)
        assertNotNull("El cursor de usuarios es nulo", cursorUsuarios)
        cursorUsuarios.moveToFirst()
        val cantidadUsuarios = cursorUsuarios.getInt(0)
        cursorUsuarios.close()

        // Se verifica la existencia de los 3 usuarios semilla.
        assertEquals("La cantidad de usuarios semilla no es correcta", 3, cantidadUsuarios)

        // Validación de la correcta inserción del socio Lionel Messi y coincidencia de DNI.
        val cursorCliente = db.rawQuery("SELECT nombre, apellido, dni FROM Cliente WHERE apodo = 'La Pulga'", null)
        assertNotNull("El cursor de cliente es nulo", cursorCliente)
        if (cursorCliente.moveToFirst()) {
            val nombre = cursorCliente.getString(0)
            val apellido = cursorCliente.getString(1)
            val dni = cursorCliente.getString(2)

            assertEquals("El nombre no coincide", "Lionel", nombre)
            assertEquals("El apellido no coincide", "Messi", apellido)
            assertEquals("El DNI de Lionel Messi no coincide con el establecido", "33000001", dni)
        } else {
            throw AssertionError("No se encontró el registro de Lionel Messi en la tabla Cliente")
        }
        cursorCliente.close()
    }
}