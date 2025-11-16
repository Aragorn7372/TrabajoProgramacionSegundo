package dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioPutRequestByUserDto {
    @NotEmpty
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String oldPassword;
    @NotEmpty
    @Pattern(regexp = "^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", message = "El nombre no puede estar vacío si se envía")
    private String email;

}
