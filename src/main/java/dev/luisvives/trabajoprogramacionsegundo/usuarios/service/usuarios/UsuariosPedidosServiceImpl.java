package dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios;

import dev.luisvives.trabajoprogramacionsegundo.pedidos.repository.PedidosRepository;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.*;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.auth.UserEmailOrUsernameExists;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios.IncorrectOldPassword;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.exceptions.usuarios.UserNotFound;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper.UsuariosMapper;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.repository.UsuariosRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Service
@Slf4j
@CacheConfig(cacheNames = "usuarios")
public class UsuariosPedidosServiceImpl implements UsuariosPedidosService{
    private final UsuariosRepository usuariosRepository;
    private final PedidosRepository pedidosRepository;
    private final UsuariosMapper usuariosMapper;
    private final PasswordEncoder encoder;
    @Autowired
    public UsuariosPedidosServiceImpl(UsuariosRepository usuariosRepository,PedidosRepository pedidosRepository,UsuariosMapper usuariosMapper,PasswordEncoder encoder) {
        this.usuariosRepository = usuariosRepository;
        this.pedidosRepository = pedidosRepository;
        this.usuariosMapper = usuariosMapper;
        this.encoder = encoder;
    }
    @Override
    public Page<UsuariosResponseDto> findAll(Optional<Boolean> isDeleted, Pageable pageable) {
        log.info("Finding all user pendientes del usuario");
        Specification<Usuario> specIsDeleted= (root, query, builder) ->
                isDeleted.map(m -> builder.equal(root.get("isDeleted"),m))
                        .orElseGet(() -> builder.isTrue(builder.literal(true)));
        Specification<Usuario> crit = Specification.allOf(specIsDeleted);
        return usuariosRepository.findAll(crit,pageable).map(usuariosMapper::usuariosResponseDtoToUsuariosDto);
    }

    @Override
    @Cacheable(key = "#id")
    public UsuariosAdminResponseDto findById(Long id) {
        log.info("Finding user pendientes del usuario");
        val user = usuariosRepository.findById(id).orElseThrow(()->{
            log.info("User not found: "+id);
            return new UserNotFound("User not found:"+id);
            }

        );
        val pedidos= pedidosRepository.findPedidosByIdsByIdUsuario(id).stream().map(p-> p.getId().toHexString()).toList();
        return usuariosMapper.usuariosAdminResponseDto(user,pedidos);
    }

    @Override
    @CacheEvict( key = "#result.id")
    public UsuariosResponseDto update(Long id, UsuarioPutRequestByUserDto usuarioPutRequestByUserDto) {
        log.info("Updating user pendientes del usuario");
        val contraseñaAntigua=usuariosRepository.findById(id).orElseThrow(()->{
            log.info("User not found: "+id);
            return new UserNotFound("User not found:"+id);
        });
        if (encoder.matches(usuarioPutRequestByUserDto.getOldPassword(),contraseñaAntigua.getPassword())){
            log.info("Old password not correct");
            throw new IncorrectOldPassword("Old password not correct");
        }
        usuariosRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(usuarioPutRequestByUserDto.getUsername(),usuarioPutRequestByUserDto.getEmail()).ifPresent(
                usuario -> {
                    if (usuario.getId().equals(id)){
                        log.info("User already exists");
                        throw new UserEmailOrUsernameExists("ya existe un usuario con ese nombre o email");
                    }
                });
        val usuario= UsuarioPutRequestByUserDto.builder()
                .username(usuarioPutRequestByUserDto.getUsername())
                .email(usuarioPutRequestByUserDto.getEmail())
                .password(encoder.encode(usuarioPutRequestByUserDto.getPassword()))
                .build();
        return usuariosMapper.usuariosResponseDtoToUsuariosDto(usuariosRepository.save(usuariosMapper.postPutDtoByUserDtoToUsuario(usuario)));
    }

    @Override
    @Transactional
    public UsuariosDeleteResponse delete(Long id) {
        log.info("Deleting user:" +id);
        val usuario= usuariosRepository.findById(id).orElseThrow(()->{
            log.info("User not found: "+id);
            return new UserNotFound("User not found:"+id);
        });
        if (!pedidosRepository.findPedidosByIdsByIdUsuario(id).isEmpty()){
            log.info("User con pedidos");
            usuariosRepository.updateIsDeletedToTrueById(id);
            return UsuariosDeleteResponse.builder()
                    .message("usuario eliminado con borrado logico exitoso")
                    .usuario(usuariosMapper.usuariosResponseDtoToUsuariosDto(usuario)).build();
        }
        log.info("no tiene pedidos borrado fisico");
        usuariosRepository.delete(usuario);
        return UsuariosDeleteResponse.builder().message("usuario eliminado")
                .usuario(usuariosMapper.usuariosResponseDtoToUsuariosDto(usuario)).build();
    }

    @Override
    public UsuariosResponseDto updateAdmin(Long id, UsuariosPutPostDto usuariosPutPostDto) {
        log.info("Updating user pendientes del usuario");
        val contraseñaAntigua=usuariosRepository.findById(id).orElseThrow(()->{
            log.info("User not found: "+id);
            return new UserNotFound("User not found:"+id);
        });
        usuariosRepository.findByUsernameEqualsIgnoreCaseOrEmailEqualsIgnoreCase(usuariosPutPostDto.getUsername(),usuariosPutPostDto.getEmail()).ifPresent(
                usuario -> {
                    if (usuario.getId().equals(id)){
                        log.info("User already exists");
                        throw new UserEmailOrUsernameExists("ya existe un usuario con ese nombre o email");
                    }
                });
        val usuario= UsuariosPutPostDto.builder()
                .username(usuariosPutPostDto.getUsername())
                .email(usuariosPutPostDto.getEmail())
                .password(encoder.encode(usuariosPutPostDto.getPassword()))
                .roles(usuariosPutPostDto.getRoles())
                .build();
        return usuariosMapper.usuariosResponseDtoToUsuariosDto(usuariosRepository.save(usuariosMapper.postPutDtoToUsuario(usuariosPutPostDto)));
    }
}
