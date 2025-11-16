package dev.luisvives.trabajoprogramacionsegundo.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que representa a los usuarios del sistema.
 * @see Tipo
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="usuarios")
@EntityListeners(AuditingEntityListener.class)
public class Usuario implements UserDetails {
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




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return tipo.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        // email in our case
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted;
    }
}
