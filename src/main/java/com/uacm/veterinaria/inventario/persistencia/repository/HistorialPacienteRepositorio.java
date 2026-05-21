package com.uacm.veterinaria.inventario.persistencia.repository;

import com.uacm.veterinaria.inventario.persistencia.entitys.HistorialPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialPacienteRepositorio extends JpaRepository<HistorialPaciente, Long> {
    // Obtener las consultas más recientes primero
    List<HistorialPaciente> findAllByOrderByFechaConsultaDesc();
    // NUEVO MÉTODO: Busca por nombre, raza o por la fecha formateada como texto en la BD
    @Query("SELECT h FROM HistorialPaciente h WHERE " +
            "LOWER(h.nombreMascota) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(h.raza) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(h.fechaConsulta AS string) LIKE CONCAT('%', :keyword, '%') " +
            "ORDER BY h.fechaConsulta DESC")
    List<HistorialPaciente> buscarPorNombreRazaOFecha(@Param("keyword") String keyword);
}