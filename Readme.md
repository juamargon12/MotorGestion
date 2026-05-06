# 🚗 MotorGestión — App Android + Backend RESTful

**MotorGestión** es una aplicación Full-Stack para la **gestión integral de una flota de vehículos**: coches, motos y furgonetas. Permite registrar y consultar vehículos, asignarles tareas de mantenimiento, gestionar fotos con la cámara del dispositivo y funcionar en modo offline cuando el servidor no está disponible.

El proyecto está diseñado con una arquitectura cliente-servidor limpia: una **app Android nativa** (Java) consume una **API REST** propia construida con **Spring Boot**, respaldada por una base de datos **PostgreSQL**.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| App móvil | Android (Java), Volley, Gson, SharedPreferences |
| API REST | Java 17, Spring Boot, JDBC puro |
| Base de datos | PostgreSQL |
| Herramientas | Android Studio, Maven |

---

## ✨ Funcionalidades

### 🔐 Autenticación y Roles (RBAC)
- Pantalla de **Login** con llamada `POST /api/login` en hilo secundario (sin bloquear la UI).
- El servidor devuelve el rol del usuario (`JEFE` o `EMPLEADO`), que se propaga por todos los `Intent` de la app.
- **JEFE:** acceso completo — puede crear, editar, borrar vehículos y gestionar fotos.
- **EMPLEADO:** modo consulta — solo puede ver listados y detalles, sin opción de modificar.
- Botón de **acceso offline** en la pantalla de login: entra como empleado en modo solo lectura sin necesidad de servidor.

### 🚙 Gestión de Vehículos (CRUD completo)
Tres módulos independientes y simétricos para **Coches**, **Motos** y **Furgonetas**:
- **Listado** con búsqueda en tiempo real mediante `TextWatcher`.
- **Detalle/Edición**: formulario con todos los campos del vehículo (modelo, matrícula, número de bastidor, año, zona).
- **Alta**: formulario de creación con validación de campos.
- **Baja**: botón de eliminación con confirmación, solo disponible para el JEFE.
- **Foto**: captura con la cámara nativa, conversión a Base64 y persistencia en la base de datos como `BYTEA`. Decodificación y visualización en el `ImageView` al cargar el detalle.

### 🔧 Gestión de Mantenimientos
- **Listado** de tareas de mantenimiento con buscador en tiempo real.
- Cada tarea muestra su **descripción**, **estado** (✓ Hecho / ✗ Pendiente) y el **vehículo asignado** (modelo y matrícula).
- **Alta** (solo JEFE): dos spinners encadenados permiten seleccionar primero el tipo de vehículo (Coche / Moto / Furgoneta) y después el vehículo concreto de la flota.
- **Toggle de estado**: pulsar una tarea abre un diálogo con la opción de marcarla como completada o pendiente (`PUT /api/mantenimientos/{num}`).
- **Baja** (solo JEFE): eliminación permanente de la tarea desde el mismo diálogo.

### 📶 Modo Offline (Caché con SharedPreferences)
- Al iniciar sesión con éxito, `SyncManager.syncAll()` descarga en segundo plano todos los listados (coches, motos, furgonetas, mantenimientos) y los persiste en `SharedPreferences`.
- Si el servidor no responde, la app detecta el error de red y entra automáticamente en **Modo Offline**, mostrando un banner de aviso rojo.
- En modo offline los datos se cargan desde la caché local y la interfaz pasa a ser de **solo lectura** (se ocultan botones de edición y el panel de alta).
- Los detalles de vehículos en modo offline también cargan desde la caché, buscando por ID dentro del JSON guardado.

---

## 📂 Estructura del Repositorio

