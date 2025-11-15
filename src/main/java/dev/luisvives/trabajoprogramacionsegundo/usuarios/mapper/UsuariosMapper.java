package dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.UsuariosPutPostDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.UsuariosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;

public class UsuariosMapper {
    public UsuariosResponseDto usuariosResponseDtoToUsuariosDto(Usuario usuario) {
        return UsuariosResponseDto.builder()
                .email(usuario.getEmail())
                .nombre(usuario.getUsername())
                .password(usuario.getPassword())
                .rol(usuario.getTipo().toString())
                .build();
    }
    public Usuario postPutDtoToUsuario(UsuariosPutPostDto usuario) {
        return Usuario.builder()
                .email(usuario.getEmail())
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .tipo(usuario.getRoles().stream().map(it-> Tipo.valueOf(it.toUpperCase())).toList())
                .build();
    }
}
