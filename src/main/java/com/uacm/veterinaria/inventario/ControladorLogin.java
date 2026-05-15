package com.uacm.veterinaria.inventario;

import com.uacm.veterinaria.inventario.persistencia.entitys.Usuario;
import com.uacm.veterinaria.inventario.persistencia.repository.UsuarioRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/principal")
public class ControladorLogin {

    private final UsuarioRepositorio usuarioRepositorio;

    public ControladorLogin(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

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
    public String cambiarPassword(@RequestParam String passwordActual, @RequestParam String passwordNueva,
                                  @RequestParam String passwordConfirmar, HttpSession session,
                                  RedirectAttributes flash) {

        //¿Hay alguien logueado?
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) return "redirect:/principal";

        //¿La contraseña actual es correcta?
        // Buscamos en la BD para tener el dato más fresco
        Usuario usuarioBD = usuarioRepositorio.findById(usuarioSesion.getId()).orElse(null);

        if (!usuarioBD.getContrasena().equals(passwordActual)) {
            flash.addFlashAttribute("errorPass", "La contraseña actual no es correcta.");
            return "redirect:/productos";
        }

        //¿Las nuevas contraseñas coinciden?
        if (!passwordNueva.equals(passwordConfirmar)) {
            flash.addFlashAttribute("errorPass", "Las nuevas contraseñas no coinciden.");
            return "redirect:/productos";
        }

        //Guardar en la base de datos
        usuarioBD.setContrasena(passwordNueva);
        try {
            Usuario guardado = usuarioRepositorio.save(usuarioBD);
            System.out.println("Contraseña actualizada para: " + guardado.getNombre());
        } catch (Exception e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }
        return "redirect:/productos";
    }


}