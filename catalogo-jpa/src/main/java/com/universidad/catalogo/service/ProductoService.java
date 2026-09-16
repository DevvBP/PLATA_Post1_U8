package com.universidad.catalogo.service;

import com.universidad.catalogo.model.Categoria;
import com.universidad.catalogo.model.Producto;
import com.universidad.catalogo.repository.CategoriaRepository;
import com.universidad.catalogo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAllConCategoria();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Persiste o actualiza un producto asociándole la categoría correspondiente.
     *
     * @param producto    entidad a guardar
     * @param categoriaId ID de la categoría a la que pertenece el producto
     * @return entidad persistida
     * @throws IllegalArgumentException si la categoría no existe
     */
    public Producto guardar(Producto producto, Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada con ID: " + categoriaId));
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoriaConPrecioMayorA(Long categoriaId, BigDecimal precioMinimo) {
        return productoRepository.buscarPorCategoriaConPrecioMayorA(categoriaId, precioMinimo);
    }
}
