package com.uacm.veterinaria.inventario.persistencia.repository;

import com.uacm.veterinaria.inventario.persistencia.entitys.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByNombreAndContrasena(String nombre, String contrasena);
}
