package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Usuario;
import com.uacm.veterinaria.inventario.persistencia.repository.UsuarioRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/principal")
public class ControladorLogin {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio; // Cambia el nombre según tu interfaz real

    @GetMapping
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/validar")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session){

        List<Usuario> listaUsuarios = usuarioRepositorio.findAllByNombreAndContrasena(username,password);

        if(listaUsuarios.isEmpty()){
            return "redirect:/principal?error";
        }

        //Guardar al usuario en la Sesión de Java
        // Esto sirve para que otras páginas sepan que ya se logueó
        session.setAttribute("usuarioLogueado", listaUsuarios.get(0));

        return "redirect:/productos";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            @RequestParam("passwordActual") String passwordActual,
            @RequestParam("passwordNueva") String passwordNueva,
            HttpSession session,
            RedirectAttributes flash) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) {
            return "redirect:/principal";
        }

        Usuario usuarioBD = usuarioRepositorio.findById(usuarioSesion.getId()).orElse(null);

        if (usuarioBD == null) {
            flash.addFlashAttribute("errorPass", "El usuario no existe en el sistema.");
            return "redirect:/productos";
        }

        if (!usuarioBD.getContrasena().equals(passwordActual)) {
            flash.addFlashAttribute("errorPass", "La contraseña actual es incorrecta.");
            return "redirect:/productos";
        }

        usuarioBD.setContrasena(passwordNueva);
        usuarioRepositorio.save(usuarioBD);

        session.setAttribute("usuarioLogueado", usuarioBD);

        // Estas variables viajan de forma segura a /productos (index.html)
        flash.addFlashAttribute("successPass", "¡Contraseña actualizada con éxito!");
        return "redirect:/productos";
    }
}