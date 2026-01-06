# 🧠 Foro Hub API

API REST desarrollada con **Spring Boot** para la gestión de un foro de discusión
basado en tópicos y respuestas, con foco en **seguridad, testing y buenas prácticas backend**.

Implementa **autenticación JWT**, control de acceso por **roles (USER / ADMIN)** y
validaciones de permisos a nivel de negocio.

---

## 🚀 Características principales

- 🔐 Autenticación con **JWT (login real)**
- 👥 Control de acceso por roles y autor del recurso
- 🧵 Gestión de tópicos y respuestas
- 💬 Respuestas hijas (1 solo nivel, diseño controlado)
- 📚 Cursos y categorías administrables
- ⚠️ Manejo centralizado de errores
- 📊 Métricas y monitoreo con Actuator + Micrometer

---

## 🛠️ Tecnologías

- Java 17
- Spring Boot 3 (Web, Security, Data JPA, Validation)
- JWT
- MySQL 8 / H2 (tests)
- Flyway
- Hibernate
- Swagger / OpenAPI
- Maven

---

## 🧪 Testing

Estrategia de testing por capas:

- Tests de **Service** (reglas de negocio y permisos)
- Tests de **Controller** (HTTP, códigos de estado)
- Tests de **Integración**:
    - Login real (`/auth/login`)
    - Generación y uso de JWT
    - Endpoints protegidos con seguridad activa

Perfil `test` con **H2 en memoria**.

```bash
mvn test
```