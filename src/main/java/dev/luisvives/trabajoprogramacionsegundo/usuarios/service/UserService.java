package dev.luisvives.trabajoprogramacionsegundo.usuarios.service;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.*;

public interface UserService {
    UsuariosDeleteResponse deleteUser(SignInRequest signInRequest);
    UsuariosResponseDto actualizarUsuario(UsuariosPutPostDto usuario);
    UsuariosResponseDto actualizarUsuarioPorUsuario(UsuarioPutRequestByUserDto usuario);
}
