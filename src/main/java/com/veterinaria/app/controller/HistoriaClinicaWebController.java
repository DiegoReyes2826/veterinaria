package com.veterinaria.app.controller;

import com.veterinaria.app.model.HistoriaClinica;
import com.veterinaria.app.repository.HistoriaClinicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;

@Controller
@RequestMapping("/historias")
public class HistoriaClinicaWebController {

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @GetMapping
    public String listarHistorias(Model model) {
        model.addAttribute("listaHistorias", historiaClinicaRepository.findAll());
        return "citas/verHistoria";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        HistoriaClinica historia = new HistoriaClinica();
        historia.setFecha(LocalDateTime.now());
        // historia.setMascotaId(...) // No puedes poner un ObjectId por defecto sin saber la mascota
        model.addAttribute("historia", historia);
        model.addAttribute("editMode", "false");
        return "citas/formHistoria";
    }


    @PostMapping("/guardar")
    public String guardar(@ModelAttribute HistoriaClinica historia, 
                         @RequestParam String editMode, 
                         Model model) {
        
        // Si es nuevo registro, asegurarnos que no tenga ID
        if ("false".equals(editMode)) {
            historia.setId(null); // MongoDB generará automáticamente un nuevo ObjectId
        }
        
        // Validar mascotaId
        if (historia.getMascotaId() == null) {
            // Manejar el error o asignar un valor por defecto
            throw new IllegalArgumentException("Se requiere el ID de la mascota");
        }
        
        historiaClinicaRepository.save(historia);
        return "redirect:/historias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable ObjectId id, Model model) {
        HistoriaClinica historia = historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Historia no encontrada"));
        model.addAttribute("historia", historia);
        model.addAttribute("editMode", "true");
        return "citas/formHistoria";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable ObjectId id) {
        historiaClinicaRepository.deleteById(id);
        return "redirect:/historias";
    }
    
    @GetMapping("/nuevo/{mascotaId}")
    public String nuevaConMascota(@PathVariable ObjectId mascotaId, Model model) {
        HistoriaClinica historia = new HistoriaClinica();
        historia.setMascotaId(mascotaId);
        historia.setFecha(LocalDateTime.now());
        model.addAttribute("historia", historia);
        model.addAttribute("editMode", "false");
        return "citas/formHistoria";
    }

    
    
    @GetMapping("/mascota/{mascotaId}")
    public String verHistoriasVeterinario(@PathVariable ObjectId mascotaId, Model model) {
        List<HistoriaClinica> historias = historiaClinicaRepository.findAllByMascotaId(mascotaId);
        model.addAttribute("historias", historias);
        return "citas/verHistorias";
    }

    @GetMapping("/observaciones/{mascotaId}")
    public String verObservacionesTecnico(@PathVariable ObjectId mascotaId, Model model) {
        List<HistoriaClinica> historias = historiaClinicaRepository.findAllByMascotaId(mascotaId);
        model.addAttribute("historias", historias);
        return "citas/verObservacionesTecnico";
    }

    @GetMapping("/cliente/{mascotaId}")
    public String verHistoriasCliente(@PathVariable ObjectId mascotaId, Model model) {
        List<HistoriaClinica> historias = historiaClinicaRepository.findAllByMascotaId(mascotaId);
        model.addAttribute("historias", historias);
        return "citas/verHistoriasCliente";
    }

}
