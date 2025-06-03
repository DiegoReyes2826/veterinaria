package com.veterinaria.app.controller;

import com.veterinaria.app.model.Cita;
import com.veterinaria.app.model.HistoriaClinica;
import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.model.Usuario;
import com.veterinaria.app.repository.CitaRepository;
import com.veterinaria.app.repository.HistoriaClinicaRepository;
import com.veterinaria.app.repository.MascotaRepository;
import com.veterinaria.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class VeterinarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @GetMapping("/veterinario/citas")
    public String verCitasDelDiaVeterinario(HttpSession session, Model model) {
        String veterinarioId = (String) session.getAttribute("usuarioId");
        if (veterinarioId == null) {
            return "redirect:/veterinario/seleccionar";
        }

        LocalDateTime inicioHoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1).minusSeconds(1);

        List<Cita> citas = citaRepository.findByVeterinarioIdAndFechaHoraBetween(
                veterinarioId, inicioHoy, finHoy
        );

        Map<String, String> nombresClientes = usuarioRepository.findAll().stream()
                .collect(Collectors.toMap(u -> u.getId().toHexString(), Usuario::getNombre));

        Map<String, String> nombresMascotas = mascotaRepository.findAll().stream()
                .collect(Collectors.toMap(m -> m.getId().toHexString(), Mascota::getNombre));

        model.addAttribute("citasHoy", citas);
        model.addAttribute("nombresClientes", nombresClientes);
        model.addAttribute("nombresMascotas", nombresMascotas);

        return "citas/verCitasVeterinarioHoy";
    }
    @GetMapping("/veterinario/set-id")
    public String setVeterinarioId(@RequestParam String veterinarioId, HttpSession session) {
        session.setAttribute("usuarioId", veterinarioId);
        return "redirect:/veterinario/citas";
    }


    @GetMapping("/veterinario/mascotas")
    public String verMascotasAtendidasVeterinario(HttpSession session, Model model) {
        String veterinarioId = (String) session.getAttribute("usuarioId");
        if (veterinarioId == null) {
            return "redirect:/veterinario/seleccionar";
        }

        // 🟡 Esto debería usarse si guardas el id del veterinario en la historia
        // por ahora se listan todas las mascotas con historia clínica
        List<HistoriaClinica> historias = historiaClinicaRepository.findAll();
        List<ObjectId> idsMascotasAtendidas = historias.stream()
                .map(HistoriaClinica::getMascotaId)
                .distinct()
                .toList();

        List<Mascota> mascotas = mascotaRepository.findAllById(idsMascotasAtendidas);
        model.addAttribute("mascotasAtendidas", mascotas);

        return "citas/verMascotasVeterinario";
    }

    @GetMapping("/veterinario/disponibilidad")
    public String verDisponibilidadVeterinario(@RequestParam(required = false) String fecha,
                                               HttpSession session,
                                               Model model) {

        String veterinarioId = (String) session.getAttribute("usuarioId");
        if (veterinarioId == null) {
            return "redirect:/veterinario/seleccionar";
        }

        Usuario veterinario = usuarioRepository.findById(new ObjectId(veterinarioId))
                .orElseThrow(() -> new IllegalArgumentException("Veterinario no encontrado"));

        model.addAttribute("veterinarioSeleccionado", veterinario);

        if (fecha != null) {
            LocalDateTime inicioDia = LocalDateTime.parse(fecha + "T00:00:00");
            LocalDateTime finDia = inicioDia.plusDays(1).minusSeconds(1);

            List<Cita> citas = citaRepository.findByVeterinarioIdAndFechaHoraBetween(
                    veterinarioId, inicioDia, finDia);

            List<String> horasOcupadas = citas.stream()
                    .map(c -> c.getFechaHora().toLocalTime().toString().substring(0, 5))
                    .toList();

            List<String> bloques = new ArrayList<>();
            for (int h = 8; h <= 17; h++) {
                bloques.add(String.format("%02d:00", h));
                bloques.add(String.format("%02d:30", h));
            }

            List<String> disponibles = bloques.stream()
                    .filter(h -> !horasOcupadas.contains(h))
                    .toList();

            model.addAttribute("fecha", fecha);
            model.addAttribute("horasDisponibles", disponibles);
        }

        return "citas/disponibilidadVeterinario";
    }

    @GetMapping("/veterinario/seleccionar")
    public String seleccionarVeterinario(Model model) {
        List<Usuario> veterinarios = usuarioRepository.findByRol("VETERINARIO");
        model.addAttribute("usuarios", veterinarios);
        model.addAttribute("ruta", "/veterinario/citas");
        return "seleccionarUsuario";
    }

    // Mantengo la vista original de disponibilidad por nombre como auxiliar
    @GetMapping("/recepcionista/veterinarios-disponibles")
    public String verVeterinariosDisponibles(@RequestParam(value = "nombre", required = false) String nombre,
                                             Model model) {
        List<Usuario> todosVeterinarios = usuarioRepository.findByRol("VETERINARIO");
        model.addAttribute("todosVeterinarios", todosVeterinarios);

        if (nombre != null && !nombre.isEmpty()) {
            List<Usuario> veterinariosEncontrados = usuarioRepository.findByNombreContainingIgnoreCase(nombre.trim())
                    .stream().filter(v -> "VETERINARIO".equalsIgnoreCase(v.getRol()))
                    .toList();

            if (!veterinariosEncontrados.isEmpty()) {
                Usuario veterinario = veterinariosEncontrados.get(0);
                List<Cita> citas = citaRepository.findByVeterinarioId(veterinario.getId().toString());

                for (Cita cita : citas) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findById(new ObjectId(cita.getUsuarioId()));
                    Optional<Mascota> mascotaOpt = mascotaRepository.findById(new ObjectId(cita.getMascotaId()));
                    cita.setUsuarioNombre(usuarioOpt.map(Usuario::getNombre).orElse("Desconocido"));
                    cita.setMascotaNombre(mascotaOpt.map(Mascota::getNombre).orElse("Desconocida"));
                }

                model.addAttribute("citas", citas);
                model.addAttribute("veterinarioSeleccionado", veterinario);
            } else {
                model.addAttribute("mensaje", "No se encontró ningún veterinario con ese nombre.");
            }
        }

        return "citas/verVeterinariosDisponibles";
    }
}
