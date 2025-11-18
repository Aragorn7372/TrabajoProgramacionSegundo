package dev.luisvives.trabajoprogramacionsegundo.common.tareaProgramada;

import dev.luisvives.trabajoprogramacionsegundo.common.email.EmailServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.productos.model.Producto;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.ProductoServiceImpl;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.model.Usuario;
import dev.luisvives.trabajoprogramacionsegundo.usuarios.service.usuarios.UsuariosPedidosServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TareaProgramada {

    private final ProductoServiceImpl productosService;
    private final EmailServiceImpl emailService;
    private final UsuariosPedidosServiceImpl usersService;


    private LocalDateTime ultimaEjecucion = LocalDateTime.now().minusDays(1);

    @Autowired
    public TareaProgramada(ProductoServiceImpl productosService,
                           EmailServiceImpl emailService,
                           UsuariosPedidosServiceImpl usersService) {
        this.productosService = productosService;
        this.emailService = emailService;
        this.usersService = usersService;
    }


    @Scheduled(cron = "0 30 8 * * ?")
    public void enviarCorreoNovedades() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Producto> nuevosProductos = productosService.findByCreatedAtBetween(ultimaEjecucion, ahora);

        if (!nuevosProductos.isEmpty()) {
            StringBuilder html = new StringBuilder();
            html.append("<h1>¡Novedades en la tienda!</h1>");
            html.append("<ul>");
            for (Producto producto : nuevosProductos) {
                html.append("<li>")
                        .append("<strong>").append(producto.getNombre()).append("</strong>")
                        .append(" - ").append(producto.getCategoria())
                        .append(" - ").append(producto.getPrecio()).append(" €")
                        .append(" - ").append(producto.getDescripcion())
                        .append("<img src='").append(producto.getImagen() == null ? Producto.IMAGE_DEFAULT : producto.getImagen())
                        .append("</li>");
            }
            html.append("</ul>");
            html.append("<p>Total de nuevos productos: <b>").append(nuevosProductos.size()).append("</b></p>");


            List<Usuario> usuarios = usersService.findAll();
            for (Usuario user : usuarios) {
                if (user.getEmail() != null && !user.getEmail().isBlank()) {
                    Thread emailThread = getThread(user, html);


                    emailThread.start();

                }
            }
        }

        ultimaEjecucion = ahora;
    }

    private Thread getThread(Usuario user, StringBuilder html) {
        Thread emailThread = new Thread(() -> {
            try {


                emailService.sendHtmlEmail(
                        user.getEmail(),
                        "Novedades de productos en la tienda",
                        html.toString()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        emailThread.setName("EmailSender-Novedades-" + user.getId());
        emailThread.setDaemon(true);
        return emailThread;
    }
}
