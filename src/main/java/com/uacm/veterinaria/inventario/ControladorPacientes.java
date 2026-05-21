package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.HistorialPaciente;
import com.uacm.veterinaria.inventario.persistencia.entitys.Producto;
import com.uacm.veterinaria.inventario.persistencia.repository.HistorialPacienteRepositorio;
import com.uacm.veterinaria.inventario.persistencia.repository.ProductoRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/pacientes")
public class ControladorPacientes {

    @Autowired
    private HistorialPacienteRepositorio historialPacienteRepository;

    @Autowired
    private ProductoRepositorio productoRepository;

    // GUARDAR Y EDITAR CON REVERSIÓN E INCREMENTO DE STOCK AUTOMÁTICO
    @PostMapping("/guardar")
    public String guardarRegistro(@ModelAttribute HistorialPaciente paciente,
                                  @RequestParam(value = "productoId", required = false) Long productoId,
                                  RedirectAttributes flash) {

        Producto nuevoProducto = (productoId != null) ? productoRepository.findById(productoId).orElse(null) : null;

        // --- CASO A: ES UNA EDICIÓN / MODIFICACIÓN ---
        if (paciente.getId() != null) {
            HistorialPaciente registroAnterior = historialPacienteRepository.findById(paciente.getId()).orElse(null);

            if (registroAnterior != null) {
                // 1. DEVOLVER EL STOCK ANTERIOR AL ALMACÉN (AUMENTA EL STOCK)
                if (registroAnterior.getMedicamentoAsignado() != null && !registroAnterior.getMedicamentoAsignado().equals("Ninguno / Servicio")) {
                    Producto prodAnterior = productoRepository.findAll().stream()
                            .filter(p -> p.getNombre().equals(registroAnterior.getMedicamentoAsignado()))
                            .findFirst().orElse(null);

                    if (prodAnterior != null) {
                        prodAnterior.setStock(prodAnterior.getStock() + registroAnterior.getCajasAsignadas());
                        productoRepository.save(prodAnterior); // Guardamos el aumento en el almacén
                    }
                }
                // Conservar la fecha original de la consulta
                paciente.setFechaConsulta(registroAnterior.getFechaConsulta());
            }
        } else {
            // --- CASO B: ES UN EXPEDIENTE NUEVO ---
            paciente.setFechaConsulta(LocalDateTime.now());
        }

        // --- PROCESAR EL NUEVO MEDICAMENTO ASIGNADO Y DESCONTAR DEL STOCK ---
        if (nuevoProducto != null) {
            if (nuevoProducto.getStock() < paciente.getCajasAsignadas()) {
                flash.addFlashAttribute("errorPacientes", "Error: Stock insuficiente en almacén de " + nuevoProducto.getNombre() + ". Disponibles: " + nuevoProducto.getStock());
                return "redirect:/pacientes";
            }
            // Descontar las nuevas cajas asignadas
            nuevoProducto.setStock(nuevoProducto.getStock() - paciente.getCajasAsignadas());
            productoRepository.save(nuevoProducto);

            paciente.setMedicamentoAsignado(nuevoProducto.getNombre());
        } else {
            // Si eligió "-- Ninguno / Quitar Medicamento --"
            paciente.setMedicamentoAsignado("Ninguno / Servicio");
            paciente.setCajasAsignadas(0);
        }

        historialPacienteRepository.save(paciente);
        flash.addFlashAttribute("successPacientes", "Historial clínico actualizado. Almacén sincronizado con éxito.");
        return "redirect:/pacientes";
    }

    // ELIMINAR EXPEDIENTE Y DEVOLVER TOTALMENTE EL STOCK AL ALMACÉN (AUMENTAR STOCK)
    @GetMapping("/eliminar/{id}")
    public String eliminarRegistro(@PathVariable Long id, RedirectAttributes flash) {
        HistorialPaciente paciente = historialPacienteRepository.findById(id).orElse(null);

        if (paciente != null) {
            // Si la consulta tenía productos, regresamos las cajas al almacén antes de borrarlo
            if (paciente.getMedicamentoAsignado() != null && !paciente.getMedicamentoAsignado().equals("Ninguno / Servicio")) {
                Producto productoAsociado = productoRepository.findAll().stream()
                        .filter(p -> p.getNombre().equals(paciente.getMedicamentoAsignado()))
                        .findFirst().orElse(null);

                if (productoAsociado != null) {
                    productoAsociado.setStock(productoAsociado.getStock() + paciente.getCajasAsignadas());
                    productoRepository.save(productoAsociado); // Incrementa el stock físico en la BD
                }
            }

            // Eliminar el historial médico de la BD
            historialPacienteRepository.deleteById(id);
            flash.addFlashAttribute("successPacientes", "El historial fue eliminado. Las cajas de productos han regresado al almacén (Stock aumentado).");
        } else {
            flash.addFlashAttribute("errorPacientes", "No se encontró el registro solicitado.");
        }

        return "redirect:/pacientes";
    }

    //FILTRO DE BÚSQUEDA
    @GetMapping
    public String listarHistorial(@RequestParam(value = "buscar", required = false) String buscar,
                                  Model model,
                                  HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/principal?error";
        }

        List<HistorialPaciente> lista;

        // Si el usuario usó la barra de búsqueda
        if (buscar != null && !buscar.trim().isEmpty()) {
            lista = historialPacienteRepository.buscarPorNombreRazaOFecha(buscar);
            model.addAttribute("keyword", buscar); // Guardamos la palabra para mantenerla en el input
        } else {
            // Si no hay búsqueda, traemos todos
            lista = historialPacienteRepository.findAllByOrderByFechaConsultaDesc();
        }

        model.addAttribute("listaHistorial", lista);
        model.addAttribute("productosInventario", productoRepository.findAll());
        return "historial-pacientes";
    }
}