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
    Optional<Usuario> findByDni(String dni);

    boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    @Query("""
        select u from Usuario u
        join fetch u.perfiles
        where u.email = :email
        """)
    Optional<Usuario> findByEmailConPerfiles(@Param("email") String email);

    @Query("""
        select u from Usuario u
        join fetch u.perfiles
        where u.dni = :dni
        """)
    Optional<Usuario> findByDniConPerfiles(@Param("dni") String dni);

    @Query("select u from Usuario u join fetch u.perfiles where u.id = :id")
    Optional<Usuario> findByIdConPerfiles(@Param("id") Long id);

    @Query("""
        select count(u)
        from Usuario u
        join u.perfiles p
        where p.nombre = :rol
    """)
    long countUsuariosConRol(@Param("rol") String rol);

}
