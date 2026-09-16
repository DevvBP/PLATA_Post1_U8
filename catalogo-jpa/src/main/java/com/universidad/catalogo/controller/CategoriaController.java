package com.universidad.catalogo.controller;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // ---- Listado ----

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "categorias/lista";
    }

    // ---- Formulario crear ----

    @GetMapping("/nueva")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("titulo", "Nueva Categoría");
        return "categorias/formulario";
    }

    // ---- Formulario editar ----

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return categoriaService.buscarPorId(id)
                .map(cat -> {
                    model.addAttribute("categoria", cat);
                    model.addAttribute("titulo", "Editar Categoría");
                    return "categorias/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Categoría no encontrada.");
                    return "redirect:/categorias";
                });
    }

    // ---- Guardar (crear / actualizar) ----

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
                          BindingResult resultado,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            model.addAttribute("titulo",
                    categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            return "categorias/formulario";
        }

        try {
            categoriaService.guardar(categoria);
            redirectAttributes.addFlashAttribute("exito",
                    "Categoría '" + categoria.getNombre() + "' guardada correctamente.");
        } catch (IllegalArgumentException e) {
            resultado.rejectValue("nombre", "nombre.duplicado", e.getMessage());
            model.addAttribute("titulo",
                    categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            return "categorias/formulario";
        }

        return "redirect:/categorias";
    }

    // ---- Confirmación de eliminación ----

    @GetMapping("/eliminar/{id}")
    public String mostrarConfirmacion(@PathVariable Long id, Model model,
                                      RedirectAttributes redirectAttributes) {
        return categoriaService.buscarPorId(id)
                .map(cat -> {
                    model.addAttribute("categoria", cat);
                    return "categorias/confirmar-eliminar";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Categoría no encontrada.");
                    return "redirect:/categorias";
                });
    }

    // ---- Eliminar ----

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Categoría eliminada correctamente.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
