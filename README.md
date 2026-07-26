<div align="center">

# 🌱 EcoSocial — Backend

### API para visibilizar emprendimientos y fortalecer comunidades de economía social

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![MinIO](https://img.shields.io/badge/MinIO-Storage-C72E49?style=for-the-badge&logo=minio&logoColor=white)](https://min.io)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com)

</div>

**EcoSocial** es el backend de una plataforma orientada a emprendedores de la economía social. Reúne dos experiencias dentro de una misma API:

- un **catálogo público** de emprendimientos y productos;
- un **foro comunitario** para intercambiar consultas, experiencias y soluciones.

Los módulos comparten usuarios, autenticación, permisos, documentación, almacenamiento de archivos y monitoreo.

> Este repositorio contiene únicamente el backend. La interfaz web se desarrolla y despliega como proyecto separado.

---

## 🎯 Objetivo

La plataforma busca resolver necesidades frecuentes de emprendimientos autogestivos:

- contar con un espacio público para mostrar productos y datos de contacto;
- organizar la información de cada emprendimiento;
- administrar imágenes sin depender de enlaces externos;
- facilitar la búsqueda por categorías y ubicación;
- crear una comunidad donde las personas puedan ayudarse mediante tópicos y respuestas.

No procesa ventas ni pagos: el catálogo está orientado a la **visibilización y contacto directo**.

---

## 🧩 Módulos

```text
EcoSocial
├── Usuarios y seguridad
│   ├── registro y login
│   ├── JWT y roles
│   └── perfil del emprendimiento
├── Catálogo
│   ├── productos e imágenes
│   ├── categorías y subcategorías
│   └── filtros y búsquedas públicas
└── Foro
    ├── tópicos
    ├── respuestas
    ├── respuestas hijas
    └── cursos y categorías
```

---

## ✨ Funcionalidades

### Usuarios y autenticación

- Registro de usuarios.
- Login con token JWT.
- Roles `USER` y `ADMIN`.
- Operaciones administrativas para asignar o revocar el rol de administrador.
- Rate limiting configurable según el entorno.
- Contraseñas protegidas mediante Spring Security.

### Perfil de emprendimiento

- Relación de un emprendimiento por usuario.
- Alta, consulta, edición y eliminación del perfil propio.
- Nombre, descripción, teléfono, redes y ubicación.
- Localidades de Santa Fe precargadas a partir de datos geográficos.
- Carga, reemplazo y eliminación del logo.
- Lectura pública de emprendimientos.

### Productos

- Creación y edición de productos del emprendimiento autenticado.
- Precio, descripción, categoría y subcategoría.
- Hasta cinco imágenes por producto.
- Carga mediante `multipart/form-data`.
- Agregado, reemplazo, eliminación y reordenamiento de imágenes.
- Listado público paginado.
- Filtros por categoría, subcategoría y texto libre.
- Consulta pública del detalle de cada producto.
- Control de propiedad para impedir que un usuario modifique productos ajenos.

### Categorías y subcategorías

- Listado público de categorías activas.
- Consulta de subcategorías por categoría.
- CRUD administrativo.
- Activación y desactivación lógica.
- Vista administrativa de registros activos e inactivos.

### Foro

- Creación, consulta, edición y eliminación de tópicos.
- Filtros y paginación.
- Respuestas a tópicos.
- Respuestas hijas con un nivel de profundidad.
- Marcado de una respuesta como solución.
- Permisos según autoría, propietario del tópico y rol administrativo.
- Administración de cursos y categorías del foro.

---

## 🔐 Permisos

| Recurso | Público | USER | ADMIN |
|---|---:|---:|---:|
| Ver emprendimientos y productos | ✅ | ✅ | ✅ |
| Crear o editar emprendimiento propio | ❌ | ✅ | ✅ |
| Crear o editar productos propios | ❌ | ✅ | ✅ |
| Gestionar imágenes propias | ❌ | ✅ | ✅ |
| Administrar categorías del catálogo | ❌ | ❌ | ✅ |
| Crear tópicos y respuestas | ❌ | ✅ | ✅ |
| Editar contenido propio del foro | ❌ | ✅ | ✅ |
| Moderar contenido del foro | ❌ | Según autoría | ✅ |
| Administrar cursos y categorías del foro | ❌ | ❌ | ✅ |
| Gestionar roles de usuario | ❌ | ❌ | ✅ |

Los permisos sensibles se validan en la capa de servicio y no dependen únicamente de la interfaz cliente.

---

## 🏗️ Arquitectura

El proyecto utiliza una arquitectura modular por capas:

```text
controller
    ↓
service ──► reglas de negocio y autorización
    ↓
repository
    ↓
domain / database

DTOs y mappers separan el contrato HTTP de las entidades persistidas.
```

### Organización principal

```text
src/main/java/com/alura/foro/hub/api/
├── modules/
│   ├── catalogo/
│   └── foro/
├── user/
├── security/
└── helpers/
```

Cada módulo agrupa sus controladores, servicios, repositorios, entidades, DTOs y mappers.

---

## 🛠️ Stack

| Área | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Web | Spring MVC |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MySQL 8 |
| Migraciones | Flyway |
| Seguridad | Spring Security + JWT Auth0 |
| Validación | Jakarta Bean Validation |
| Archivos | MinIO o filesystem local |
| Documentación | springdoc OpenAPI / Swagger UI |
| Observabilidad | Actuator, Micrometer y Prometheus |
| Tests | JUnit 5, Mockito, MockMvc, H2 y Testcontainers |
| Cobertura | JaCoCo |
| Seguridad de código | CodeQL |
| Infraestructura | Docker y Docker Compose |
| Build | Maven |

El README evita publicar un número fijo de tests porque la suite continúa evolucionando. El estado real se valida ejecutando Maven y consultando los workflows de GitHub Actions.

---

## 🖼️ Almacenamiento de imágenes

El backend abstrae el almacenamiento para utilizar diferentes implementaciones:

- **MinIO:** opción recomendada para un entorno desplegado o para Docker Compose.
- **Filesystem local:** opción simple para desarrollo.

Las imágenes pasan por validaciones de formato, cantidad y tamaño. La aplicación mantiene las operaciones de producto y almacenamiento coordinadas para evitar referencias inconsistentes.

---

## 🚀 Ejecución con Docker

### Requisitos

- Docker.
- Docker Compose.

```bash
git clone https://github.com/LeandroMelchiori/EcoSocial-BackEnd.git
cd EcoSocial-BackEnd
```

Crear un archivo `.env` con las variables requeridas y ejecutar:

```bash
docker compose up --build
```

Servicios principales:

| Servicio | Dirección local |
|---|---|
| API | `http://localhost:8080` |
| MySQL | `localhost:3307` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |

---

## ☕ Ejecución con Maven

Requiere Java 17 y una instancia de MySQL.

```bash
mvn spring-boot:run
```

Ejemplo de configuración:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecosocial
spring.datasource.username=usuario
spring.datasource.password=contraseña

api.security.secret=una-clave-segura

catalogo.storage=local
# o catalogo.storage=minio

minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=una-clave-segura
minio.bucket=ecosocial
```

> Las credenciales y secretos deben suministrarse mediante variables de entorno o archivos excluidos de Git. No deben escribirse directamente en archivos versionados.

---

## 🔑 Flujo de autenticación

### Registro

```http
POST /auth/registro
Content-Type: application/json

{
  "username": "maria",
  "email": "maria@ejemplo.com",
  "password": "unaClaveSegura"
}
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "maria",
  "password": "unaClaveSegura"
}
```

Las rutas protegidas reciben el token mediante:

```http
Authorization: Bearer <token>
```

---

## 📄 Documentación y monitoreo

Con la aplicación en ejecución:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Métricas Prometheus: según la exposición configurada para Actuator.

La colección Postman incluida permite probar flujos de autenticación, catálogo, foro, permisos e imágenes.

---

## ✅ Pruebas

```bash
mvn test
```

Para generar además el reporte de cobertura:

```bash
mvn verify
```

La suite incluye:

- tests unitarios de servicios;
- tests de controladores con MockMvc;
- pruebas de seguridad y permisos;
- pruebas de reglas de negocio;
- pruebas de integración del almacenamiento;
- escenarios con H2 y Testcontainers.

GitHub Actions ejecuta validaciones automatizadas y análisis CodeQL sobre el repositorio.

---

## 📂 Recursos del repositorio

- `docker-compose.yml`: API, MySQL y MinIO.
- `postman/`: colección y archivos utilizados en pruebas manuales.
- `src/main/resources/db/migration/`: migraciones Flyway.
- `.github/workflows/`: CI y análisis de seguridad.
- `pom.xml`: dependencias, JaCoCo y configuración de build.

---

## 🛣️ Próximas mejoras

- Completar la integración con el frontend público y el panel del emprendimiento.
- Publicar una demo estable con datos ficticios.
- Incorporar auditoría de operaciones administrativas.
- Mejorar búsqueda y ordenamiento del catálogo.
- Agregar moderación y notificaciones al foro.
- Preparar almacenamiento externo administrado para producción.

---

## Autor

Desarrollado por **Leandro Melchiori**.

- [GitHub](https://github.com/LeandroMelchiori)
- [LinkedIn](https://www.linkedin.com/in/leandromelchiori-developer/)

## Licencia

Distribuido bajo licencia MIT. Consultá [`LICENSE`](LICENSE) para más información.
