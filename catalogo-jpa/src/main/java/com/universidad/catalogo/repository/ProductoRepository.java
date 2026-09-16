package com.universidad.catalogo.repository;

import com.universidad.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Recupera todos los productos junto con su categoría usando JOIN FETCH
     * para evitar el problema N+1 de consultas.
     */
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria")
    List<Producto> findAllConCategoria();

    /**
     * Busca productos de una categoría específica cuyo precio sea mayor al mínimo indicado.
     * Usa JOIN FETCH para traer la categoría en la misma consulta, ordenado por precio DESC.
     */
    @Query("""
            SELECT p FROM Producto p
            JOIN FETCH p.categoria c
            WHERE c.id = :categoriaId
              AND p.precio > :precioMinimo
            ORDER BY p.precio DESC
            """)
    List<Producto> buscarPorCategoriaConPrecioMayorA(
            @Param("categoriaId") Long categoriaId,
            @Param("precioMinimo") BigDecimal precioMinimo
    );
}
