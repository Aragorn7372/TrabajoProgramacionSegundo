package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuariosPedidosService {
    Page<UsuariosResponseDto> findAll(Optional<Boolean> isDeleted, Pageable pageable);
    UsuariosAdminResponseDto findById(Long id);
    UsuariosResponseDto update(Long id, UsuarioPutRequestByUserDto usuarioPutRequestByUserDto);
    UsuariosDeleteResponse delete(Long id);
    UsuariosResponseDto updateAdmin(Long id, UsuariosPutPostDto usuariosPutPostDto);

}
