package dev.luisvives.trabajoprogramacionsegundo.usuarios.controller;

import dev.luisvives.trabajoprogramacionsegundo.common.dto.PageResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.mappers.PedidosMapper;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.model.Pedido;
import dev.luisvives.trabajoprogramacionsegundo.pedidos.service.PedidosServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.dto.usuario.*;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.mapper.UsuariosMapper;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios.UsuariosPedidosServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/usuario")
@PreAuthorize("hasRole('USUARIO')")
public class UsuariosRestController {
    private final UsuariosPedidosServiceImpl usuariosService;
    private final PedidosServiceImpl pedidosService;
    private final UsuariosMapper mapper;
    private final PedidosMapper pedidosMapper;
    @Autowired
    public UsuariosRestController(UsuariosPedidosServiceImpl usuariosService, PedidosServiceImpl pedidosService, UsuariosMapper usuariosMapper, PedidosMapper pedidosMapper) {
        this.usuariosService = usuariosService;
        this.pedidosService = pedidosService;
        this.mapper = usuariosMapper;
        this.pedidosMapper = pedidosMapper;
    }
    @GetMapping({"", "/"})

    public ResponseEntity<PageResponseDTO<UsuariosResponseDto>> findAll(
            @RequestParam(required = false)Optional<Boolean> isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            HttpServletRequest httpServletRequest
            ){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        Sort sort= order.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(httpServletRequest.getRequestURL().toString());
        Page<UsuariosResponseDto> result= usuariosService.findAll(isDeleted, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(mapper.pageToDTO(result,sortBy,order));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuariosAdminResponseDto> findById(@PathVariable Long id){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        return ResponseEntity.ok(usuariosService.findById(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuariosResponseDto> updateUser(@PathVariable Long id, @Valid@RequestBody UsuariosPutPostDto usuario){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        return ResponseEntity.ok(usuariosService.updateAdmin(id, usuario));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuariosDeleteResponse> deleteUser(@PathVariable Long id){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        return ResponseEntity.ok(usuariosService.delete(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<UsuariosAdminResponseDto> me(@AuthenticationPrincipal Usuario usuario){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        return ResponseEntity.ok(usuariosService.findById(usuario.getId()));
    }
    @PutMapping("/me")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<UsuariosResponseDto> updateMe(@AuthenticationPrincipal Usuario usuario, @Valid @RequestBody UsuarioPutRequestByUserDto usuariosPutPostDto){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        return ResponseEntity.ok(usuariosService.update(usuario.getId(), usuariosPutPostDto));
    }
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<UsuariosDeleteResponse> deleteMe(@AuthenticationPrincipal Usuario usuario){
        log.info("CONTROLLER: Buscando todas las Usuarios");
        return ResponseEntity.ok(usuariosService.delete(usuario.getId()));
    }
    @GetMapping("me/pedidos")
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<PageResponseDTO<Pedido>> getPedidosByUsuario(
            @AuthenticationPrincipal Usuario usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
            ){
        log.info("CONTROLLER: Buscando todas las Pedidos");
        Sort sort= order.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<UsuariosResponseDto> result= usuariosService.findAll(PageRequest.of(page, size, sort));
    }
}

