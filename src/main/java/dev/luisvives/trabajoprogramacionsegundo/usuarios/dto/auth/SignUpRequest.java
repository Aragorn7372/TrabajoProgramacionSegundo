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
/*
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER) // Pocos datos, tipo eaguer para ir mas rapido
    @Enumerated(EnumType.STRING) // Guardar el nombre del enum en lugar de el "indice" del valor Ej.: Tipo[0] = ADMIN / Tipo[1] = USER
    private List<Tipo> tipo;
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted=false;
    @Column(nullable = false)
    @LastModifiedDate
    @Builder.Default
    private LocalDateTime fechaModificacion=LocalDateTime.now();
    @Column(nullable = false)
    @CreatedDate
    @Builder.Default
    private LocalDateTime fechaCreacion=LocalDateTime.now();
 */