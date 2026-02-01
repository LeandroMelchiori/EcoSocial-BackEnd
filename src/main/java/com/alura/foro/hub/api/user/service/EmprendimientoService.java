package com.alura.foro.hub.api.user.service;

import com.alura.foro.hub.api.modules.catalogo.repository.ProductoRepository;
import com.alura.foro.hub.api.security.auth.CurrentUserService;
import com.alura.foro.hub.api.security.exception.BadRequestException;
import com.alura.foro.hub.api.security.exception.ConflictException;
import com.alura.foro.hub.api.security.exception.ForbiddenException;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosDetalleEmprendimiento;
import com.alura.foro.hub.api.user.repository.LocalidadRepository;
import com.alura.foro.hub.api.user.repository.PerfilEmprendimientoRepository;
import com.alura.foro.hub.api.user.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmprendimientoService {

    private final PerfilEmprendimientoRepository emprendimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;
    private final ProductoRepository productoRepository;
    private final CurrentUserService currentUser;

    public EmprendimientoService(
            PerfilEmprendimientoRepository emprendimientoRepository,
            UsuarioRepository usuarioRepository,
            LocalidadRepository localidadRepository,
            ProductoRepository productoRepository,
            CurrentUserService currentUser
    ) {
        this.emprendimientoRepository = emprendimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.localidadRepository = localidadRepository;
        this.productoRepository = productoRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public DatosDetalleEmprendimiento crearMiEmprendimiento(Long userId, DatosCrearEmprendimiento dto) {

        if (userId == null) throw new ForbiddenException("No autenticado");

        if (emprendimientoRepository.existsByUsuarioId(userId)) {
            // 409 es lo correcto, pero como vos tenés BadRequest/Forbidden,
            // podés crear ConflictException. Por ahora tiro BadRequest con mensaje claro:
            throw new BadRequestException("El usuario ya tiene un emprendimiento creado.");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Localidad localidad = localidadRepository.findById(dto.localidadId())
                .orElseThrow(() -> new EntityNotFoundException("Localidad no encontrada"));

        if (Boolean.FALSE.equals(localidad.getActivo())) {
            throw new BadRequestException("La localidad está inactiva.");
        }

        PerfilEmprendimiento pe = new PerfilEmprendimiento();
        pe.setUsuario(usuario);
        pe.setNombre(dto.nombre());
        pe.setDescripcion(dto.descripcion());
        pe.setTelefonoContacto(dto.telefonoContacto());
        pe.setInstagram(dto.instagram());
        pe.setFacebook(dto.facebook());

        // provincia: si viene null/vacía, @PrePersist ya pone Santa Fe,
        // pero lo seteamos por si el dto trae algo
        if (dto.provincia() != null && !dto.provincia().isBlank()) {
            pe.setProvincia(dto.provincia());
        }

        pe.setLocalidad(localidad);
        pe.setDireccion(dto.direccion());
        pe.setCodigoPostal(dto.codigoPostal());

        // activo true por defecto (tu entidad ya lo trae)
        PerfilEmprendimiento guardado = emprendimientoRepository.save(pe);

        return toDetalle(guardado);
    }

    @Transactional(readOnly = true)
    public DatosDetalleEmprendimiento verMiEmprendimiento(Long userId) {
        if (userId == null) throw new ForbiddenException("No autenticado");

        PerfilEmprendimiento pe = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no tiene emprendimiento"));

        return toDetalle(pe);
    }

    private DatosDetalleEmprendimiento toDetalle(PerfilEmprendimiento pe) {
        return new DatosDetalleEmprendimiento(
                pe.getId(),
                pe.getUsuario().getId(),
                pe.getNombre(),
                pe.getDescripcion(),
                pe.getLogoKey(),
                pe.getTelefonoContacto(),
                pe.getInstagram(),
                pe.getFacebook(),
                pe.getProvincia(),
                pe.getLocalidad().getId(),
                pe.getLocalidad().getNombre(),
                pe.getDireccion(),
                pe.getCodigoPostal(),
                pe.getActivo()
        );
    }

    @Transactional
    public void eliminarMiEmprendimiento(Authentication auth) {

        Long userId = currentUser.userId(auth);

        var emp = emprendimientoRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario no tiene un emprendimiento creado."));

        if (productoRepository.existsByEmprendimientoId(emp.getId())) {
            throw new ConflictException("No se puede eliminar el emprendimiento porque tiene productos asociados.");
        }

        emprendimientoRepository.delete(emp);
    }
}
