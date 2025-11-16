package dev.luisvives.trabajoprogramacionsegundo.usuarios.repository;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UsuariosRepository extends JpaRepository<Usuario,Long> {
    
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    @Modifying // Para indicar que es una consulta de actualización
    @Query("UPDATE Usuario p SET p.isDeleted = true WHERE p.id = :id")
    void updateIsDeletedToTrueById(Long id);
    List<Usuario> findAllByIsDeletedFalse();

    Page<Usuario> findAll(Specification<Usuario> crit, Pageable pageable);

    Optional<Usuario> findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(String username,String email) ;
}
