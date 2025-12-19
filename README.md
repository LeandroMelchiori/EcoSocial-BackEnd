# 🧠 Foro Hub API

API REST desarrollada con **Spring Boot** para la gestión de un foro de discusión basado en
tópicos y respuestas.

El sistema implementa **autenticación con JWT**, control de acceso por **roles (USER / ADMIN)**,
y validaciones de seguridad para garantizar que solo los autores o administradores puedan
modificar o eliminar los recursos correspondientes.

El proyecto está pensado para ser probado principalmente con **Postman**, aunque también
cuenta con documentación OpenAPI (Swagger) para referencia.

## 🛠️ Tecnologías utilizadas

- **Java 17**
- **Spring Boot 3**
    - Spring Web
    - Spring Security
    - Spring Data JPA
    - Spring Validation
- **JWT (JSON Web Tokens)** para autenticación
- **MySQL 8** como base de datos
- **Flyway** para migraciones de base de datos
- **Hibernate / JPA**
- **Swagger / OpenAPI (springdoc)** para documentación de la API
- **Postman** para pruebas de los endpoints
- **Maven** como gestor de dependencias

## 🚀 Funcionalidades principales

### 👤 Autenticación y usuarios
- Registro de usuarios
- Login con generación de **token JWT**
- Control de acceso basado en roles (**USER / ADMIN**)
- Conversión y revocación de privilegios de administrador

### 🧵 Tópicos
- Crear tópicos (usuario autenticado)
- Listar todos los tópicos (paginado)
- Ver detalle de un tópico con sus respuestas
- Editar tópicos (solo autor o administrador)
- Eliminar tópicos (solo autor o administrador)

### 💬 Respuestas
- Crear respuestas en un tópico
- Listar respuestas de un tópico
- Editar respuestas (solo autor)
- Eliminar respuestas (autor, autor del tópico o administrador)
- Marcar una respuesta como solución (solo autor del tópico)

### 📚 Cursos y categorías
- Listar categorías
- Listar cursos por categoría
- Crear, editar y eliminar cursos (**solo ADMIN**)
- Crear, editar y eliminar categorías (**solo ADMIN**)

### 🔐 Seguridad
- Autenticación mediante **Bearer Token**
- Filtros de seguridad personalizados
- Manejo centralizado de errores
- Validaciones de datos con mensajes claros

## ⚙️ Requisitos y configuración

### 📌 Requisitos previos
Para ejecutar el proyecto es necesario contar con:

- **Java 17**
- **MySQL 8**
- **Maven**
- **Postman** (recomendado para pruebas)
- IDE compatible con Spring Boot (IntelliJ IDEA recomendado)

---

### 🗄️ Base de datos

Crear una base de datos MySQL:

```sql
CREATE DATABASE foro_hub;
```
Las tablas y datos iniciales se crean automáticamente al iniciar la aplicación mediante
Flyway.

### 🔐 Variables de entorno

Configurar las siguientes variables de entorno o propiedades en application.properties:

```properties
spring.datasource.url=jdbc:mysql://localhost/foro_hub
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD


api.security.secret=CLAVE_SECRETA_JWT
```

### ⚠️ Importante:
La clave api.security.secret debe mantenerse privada.
No se recomienda subirla a repositorios públicos.

## ▶️ Ejecución del proyecto

### 1. Clonar el repositorio:

```bash
git clone https://github.com/tu-usuario/foro-hub-api.git
```

### 2. Ingresar al directorio del proyecto:
```bash
cd foro-hub-api
```

### 3. Ejecutar la aplicacion con Maven:

```bash
mvn spring-boot:run
```
### 4. La API quedará disponible en:

```yaml
http://localhost:8080/api/v1/
```

## 📄 Documentación OpenAPI (Swagger)

### La documentación de la API está disponible en:

```yaml
http://localhost:8080/swagger-ui/index.html
```

## 🔐 Autenticación JWT (Postman)

La API utiliza **JSON Web Tokens (JWT)** para autenticar y autorizar las solicitudes.

---

### 1️⃣ Login de usuario

#### Endpoint:
#### POST /auth/login



Body (JSON):

```json
{
  "username": "user",
  "password": "123456"
}
```

