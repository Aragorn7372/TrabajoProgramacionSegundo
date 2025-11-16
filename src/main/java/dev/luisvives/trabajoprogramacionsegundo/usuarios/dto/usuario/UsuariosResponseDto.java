package dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuariosResponseDto {
    private String nombre;
    private String email;
    private String rol;
}
