package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import com.uacm.veterinaria.inventario.persistencia.repository.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

/*
*   Metodo que controla la vista principal
*
*/

@Controller
@RequestMapping("/productos")
public class ControladorPrincipal {

    //Mando a llamar a los metodos de consulta de la base de datos
    @Autowired
    private ProductoRepositorio productoRepository;

    // Ruta donde se guardaran las imagenes
    @Value("${ruta.imagenes}")
    private String rutaDirectorio;

    @GetMapping
    public String listarProductos(@RequestParam(name = "buscar", required = false) String buscar, Model model) {
        List<Producto> lista;
        LocalDate diaAcutual = LocalDate.now();

        // 1. Buscamos en la BD
        if (buscar != null && !buscar.isEmpty()) {
            lista = productoRepository.findByNombreContainingIgnoreCase(buscar);
        } else {
            lista = productoRepository.findAll(); // Trae la lista de productos
        }

        // 2. Filtramos alertas (Stock bajo)
        List<Producto> alertas = lista.stream()
                .filter(p -> p.getStock() < 5)
                .collect(Collectors.toList());

        // 3. Filtrar alerta (Fecha de vencimiento próxima)
        List<Producto> alertaFecha = lista.stream()
                .filter(p -> p.getFechaCaducidad() != null && p.getFechaCaducidad().isBefore(diaAcutual))
                .collect(Collectors.toList());

        // 4. Enviamos a la vista HTML
        model.addAttribute("productos", lista);
        model.addAttribute("alertas", alertas);
        model.addAttribute("alertaFecha", alertaFecha);

        return "index";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, @RequestParam("archivoImagen") MultipartFile archivo){
        if(!archivo.isEmpty()){
            try{
                // Obtener el nombre del archivo
                String nombreImagen = archivo.getOriginalFilename();
                producto.setImagenNombre(nombreImagen);

                // Crear la ruta del directorio
                Path directorioImagenes = Paths.get(rutaDirectorio);

                // Verificar si el directorio existe, si no, crearlo
                if (!Files.exists(directorioImagenes)) {
                    Files.createDirectories(directorioImagenes);
                }

                // Guardar el archivo fisicamente en la carpeta static/images
                byte[] bytes = archivo.getBytes();
                Path rutaCompleta = Paths.get(rutaDirectorio).resolve(nombreImagen);
                Files.write(rutaCompleta, bytes);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        // Gudardar en base de datos
        productoRepository.save(producto);
        return "redirect:/productos";
    }
}