```
MotorGestion/
├── App_Android/        # App Android (Android Studio)
│   └── app/src/main/
│       ├── java/com/example/motorgestion/
│       │   ├── LoginActivity.java          # Pantalla de login
│       │   ├── MainActivity.java           # Menú principal + SyncManager
│       │   ├── SyncManager.java            # Caché offline (SharedPreferences)
│       │   ├── MantenimientoActivity.java  # Gestión de tareas con vehículo asignado
│       │   ├── Listado*Activity.java       # Listados de vehículos con buscador
│       │   ├── Detalle*Activity.java       # Detalle/edición + cámara + offline
│       │   ├── Anadir*Activity.java        # Formularios de alta
│       │   └── model/                      # POJOs: Coche, Moto, Furgoneta, Mantenimiento
│       └── res/layout/                     # Layouts XML de cada pantalla
│
├── Backend/            # API REST (Spring Boot + Maven)
│   └── src/main/java/com/example/restservice/
│       ├── RestServiceApplication.java     # Punto de entrada Spring Boot
│       ├── MotorController.java            # Todos los endpoints REST
│       ├── Coche.java / Moto.java / Furgoneta.java / Mantenimiento.java
│
└── Base_de_Datos/
    └── bbdd.sql        # Script completo: tablas, restricciones y datos de ejemplo
```

---

## 🗄️ Modelo de Base de Datos

```
Coches        (num, modelo, n_bastidor, matricula, anio_fabricacion, zona, foto)
Motos         (num, modelo, n_bastidor, matricula, anio_fabricacion, zona, foto)
Furgonetas    (num, modelo, matricula, combustible, carga_maxima, zona, foto)
Mantenimientos(num, texto, realizada, tipo_vehiculo, vehiculo_num)
Usuarios      (id, usuario, password, rol)
```

- `Mantenimientos.tipo_vehiculo`: `'COCHE'`, `'MOTO'` o `'FURGONETA'` (nullable).
- `Mantenimientos.vehiculo_num`: referencia lógica al `num` de la tabla de vehículos correspondiente.

---

## 🌐 Endpoints de la API REST

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/login` | Valida credenciales y devuelve el rol |
| `GET` | `/api/coches` | Lista todos los coches |
| `GET` | `/api/coches/{num}` | Detalle de un coche |
| `POST` | `/api/coches` | Crea un coche |
| `PUT` | `/api/coches/{num}` | Actualiza un coche |
| `PUT` | `/api/coches/{num}/foto` | Actualiza solo la foto (Base64) |
| `DELETE` | `/api/coches/{num}` | Elimina un coche |
| *(ídem para `/motos` y `/furgonetas`)* | | |
| `GET` | `/api/mantenimientos` | Lista todas las tareas |
| `POST` | `/api/mantenimientos` | Crea una tarea con vehículo asignado |
| `PUT` | `/api/mantenimientos/{num}` | Alterna el estado realizada/pendiente |
| `DELETE` | `/api/mantenimientos/{num}` | Elimina una tarea |

---

## 🚀 Guía de Ejecución Local

### 1. Base de Datos (PostgreSQL)
```bash
psql -U <usuario> -f Base_de_Datos/bbdd.sql
```
El script recrea la base de datos `motorgestion` desde cero con datos de ejemplo.

**Usuarios preconfigurados:**

| Rol | Usuario | Contraseña |
|---|---|---|
| JEFE | `admin` | `admin123` |
| EMPLEADO | `empleado1` | `1234` |
| EMPLEADO | `empleado2` | `1234` |

### 2. API REST (Spring Boot)
```bash
cd Backend
mvn spring-boot:run
```
El servidor arranca en `http://localhost:9000`.

### 3. App Android
1. Abrir `/App_Android` en Android Studio.
2. Ejecutar en un emulador AVD o dispositivo físico.
3. La app se conecta a `10.0.2.2:9000` (alias del `localhost` del host dentro del emulador Android).

---

## 📋 Cumplimiento Académico (DAM)

| Tema | Requisito | Implementación |
|---|---|---|
| T02 | Autenticación y Roles | `LoginActivity` + `POST /api/login` + propagación de rol por Intent |
| T03 | Interfaz avanzada y eventos | `TextWatcher` (búsqueda), `AlertDialog`, spinners encadenados, control de visibilidad por rol |
| T04 | Persistencia de datos | `SyncManager` con `SharedPreferences`, caché automática al login, modo offline transparente |
| T05 | Servicios Web RESTful + Cámara | API REST completa con CRUD, `ActivityResultLauncher` para cámara, Base64 ↔ BYTEA |
