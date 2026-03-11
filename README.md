<div align="center">

# 🌱 EcoSocial — Backend

### Plataforma digital para emprendedores de la economía social

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL_8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![MinIO](https://img.shields.io/badge/MinIO-C72E49?style=for-the-badge&logo=minio&logoColor=white)](https://min.io)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)
[![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

[![Tests](https://img.shields.io/badge/Tests-155_passing-brightgreen?style=flat-square&logo=checkmarx)](/)
[![Security](https://img.shields.io/badge/CodeQL-Passed-blue?style=flat-square&logo=github)](/)
[![Swagger](https://img.shields.io/badge/Docs-Swagger_UI-85EA2D?style=flat-square&logo=swagger)](/)

</div>

---

## 📖 ¿Qué es EcoSocial?

**EcoSocial** es una API REST que potencia una plataforma integral para emprendedores de la economía social. No es un simple catálogo ni un marketplace: es un **espacio digital que combina visibilización y comunidad**.

El sistema resuelve dos necesidades reales:

> 🏪 **Catálogo** — Los emprendedores publican sus productos y emprendimientos para que cualquier persona pueda descubrirlos, sin necesidad de registrarse.

> 🧵 **Foro** — Los usuarios registrados intercambian ideas, consultas y experiencias, fortaleciendo la red emprendedora desde adentro.

Ambas partes comparten la misma infraestructura de usuarios, autenticación y seguridad.

---

## 🗂️ Módulos del sistema

```
EcoSocial
├── 👤 Usuarios & Emprendimientos    → Registro, perfil, 1 usuario = 1 emprendimiento
├── 🏪 Catálogo                      → Productos, categorías, subcategorías, imágenes
└── 🧵 Foro                          → Tópicos, respuestas, respuestas hijas
```

---

## 🛠️ Stack tecnológico

| Área | Tecnología |
|------|-----------|
| 🔧 Lenguaje | Java 17 |
| 🍃 Framework | Spring Boot 3 (Web, Security, Data JPA, Validation) |
| 🗄️ Base de datos | MySQL 8 |
| 🧪 Testing DB | H2 en memoria |
| 🔐 Autenticación | JWT (JSON Web Tokens) |
| 🗃️ Migraciones | Flyway |
| 🖼️ Storage de archivos | MinIO (producción) / Local filesystem (dev) |
| 📦 Contenedores | Docker + Docker Compose |
| 📊 Monitoreo | Spring Actuator + Micrometer + Prometheus |
| 📄 Documentación | Swagger / OpenAPI (springdoc) |
| 🔒 Análisis de seguridad | CodeQL (GitHub Actions) |
| 🏗️ Build | Maven |

---

## 🚀 Funcionalidades

### 👤 Usuarios y autenticación

- ✅ Registro de nuevos usuarios
- ✅ Login con generación de **token JWT**
- ✅ Control de acceso por roles (`USER` / `ADMIN`)
- ✅ Conversión y revocación de rol administrador
- ✅ Rate limiting configurable por perfil de entorno

---

### 🏪 Catálogo de emprendimientos y productos

#### 🏢 Mi emprendimiento *(1 usuario = 1 emprendimiento)*

- ✅ Crear el emprendimiento propio (`POST /me/emprendimiento`)
- ✅ Ver el emprendimiento propio (`GET /me/emprendimiento`)
- ✅ Actualizar datos del emprendimiento
- ✅ Subir / reemplazar logo (`PUT /me/emprendimiento/logo`)
- ✅ Eliminar logo
- ✅ Eliminar emprendimiento
- ✅ Localidad georreferenciada (datos de Santa Fe pre-cargados desde GeoRef)

#### 📦 Productos *(con imágenes múltiples)*

- ✅ Listar productos con paginación y filtros por categoría, subcategoría o texto libre (**público**)
- ✅ Ver detalle de un producto (**público**)
- ✅ Crear producto con imágenes (`multipart/form-data`)
- ✅ Actualizar producto + imágenes
- ✅ Eliminar producto
- ✅ Agregar imágenes adicionales a un producto existente
- ✅ Reemplazar una imagen individual
- ✅ Eliminar una imagen individual
- ✅ Reordenar imágenes

#### 🗂️ Categorías y subcategorías

- ✅ Listar categorías activas (**público**)
- ✅ Listar subcategorías activas por categoría (**público**)
- ✅ CRUD completo de categorías y subcategorías (**solo ADMIN**)
- ✅ Activar / desactivar categorías y subcategorías (**solo ADMIN**)
- ✅ Vista admin con categorías/subcategorías activas e inactivas

---

### 🧵 Foro de emprendedores

#### 📌 Tópicos

- ✅ Crear tópico (usuario autenticado)
- ✅ Listar tópicos paginados
- ✅ Ver detalle de un tópico con sus respuestas
- ✅ Editar tópico (solo autor o administrador)
- ✅ Eliminar tópico (solo autor o administrador)

#### 💬 Respuestas

- ✅ Crear respuesta en un tópico
- ✅ Listar respuestas de un tópico
- ✅ Editar respuesta (solo autor)
- ✅ Eliminar respuesta (autor, autor del tópico o administrador)
- ✅ Marcar respuesta como ✔️ solución (solo autor del tópico)

#### 💬 Respuestas hijas *(1 solo nivel)*

- ✅ Responder a una respuesta
- ✅ Editar y eliminar respuesta hija con permisos propios
- ✅ Test de integración específico

#### 📚 Cursos y categorías del foro

- ✅ Listar categorías y cursos
- ✅ CRUD completo de cursos y categorías (**solo ADMIN**)

---

## 🧩 Arquitectura

El proyecto sigue una **arquitectura modular por capas**:

```
src/main/java/
└── com.alura.foro.hub.api
    ├── modules/
    │   ├── catalogo/       → Productos, categorías, subcategorías, imágenes
    │   └── foro/           → Tópicos, respuestas, cursos
    ├── user/               → Usuarios, emprendimientos, localidades
    ├── security/           → JWT, filtros, excepciones, rate limit
    └── helpers/            → Métricas y utilidades
```

Cada módulo sigue la estructura:
`controller` → `service` → `repository` → `domain` + `dto` + `mapper`

---

## 🔑 Roles y permisos

### 👥 Roles disponibles

| Rol | Descripción |
|-----|-------------|
| `USER` | Usuario autenticado estándar |
| `ADMIN` | Usuario con privilegios administrativos |

### 🛂 Tabla de permisos

#### Catálogo

| Recurso | Operación | 🌐 Público | 👤 USER | 🔑 ADMIN |
|---------|-----------|-----------|---------|---------|
| Emprendimiento | Ver / Listar | ✅ | ✅ | ✅ |
| Emprendimiento | Crear / Editar / Eliminar | ❌ | ✅ (propio) | ✅ |
| Emprendimiento | Subir / Eliminar logo | ❌ | ✅ (propio) | ✅ |
| Productos | Ver / Listar | ✅ | ✅ | ✅ |
| Productos | Crear / Editar / Eliminar | ❌ | ✅ (propio) | ✅ |
| Productos | Gestión de imágenes | ❌ | ✅ (propio) | ✅ |
| Categorías | Listar activas | ✅ | ✅ | ✅ |
| Categorías | CRUD + activar/desactivar | ❌ | ❌ | ✅ |
| Subcategorías | Listar activas | ✅ | ✅ | ✅ |
| Subcategorías | CRUD + activar/desactivar | ❌ | ❌ | ✅ |

#### Foro

| Recurso | Operación | 👤 USER | 🔑 ADMIN |
|---------|-----------|---------|---------|
| Tópicos | Crear | ✅ | ✅ |
| Tópicos | Editar / Eliminar | ✅ (autor) | ✅ |
| Respuestas | Crear / Editar | ✅ | ✅ |
| Respuestas | Eliminar | ✅ (autor/topico) | ✅ |
| Respuestas | Marcar como solución | ✅ (autor del tópico) | ✅ |
| Cursos y categorías | Listar | ✅ | ✅ |
| Cursos y categorías | CRUD | ❌ | ✅ |

#### Usuarios

| Operación | 👤 USER | 🔑 ADMIN |
|-----------|---------|---------|
| Registro | ✅ | ✅ |
| Convertir en ADMIN | ❌ | ✅ |
| Quitar ADMIN | ❌ | ✅ |

---

## ⚙️ Requisitos previos

- **Java 17**
- **MySQL 8** (o Docker)
- **Maven**
- **Docker + Docker Compose** *(recomendado para levantar todo con un comando)*
- IDE compatible con Spring Boot (IntelliJ IDEA recomendado)
- **Postman** *(para pruebas manuales)*

---

## ▶️ Ejecución

### 🐳 Opción A — Docker Compose *(recomendado)*

Levanta automáticamente **MySQL + MinIO + la API**:

```bash
git clone https://github.com/LeandroMelchiori/EcoSocial-BackEnd.git
cd EcoSocial-BackEnd

# Crear .env con tus variables (ver sección de configuración)
docker-compose up --build
```

Servicios disponibles:
- 🌐 API → `http://localhost:8080`
- 🗄️ MySQL → `localhost:3307`
- 🖼️ MinIO → `http://localhost:9001` (consola web)

---

### ☕ Opción B — Maven local

```bash
# 1. Clonar
git clone https://github.com/LeandroMelchiori/EcoSocial-BackEnd.git
cd EcoSocial-BackEnd

# 2. Configurar application.properties (ver sección de configuración)

# 3. Ejecutar
mvn spring-boot:run
```

La API quedará disponible en: `http://localhost:8080/api/v1/`

---

## 🔐 Configuración

### Variables de entorno / application.properties

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost/ecosocial
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD

# JWT
api.security.secret=CLAVE_SECRETA_JWT

# Storage de imágenes: "minio" | "local"
catalogo.storage=minio

# Si storage = minio
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin123
minio.bucket=ecosocial
```

> ⚠️ **Importante:** No subas `api.security.secret` ni credenciales de MinIO a repositorios públicos. Usá variables de entorno o un archivo `.env` excluido del `.gitignore`.

---

## 🔐 Autenticación JWT

### 1️⃣ Registrar usuario

```http
POST /auth/registro
Content-Type: application/json

{
  "username": "maria",
  "email": "maria@ejemplo.com",
  "password": "miPassword123"
}
```

### 2️⃣ Obtener token

```http
POST /auth/login
Content-Type: application/json

{
  "username": "maria",
  "password": "miPassword123"
}
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3️⃣ Usar el token

En cada request protegido, incluir en el header:

```
Authorization: Bearer <TOKEN>
```

> 📌 El prefijo `Bearer` es obligatorio. No agregar comillas al token.

---

## 📄 Documentación Swagger

La documentación interactiva de todos los endpoints está disponible en:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📊 Monitoreo

El proyecto integra **Spring Boot Actuator + Micrometer + Prometheus** con métricas de:

- ⚡ Performance HTTP (tiempos de respuesta)
- 🧠 Uso de JVM (heap, GC)
- 🔗 Pool de conexiones HikariCP
- 💓 Estado general de la aplicación

Las métricas están listas para ser consumidas por **Prometheus** y visualizadas en **Grafana**.

---

## ⚙️ Perfiles de ejecución

### 🧪 `test` — Tests automáticos

```properties
spring.datasource.url=jdbc:h2:mem:ecosocial_test;MODE=MySQL;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
app.ratelimit.enabled=false
```

### 🧑‍💻 `dev` — Desarrollo local

```properties
# MySQL local + rate limit desactivado para no interferir con Postman Runner
app.ratelimit.enabled=false
```

### 🚀 `prod` — Producción

```properties
app.ratelimit.enabled=true
app.ratelimit.loginMax=50
app.ratelimit.writeMax=200
app.ratelimit.readMax=800
app.ratelimit.windowSeconds=600
```

---

## 🗄️ Migraciones de base de datos

El proyecto usa **Flyway** para el versionado del esquema. Las migraciones se ejecutan automáticamente al iniciar la aplicación.

```
src/main/resources/db/migration/
├── V1__estructura-usuarios-emprendimiento.sql
├── V1_1__seed-perfiles.sql
├── V1_2__create-user-admin.sql
├── V1_3__seed-localidades-santa-fe.sql   ← localidades georreferenciadas de Santa Fe
├── V2__foro-estructura.sql
├── V2_1__seed-foro-basico.sql
├── V3__catalogo-estructura.sql
└── V3_1__seed_categorias_y_subcategorias.sql
```

> 📍 Las localidades de la provincia de Santa Fe están pre-cargadas con datos georreferenciados (GeoRef Argentina), listas para usar sin configuración adicional.

---

## 🧪 Estrategia de Testing

El proyecto cuenta con **155 tests** que cubren todas las capas.

### 🔹 Tests de Service
- Reglas de negocio
- Permisos por autor / admin
- Restricciones de estado (tópico abierto/cerrado)

### 🔹 Tests de Controller
- Contratos HTTP
- Códigos de respuesta (`200 / 201 / 401 / 403 / 404 / 409`)
- MockMvc

### 🔹 Tests de Integración *(End-to-End)*
- Login real → JWT → endpoint protegido
- Persistencia en H2 con Spring Security activo
- `AuthenticationIntegrationTest`
- `RespuestaHijaIntegrationTest`
- `MinioStorageServiceIntegrationTest`

### Ejecutar los tests

```bash
# Todos los tests
mvn test

# Con reporte
mvn test surefire-report:report
```

### 📬 Colección de Postman

Incluye flujos completos para todos los módulos (registro, login, emprendimiento, productos, imágenes, tópicos, respuestas, categorías):

```
postman/
├── collections/     → Colección principal
├── environments/    → Variables de entorno
└── fixtures/        → Imágenes de prueba para upload
```

---

## ⚠️ Manejo de errores

Todos los endpoints devuelven errores en el formato `ApiError` estándar:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "El usuario ya tiene un emprendimiento creado.",
  "path": "/me/emprendimiento"
}
```

| Código | Descripción |
|--------|-------------|
| `400` | Datos inválidos o request mal formado |
| `401` | No autenticado (token ausente o expirado) |
| `403` | Sin permisos (rol insuficiente o no es autor) |
| `404` | Recurso no encontrado |
| `409` | Conflicto (recurso duplicado o estado inválido) |
| `429` | Rate limit excedido |
| `500` | Error interno del servidor |

---

## 🧠 Decisiones de diseño

- **1 usuario = 1 emprendimiento** para simplificar el modelo y el flujo de alta.
- **Endpoints de catálogo son públicos** para maximizar la visibilización sin requerir registro.
- **Storage pluggeable** (MinIO/local) mediante interfaz `StorageService` + `@ConditionalOnProperty`.
- **Respuestas hijas limitadas a 1 nivel** para evitar estructuras recursivas complejas.
- **Autorización validada en la capa Service**, no solo en el Controller.
- **Formato de error unificado** (`ApiError`) en todos los endpoints para consistencia de la API.
- **CodeQL en CI** para análisis de seguridad automático en cada push.

---

## 🎯 Alcance

- ✅ API REST completa (backend)
- ❌ Sin frontend incluido
- ❌ Sin notificaciones en tiempo real
- ❌ Sin moderación automática de contenido

---

## 👨‍💻 Autor

Proyecto desarrollado por **Leandro Melchiori**

Como parte de un proceso de aprendizaje y consolidación en **Spring Boot, APIs REST, seguridad con JWT, almacenamiento de archivos con MinIO y arquitectura de sistemas**.

---

<div align="center">

⭐ Si este proyecto te resultó útil, ¡dejá una estrella!

</div>
