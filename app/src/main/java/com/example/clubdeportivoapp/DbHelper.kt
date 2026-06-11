package com.example.clubdeportivoapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Clase principal para la gestión y persistencia de datos locales en SQLite.
// Hereda de SQLiteOpenHelper para controlar la creación y actualización del esquema.
class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "clubdeportivo.db"
        // Se incrementa la versión para forzar el reinicio (Drop & Create) de la estructura y datos semilla
        private const val DATABASE_VERSION = 2
    }

    // Se fuerza la activación de claves foráneas en SQLite (por defecto vienen desactivadas)
    // Esto garantiza el funcionamiento de ON DELETE CASCADE.
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Creación del esquema relacional de tablas

        db.execSQL("""
            CREATE TABLE Configuracion (
                clave TEXT PRIMARY KEY,
                valor TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE Roles (
                RolUsu INTEGER PRIMARY KEY,
                NomRol TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE Usuario (
                CodUsu INTEGER PRIMARY KEY AUTOINCREMENT,
                NomUsuario TEXT NOT NULL UNIQUE,
                Pass TEXT NOT NULL,
                RolUsu INTEGER,
                Activo INTEGER DEFAULT 1,
                FOREIGN KEY(RolUsu) REFERENCES Roles(RolUsu)
            )
        """)

        // Los tipos ENUM de MySQL se reemplazan por restricciones CHECK en SQLite
        db.execSQL("""
            CREATE TABLE Cliente (
                dni TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                apodo TEXT,
                apellido TEXT NOT NULL,
                fecha_nacimiento TEXT NOT NULL,
                telefono TEXT,
                email TEXT UNIQUE,
                tipo_cliente TEXT NOT NULL CHECK(tipo_cliente IN ('SOCIO', 'NO SOCIO'))
            )
        """)

        db.execSQL("""
            CREATE TABLE Socio (
                dni TEXT PRIMARY KEY,
                numero_socio INTEGER UNIQUE,
                fecha_ingreso TEXT NOT NULL,
                numero_carnet INTEGER UNIQUE NOT NULL,
                FOREIGN KEY(dni) REFERENCES Cliente(dni) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE NoSocio (
                dni TEXT PRIMARY KEY,
                ultima_visita TEXT,
                FOREIGN KEY(dni) REFERENCES Cliente(dni) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE Actividad (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                precio REAL NOT NULL,
                cupo INTEGER NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE Profesor (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dni TEXT UNIQUE NOT NULL,
                nombre TEXT NOT NULL,
                apellido TEXT NOT NULL,
                telefono TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE Profesor_Actividad (
                id_profesor INTEGER,
                id_actividad INTEGER,
                PRIMARY KEY (id_profesor, id_actividad),
                FOREIGN KEY (id_profesor) REFERENCES Profesor(id),
                FOREIGN KEY (id_actividad) REFERENCES Actividad(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE Cliente_Actividad (
                dni_cliente TEXT,
                id_actividad INTEGER,
                fecha_inscripcion TEXT NOT NULL,
                estado_pago TEXT DEFAULT 'PENDIENTE' CHECK(estado_pago IN ('PAGO', 'PENDIENTE')),
                PRIMARY KEY (dni_cliente, id_actividad),
                FOREIGN KEY (dni_cliente) REFERENCES Cliente(dni) ON DELETE CASCADE,
                FOREIGN KEY (id_actividad) REFERENCES Actividad(id)
            )
        """)

        db.execSQL("""
            CREATE TABLE CuotaSocial (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dni_socio TEXT NOT NULL,
                fecha_pago TEXT NOT NULL,
                fecha_vencimiento TEXT NOT NULL,
                monto REAL NOT NULL,
                forma_de_pago TEXT NOT NULL,
                FOREIGN KEY (dni_socio) REFERENCES Socio(dni) ON DELETE CASCADE
            )
        """)

        // Inserción de datos semilla tras la creación de las tablas
        insertarDatosSemilla(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización de versión, se destruye el esquema viejo y se recrea.
        db.execSQL("DROP TABLE IF EXISTS CuotaSocial")
        db.execSQL("DROP TABLE IF EXISTS Cliente_Actividad")
        db.execSQL("DROP TABLE IF EXISTS Profesor_Actividad")
        db.execSQL("DROP TABLE IF EXISTS Profesor")
        db.execSQL("DROP TABLE IF EXISTS Actividad")
        db.execSQL("DROP TABLE IF EXISTS NoSocio")
        db.execSQL("DROP TABLE IF EXISTS Socio")
        db.execSQL("DROP TABLE IF EXISTS Cliente")
        db.execSQL("DROP TABLE IF EXISTS Usuario")
        db.execSQL("DROP TABLE IF EXISTS Roles")
        db.execSQL("DROP TABLE IF EXISTS Configuracion")
        onCreate(db)
    }

    private fun insertarDatosSemilla(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO Configuracion (clave, valor) VALUES ('monto_cuota_social', '3500.00')")

        db.execSQL("INSERT INTO Roles (RolUsu, NomRol) VALUES (1, 'Administrador'), (2, 'Empleado')")

        db.execSQL("INSERT INTO Usuario (NomUsuario, Pass, RolUsu) VALUES ('messi', '123456', 1), ('pareto', '123456', 2), ('ginobili', '123456', 2)")

        db.execSQL("INSERT INTO Actividad (nombre, precio, cupo) VALUES ('Fútbol', 1500.00, 20), ('Judo', 2500.00, 15), ('Básquet', 2000.00, 25), ('Hockey sobre césped', 2200.00, 18), ('Automovilismo (Karting)', 5000.00, 10), ('BMX Freestyle', 3000.00, 12), ('Ajedrez', 800.00, 30), ('Vóley', 1800.00, 24), ('Handball', 1700.00, 22), ('Natación', 3500.00, 16), ('Tenis', 4000.00, 8)")

        db.execSQL("INSERT INTO Profesor (dni, nombre, apellido, telefono) VALUES ('18000001', 'Carlos', 'Bianchi', '111111'), ('18000002', 'Julio', 'Lamas', '222222')")
        db.execSQL("INSERT INTO Profesor_Actividad (id_profesor, id_actividad) VALUES (1, 1), (2, 3)")

        // Inserción de clientes de prueba con fechas y DNI consistentes.
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('14000006', 'Diego', 'Maradona', 'Pelusa', '1960-10-30', '000000', 'diego@maradona.com', 'SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('21000005', 'Gabriela', 'Sabatini', 'Gaby', '1970-05-16', '777777', 'gaby@sabatini.com', 'SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('25000003', 'Emanuel', 'Ginóbili', 'Manu', '1977-07-28', '333333', 'manu@ginobili.com', 'SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('26000004', 'Luciana', 'Aymar', 'Lucha', '1977-08-10', '444444', 'lucha@aymar.com', 'SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('32000002', 'Paula', 'Pareto', 'La Peque', '1986-01-16', '222222', 'paula@pareto.com', 'SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('33000001', 'Lionel', 'Messi', 'La Pulga', '1987-06-24', '111111', 'leo@messi.com', 'SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('38000009', 'José', 'Torres', 'Maligno', '1995-03-28', '666666', 'jose@torres.com', 'NO SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('42000008', 'Delfina', 'Pignatiello', 'Delfi', '2000-04-19', '999999', 'delfi@pignatiello.com', 'NO SOCIO')")
        db.execSQL("INSERT INTO Cliente (dni, nombre, apellido, apodo, fecha_nacimiento, telefono, email, tipo_cliente) VALUES ('45000007', 'Franco', 'Colapinto', NULL, '2003-05-27', '555555', 'franco@colapinto.com', 'NO SOCIO')")

        // Inserción en tablas de especialización
        db.execSQL("INSERT INTO Socio (dni, numero_socio, fecha_ingreso, numero_carnet) VALUES ('14000006', 1, '2023-01-05', 1001)")
        db.execSQL("INSERT INTO Socio (dni, numero_socio, fecha_ingreso, numero_carnet) VALUES ('21000005', 2, '2023-01-10', 1002)")
        db.execSQL("INSERT INTO Socio (dni, numero_socio, fecha_ingreso, numero_carnet) VALUES ('25000003', 3, '2023-03-20', 1003)")
        db.execSQL("INSERT INTO Socio (dni, numero_socio, fecha_ingreso, numero_carnet) VALUES ('26000004', 4, '2023-04-25', 1004)")
        db.execSQL("INSERT INTO Socio (dni, numero_socio, fecha_ingreso, numero_carnet) VALUES ('32000002', 5, '2023-05-15', 1005)")
        db.execSQL("INSERT INTO Socio (dni, numero_socio, fecha_ingreso, numero_carnet) VALUES ('33000001', 6, '2023-06-24', 1006)")

        db.execSQL("INSERT INTO NoSocio (dni, ultima_visita) VALUES ('38000009', '2024-06-15')")
        db.execSQL("INSERT INTO NoSocio (dni, ultima_visita) VALUES ('42000008', '2024-06-20')")
        db.execSQL("INSERT INTO NoSocio (dni, ultima_visita) VALUES ('45000007', '2024-05-10')")

        // Inscripciones iniciales en actividades
        db.execSQL("INSERT INTO Cliente_Actividad (dni_cliente, id_actividad, fecha_inscripcion, estado_pago) VALUES ('33000001', 1, '2024-01-01', 'PAGO')")
        db.execSQL("INSERT INTO Cliente_Actividad (dni_cliente, id_actividad, fecha_inscripcion, estado_pago) VALUES ('32000002', 2, '2024-01-01', 'PAGO')")
        db.execSQL("INSERT INTO Cliente_Actividad (dni_cliente, id_actividad, fecha_inscripcion, estado_pago) VALUES ('25000003', 3, '2024-01-01', 'PAGO')")
        db.execSQL("INSERT INTO Cliente_Actividad (dni_cliente, id_actividad, fecha_inscripcion, estado_pago) VALUES ('26000004', 4, '2024-01-01', 'PAGO')")
        db.execSQL("INSERT INTO Cliente_Actividad (dni_cliente, id_actividad, fecha_inscripcion, estado_pago) VALUES ('45000007', 5, '2024-01-01', 'PENDIENTE')")
        db.execSQL("INSERT INTO Cliente_Actividad (dni_cliente, id_actividad, fecha_inscripcion, estado_pago) VALUES ('38000009', 6, '2024-01-01', 'PENDIENTE')")

        // Historial de cobro
        db.execSQL("INSERT INTO CuotaSocial (dni_socio, fecha_pago, fecha_vencimiento, monto, forma_de_pago) VALUES ('32000002', '2024-04-15', '2024-05-15', 3500.00, 'Efectivo')")

        // Inserción de cuotas con fechas de vencimiento pasadas para validar el reporte de morosidad.
        db.execSQL("INSERT INTO CuotaSocial (dni_socio, fecha_pago, fecha_vencimiento, monto, forma_de_pago) VALUES ('14000006', '2025-12-10', '2026-01-10', 3500.00, 'Tarjeta')")
        db.execSQL("INSERT INTO CuotaSocial (dni_socio, fecha_pago, fecha_vencimiento, monto, forma_de_pago) VALUES ('25000003', '2026-02-20', '2026-03-20', 3500.00, 'Efectivo')")
    }
}