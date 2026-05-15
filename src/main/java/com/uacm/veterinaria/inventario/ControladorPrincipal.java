package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import com.uacm.veterinaria.inventario.persistencia.repository.ProductoRepositorio;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/*
*   Metodo que controla la vista para administrar los productos
*
*/

@Controller
@RequestMapping("/productos")
public class ControladorPrincipal {

    //Mando a llamar a los metodos de consulta de la base de datos
    @Autowired
    private ProductoRepositorio productoRepository;
    @Autowired
    private JavaMailSender mailSender;

    // Ruta donde se guardaran las imagenes
    @Value("${ruta.imagenes}")
    private String rutaDirectorio;

    //Metodo que busca los productos en la BD por nombre
    @GetMapping
    public String listarProductos(@RequestParam(name = "buscar", required = false) String buscar,
                                  @RequestParam(name = "buscarStock", required = false) Integer stock,
                                  @RequestParam(name = "buscarFecha", required = false) LocalDate fecha,
                                  Model model, HttpSession session) {
        List<Producto> lista;
        //Fecha para las notificaciones de alertas
        LocalDate diaAcutual = LocalDate.now();

        // Verificamos si el atributo que definimos en el login existe
        if (session.getAttribute("usuarioLogueado") == null) {
            // Si no hay nadie en sesión, lo mandamos de vuelta al login
            return "redirect:/principal?error";
        }

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

        //Filtrar alerta por fecha de vencimiento
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
    public String prepararEdicion(@PathVariable("id") Long id, Model model, @RequestParam(name = "buscar", required = false) String buscar,
                                  HttpSession session) {
        //Protección: Si no está logueado, fuera.
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/principal?error";
        }

        //Reutilizar la lógica de listar para no perder la vista de fondo
        listarProductos(buscar, null,null, model, session);

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

    //Método para cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Esto destruye toda la información de la sesión actual
        return "redirect:/principal?logout";
    }

    // Dentro de tu ControladorPrincipal
    @GetMapping("/reporte")
    public String generarReporte(Model model, HttpSession session) {
        // 1. Validar sesión
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/principal?error";
        }

        LocalDate hoy = LocalDate.now();
        List<Producto> todos = productoRepository.findAll();

        // 2. Filtrar productos faltantes o caducados
        List<Producto> reporteLista = todos.stream()
                .filter(p -> p.getStock() < 10 || (p.getFechaCaducidad() != null && p.getFechaCaducidad().isBefore(hoy)))
                .collect(Collectors.toList());

        // 3. Calcular el costo total de venta de estos productos
        double costoTotalVenta = reporteLista.stream()
                .mapToDouble(p -> p.getPrecioVenta() * p.getStock())
                .sum();

        model.addAttribute("productosReporte", reporteLista);
        model.addAttribute("totalVenta", costoTotalVenta);
        model.addAttribute("fechaGeneracion", hoy);

        return "reporte-inventario"; // Crearemos esta vista nueva
    }

    @PostMapping("/enviar-reporte")
    public String enviarReportePorCorreo(@RequestParam("correoDestino") String destino, HttpSession session, RedirectAttributes flash) {
        if (session.getAttribute("usuarioLogueado") == null) return "redirect:/principal";

        try {
            // 1. Obtener datos (mismo filtro que el reporte)
            List<Producto> reporteLista = productoRepository.findAll().stream()
                    .filter(p -> p.getStock() < 10 || (p.getFechaCaducidad() != null && p.getFechaCaducidad().isBefore(LocalDate.now())))
                    .collect(Collectors.toList());

            double total = reporteLista.stream().mapToDouble(p -> p.getPrecioVenta() * p.getStock()).sum();

            // 2. Construir el cuerpo del correo (Texto o HTML)
            StringBuilder cuerpo = new StringBuilder();
            cuerpo.append("<h1>Pedido de Inventario Crítico</h1><table border='1'><tr><th>Producto</th><th>Proveedor</th><th>Stock</th><th>Subtotal</th></tr>");

            for (Producto p : reporteLista) {
                cuerpo.append(String.format("<tr><td>%s</td><td>%s</td><td>%d</td><td>$%.2f</td></tr>",
                        p.getNombre(), p.getNombreProveedor(), p.getStock(), (p.getPrecioVenta() * p.getStock())));
            }
            cuerpo.append("</table><h3>Total de Venta: $").append(total).append("</h3>");

            // 3. Enviar correo
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(destino);
            helper.setSubject("Pedido de Productos - Veterinaria Bloom");
            helper.setText(cuerpo.toString(), true); // 'true' indica que es HTML

            mailSender.send(message);
            flash.addFlashAttribute("successMail", "Correo enviado exitosamente a " + destino);

        } catch (Exception e) {
            flash.addFlashAttribute("errorMail", "Error al enviar: " + e.getMessage());
        }

        return "redirect:/productos/reporte";
    }
}
