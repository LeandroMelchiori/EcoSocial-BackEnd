package com.alura.foro.hub.api.user.service;

import com.alura.foro.hub.api.modules.catalogo.repository.ProductoRepository;
import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ConflictException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosActualizarEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosDetalleEmprendimiento;
import com.alura.foro.hub.api.user.mapper.EmprendimientoMapper;
import com.alura.foro.hub.api.user.repository.LocalidadRepository;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EmprendimientoService {

    private final PerfilEmprendimientoRepository emprendimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;
    private final ProductoRepository productoRepository;
    private final CurrentUserService currentUser;
    private final StorageService storageService;
    private final EmprendimientoMapper mapper;

    public EmprendimientoService(
            PerfilEmprendimientoRepository emprendimientoRepository,
            UsuarioRepository usuarioRepository,
            LocalidadRepository localidadRepository,
            ProductoRepository productoRepository,
            CurrentUserService currentUser,
            StorageService storageService,
            EmprendimientoMapper mapper
    ) {
        this.emprendimientoRepository = emprendimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.localidadRepository = localidadRepository;
        this.productoRepository = productoRepository;
        this.currentUser = currentUser;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Transactional
    public DatosDetalleEmprendimiento crearMiEmprendimiento(Authentication auth, DatosCrearEmprendimiento dto) {
        Long userId = requireUserId(auth);

        if (emprendimientoRepository.existsByUsuarioId(userId)) {
            throw new ConflictException("El usuario ya tiene un emprendimiento creado.");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Localidad localidad = localidadRepository.findById(dto.localidadId())
                .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada"));

        validarLocalidadActiva(localidad);

        PerfilEmprendimiento pe = mapper.toEntity(dto, usuario, localidad);

        PerfilEmprendimiento guardado = emprendimientoRepository.save(pe);

        return mapper.toDetalle(guardado);
    }

    @Transactional(readOnly = true)
    public DatosDetalleEmprendimiento verMiEmprendimiento(Authentication auth) {
        Long userId = requireUserId(auth);

        PerfilEmprendimiento pe = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no tiene emprendimiento"));

        return mapper.toDetalle(pe);
    }

    @Transactional
    public void eliminarMiEmprendimiento(Authentication auth) {
        Long userId = requireUserId(auth);

        PerfilEmprendimiento emp = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no tiene un emprendimiento creado."));

        if (productoRepository.existsByEmprendimientoId(emp.getId())) {
            throw new ConflictException("No se puede eliminar el emprendimiento porque tiene productos asociados.");
        }

        // si querés, también borrás el logo de storage para no dejar basura
        if (emp.getLogoKey() != null && !emp.getLogoKey().isBlank()) {
            storageService.deleteObject(emp.getLogoKey());
        }

        emprendimientoRepository.delete(emp);
    }

    @Transactional
    public DatosDetalleEmprendimiento subirLogo(Authentication auth, MultipartFile logo) {
        Long userId = requireUserId(auth);

        validarArchivoLogo(logo);

        PerfilEmprendimiento emp = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no tiene un emprendimiento"));

        // borrar logo anterior
        if (emp.getLogoKey() != null && !emp.getLogoKey().isBlank()) {
            storageService.deleteObject(emp.getLogoKey());
        }

        String newKey = storageService.saveEmprendimientoLogo(emp.getId(), logo);
        emp.setLogoKey(newKey);

        // persist explícito por claridad
        emprendimientoRepository.save(emp);

        return mapper.toDetalle(emp);
    }

    @Transactional
    public void eliminarLogo(Authentication auth) {
        Long userId = requireUserId(auth);

        PerfilEmprendimiento emp = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no tiene un emprendimiento"));

        if (emp.getLogoKey() != null && !emp.getLogoKey().isBlank()) {
            storageService.deleteObject(emp.getLogoKey());
            emp.setLogoKey(null);
            emprendimientoRepository.save(emp);
        }
    }

    @Transactional
    public DatosDetalleEmprendimiento actualizar(Authentication auth, DatosActualizarEmprendimiento datos) {
        Long userId = requireUserId(auth);

        PerfilEmprendimiento emp = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Emprendimiento no encontrado"));

        Localidad loc = localidadRepository.findById(datos.localidadId())
                .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada"));

        validarLocalidadActiva(loc);

        // Campos actualizables
        emp.setNombre(datos.nombre());
        emp.setDescripcion(datos.descripcion());
        emp.setTelefonoContacto(datos.telefonoContacto());
        emp.setInstagram(datos.instagram());
        emp.setFacebook(datos.facebook());
        emp.setLocalidad(loc);
        emp.setDireccion(datos.direccion());
        emp.setCodigoPostal(datos.codigoPostal());

        PerfilEmprendimiento actualizado = emprendimientoRepository.save(emp);
        return mapper.toDetalle(actualizado);
    }

    // ------------------ helpers ------------------

    private Long requireUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ForbiddenException("No autenticado");
        }
        Long userId = currentUser.userId(auth);
        if (userId == null) throw new ForbiddenException("No autenticado");
        return userId;
    }

    private void validarLocalidadActiva(Localidad localidad) {
        if (Boolean.FALSE.equals(localidad.getActivo())) {
            throw new BadRequestException("La localidad está inactiva.");
        }
    }

    private void validarArchivoLogo(MultipartFile logo) {
        if (logo == null || logo.isEmpty()) {
            throw new BadRequestException("Debés subir un archivo de imagen.");
        }

        String ct = logo.getContentType();
        if (ct == null || (!ct.equals("image/jpeg") && !ct.equals("image/png") && !ct.equals("image/webp"))) {
            throw new BadRequestException("Formato inválido. Permitidos: JPG, PNG, WEBP.");
        }
    }
}
