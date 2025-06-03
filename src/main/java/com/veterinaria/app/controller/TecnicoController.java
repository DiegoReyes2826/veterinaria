package com.veterinaria.app.controller;

import com.veterinaria.app.model.HistoriaClinica;
import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.repository.HistoriaClinicaRepository;
import com.veterinaria.app.repository.MascotaRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tecnico")
public class TecnicoController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @GetMapping("/mascotas")
    public String verMascotasEnTratamiento(Model model) {
        List<Mascota> mascotas = mascotaRepository.findAll(); // Puedes filtrar por condición si lo necesitas
        model.addAttribute("mascotas", mascotas);
        return "tecnico/verMascotasTecnico";
    }

    @GetMapping("/historial/{mascotaId}")
    public String verHistorialPorMascota(@PathVariable ObjectId mascotaId, Model model) {
        List<HistoriaClinica> historias = historiaClinicaRepository.findAllByMascotaId(mascotaId);
        model.addAttribute("historias", historias);
        model.addAttribute("mascotaId", mascotaId);
        return "tecnico/verHistorialTecnico";
    }

    @GetMapping("/historia/editar/{id}")
    public String editarNotasTecnicas(@PathVariable ObjectId id, Model model) {
        HistoriaClinica historia = historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Historia no encontrada"));
        model.addAttribute("historia", historia);
        return "tecnico/editarNotasTecnicas";
    }

    @PostMapping("/historia/guardar")
    public String guardarNotasTecnicas(@ModelAttribute HistoriaClinica historia) {
        HistoriaClinica existente = historiaClinicaRepository.findById(historia.getId())
                .orElseThrow(() -> new IllegalArgumentException("Historia no encontrada"));
        existente.setNotasTecnicas(historia.getNotasTecnicas());
        historiaClinicaRepository.save(existente);
        return "redirect:/tecnico/historial/" + existente.getMascotaId().toHexString();
    }
}
