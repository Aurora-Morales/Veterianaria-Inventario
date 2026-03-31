package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import com.uacm.veterinaria.inventario.persistencia.repository.ProductoRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VeterianariaInventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeterianariaInventarioApplication.class, args);
    }

    //Prueba para visualizar datos
    /*@Bean
    CommandLineRunner init(ProductoRepositorio repository) {
        return args -> {

            Producto p2 = new Producto();
            p2.setNombre("Bravecto");
            p2.setCategoria("Desparacitante");
            p2.setPrecio(400);
            p2.setStock(10);
            p2.setImagenNombre("bravecto.jpeg");

            repository.save(p2);
            System.out.println("Producto guardado con éxito");
        };
    }*/
}
