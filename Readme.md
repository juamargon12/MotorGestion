# 🚗 MotorGestión - App Android & Backend Spring Boot 

**MotorGestión** es un proyecto Full-Stack diseñado para la gestión integral de vehículos (Coches, Motos, Furgonetas) y sus respectivos registros de Mantenimiento. Esta solución está compuesta por un servidor backend en Java (Spring Boot), una base de datos relacional (PostgreSQL) y un cliente móvil nativo (Android).

## 🛠️ Tecnologías Utilizadas

- **Frontend (Móvil):** Android (Java), Librería Volley (Peticiones HTTP REST), Gson.
- **Backend (API REST):** Java, Spring Boot, Spring Data JPA, Apache Tomcat.
- **Base de Datos:** PostgreSQL.
- **Entornos (IDEs):** Android Studio, Eclipse.

---

## 📂 Estructura del Repositorio

- `/Backend_SpringBoot`: Contiene la API REST desarrollada en Spring Boot. Actúa como intermediario directo entre la persistencia de datos (SQL) y la vista en el móvil.
- `/App_Android`: Contiene el código fuente de la aplicación móvil nativa estructurada en un patrón MVC mediante peticiones asíncronas de base de datos.
- `/Base_De_Datos`: Contiene el fichero de respaldo `bbdd.sql` con la estructura de todas las entidades, inserciones y relaciones.

---

## 🚀 Guía de Instalación y Ejecución Local

Para reproducir el proyecto localmente es estrictamente **necesario levantar la infraestructura en este orden específico**:

### 1. Despliegue de la Base de Datos (PostgreSQL)
1. Validar que el demonio de PostgreSQL está corriendo localmente en el puerto `5432`.
2. A través del gestor correspondiente (ej. *pgAdmin 4* o *DBeaver*), crear una base de datos nueva llamada `motorgestion`.
3. Importar y ejecutar el script `bbdd.sql` ubicado en la carpeta del repositorio para poblar los registros.
4. *Punte de seguridad:* El proyecto Spring Boot conectará por defecto usando el binomio de credenciales definidos en `application.properties` (por ejemplo, `dit`/`dit`).

### 2. Arranque de la API REST (Spring Boot)
1. Importar la carpeta `/Backend_SpringBoot` en el entorno de Eclipse como *Existing Maven Project*.
2. Ejecutar la main class principal (por defecto `MotorGestionApplication.java`) como  *Spring Boot App*.
3. Verificar a través de la salida del terminal que el servicio se inicia sin excepciones.
4. Tomcat servirá la subred en el **puerto local 9000** (`localhost:9000`).

### 3. Ejecución del Entorno Gráfico (Android Studio)
1. Cargar la carpeta `/App_Android` directamente sobre Android Studio y sincronizar Gradle para obtener Volley y Gson.
2. Hacer *deploy* y ejecutar un dispositivo virtual emulado.
3. La capa de Volley está conectada al Endpoint host `http://10.0.2.2:9000/api/...`, que establece el bypass al localhost de tu ordenador físico de forma transparente.

---

## ✨ Características de la Aplicación

- **Catálogo Conectado (GET):** Generación de listados de vehículos pidiendo recursos a la API REST.
- **Fichas Técnicas Parametrizadas:** Visualización de bastidores y cargas recuperando el JSON correspondiente del backend y pasándolo por los POJO de modelo.
- **Mantenimiento del Inventario:** Borrado en remoto de vehículos a través de peticiones asíncronas `DELETE` y `POST`.
- **Trazabilidad de Revisiones:** Vista detallada e independiente del Mantenimiento, con estados *Booleanos* calculados.
