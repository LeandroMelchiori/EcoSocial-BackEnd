# Guía de Seguridad - Foro Hub API

## 🔒 Mejores Prácticas Implementadas

### 1. Autenticación y Autorización
- ✅ JWT con expiración de 2 horas
- ✅ Control de acceso basado en roles (USER/ADMIN)
- ✅ Validación de permisos a nivel de servicio
- ✅ Filtros de seguridad personalizados

### 2. Validación de Datos
- ✅ Validación de entrada con Bean Validation
- ✅ Sanitización de nombres de archivo
- ✅ Protección contra path traversal
- ✅ Validación de extensiones de archivo permitidas
- ✅ Límite de tamaño de archivos (5MB)

### 3. Protección de Datos Sensibles
- ✅ Contraseñas encriptadas con BCrypt
- ✅ Secrets en variables de entorno (no hardcodeados)
- ✅ No se expone información sensible en logs de error

### 4. Protección contra Ataques Comunes

#### SQL Injection
- ✅ Uso de JPA/Hibernate con consultas parametrizadas
- ✅ Repository pattern con métodos tipados

#### Path Traversal
- ✅ Validación de rutas en LocalStorageService
- ✅ Normalización de paths
- ✅ Verificación de que paths resueltos están dentro del root

#### XSS (Cross-Site Scripting)
- ✅ API REST pura (sin generación de HTML)
- ✅ Content-Type: application/json

#### CSRF (Cross-Site Request Forgery)
- ✅ Stateless JWT (no cookies de sesión)
- ✅ CORS configurado adecuadamente

### 5. Rate Limiting
- ✅ Protección contra fuerza bruta en login
- ✅ Límites por ventana de tiempo configurables
- ✅ Diferentes límites para lectura/escritura

## ⚠️ Recomendaciones Adicionales

### Para Producción

1. **Variables de Entorno**
   - Nunca incluir secrets en el código
   - Usar gestores de secrets (AWS Secrets Manager, Vault, etc.)
   - Copiar `.env.example` a `.env` y configurar valores reales

2. **Base de Datos**
   - Usar usuarios con permisos mínimos necesarios
   - Habilitar SSL/TLS para conexiones
   - Implementar backups automáticos
   - Rotar credenciales regularmente

3. **JWT**
   - Usar claves de al menos 256 bits
   - Considerar refresh tokens para sesiones largas
   - Implementar blacklist de tokens revocados
   - Reducir tiempo de expiración en ambientes críticos

4. **HTTPS**
   - Forzar HTTPS en producción
   - Usar certificados válidos (Let's Encrypt)
   - Configurar HSTS headers

5. **Actuator**
   - Restringir endpoints de actuator
   - Usar autenticación para endpoints sensibles
   - Exponer solo métricas necesarias

6. **Logging**
   - No loggear información sensible (passwords, tokens)
   - Implementar logging centralizado
   - Monitorear intentos de acceso fallidos
   - Configurar alertas para patrones anómalos

7. **Dependencias**
   - Mantener dependencias actualizadas
   - Usar herramientas de análisis de vulnerabilidades
   - Revisar advisories de seguridad regularmente

8. **Docker/Contenedores**
   - No ejecutar como root
   - Usar imágenes base oficiales y mínimas
   - Escanear imágenes por vulnerabilidades
   - No incluir secrets en imágenes

## 🔍 Auditoría de Seguridad

### Verificaciones Periódicas

1. **Código**
   - [ ] Ejecutar análisis estático (SonarQube, Qodana)
   - [ ] Revisar dependencias con `mvn dependency:analyze`
   - [ ] Verificar vulnerabilidades con OWASP Dependency-Check

2. **Configuración**
   - [ ] Revisar configuraciones de Spring Security
   - [ ] Verificar CORS y headers de seguridad
   - [ ] Auditar permisos de endpoints

3. **Base de Datos**
   - [ ] Revisar migraciones de Flyway
   - [ ] Verificar índices en campos sensibles
   - [ ] Auditar consultas complejas

4. **Logs y Monitoreo**
   - [ ] Revisar logs de acceso fallido
   - [ ] Monitorear métricas de seguridad
   - [ ] Verificar alertas activas

## 📚 Recursos Adicionales

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [CWE Top 25](https://cwe.mitre.org/top25/)

## 🆘 Reporte de Vulnerabilidades

Si encuentras una vulnerabilidad de seguridad, por favor:
1. NO la hagas pública inmediatamente
2. Contacta al equipo de desarrollo de forma privada
3. Proporciona detalles del problema y pasos para reproducirlo
4. Permite tiempo razonable para resolución antes de disclosure público
