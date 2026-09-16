package com.universidad.catalogo.controller;

import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.service.CategoriaService;
import com.universidad.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    // ---- Listado ----

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        return "productos/lista";
    }

    // ---- Formulario crear ----

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("titulo", "Nuevo Producto");
        return "productos/formulario";
    }

    // ---- Formulario editar ----

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model,
                                          RedirectAttributes redirectAttributes) {
        return productoService.buscarPorId(id)
                .map(prod -> {
                    model.addAttribute("producto", prod);
                    model.addAttribute("categorias", categoriaService.listarTodas());
                    model.addAttribute("titulo", "Editar Producto");
                    return "productos/formulario";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
                    return "redirect:/productos";
                });
    }

    // ---- Guardar ----

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto producto,
                          BindingResult resultado,
                          @RequestParam("categoriaId") Long categoriaId,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo",
                    producto.getId() == null ? "Nuevo Producto" : "Editar Producto");
            model.addAttribute("categoriaIdSeleccionada", categoriaId);
            return "productos/formulario";
        }

        try {
            productoService.guardar(producto, categoriaId);
            redirectAttributes.addFlashAttribute("exito",
                    "Producto '" + producto.getNombre() + "' guardado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/productos";
    }

    // ---- Confirmación de eliminación ----

    @GetMapping("/eliminar/{id}")
    public String mostrarConfirmacion(@PathVariable Long id, Model model,
                                      RedirectAttributes redirectAttributes) {
        return productoService.buscarPorId(id)
                .map(prod -> {
                    model.addAttribute("producto", prod);
                    return "productos/confirmar-eliminar";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado.");
                    return "redirect:/productos";
                });
    }

    // ---- Eliminar ----

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("exito", "Producto eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    // ---- Filtrado por categoría y precio mínimo ----

    @GetMapping("/categoria/{categoriaId}/precio-mayor")
    public String filtrarPorCategoriaYPrecio(@PathVariable Long categoriaId,
                                              @RequestParam("minimo") BigDecimal minimo,
                                              Model model,
                                              RedirectAttributes redirectAttributes) {
        return categoriaService.buscarPorId(categoriaId)
                .map(cat -> {
                    model.addAttribute("productos",
                            productoService.buscarPorCategoriaConPrecioMayorA(categoriaId, minimo));
                    model.addAttribute("categoria", cat);
                    model.addAttribute("precioMinimo", minimo);
                    return "productos/filtrados";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Categoría no encontrada.");
                    return "redirect:/productos";
                });
    }
}
