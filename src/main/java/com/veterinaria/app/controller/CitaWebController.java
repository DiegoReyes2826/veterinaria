package com.veterinaria.app.controller;

import com.veterinaria.app.model.Cita;
import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.model.Usuario;
import com.veterinaria.app.repository.CitaRepository;
import com.veterinaria.app.repository.MascotaRepository;
import com.veterinaria.app.repository.UsuarioRepository;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/citas")
public class CitaWebController {

    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @GetMapping
    public String listarCitas(@RequestParam(required = false) String estado,
                              @RequestParam(required = false) String fecha,
                              @RequestParam(required = false) String rol, // ✅ nuevo
                              Model model) {

        List<Cita> citas;
        boolean hayEstado = estado != null && !estado.isBlank();
        boolean hayFecha = fecha != null && !fecha.isBlank();

        try {
            if (hayEstado && hayFecha) {
                LocalDate fechaFiltro = LocalDate.parse(fecha);
                LocalDateTime desde = fechaFiltro.atStartOfDay();
                LocalDateTime hasta = desde.plusDays(1).minusSeconds(1);
                citas = citaRepository.findByEstadoAndFechaHoraBetween(estado, desde, hasta);
            } else if (hayEstado) {
                citas = citaRepository.findByEstado(estado);
            } else if (hayFecha) {
                LocalDate fechaFiltro = LocalDate.parse(fecha);
                LocalDateTime desde = fechaFiltro.atStartOfDay();
                LocalDateTime hasta = desde.plusDays(1).minusSeconds(1);
                citas = citaRepository.findByFechaHoraBetween(desde, hasta);
            } else {
                citas = citaRepository.findAll();
            }
        } catch (Exception e) {
            citas = citaRepository.findAll();
        }

        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, String> nombresUsuarios = usuarios.stream()
            .collect(Collectors.toMap(u -> u.getId().toString(), Usuario::getNombre));

        List<Mascota> mascotas = mascotaRepository.findAll();
        Map<String, String> nombresMascotas = mascotas.stream()
            .collect(Collectors.toMap(m -> m.getId().toString(), Mascota::getNombre));

        Map<String, String> nombresVeterinarios = usuarios.stream()
            .filter(u -> "VETERINARIO".equalsIgnoreCase(u.getRol()))
            .collect(Collectors.toMap(v -> v.getId().toString(), Usuario::getNombre));

        model.addAttribute("listaCitas", citas);
        model.addAttribute("nombresUsuarios", nombresUsuarios);
        model.addAttribute("nombresMascotas", nombresMascotas);
        model.addAttribute("nombresVeterinarios", nombresVeterinarios);
        model.addAttribute("estado", estado);
        model.addAttribute("fecha", fecha);
        model.addAttribute("rol", rol); // ✅ AÑADIR EL ROL

        return "citas/verCitas";
    }


