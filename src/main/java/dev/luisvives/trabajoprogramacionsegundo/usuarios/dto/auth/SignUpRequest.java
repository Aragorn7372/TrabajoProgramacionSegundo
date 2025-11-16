package dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequest {
    @NotBlank
    @NotNull
    String username;
    @Email
    String email;
    @NotNull
    @NotBlank
    String password;
    @NotNull
    @NotBlank
    String passwordConfirm;
}
