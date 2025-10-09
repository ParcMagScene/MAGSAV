package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {

  private static Category map(ResultSet rs) throws SQLException {
    long id = rs.getLong("id");
    String nom = rs.getString("nom");
    Object pid = rs.getObject("parent_id");
    Long parentId = (pid == null) ? null : rs.getLong("parent_id");
    return new Category(id, nom, parentId);
  }

  public List<Category> findAll() {
    String sql = "SELECT id, nom, parent_id FROM categories ORDER BY nom";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<Category> out = new ArrayList<>();
      while (rs.next()) out.add(map(rs));
      return out;
    } catch (SQLException e) { throw new RuntimeException("findAll failed", e); }
  }

  public Optional<Category> findById(Long id) {
    if (id == null) return Optional.empty();
    String sql = "SELECT id, nom, parent_id FROM categories WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findById failed", e); }
  }

  public long insert(String nom, Long parentId) {
    String sql = "INSERT INTO categories(nom, parent_id) VALUES(?, ?)";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, nom);
      if (parentId == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, parentId);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getLong(1) : -1L; }
    } catch (SQLException e) { throw new RuntimeException("insert failed", e); }
  }

  public boolean update(Category c) {
    String sql = "UPDATE categories SET nom=?, parent_id=? WHERE id=?";
    try (Connection cx = DB.getConnection(); PreparedStatement ps = cx.prepareStatement(sql)) {
      ps.setString(1, c.nom());
      if (c.parentId() == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, c.parentId());
      ps.setLong(3, c.id());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("update failed", e); }
  }

  public boolean delete(long id) {
    String sql = "DELETE FROM categories WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("delete failed", e); }
  }

  public List<Category> findSubcategories(long parentId) {
    String sql = "SELECT id, nom, parent_id FROM categories WHERE parent_id=? ORDER BY nom";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, parentId);
      try (ResultSet rs = ps.executeQuery()) {
        List<Category> out = new ArrayList<>();
        while (rs.next()) out.add(map(rs));
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("findSubcategories failed", e); }
  }

  public long insertSubcategory(long parentId, String nom) {
    return insert(nom, parentId);
  }

  // Alias pour compatibilité avec vos contrôleurs existants
  public List<Category> findAllCategories() { return findAll(); }
  public long insertCategory(String nom) { return insert(nom, null); }

  /**
   * Récupère la hiérarchie complète d'une catégorie (jusqu'à la racine)
   * Par exemple: "Son > Système HF > Emetteur HF"
   */
  public String getFullHierarchy(long categoryId) {
    List<String> hierarchy = new ArrayList<>();
    Long currentId = categoryId;
    
    try (Connection conn = DB.getConnection()) {
      while (currentId != null) {
        String sql = "SELECT nom, parent_id FROM categories WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, currentId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
          hierarchy.add(0, rs.getString("nom")); // Ajouter au début
          long parentIdValue = rs.getLong("parent_id");
          currentId = rs.wasNull() ? null : parentIdValue;
        } else {
          break;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Erreur récupération hiérarchie catégorie", e);
    }
    
    return String.join(" > ", hierarchy);
  }

  /**
   * Récupère toutes les sous-sous-catégories d'une catégorie parente (niveau 3)
   */
  public List<Category> findSubSubcategories(long parentId) {
    List<Category> subSubcategories = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      // Requête pour récupérer les catégories de niveau 3 (parent du parent = parentId)
      String sql = """
        SELECT c3.id, c3.nom, c3.parent_id 
        FROM categories c3
        JOIN categories c2 ON c3.parent_id = c2.id
        WHERE c2.parent_id = ?
        ORDER BY c3.nom
      """;
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, parentId);
      ResultSet rs = stmt.executeQuery();
      
      while (rs.next()) {
        subSubcategories.add(new Category(
            rs.getLong("id"),
            rs.getString("nom"),
            rs.getLong("parent_id")
        ));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Erreur récupération sous-sous-catégories", e);
    }
    return subSubcategories;
  }

  /**
   * Récupère le niveau d'une catégorie dans la hiérarchie (1=principale, 2=sous-catégorie, 3=sous-sous-catégorie, etc.)
   */
  public int getCategoryLevel(long categoryId) {
    int level = 1;
    Long currentId = categoryId;
    
    try (Connection conn = DB.getConnection()) {
      while (currentId != null) {
        String sql = "SELECT parent_id FROM categories WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, currentId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
          long parentIdValue = rs.getLong("parent_id");
          if (rs.wasNull()) {
            break; // Racine atteinte
          } else {
            level++;
            currentId = parentIdValue;
          }
        } else {
          break;
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Erreur calcul niveau catégorie", e);
    }
    
    return level;
  }

  /**
   * Trouve une catégorie par nom dans toute la hiérarchie
   */
  public Optional<Category> findByName(String name) {
    if (name == null) return Optional.empty();
    String sql = "SELECT id, nom, parent_id FROM categories WHERE nom = ?";
    try (Connection conn = DB.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, name);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Erreur recherche catégorie par nom", e);
    }
  }

  /**
   * Record pour représenter la hiérarchie des catégories
   */
  public record CategoryHierarchy(String mainCategory, String subCategory, String subSubCategory) {}
  
  /**
   * Résout la hiérarchie des catégories à partir du nom stocké dans le produit
   */
  public CategoryHierarchy resolveCategoryHierarchy(String storedCategoryName) {
    if (storedCategoryName == null || storedCategoryName.trim().isEmpty()) {
      return new CategoryHierarchy("", "", "");
    }
    
    // Nettoyer le nom (enlever les émojis comme ⚡)
    String cleanName = cleanCategoryName(storedCategoryName);
    
    try {
      // Chercher d'abord une correspondance exacte avec le nom nettoyé
      Optional<Category> category = findByName(cleanName);
      
      // Si pas trouvé, essayer avec le nom original
      if (category.isEmpty()) {
        category = findByName(storedCategoryName);
      }
      
      if (category.isEmpty()) {
        // Si pas trouvé, retourner le nom original comme sous-catégorie
        return new CategoryHierarchy("", storedCategoryName, "");
      }
      
      return buildHierarchy(category.get());
      
    } catch (Exception e) {
      // En cas d'erreur, retourner le nom original
      return new CategoryHierarchy("", storedCategoryName, "");
    }
  }
  
  private String cleanCategoryName(String name) {
    return name.replaceAll("[⚡🔊🎵🎤🎧🔈🔉🔊]", "").trim();
  }
  
  private CategoryHierarchy buildHierarchy(Category category) {
    List<String> hierarchy = new ArrayList<>();
    Category current = category;
    
    // Remonter la hiérarchie
    while (current != null) {
      hierarchy.add(0, current.nom()); // Ajouter au début pour avoir l'ordre correct
      if (current.parentId() != null) {
        current = findById(current.parentId()).orElse(null);
      } else {
        current = null;
      }
    }
    
    // Construire le résultat selon la profondeur
    String mainCategory = hierarchy.size() > 0 ? hierarchy.get(0) : "";
    String subCategory = hierarchy.size() > 1 ? hierarchy.get(1) : "";
    String subSubCategory = hierarchy.size() > 2 ? hierarchy.get(2) : "";
    
    return new CategoryHierarchy(mainCategory, subCategory, subSubCategory);
  }
}