    @GetMapping("/nuevo")
    public String mostrarFormularioNuevaCita(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("editMode", "false");

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Mascota> mascotas = mascotaRepository.findAll();
        List<Usuario> veterinarios = usuarioRepository.findByRol("VETERINARIO");

        // Generar bloques de hora: 08:00, 08:30, ..., 17:30
        List<String> bloquesHora = new ArrayList<>();
        for (int h = 8; h <= 17; h++) {
            bloquesHora.add(String.format("%02d:00", h));
            bloquesHora.add(String.format("%02d:30", h));
        }

        model.addAttribute("veterinarios", veterinarios);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("mascotas", mascotas);
        model.addAttribute("bloquesHora", bloquesHora);

        return "citas/formCita";
    }


    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable ObjectId id, Model model) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        model.addAttribute("cita", cita);
        model.addAttribute("editMode", "true");
        return "citas/formCita";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCita(@PathVariable ObjectId id) {
        citaRepository.deleteById(id);
        return "redirect:/citas";
    }

    @PostMapping("/guardar")
    public String guardarCita(@ModelAttribute Cita cita,
                              @RequestParam String hora,
                              @RequestParam String fecha,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        try {
            // Combinar fecha y hora en LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime fechaHora = LocalDateTime.parse(fecha + " " + hora, formatter);
            cita.setFechaHora(fechaHora);
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Fecha u hora inválida.");
            return "redirect:/citas/nuevo";
        }

        cita.setEstado("pendiente");

        // Validar solapamiento con otras citas del veterinario
        LocalDateTime finCita = cita.getFechaHora().plusMinutes(29);
        List<Cita> conflictoVeterinario = citaRepository.findByVeterinarioIdAndFechaHoraBetween(
            cita.getVeterinarioId(), cita.getFechaHora(), finCita);

        if (!conflictoVeterinario.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El veterinario ya tiene una cita en ese horario.");
            return "redirect:/citas/nuevo";
        }

        // Validar si la mascota ya tiene una cita ese día
        LocalDateTime inicioDia = cita.getFechaHora().toLocalDate().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1).minusSeconds(1);
        List<Cita> conflictoMascota = citaRepository.findByMascotaIdAndFechaHoraBetween(
            cita.getMascotaId(), inicioDia, finDia);

        if (!conflictoMascota.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La mascota ya tiene una cita ese día.");
            return "redirect:/citas/nuevo";
        }

        // Guardar
        citaRepository.save(cita);
        return "redirect:/citas";
    }



    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(ObjectId.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(new ObjectId(text));
            }
        });
    }

    @PostMapping("/actualizar-estado/{id}")
    public String actualizarEstado(@PathVariable ObjectId id,
                                   @RequestParam String nuevoEstado) {
        Optional<Cita> optionalCita = citaRepository.findById(id);
        if (optionalCita.isPresent()) {
            Cita cita = optionalCita.get();
            cita.setEstado(nuevoEstado);
            citaRepository.save(cita);
        }
        return "redirect:/citas";
    }


    @GetMapping("/cliente/{id}")
    public String verCitasPorCliente(@PathVariable String id, Model model) {
        List<Cita> citasCliente = citaRepository.findByUsuarioId(id);

        model.addAttribute("listaCitas", citasCliente);

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Mascota> mascotas = mascotaRepository.findAll();

        Map<String, String> nombresUsuarios = new HashMap<>();
        usuarios.forEach(u -> nombresUsuarios.put(u.getId().toHexString(), u.getNombre()));

        Map<String, String> nombresMascotas = new HashMap<>();
        mascotas.forEach(m -> nombresMascotas.put(m.getId().toHexString(), m.getNombre()));

        model.addAttribute("nombresUsuarios", nombresUsuarios);
        model.addAttribute("nombresMascotas", nombresMascotas);

        return "citas/verCitasCliente";
    }

    @PostMapping("/asignar-veterinario/{id}")
    public String asignarVeterinario(@PathVariable ObjectId id,
                                     @RequestParam String veterinarioId,
                                     RedirectAttributes redirectAttributes) {
        Optional<Cita> optional = citaRepository.findById(id);
        if (optional.isEmpty()) return "redirect:/citas";

        Cita cita = optional.get();
        LocalDateTime inicio = cita.getFechaHora();
        LocalDateTime fin = inicio.plusMinutes(29); // para evitar solapes exactos

        List<Cita> citasConflicto = citaRepository.findByVeterinarioIdAndFechaHoraBetween(
            veterinarioId, inicio, fin
        );

        // Validar: solo aceptar si no hay conflictos o si es la misma cita
        boolean tieneConflicto = citasConflicto.stream()
            .anyMatch(c -> !c.getId().equals(cita.getId()));

        if (tieneConflicto) {
            redirectAttributes.addFlashAttribute("error", "El veterinario ya tiene una cita en ese horario.");
            return "redirect:/citas";
        }

        cita.setVeterinarioId(veterinarioId);
        citaRepository.save(cita);
        return "redirect:/citas";
    }



    @GetMapping("/veterinario/{id}/citas")
    public String verCitasVeterinario(@PathVariable String id, Model model) {
        List<Cita> citas = citaRepository.findByVeterinarioId(id);

        model.addAttribute("listaCitas", citas);

        List<Mascota> mascotas = mascotaRepository.findAll();
        Map<String, String> nombresMascotas = new HashMap<>();
        mascotas.forEach(m -> nombresMascotas.put(m.getId().toHexString(), m.getNombre()));
        model.addAttribute("nombresMascotas", nombresMascotas);

        return "citas/verCitasVeterinario";
    }

    @GetMapping("/editar-veterinario/{id}")
    public String mostrarFormularioEditarVeterinario(@PathVariable ObjectId id, Model model) {
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        List<Usuario> veterinarios = usuarioRepository.findByRol("VETERINARIO");

        model.addAttribute("cita", cita);
        model.addAttribute("veterinarios", veterinarios);
        return "citas/formVeterinario";
    }
    
    @GetMapping("/filtro")
    public String filtrarCitas(@RequestParam(required = false) String estado,
                               @RequestParam(required = false) String fecha,
                               Model model) {
        List<Cita> citas;

        boolean hayEstado = estado != null && !estado.isBlank();
        boolean hayFecha = fecha != null && !fecha.isBlank();

        try {
            if (hayEstado && hayFecha) {
                LocalDate fechaFiltro = LocalDate.parse(fecha);
                LocalDateTime desde = fechaFiltro.atStartOfDay();
                LocalDateTime hasta = desde.plusDays(1).minusSeconds(1);
                citas = citaRepository.findByEstadoAndFechaHoraBetween(estado, desde, hasta);
            } else if (hayEstado) {
                citas = citaRepository.findByEstado(estado);
            } else if (hayFecha) {
                LocalDate fechaFiltro = LocalDate.parse(fecha);
                LocalDateTime desde = fechaFiltro.atStartOfDay();
                LocalDateTime hasta = desde.plusDays(1).minusSeconds(1);
                citas = citaRepository.findByFechaHoraBetween(desde, hasta);
            } else {
                citas = citaRepository.findAll();
            }
        } catch (Exception e) {
            citas = citaRepository.findAll();
        }

        // Mapas de nombres como antes
        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, String> nombresUsuarios = usuarios.stream()
                .collect(Collectors.toMap(u -> u.getId().toString(), Usuario::getNombre));

        List<Mascota> mascotas = mascotaRepository.findAll();
        Map<String, String> nombresMascotas = mascotas.stream()
                .collect(Collectors.toMap(m -> m.getId().toString(), Mascota::getNombre));

        List<Usuario> veterinarios = usuarios.stream()
                .filter(u -> "VETERINARIO".equalsIgnoreCase(u.getRol()))
                .collect(Collectors.toList());

        Map<String, String> nombresVeterinarios = veterinarios.stream()
                .collect(Collectors.toMap(v -> v.getId().toString(), Usuario::getNombre));

        // Enviar al modelo
        model.addAttribute("listaCitas", citas);
        model.addAttribute("nombresUsuarios", nombresUsuarios);
        model.addAttribute("nombresMascotas", nombresMascotas);
        model.addAttribute("nombresVeterinarios", nombresVeterinarios);

        return "citas/verCitas";
    }

}