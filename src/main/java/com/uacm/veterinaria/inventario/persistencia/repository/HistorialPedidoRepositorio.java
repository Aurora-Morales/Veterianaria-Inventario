package com.uacm.veterinaria.inventario.persistencia.repository;

import com.uacm.veterinaria.inventario.persistencia.entitys.HistorialPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialPedidoRepositorio extends JpaRepository<HistorialPedido, Long> {
    // Ordena automáticamente del más reciente al más antiguo
    List<HistorialPedido> findAllByOrderByFechaEnvioDesc();
}   