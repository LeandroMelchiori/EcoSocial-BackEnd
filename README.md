# ForoHub

ForoHub es una aplicación de foro desarrollada en Java con Spring Boot, diseñada para gestionar la creación y discusión de tópicos en distintos cursos.


## 🚀 Instalación y Configuración

### Prerrequisitos

Asegúrate de tener instalados:

- [Java 17+](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- [Maven 3+](https://maven.apache.org/download.cgi)
- [MYSQL](https://dev.mysql.com/downloads/)
  
### Pasos para la instalación

1. Clona el repositorio:

    ```bash
    git clone https://github.com/leandromelchiori/foro-hub.git
    cd foro-hub
    ```

2. Configura la base de datos en `src/main/resources/application.properties`:

    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/foro_hub
    spring.datasource.username=tu_usuario
    spring.datasource.password=tu_contraseña
    ```

4. Accede al SpringDoc de la API en: [http://localhost:8080/](http://localhost:8080/swagger-ui/index.html]([http://localhost:8080/](http://localhost:8080/swagger-ui/index.html))

## 🛠️ Endpoints de la API

Algunos endpoints básicos incluyen:

- `GET /topicos` - Obtener todos los tópicos.
- `POST /topicos` - Crear un nuevo tópico.
- `PUT /topicos/{id}` - Actualizar un tópico.
- `DELETE /topicos/{id}` - Eliminar un tópico.
