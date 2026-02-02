package com.alura.foro.hub.api.user.mapper;

import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
import com.alura.foro.hub.api.user.domain.Localidad;
import com.alura.foro.hub.api.user.domain.PerfilEmprendimiento;
import com.alura.foro.hub.api.user.domain.Usuario;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosCrearEmprendimiento;
import com.alura.foro.hub.api.user.dto.emprendimiento.DatosDetalleEmprendimiento;
import org.springframework.stereotype.Component;

@Component
public class EmprendimientoMapper {

    private final StorageService storage;

    public EmprendimientoMapper(StorageService storage) {
        this.storage = storage;
    }

    public PerfilEmprendimiento toEntity(
            DatosCrearEmprendimiento dto,
            Usuario usuario,
            Localidad localidad
    ) {
        PerfilEmprendimiento pe = new PerfilEmprendimiento();
        pe.setUsuario(usuario);
        pe.setNombre(dto.nombre());
        pe.setDescripcion(dto.descripcion());
        pe.setTelefonoContacto(dto.telefonoContacto());
        pe.setInstagram(dto.instagram());
        pe.setFacebook(dto.facebook());
        if (dto.provincia() != null && !dto.provincia().isBlank()) {
            pe.setProvincia(dto.provincia());
        }
        pe.setLocalidad(localidad);
        pe.setDireccion(dto.direccion());
        pe.setCodigoPostal(dto.codigoPostal());
        pe.setActivo(true); // default claro

        return pe;
    }

    public DatosDetalleEmprendimiento toDetalle(PerfilEmprendimiento pe) {
        String logoUrl = null;
        if (pe.getLogoKey() != null && !pe.getLogoKey().isBlank()) {
            logoUrl = storage.getUrl(pe.getLogoKey());
        }

        return new DatosDetalleEmprendimiento(
                pe.getId(),
                pe.getUsuario().getId(),
                pe.getNombre(),
                pe.getDescripcion(),
                pe.getLogoKey(),
                logoUrl,
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
}
