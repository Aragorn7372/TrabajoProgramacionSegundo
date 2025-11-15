package dev.luisvives.trabajoprogramacionsegundo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TrabajoProgramacionSegundoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrabajoProgramacionSegundoApplication.class, args);
    }

}
