package com.alura.foro.hub.api.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length = 100)
    private String nombre;

    @Column(nullable=false, length = 100)
    private String apellido;

    @Column(nullable=false, unique = true, length = 20)
    private String dni;

    @Column(nullable=false, unique = true, length = 150)
    private String email;

    @Column(nullable=false, length = 255)
    private String password;

    @Column(nullable=false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable=false, updatable=false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }

    @PreUpdate
    void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now(); // primera edición
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_perfiles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private List<Perfil> perfiles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (perfiles == null || perfiles.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return perfiles.stream()
                .map(p -> new SimpleGrantedAuthority("ROLE_" + p.getNombre().toUpperCase()))
                .toList();
    }

    // Identificador “principal” para Spring:
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(activo);
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    public boolean esAdmin() {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
