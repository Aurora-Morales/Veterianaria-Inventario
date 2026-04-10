package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import com.uacm.veterinaria.inventario.persistencia.repository.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
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

    //Metodo que busca los productos en la BD por nombre
    @GetMapping
    public String listarProductos(@RequestParam(name = "buscar", required = false) String buscar,
                                  @RequestParam(name = "buscarStock", required = false) Integer stock,
                                  @RequestParam(name = "buscarFecha", required = false) LocalDate fecha,
                                  Model model) {
        List<Producto> lista;
        LocalDate diaAcutual = LocalDate.now();

        //Buscar en la BD
        if (buscar != null && !buscar.isEmpty()) {
            lista = productoRepository.findByNombreContainingIgnoreCase(buscar);
        } else {
            if(stock != null) {
                lista = productoRepository.findAllByStockLessThanEqual(stock);
            }else{
                if (fecha != null){
                    lista = productoRepository.findAllByFechaCaducidadLessThanEqual(fecha);
                }else{
                    lista = productoRepository.findAll(); // Trae la lista de productos
                }
            }
        }

        //Filtrar alertas (Stock bajo)
        List<Producto> alertas = lista.stream()
                .filter(p -> p.getStock() < 10)
                .collect(Collectors.toList());

        // 3. Filtrar alerta (Fecha de vencimiento)
        List<Producto> alertaFecha = lista.stream()
                .filter(p -> p.getFechaCaducidad() != null && p.getFechaCaducidad().isBefore(diaAcutual))
                .collect(Collectors.toList());

        //Enviamos a la vista HTML
        model.addAttribute("productos", lista);
        model.addAttribute("alertas", alertas);
        model.addAttribute("alertaFecha", alertaFecha);

        return "index";
    }

    //Metodo para agregar un nuevo producto
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, @RequestParam("archivoImagen") MultipartFile archivo){
        if(!archivo.isEmpty()){
            try{
                //Obtener el nombre del archivo
                String nombreImagen = archivo.getOriginalFilename();
                producto.setImagenNombre(nombreImagen);

                //Crear la ruta del directorio
                Path directorioImagenes = Paths.get(rutaDirectorio);

                //Verificar si el directorio existe, si no, crearlo
                if (!Files.exists(directorioImagenes)) {
                    Files.createDirectories(directorioImagenes);
                }

                //Guardar el archivo fisicamente en la carpeta static/images
                byte[] bytes = archivo.getBytes();
                Path rutaCompleta = Paths.get(rutaDirectorio).resolve(nombreImagen);
                Files.write(rutaCompleta, bytes);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //Gudardar en base de datos
        productoRepository.save(producto);
        return "redirect:/productos";
    }

    //Metodo para eliminar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id) {
        //Borrar el producto de la base de datos usando su ID
        productoRepository.deleteById(id);

        //Redirigir a la lista principal para ver el cambio
        return "redirect:/productos";
    }

    //Método para abrir la edición (Carga el producto y vuelve al index)
    @GetMapping("/editar/{id}")
    public String prepararEdicion(@PathVariable("id") Long id, Model model, @RequestParam(name = "buscar", required = false) String buscar) {
        // Reutilizamos la lógica de listar para no perder la vista de fondo
        listarProductos(buscar, null,null, model);

        Producto producto = productoRepository.findById(id).orElse(null);
        model.addAttribute("productoEdit", producto); // Producto que se va a editar
        model.addAttribute("abrirModal", true); // Señal para abrir el modal automáticamente

        return "index";
    }

    //Método para procesar la actualización
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Producto producto, @RequestParam("archivoImagen") MultipartFile archivo) {
        Producto existente = productoRepository.findById(producto.getId()).orElse(null);

        if (!archivo.isEmpty()) {
            try {
                String nombreImagen = archivo.getOriginalFilename();
                producto.setImagenNombre(nombreImagen);
                Files.write(Paths.get(rutaDirectorio).resolve(nombreImagen), archivo.getBytes());
            } catch (IOException e) { e.printStackTrace(); }
        } else if (existente != null) {
            producto.setImagenNombre(existente.getImagenNombre()); // Mantiene la imagen anterior
        }

        productoRepository.save(producto); // Guarda los cambios
        return "redirect:/productos";
    }
}
