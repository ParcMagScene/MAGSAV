package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Brand;
import com.magscene.magsav.backend.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des marques
 */
@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    @Autowired
    private BrandRepository brandRepository;

    /**
     * Récupère toutes les marques
     */
    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String search) {
        
        List<Brand> brands;
        
        if (active != null || country != null || search != null) {
            brands = brandRepository.findWithFilters(active, country, search);
        } else {
            brands = brandRepository.findAllByOrderByNameAsc();
        }
        
        return ResponseEntity.ok(brands);
    }

    /**
     * Récupère toutes les marques actives (pour les ComboBox)
     */
    @GetMapping("/active")
    public ResponseEntity<List<Brand>> getActiveBrands() {
        List<Brand> brands = brandRepository.findByActiveTrueOrderByNameAsc();
        return ResponseEntity.ok(brands);
    }

    /**
     * Récupère une marque par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Long id) {
        Optional<Brand> brand = brandRepository.findById(id);
        return brand.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recherche des marques par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<Brand>> searchBrands(@RequestParam String query) {
        List<Brand> brands = brandRepository.findByNameOrDescriptionContaining(query);
        return ResponseEntity.ok(brands);
    }

    /**
     * Crée une nouvelle marque
     */
    @PostMapping
    public ResponseEntity<?> createBrand(@Valid @RequestBody Brand brand) {
        try {
            // Vérifier si le nom existe déjà
            if (brandRepository.existsByNameIgnoreCase(brand.getName())) {
                return ResponseEntity.badRequest()
                    .body("Une marque avec ce nom existe déjà");
            }

            Brand savedBrand = brandRepository.save(brand);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBrand);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erreur lors de la création de la marque : " + e.getMessage());
        }
    }

    /**
     * Met à jour une marque existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrand(@PathVariable Long id, @Valid @RequestBody Brand brandDetails) {
        try {
            Optional<Brand> optionalBrand = brandRepository.findById(id);
            
            if (optionalBrand.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Brand brand = optionalBrand.get();

            // Vérifier si le nouveau nom existe déjà (sauf pour cette marque)
            if (!brand.getName().equalsIgnoreCase(brandDetails.getName()) &&
                brandRepository.existsByNameIgnoreCase(brandDetails.getName())) {
                return ResponseEntity.badRequest()
                    .body("Une marque avec ce nom existe déjà");
            }

            // Mettre à jour les champs
            brand.setName(brandDetails.getName());
            brand.setDescription(brandDetails.getDescription());
            brand.setLogoUrl(brandDetails.getLogoUrl());
            brand.setCountry(brandDetails.getCountry());
            brand.setWebsite(brandDetails.getWebsite());
            brand.setActive(brandDetails.getActive());

            Brand updatedBrand = brandRepository.save(brand);
            return ResponseEntity.ok(updatedBrand);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erreur lors de la mise à jour de la marque : " + e.getMessage());
        }
    }

    /**
     * Supprime une marque
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBrand(@PathVariable Long id) {
        try {
            if (!brandRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            brandRepository.deleteById(id);
            return ResponseEntity.ok().body("Marque supprimée avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erreur lors de la suppression de la marque : " + e.getMessage());
        }
    }

    /**
     * Active/désactive une marque
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleBrandActive(@PathVariable Long id) {
        try {
            Optional<Brand> optionalBrand = brandRepository.findById(id);
            
            if (optionalBrand.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Brand brand = optionalBrand.get();
            brand.setActive(!brand.getActive());
            
            Brand updatedBrand = brandRepository.save(brand);
            return ResponseEntity.ok(updatedBrand);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Erreur lors de la modification du statut : " + e.getMessage());
        }
    }
}