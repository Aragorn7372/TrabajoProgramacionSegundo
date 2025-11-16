package dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuariosPutPostDto {
    @NotEmpty
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacío si se envía")
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    @Pattern(regexp = "^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$", message = "El nombre no puede estar vacío si se envía")
    private String email;
    @NotEmpty
    private List<@Pattern(regexp = "^ADMIN|USUARIO$") String> roles;
}
