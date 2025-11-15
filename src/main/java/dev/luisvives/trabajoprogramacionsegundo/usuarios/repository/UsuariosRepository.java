package dev.luisvives.trabajoprogramacionsegundo.usuarios.repository;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsuariosRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    @Modifying // Para indicar que es una consulta de actualización
    @Query("UPDATE Usuario p SET p.isDeleted = true WHERE p.id = :id")
    void updateIsDeletedToTrueById(Long id);
}
