package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.auth;

import dev.luisvives.trabajoprogramacionsegundo.usuarios.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class UserServiceImpl implements UserService {
    private final UsuariosRepository usuariosRepository;
    @Autowired
    public UserServiceImpl(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
        if (value.contains("@")) {
            return usuariosRepository.findByEmail(value)
                    .orElseThrow(() -> new UsernameNotFoundException("Email no encontrado: " + value));
        }

        // Si no es email → es username
        return usuariosRepository.findByUsername(value)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + value));
    }
}
