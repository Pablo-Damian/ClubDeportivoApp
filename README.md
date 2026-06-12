# ⚽ 🏀 🏐 🎾 🏆 ClubDeportivoApp - Gestión Móvil

<p align="center">
  <img src="app/src/main/res/drawable/escudo_logo.png" alt="Logo Club Deportivo" width="40%">
</p>

## 📌 Contexto de la Migración y Paradigma Arquitectónico
Este proyecto representa la migración y optimización del sistema de gestión original desarrollado en C# para entorno Desktop (Escritorio) hacia una arquitectura móvil nativa en Android utilizando Kotlin y SQLite.

Esta transición introduce diferencias estructurales clave debido al cambio de paradigma:
*   **Centralización vs. Descentralización (SQLite):** Mientras que el sistema de escritorio en C# operaba conectado de forma directa a un motor de base de datos centralizado en un servidor (como MySQL), el entorno móvil utiliza **SQLite** como motor embebido local. Esto permite que el sistema funcione de manera independiente en el dispositivo, simulando la persistencia de datos localmente.
*   **Paradigmas de Movilidad y Perfiles de Usuario:** El sistema original de escritorio estaba diseñado exclusivamente para el empleado administrativo en la recepción. La introducción de la movilidad en Android expande la visión del negocio, proyectando dos perfiles de usuario diferenciados:
    1.  **Perfil Empleado / Administrador:** Acceso a la gestión integral, cobros, registros y alertas de morosidad desde su dispositivo móvil *(Alcance implementado en este repositorio)*.
    2.  **Perfil Socio:** Diseñado para que los afiliados accedan de forma remota únicamente a la visualización de su credencial digital (Carnet de Socio).

---

## 🛠️ Optimizaciones de Código de Calidad Implementadas
Se incorporaron patrones de diseño y buenas prácticas del desarrollo nativo en Android para asegurar el rendimiento del sistema:

*   **Seguridad en el Control de la Pila de Navegación (Back Stack):** Al accionar el cierre de sesión, se configuraron los flags `Intent.FLAG_ACTIVITY_CLEAR_TASK` y `FLAG_ACTIVITY_NEW_TASK` en la transición hacia `MainActivity`. Esto destruye el historial de actividades, previniendo accesos no autorizados mediante la navegación inversa física del celular.
*   **Carga Dinámica de Datos de SQLite:** Las actividades deportivas y sus aranceles no se encuentran definidos de forma estática en las vistas XML. El sistema consulta las tablas en tiempo real al inicializar la pantalla para inyectar los componentes visuales dinámicamente.
*   **Lógica Transaccional y Control de Cupos:** Al confirmarse el cobro de una actividad para un No Socio, el sistema actualiza automáticamente la disponibilidad del deporte ejecutando una sentencia `UPDATE` de decremento en SQLite, impidiendo sobreventas si el cupo llega a cero.
*   **Manejo de Conflictos de Claves Primarias:** En la tabla `Cliente_Actividad`, la combinación de `dni_cliente` e `id_actividad` actúa como clave primaria. Para evitar caídas de la aplicación ante intentos de reinscripción, se implementó la cláusula `insertWithOnConflict` junto con `CONFLICT_REPLACE` (UPSERT) para actualizar el estado del pago de manera segura.
*   **QA y Aseguramiento de Calidad:** La base de datos relacional local se auditó mediante **Pruebas Automatizadas de Integración** (`DatabaseTest.kt`) desarrolladas con el framework JUnit4/Espresso, validando por consola el esquema DDL y la integridad de los registros iniciales insertados.

---

## 🛣️ Trabajo Futuro y Siguientes Iteraciones (Roadmap)
Para enmarcar el prototipo dentro de un Producto Mínimo Viable (MVP) funcional, se definieron los alcances de la siguiente etapa de desarrollo:

*   **Migración a RecyclerView y Operaciones UPDATE/DELETE:** En la presente etapa de diseño, los listados de datos (como el reporte de morosidad) se muestran inyectando vistas dinámicas sobre contenedores `ScrollView`. En la próxima iteración, en conjunto con la lógica de edición y baja de clientes (incorporando el material de la Clase 13), estas listas serán refactorizadas mediante la estructura **`RecyclerView`**, utilizando `Adapters` y `ViewHolders` para optimizar el consumo de memoria del dispositivo.
*   **Integración con Servicios Externos:** Las acciones de "Descargar PDF", "Imprimir Carnet" y la pasarela de pago para "Tarjeta/Transferencia" se encuentran simuladas a nivel de interfaz mediante alertas `Toast`. Su desarrollo requerirá la integración de APIs de pago (Stripe/MercadoPago SDK) y librerías de generación de documentos en el futuro.
