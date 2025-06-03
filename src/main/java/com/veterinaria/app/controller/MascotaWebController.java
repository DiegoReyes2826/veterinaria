package com.veterinaria.app.controller;

import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.model.Usuario;
import com.veterinaria.app.repository.MascotaRepository;
import com.veterinaria.app.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/mascotas")
public class MascotaWebController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarMascotas(Model model) {
        List<Mascota> mascotas = mascotaRepository.findAll();
        model.addAttribute("mascotas", mascotas);

        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, String> nombresUsuarios = new HashMap<>();
        for (Usuario u : usuarios) {
            if (u.getId() != null) {
                nombresUsuarios.put(u.getId().toHexString(), u.getNombre());
            }
        }

        model.addAttribute("nombresUsuarios", nombresUsuarios); // 👈 Esto debe existir
        return "mascotas/verMascotas";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("mascota", new Mascota());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("editMode", false);
        return "mascotas/formMascota";
    }

    @PostMapping("/guardar")
    public String guardarMascota(@ModelAttribute Mascota mascota) {
        mascotaRepository.save(mascota);
        return "redirect:/mascotas";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable ObjectId id, Model model) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada"));
        model.addAttribute("mascota", mascota);
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("editMode", true);
        return "mascotas/formMascota";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMascota(@PathVariable ObjectId id) {
        mascotaRepository.deleteById(id);
        return "redirect:/mascotas";
    }
    
    @GetMapping("/usuario/{id}")
    public String listarMascotasPorUsuario(@PathVariable String id, Model model) {
        ObjectId objectId = new ObjectId(id); // convertir string a ObjectId
        List<Mascota> mascotas = mascotaRepository.findByIdUsuario(objectId);
        model.addAttribute("mascotas", mascotas);

        // Obtener el nombre del usuario
        Usuario usuario = usuarioRepository.findById(objectId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("nombreUsuario", usuario.getNombre());

        return "mascotas/verMascotasPorUsuario";
    }

}
