package dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInRequest {
    @NotBlank
    @NotNull
    String userNameOrEmail;
    @NotNull
    @NotBlank
    String password;

}
