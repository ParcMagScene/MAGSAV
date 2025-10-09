package com.magsav.service;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.ProductRepository.ProductRowDetailed;
import com.magsav.repo.InterventionRepository;
import com.magsav.model.InterventionRow;
import com.magsav.util.AppLogger;


import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service d'export simple pour HTML et impression
 */
public class SimpleExportService {
    
    private final ProductRepository productRepo;
    private final InterventionRepository interventionRepo;
    
    private Consumer<String> logCallback;
    private Consumer<Double> progressCallback;
    
    public SimpleExportService(ProductRepository productRepo, InterventionRepository interventionRepo) {
        this.productRepo = productRepo;
        this.interventionRepo = interventionRepo;
    }
    
    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }
    
    public void setProgressCallback(Consumer<Double> callback) {
        this.progressCallback = callback;
    }
    
    private void log(String message) {
        AppLogger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
    
    private void updateProgress(double progress) {
        if (progressCallback != null) {
            progressCallback.accept(progress);
        }
    }
    
    /**
     * Exporte un produit en HTML
     */
    public CompletableFuture<Path> exportProductToHtml(long productId, Path outputDir) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📄 Export HTML du produit ID: " + productId);
                updateProgress(0.1);
                
                ProductRowDetailed product = productRepo.findDetailedById(productId)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + productId));
                updateProgress(0.3);
                
                List<InterventionRow> interventions = interventionRepo.findByProductId(productId);
                updateProgress(0.6);
                
                String htmlContent = generateProductHtml(product, interventions);
                
                String filename = sanitizeFilename(product.nom()) + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
                Path htmlPath = outputDir.resolve(filename);
                Files.createDirectories(outputDir);
                Files.writeString(htmlPath, htmlContent);
                updateProgress(1.0);
                
                log("✅ Export HTML terminé: " + htmlPath);
                return htmlPath;
                
            } catch (Exception e) {
                log("❌ Erreur export HTML: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Exporte tous les produits en HTML
     */
    public CompletableFuture<Path> exportAllProductsToHtml(Path outputDir) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📄 Export HTML de tous les produits");
                updateProgress(0.1);
                
                List<ProductRowDetailed> allProducts = productRepo.findAllDetailed();
                updateProgress(0.2);
                
                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append(generateHtmlHeader("Export Complet MAGSAV"));
                
                htmlContent.append("<h1>Produits (").append(allProducts.size()).append(")</h1>");
                htmlContent.append("<div class='summary'>");
                htmlContent.append("<p>Export généré le ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))).append("</p>");
                htmlContent.append("<p>Nombre total de produits: ").append(allProducts.size()).append("</p>");
                htmlContent.append("</div>");
                
                for (int i = 0; i < allProducts.size(); i++) {
                    ProductRowDetailed product = allProducts.get(i);
                    List<InterventionRow> interventions = interventionRepo.findByProductId(product.id());
                    htmlContent.append(generateProductHtml(product, interventions));
                    htmlContent.append("<hr class='product-separator'/>");
                    
                    updateProgress(0.2 + (0.7 * (i + 1) / allProducts.size()));
                }
                
                htmlContent.append(generateHtmlFooter());
                
                String filename = "magsav_export_complet_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
                Path htmlPath = outputDir.resolve(filename);
                Files.createDirectories(outputDir);
                Files.writeString(htmlPath, htmlContent.toString());
                updateProgress(1.0);
                
                log("✅ Export HTML complet terminé: " + htmlPath);
                return htmlPath;
                
            } catch (Exception e) {
                log("❌ Erreur export HTML complet: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Génère un rapport de stock en HTML
     */
    public CompletableFuture<Path> exportStockReport(Path outputDir) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log("📊 Génération du rapport de stock");
                updateProgress(0.1);
                
                List<ProductRowDetailed> allProducts = productRepo.findAllDetailed();
                updateProgress(0.3);
                
                StringBuilder htmlContent = new StringBuilder();
                htmlContent.append(generateHtmlHeader("Rapport de Stock MAGSAV"));
                
                // Statistiques générales
                htmlContent.append("<div class='stock-summary'>");
                htmlContent.append("<h2>Résumé du Stock</h2>");
                htmlContent.append("<div class='stats-grid'>");
                htmlContent.append("<div class='stat-item'>");
                htmlContent.append("<h3>").append(allProducts.size()).append("</h3>");
                htmlContent.append("<p>Total Produits</p>");
                htmlContent.append("</div>");
                
                long available = allProducts.stream().filter(p -> "Disponible".equalsIgnoreCase(p.situation())).count();
                htmlContent.append("<div class='stat-item'>");
                htmlContent.append("<h3>").append(available).append("</h3>");
                htmlContent.append("<p>Disponibles</p>");
                htmlContent.append("</div>");
                
                long inUse = allProducts.stream().filter(p -> "En utilisation".equalsIgnoreCase(p.situation())).count();
                htmlContent.append("<div class='stat-item'>");
                htmlContent.append("<h3>").append(inUse).append("</h3>");
                htmlContent.append("<p>En utilisation</p>");
                htmlContent.append("</div>");
                
                long maintenance = allProducts.stream().filter(p -> "Maintenance".equalsIgnoreCase(p.situation())).count();
                htmlContent.append("<div class='stat-item'>");
                htmlContent.append("<h3>").append(maintenance).append("</h3>");
                htmlContent.append("<p>Maintenance</p>");
                htmlContent.append("</div>");
                
                htmlContent.append("</div>");
                htmlContent.append("</div>");
                updateProgress(0.6);
                
                // Tableau détaillé
                htmlContent.append("<h2>Détail du Stock</h2>");
                htmlContent.append("<table class='stock-table'>");
                htmlContent.append("<thead><tr>");
                htmlContent.append("<th>Nom</th><th>N° Série</th><th>Fabricant</th>");
                htmlContent.append("<th>Catégorie</th><th>Situation</th><th>Prix</th>");
                htmlContent.append("</tr></thead>");
                htmlContent.append("<tbody>");
                
                for (ProductRowDetailed product : allProducts) {
                    htmlContent.append("<tr>");
                    htmlContent.append("<td>").append(escapeHtml(product.nom())).append("</td>");

                    htmlContent.append("<td>").append(escapeHtml(product.sn())).append("</td>");
                    htmlContent.append("<td>").append(escapeHtml(product.fabricant())).append("</td>");
                    htmlContent.append("<td>").append(escapeHtml(product.category())).append("</td>");
                    htmlContent.append("<td class='situation-").append(sanitizeClassName(product.situation())).append("'>");
                    htmlContent.append(escapeHtml(product.situation())).append("</td>");
                    htmlContent.append("<td>").append(escapeHtml(product.prix())).append("</td>");
                    htmlContent.append("</tr>");
                }
                
                htmlContent.append("</tbody></table>");
                updateProgress(0.9);
                
                htmlContent.append(generateHtmlFooter());
                
                String filename = "magsav_rapport_stock_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
                Path htmlPath = outputDir.resolve(filename);
                Files.createDirectories(outputDir);
                Files.writeString(htmlPath, htmlContent.toString());
                updateProgress(1.0);
                
                log("✅ Rapport de stock généré: " + htmlPath);
                return htmlPath;
                
            } catch (Exception e) {
                log("❌ Erreur génération rapport: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    private String generateProductHtml(ProductRowDetailed product, List<InterventionRow> interventions) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div class='product-card'>");
        html.append("<h2>").append(escapeHtml(product.nom())).append("</h2>");
        html.append("<table class='product-info'>");

        html.append("<tr><td><strong>N° Série:</strong></td><td>").append(escapeHtml(product.sn())).append("</td></tr>");
        html.append("<tr><td><strong>Fabricant:</strong></td><td>").append(escapeHtml(product.fabricant())).append("</td></tr>");
        html.append("<tr><td><strong>Catégorie:</strong></td><td>").append(escapeHtml(product.category())).append("</td></tr>");
        html.append("<tr><td><strong>Sous-catégorie:</strong></td><td>").append(escapeHtml(product.subcategory())).append("</td></tr>");
        html.append("<tr><td><strong>Situation:</strong></td><td>").append(escapeHtml(product.situation())).append("</td></tr>");
        html.append("<tr><td><strong>Prix:</strong></td><td>").append(escapeHtml(product.prix())).append("</td></tr>");
        html.append("<tr><td><strong>Date d'achat:</strong></td><td>").append(escapeHtml(product.dateAchat())).append("</td></tr>");
        html.append("<tr><td><strong>Client:</strong></td><td>").append(escapeHtml(product.client())).append("</td></tr>");
        html.append("</table>");
        
        if (product.description() != null && !product.description().trim().isEmpty()) {
            html.append("<div class='description'>");
            html.append("<h4>Description:</h4>");
            html.append("<p>").append(escapeHtml(product.description())).append("</p>");
            html.append("</div>");
        }
        
        if (!interventions.isEmpty()) {
            html.append("<h3>Historique des interventions (").append(interventions.size()).append(")</h3>");
            html.append("<table class='interventions'>");
            html.append("<tr><th>Date entrée</th><th>Statut</th><th>Panne</th><th>Date sortie</th></tr>");
            for (InterventionRow inter : interventions) {
                html.append("<tr>");
                html.append("<td>").append(escapeHtml(inter.dateEntree())).append("</td>");
                html.append("<td>").append(escapeHtml(inter.statut())).append("</td>");
                html.append("<td>").append(escapeHtml(inter.panne())).append("</td>");
                html.append("<td>").append(inter.dateSortie() != null ? escapeHtml(inter.dateSortie()) : "-").append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
        }
        
        html.append("</div>");
        return html.toString();
    }
    
    private String generateHtmlHeader(String title) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; color: #333; }
                    .product-card { margin-bottom: 30px; border: 1px solid #ccc; padding: 20px; border-radius: 8px; }
                    .summary { background: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                    .stock-summary { background: linear-gradient(135deg, #007cba, #0099d6); color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; }
                    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 20px; margin-top: 15px; }
                    .stat-item { text-align: center; background: rgba(255,255,255,0.1); padding: 15px; border-radius: 8px; }
                    .stat-item h3 { margin: 0; font-size: 2em; }
                    .stat-item p { margin: 5px 0 0 0; opacity: 0.9; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 10px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #007cba; color: white; }
                    .product-info td:first-child { width: 150px; font-weight: bold; background: #f8f9fa; }
                    .stock-table th { position: sticky; top: 0; z-index: 10; }
                    .stock-table { margin-top: 15px; font-size: 0.9em; }
                    .situation-disponible { background: #d4edda; color: #155724; }
                    .situation-en-utilisation { background: #fff3cd; color: #856404; }
                    .situation-maintenance { background: #f8d7da; color: #721c24; }
                    .product-separator { margin: 30px 0; border: 2px solid #007cba; }
                    h1 { color: #007cba; border-bottom: 3px solid #007cba; padding-bottom: 10px; }
                    h2 { color: #007cba; margin-top: 0; }
                    h3 { color: #555; }
                    .description { background: #f8f9fa; padding: 10px; border-left: 4px solid #007cba; margin: 10px 0; }
                    @media print {
                        .product-card { page-break-inside: avoid; }
                        .stock-summary { print-color-adjust: exact; }
                    }
                </style>
            </head>
            <body>
            <h1>%s</h1>
            <p><em>Généré le %s</em></p>
            """.formatted(title, title, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")));
    }
    
    private String generateHtmlFooter() {
        return """
            <footer style='margin-top: 50px; padding-top: 20px; border-top: 1px solid #ddd; text-align: center; color: #666; font-size: 0.9em;'>
                <p>Généré par MAGSAV - Système de gestion de matériel audio/vidéo</p>
            </footer>
            </body></html>""";
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    private String sanitizeFilename(String filename) {
        if (filename == null) return "export";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }
    
    private String sanitizeClassName(String str) {
        if (str == null) return "unknown";
        return str.toLowerCase()
                  .replace(" ", "-")
                  .replaceAll("[^a-zA-Z0-9-]", "");
    }
}