#### Respuesta esperada:
```json
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 2️⃣ Uso del token en Postman

Para realizar solicitudes a los endpoints protegidos, se debe incluir el token generado en el header de la solicitud:

```makefile
Authorization: Bearer <TOKEN>
```

### 📌 Importante en Postman:
* No agregar comillas al token
* Verificar que no haya espacios extra
* El prefijo Bearer es obligatorio

### 3️⃣ Endpoints protegidos

Requieren autenticación JWT:

* Crear, editar y eliminar tópicos
* Crear, editar y eliminar respuestas
* Marcar respuestas como solución
* Gestión de cursos y categorías
* Operaciones de administración de usuarios

### ⚠️ Errores comunes

* <b> 401 Unauthorized→ </b>  Token ausente, inválido o expirado

* <b>403 Forbidden → </b> El usuario no tiene permisos suficientes

## 🔑 Roles y permisos

El sistema maneja control de acceso basado en **roles**, definidos a nivel de seguridad
con Spring Security.

### 👥 Roles disponibles

| Rol   | Descripción |
|------|-------------|
| USER | Usuario autenticado estándar |
| ADMIN | Usuario con privilegios administrativos |

---

### 🛂 Permisos por operación

| Módulo | Operación | USER | ADMIN |
|------|----------|:---:|:----:|
| Auth | Login | ✅ | ✅ |
| Usuarios | Registro | ✅ | ✅ |
| Usuarios | Convertir en ADMIN | ❌ | ✅ |
| Usuarios | Quitar ADMIN | ❌ | ✅ |
| Categorías | Listar | ✅ | ✅ |
| Categorías | Crear | ❌ | ✅ |
| Categorías | Editar | ❌ | ✅ |
| Categorías | Eliminar | ❌ | ✅ |
| Cursos | Listar | ✅ | ✅ |
| Cursos | Crear | ❌ | ✅ |
| Cursos | Editar | ❌ | ✅ |
| Cursos | Eliminar | ❌ | ✅ |
| Tópicos | Crear | ✅ | ✅ |
| Tópicos | Editar (autor) | ✅ | ✅ |
| Tópicos | Eliminar (autor) | ✅ | ✅ |
| Respuestas | Crear | ✅ | ✅ |
| Respuestas | Editar (autor) | ✅ | ✅ |
| Respuestas | Eliminar | ✅ | ✅ |
| Respuestas | Marcar como solución | ✅ | ✅ |

---

## 🧪 Flujo recomendado de pruebas (Postman)

1. Registrar un usuario
2. Realizar login para obtener el token JWT
3. Guardar el token como variable de entorno en Postman
4. Enviar el token en el header:

```yaml
Authorization: Bearer {{token}}
```

5. Probar endpoints según el rol del usuario

---

## ⚠️ Manejo de errores

La API devuelve respuestas claras y consistentes ante errores comunes:

| Código | Descripción |
|------|-------------|
| 400 | Datos inválidos |
| 401 | No autenticado |
| 403 | No autorizado |
| 404 | Recurso no encontrado |
| 409 | Conflicto de negocio |
| 500 | Error interno del servidor |

---
## 🗄️ Migraciones de base de datos

El proyecto utiliza **Flyway** para el versionado y control de la base de datos.

- Las migraciones se ejecutan automáticamente al iniciar la aplicación
- Los scripts se encuentran en:

```markdown
src/main/resources/db/migration
```


Esto garantiza:
- Consistencia entre entornos
- Control de versiones del esquema
- Facilidad para desplegar el proyecto desde cero

---

## 🔒 Seguridad

La API implementa medidas de seguridad orientadas a entornos reales:

- Autenticación basada en **JWT**
- Filtro de seguridad personalizado
- Control de acceso por roles
- Validaciones de autorización por autor del recurso
- Manejo centralizado de excepciones
- Protección contra accesos no autenticados

---

## 📌 Notas finales

- El proyecto está diseñado como **API REST**, sin interfaz gráfica
- Swagger se utiliza como documentación visual
- **Postman es la herramienta principal de prueba**
- El código sigue una arquitectura clara por capas:
    - Controller
    - Service
    - Repository
    - DTOs
    - Seguridad

---

## 👨‍💻 Autor

Proyecto desarrollado por **Sacha**  
Como parte de un proceso de aprendizaje y consolidación de conocimientos en
**Spring Boot, APIs REST y seguridad con JWT**.

---

⭐ Si este proyecto te resultó útil, ¡no dudes en dejar una estrella!
