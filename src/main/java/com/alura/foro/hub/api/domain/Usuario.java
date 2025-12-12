package com.alura.foro.hub.api.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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

    private String nombre;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String username;

    @ManyToMany(fetch = FetchType.EAGER)   // así te evitás dramas de lazy con authorities
    private List<Perfil> perfiles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (perfiles == null || perfiles.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return perfiles.stream()
                .map(p -> new SimpleGrantedAuthority("ROLE_" + p.getNombre().toUpperCase()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}

