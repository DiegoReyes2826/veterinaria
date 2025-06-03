package com.veterinaria.app.controller;

import com.veterinaria.app.model.HistoriaClinica;
import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.repository.HistoriaClinicaRepository;
import com.veterinaria.app.repository.MascotaRepository;
import com.veterinaria.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/mascotas")
    public String verMisMascotas(HttpSession session, Model model) {
        String clienteId = (String) session.getAttribute("usuarioId");
        if (clienteId == null) return "redirect:/cliente/seleccionar";

        List<Mascota> mascotas = mascotaRepository.findByIdUsuario(new ObjectId(clienteId));
        model.addAttribute("misMascotas", mascotas);
        return "cliente/verMascotasCliente";
    }

    @GetMapping("/historial")
    public String verHistorialClinico(HttpSession session, Model model) {
        String clienteId = (String) session.getAttribute("usuarioId");
        if (clienteId == null) return "redirect:/cliente/seleccionar";

        List<Mascota> mascotas = mascotaRepository.findByIdUsuario(new ObjectId(clienteId));
        List<ObjectId> idsMascotas = mascotas.stream().map(Mascota::getId).toList();
        List<HistoriaClinica> historias = historiaClinicaRepository.findAll().stream()
                .filter(h -> idsMascotas.contains(h.getMascotaId()))
                .toList();

        model.addAttribute("historias", historias);
        return "citas/verHistorialCliente";
    }

    @GetMapping("/seleccionar")
    public String seleccionarCliente(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findByRol("CLIENTE"));
        model.addAttribute("ruta", "/cliente/mascotas");
        return "seleccionarUsuario";
    }

    @GetMapping("/set-id")
    public String setClienteId(@RequestParam String clienteId, HttpSession session) {
        session.setAttribute("usuarioId", clienteId);
        return "redirect:/cliente/mascotas";
    }
    
    @GetMapping("/mascota/{mascotaId}/historia")
    public String verHistoriaDeMascota(@PathVariable String mascotaId, Model model) {
        List<HistoriaClinica> historias = historiaClinicaRepository.findAllByMascotaId(new ObjectId(mascotaId));
        model.addAttribute("historias", historias);
        return "cliente/verHistorialCliente";
    }

}
