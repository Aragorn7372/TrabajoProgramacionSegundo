package dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.producto.GENERICProductosResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuarioPutRequestByUserDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuariosAdminResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuariosPutPostDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.UsuariosResponseDto;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Tipo;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuariosMapper {
    public UsuariosResponseDto usuariosResponseDtoToUsuariosDto(Usuario usuario) {
        return UsuariosResponseDto.builder()
                .email(usuario.getEmail())
                .nombre(usuario.getUsername())
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
    public Usuario postPutDtoByUserDtoToUsuario(UsuarioPutRequestByUserDto usuario) {
        return Usuario.builder()
                .email(usuario.getEmail())
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .build();
    }
    public UsuariosAdminResponseDto usuariosAdminResponseDto(Usuario usuario, List<String> pedidos) {
        return UsuariosAdminResponseDto.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .isDeleted(usuario.getIsDeleted())
                .tipo(usuario.getTipo().stream().map(it->it.toString().toUpperCase()).toList())
                .pedidos(pedidos)
                .build();
    }
    public PageResponseDTO<UsuariosResponseDto> pageToDTO (Page<UsuariosResponseDto> page, String sortBy, String direction) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(it -> it)
                        .toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                page.getNumberOfElements(),
                page.isEmpty(),
                page.isFirst(),
                page.isLast(),
                sortBy,
                direction
        );
    }
}
