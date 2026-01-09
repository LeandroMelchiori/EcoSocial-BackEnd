package com.alura.foro.hub.api.user.repository;

import com.alura.foro.hub.api.user.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    // Método para buscar usuario por nombre de usuario (username)
    Optional<Usuario> findByUsername(String username);

    // Método para verificar existencia de email
    boolean existsByEmail(String email);

    // Verificar existencia de username
    boolean existsByUsername(String username);

    // Traer usuario con sus perfiles/roles
    @Query("""
        select u from Usuario u
        join fetch u.perfiles
        where u.username = :username
        """)
    Usuario findByUsernameConPerfiles(String username);

    @Query("""
    select count(u)
    from Usuario u
    join u.perfiles p
    where p.nombre = :rol
""")
long countUsuariosConRol(@Param("rol") String rol);
}
