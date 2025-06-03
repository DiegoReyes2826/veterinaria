package com.veterinaria.app.controller;

import com.veterinaria.app.model.Usuario;
import com.veterinaria.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class IndexController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mostrar el formulario de login
    @GetMapping("/")
    public String mostrarFormularioLogin() {
        return "login";  // login.html
    }

    // Procesar el login
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                 @RequestParam String contrasena,
                                 HttpSession session,
                                 Model model) {

        // Validar que no vengan nulos
        Optional<Usuario> usuarioOpt = usuarioRepository.findAll().stream()
                .filter(u -> correo != null && contrasena != null
                        && u.getCorreo() != null && u.getContrasena() != null
                        && u.getCorreo().equalsIgnoreCase(correo)
                        && u.getContrasena().equals(contrasena))
                .findFirst();

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuarioId", usuario.getId().toHexString());
            session.setAttribute("nombreUsuario", usuario.getNombre());
            session.setAttribute("rol", usuario.getRol().toLowerCase());

            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Credenciales inválidas");
            return "login";  // Vuelve al formulario
        }
    }

    // Dashboard redirige a la vista dependiendo el rol
    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        String nombre = (String) session.getAttribute("nombreUsuario");
        String rol = (String) session.getAttribute("rol");

        if (nombre == null || rol == null) {
            return "redirect:/";
        }

        model.addAttribute("nombre", nombre);
        model.addAttribute("rol", rol);
        return "dashboard"; // dashboard.html debe contener la lógica de roles
    }

    // Cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
