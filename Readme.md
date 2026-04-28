# 🚗 MotorGestión - App Android & Backend Spring Boot 

**MotorGestión** es un proyecto Full-Stack diseñado para la gestión integral de vehículos (Coches, Motos, Furgonetas) y sus respectivos registros de Mantenimiento. Esta solución incluye un robusto sistema de **Autenticación**, **Roles de Usuario** y un **Modo Offline** para garantizar la disponibilidad de los datos sin conexión.

## 🛠️ Tecnologías Utilizadas

- **Frontend (Móvil):** Android (Java), Volley (REST), Gson, SharedPreferences (Caché).
- **Backend (API REST):** Java 17, Spring Boot, JDBC/JPA.
- **Base de Datos:** PostgreSQL.
- **Entornos:** Android Studio, VS Code / IntelliJ / Eclipse.

---

## ✨ Características Principales

- **🔐 Sistema de Autenticación:** Pantalla de Login conectada al backend para validar credenciales.
- **👥 Roles de Usuario (RBAC):**
    - **JEFE:** Control total. Puede añadir, editar, borrar vehículos y gestionar fotos.
    - **EMPLEADO:** Modo consulta. Puede ver los listados y detalles pero no modificar datos.
- **📶 Modo Offline (Caché):** Gracias al `SyncManager`, la app descarga los datos al iniciar sesión. Si el servidor se cae, la app entra automáticamente en "Modo Lectura" usando la caché local.
- **📸 Gestión de Fotos:** Captura de imágenes con la cámara nativa, conversión a Base64 y persistencia en base de datos (BYTEA).
- **🔍 Filtrado en Tiempo Real:** Búsqueda dinámica en los listados de vehículos mediante `TextWatcher`.

---

## 📂 Estructura del Repositorio

- `/Backend`: API REST en Spring Boot. Gestiona la lógica de negocio, autenticación y persistencia.
- `/App_Android`: Código fuente de la aplicación móvil nativa.
- `/Base_de_Datos`: Script `bbdd.sql` con la estructura de tablas, incluyendo la tabla de usuarios y datos iniciales.

---

## 🚀 Guía de Ejecución Local

### 1. Base de Datos (PostgreSQL)
1. Crear una base de datos llamada `motorgestion`.
2. Ejecutar el script `/Base_de_Datos/bbdd.sql`.
3. **Usuarios configurados:**
    - **JEFE:** Usuario: `admin` | Password: `admin123`
    - **EMPLEADO:** Usuario: `empleado1` | Password: `1234`

### 2. API REST (Spring Boot)
1. Navegar a la carpeta `/Backend`.
2. Ejecutar el comando: `mvn spring-boot:run`.
3. El servicio estará disponible en `http://localhost:9000`.

### 3. Aplicación Android
1. Cargar `/App_Android` en Android Studio.
2. Ejecutar en un emulador o dispositivo físico.
3. La app conecta automáticamente al backend a través de `10.0.2.2:9000` (IP especial del emulador para localhost).

---

## 📸 Notas sobre el cumplimiento académico
Este proyecto cumple con los requisitos de la asignatura DAM:
- **Tema 02/03:** Interfaz de usuario avanzada y gestión de eventos.
- **Tema 04:** Persistencia de datos mediante caché en SharedPreferences para modo offline.
- **Tema 05:** Integración con servicios web RESTful y uso de la cámara para envío de imágenes en Base64.
