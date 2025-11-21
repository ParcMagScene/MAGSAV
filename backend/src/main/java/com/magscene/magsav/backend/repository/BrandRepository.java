package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des marques
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    /**
     * Trouve une marque par son nom (case insensitive)
     */
    Optional<Brand> findByNameIgnoreCase(String name);

    /**
     * Trouve toutes les marques actives
     */
    List<Brand> findByActiveTrue();

    /**
     * Trouve toutes les marques triées par nom
     */
    List<Brand> findAllByOrderByNameAsc();

    /**
     * Trouve les marques actives triées par nom
     */
    List<Brand> findByActiveTrueOrderByNameAsc();

    /**
     * Recherche par nom (contenant le texte)
     */
    List<Brand> findByNameContainingIgnoreCase(String name);

    /**
     * Recherche par nom ou description
     */
    @Query("SELECT b FROM Brand b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Brand> findByNameOrDescriptionContaining(@Param("search") String search);

    /**
     * Recherche avancée avec filtres
     */
    @Query("SELECT b FROM Brand b WHERE " +
           "(:active IS NULL OR b.active = :active) AND " +
           "(:country IS NULL OR LOWER(b.country) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
           "(:search IS NULL OR " +
           " LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Brand> findWithFilters(
        @Param("active") Boolean active,
        @Param("country") String country,
        @Param("search") String search
    );

    /**
     * Vérifie si une marque avec ce nom existe déjà (pour éviter les doublons)
     */
    boolean existsByNameIgnoreCase(String name);
}