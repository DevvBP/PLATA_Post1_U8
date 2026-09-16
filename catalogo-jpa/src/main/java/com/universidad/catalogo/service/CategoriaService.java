package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    /**
     * Guarda o actualiza una categoría.
     * Valida de forma explícita que no exista otra categoría con el mismo nombre
     * (ignorando mayúsculas/minúsculas) antes de persistir.
     *
     * @param categoria objeto a guardar
     * @throws IllegalArgumentException si el nombre ya está registrado en otra categoría
     */
    public Categoria guardar(Categoria categoria) {
        Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCase(categoria.getNombre().trim());

        if (existente.isPresent() && !existente.get().getId().equals(categoria.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoría con el nombre '" + categoria.getNombre() + "'."
            );
        }

        categoria.setNombre(categoria.getNombre().trim());
        return categoriaRepository.save(categoria);
    }

    /**
     * Elimina una categoría por ID.
     * Lanza {@link IllegalStateException} si la categoría tiene productos asociados,
     * garantizando la integridad referencial a nivel de aplicación.
     *
     * @param id identificador de la categoría
     * @throws IllegalStateException    si la categoría posee productos
     * @throws IllegalArgumentException si la categoría no existe
     */
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        if (!categoria.getProductos().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar la categoría '" + categoria.getNombre() +
                    "' porque tiene " + categoria.getProductos().size() + " producto(s) asociado(s)."
            );
        }

        categoriaRepository.delete(categoria);
    }
}
