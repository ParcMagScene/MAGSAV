package com.magsav.web.controller;

import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {
  private final CategoryRepository categoryRepo;

  @Autowired
  public CategoryController(DataSource ds) {
    this.categoryRepo = new CategoryRepository(ds);
  }

  @GetMapping
  public String list(Model model) {
    try {
      List<Category> roots = categoryRepo.findRoots();
      model.addAttribute("roots", roots);
      model.addAttribute("all", categoryRepo.findAll());
    } catch (SQLException e) {
      model.addAttribute("error", "Erreur lors du chargement des catégories: " + e.getMessage());
    }
    return "categories";
  }

  @PostMapping
  public String create(
      @RequestParam String name,
      @RequestParam(required = false) Long parentId,
      RedirectAttributes redirect) {
    try {
      categoryRepo.save(new Category(null, name != null ? name.trim() : null, parentId));
      redirect.addFlashAttribute("success", "Catégorie créée");
    } catch (SQLException e) {
      redirect.addFlashAttribute("error", "Impossible de créer la catégorie: " + e.getMessage());
    }
    return "redirect:/categories";
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes redirect) {
    try {
      categoryRepo.delete(id);
      redirect.addFlashAttribute("success", "Catégorie supprimée");
    } catch (SQLException e) {
      redirect.addFlashAttribute("error", "Suppression impossible: " + e.getMessage());
    }
    return "redirect:/categories";
  }
}
