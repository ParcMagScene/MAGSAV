package com.magsav.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magsav.exception.DatabaseException;
import com.magsav.repo.ProductRepository;
import com.magsav.util.AppLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service d'intégration pour le scraping d'images de produits
 * Interface entre l'application Java et le script Python de scraping
 */
public class ImageScrapingService {
    
    private final ProductRepository productRepository;
    private final Path scriptsPath;
    private final Path mediasPath;
    private final ObjectMapper objectMapper;
    private final ImageNormalizationService normalizationService;
    
    // Configuration par défaut
    private static final int DEFAULT_MAX_IMAGES = 3;
    private static final int SCRAPING_TIMEOUT_MINUTES = 5;
    private static final String PYTHON_EXECUTABLE = "python3";
    private static final String SCRAPER_SCRIPT = "image_scraper.py";
    
    public ImageScrapingService() {
        this.productRepository = new ProductRepository();
        this.scriptsPath = Paths.get("scripts");
        this.mediasPath = Paths.get("medias");
        this.objectMapper = new ObjectMapper();
        this.normalizationService = new ImageNormalizationService();
    }
    
    public ImageScrapingService(String customScriptsPath, String customMediasPath) {
        this.productRepository = new ProductRepository();
        this.scriptsPath = Paths.get(customScriptsPath);
        this.mediasPath = Paths.get(customMediasPath);
        this.objectMapper = new ObjectMapper();
        this.normalizationService = new ImageNormalizationService();
    }
    
    /**
     * Lance le scraping d'images pour un produit donné
     */
    public ScrapingResult scrapeProductImages(String productName, String manufacturer, 
                                            String productUid, int maxImages) {
        AppLogger.info("image_scraping", "Début scraping images: " + productName + " (" + manufacturer + ")");
        
        try {
            // Vérifier que le script Python existe
            Path scriptFile = scriptsPath.resolve(SCRAPER_SCRIPT);
            if (!Files.exists(scriptFile)) {
                throw new RuntimeException("Script de scraping introuvable: " + scriptFile);
            }
            
            // Créer un fichier temporaire pour la sortie JSON
            Path tempOutput = Files.createTempFile("scraping_result_", ".json");
            
            // Construire la commande Python
            List<String> command = buildScrapingCommand(productName, manufacturer, 
                                                      productUid, maxImages, tempOutput);
            
            // Exécuter le script
            ProcessResult processResult = executePythonScript(command);
            
            // Lire les résultats
            ScrapingResult result = parseScrapingResults(tempOutput, processResult);
            
            // Normaliser les images scrapées
            if (result.isSuccess() && !result.getScrapedImages().isEmpty()) {
                result = normalizeScrapedImages(result);
            }
            
            // Nettoyer le fichier temporaire
            Files.deleteIfExists(tempOutput);
            
            AppLogger.info("image_scraping", "Scraping terminé: " + result.getScrapedImages().size() + " images normalisées");
            return result;
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du scraping: " + e.getMessage());
            return new ScrapingResult(productName, manufacturer, productUid, 
                                    Collections.emptyList(), Arrays.asList(e.getMessage()), false);
        }
    }
    
    /**
     * Lance le scraping pour un produit à partir de son ID
     */
    public ScrapingResult scrapeProductImagesById(long productId, int maxImages) {
        try {
            Optional<ProductRepository.ProductRowDetailed> product = productRepository.findDetailedById(productId);
            
            if (product.isEmpty()) {
                throw new DatabaseException("Produit non trouvé: " + productId);
            }
            
            ProductRepository.ProductRowDetailed productData = product.get();
            return scrapeProductImages(productData.nom(), productData.fabricant(), 
                                     productData.uid(), maxImages);
            
        } catch (Exception e) {
            AppLogger.error("Erreur scraping produit ID " + productId + ": " + e.getMessage());
            return new ScrapingResult("", "", "", Collections.emptyList(), 
                                    Arrays.asList(e.getMessage()), false);
        }
    }
    
    /**
     * Lance le scraping pour tous les produits sans images
     */
    public CompletableFuture<BatchScrapingResult> scrapeAllProductsWithoutImages(int maxImages) {
        return CompletableFuture.supplyAsync(() -> {
            AppLogger.info("image_scraping", "Début scraping batch pour tous les produits");
            
            List<ScrapingResult> results = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            try {
                // Récupérer tous les produits
                List<ProductRepository.ProductRowDetailed> products = productRepository.findAllDetailed();
                
                int processed = 0;
                int successful = 0;
                
                for (ProductRepository.ProductRowDetailed product : products) {
                    try {
                        // Vérifier si le produit a déjà des images
                        if (hasExistingImages(product.uid())) {
                            AppLogger.info("image_scraping", "Produit " + product.uid() + " a déjà des images, ignoré");
                            continue;
                        }
                        
                        AppLogger.info("image_scraping", "Scraping produit " + (processed + 1) + "/" + products.size() + 
                                      ": " + product.nom());
                        
                        ScrapingResult result = scrapeProductImages(product.nom(), product.fabricant(), 
                                                                  product.uid(), maxImages);
                        results.add(result);
                        
                        if (result.isSuccess()) {
                            successful++;
                        }
                        
                        processed++;
                        
                        // Délai entre les scraping pour respecter les serveurs
                        Thread.sleep(3000);
                        
                    } catch (Exception e) {
                        String error = "Erreur produit " + product.uid() + ": " + e.getMessage();
                        errors.add(error);
                        AppLogger.error(error);
                    }
                }
                
                AppLogger.info("image_scraping", "Scraping batch terminé: " + successful + "/" + processed + " réussis");
                return new BatchScrapingResult(results, errors, processed, successful);
                
            } catch (Exception e) {
                AppLogger.error("Erreur scraping batch: " + e.getMessage());
                errors.add(e.getMessage());
                return new BatchScrapingResult(results, errors, 0, 0);
            }
        });
    }
    
    /**
     * Construit la commande pour exécuter le script Python
     */
    private List<String> buildScrapingCommand(String productName, String manufacturer, 
                                            String productUid, int maxImages, Path outputFile) {
        List<String> command = new ArrayList<>();
        command.add(PYTHON_EXECUTABLE);
        command.add(scriptsPath.resolve(SCRAPER_SCRIPT).toString());
        command.add("--product");
        command.add(productName);
        
        if (manufacturer != null && !manufacturer.trim().isEmpty()) {
            command.add("--manufacturer");
            command.add(manufacturer.trim());
        }
        
        if (productUid != null && !productUid.trim().isEmpty()) {
            command.add("--uid");
            command.add(productUid.trim());
        }
        
        command.add("--max-images");
        command.add(String.valueOf(maxImages));
        command.add("--medias-path");
        command.add(mediasPath.toString());
        command.add("--output-json");
        command.add(outputFile.toString());
        
        return command;
    }
    
    /**
     * Exécute le script Python
     */
    private ProcessResult executePythonScript(List<String> command) throws IOException, InterruptedException {
        AppLogger.info("image_scraping", "Exécution: " + String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        // Lire la sortie du processus
        StringBuilder output = new StringBuilder();
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                output.append(line).append("\n");
                AppLogger.info("python_output", line);
            }
        }
        
        // Attendre la fin du processus avec timeout
        boolean finished = process.waitFor(SCRAPING_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Timeout du script Python après " + SCRAPING_TIMEOUT_MINUTES + " minutes");
        }
        
        return new ProcessResult(process.exitValue(), output.toString());
    }
    
    /**
     * Parse les résultats du scraping depuis le fichier JSON
     */
    private ScrapingResult parseScrapingResults(Path outputFile, ProcessResult processResult) 
            throws IOException {
        
        if (processResult.exitCode != 0) {
            return new ScrapingResult("", "", "", Collections.emptyList(), 
                                    Arrays.asList("Script Python failed: " + processResult.output), false);
        }
        
        if (!Files.exists(outputFile)) {
            return new ScrapingResult("", "", "", Collections.emptyList(), 
                                    Arrays.asList("Fichier de résultats introuvable"), false);
        }
        
        try {
            JsonNode jsonResult = objectMapper.readTree(outputFile.toFile());
            
            String productName = jsonResult.get("product_name").asText();
            String manufacturer = jsonResult.get("manufacturer").asText();
            String productUid = jsonResult.get("product_uid").asText();
            boolean success = jsonResult.get("success").asBoolean();
            
            // Parser les images scrapées
            List<ScrapedImage> scrapedImages = new ArrayList<>();
            JsonNode imagesNode = jsonResult.get("scraped_images");
            if (imagesNode != null && imagesNode.isArray()) {
                for (JsonNode imageNode : imagesNode) {
                    String filename = imageNode.get("filename").asText();
                    String sourceUrl = imageNode.get("source_url").asText();
                    String source = imageNode.get("source").asText();
                    scrapedImages.add(new ScrapedImage(filename, sourceUrl, source));
                }
            }
            
            // Parser les erreurs
            List<String> errors = new ArrayList<>();
            JsonNode errorsNode = jsonResult.get("errors");
            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    errors.add(errorNode.asText());
                }
            }
            
            return new ScrapingResult(productName, manufacturer, productUid, scrapedImages, errors, success);
            
        } catch (Exception e) {
            return new ScrapingResult("", "", "", Collections.emptyList(), 
                                    Arrays.asList("Erreur parsing JSON: " + e.getMessage()), false);
        }
    }
    
    /**
     * Normalise les images scrapées
     */
    private ScrapingResult normalizeScrapedImages(ScrapingResult originalResult) {
        AppLogger.info("image_scraping", "Début normalisation des images pour: " + originalResult.getProductUid());
        
        List<ScrapedImage> normalizedImages = new ArrayList<>();
        List<String> normalizationErrors = new ArrayList<>(originalResult.getErrors());
        
        Path scrapedPath = mediasPath.resolve("scraped");
        
        for (ScrapedImage image : originalResult.getScrapedImages()) {
            try {
                Path originalImagePath = scrapedPath.resolve(image.filename());
                
                if (!Files.exists(originalImagePath)) {
                    String error = "Image scrapée introuvable: " + image.filename();
                    normalizationErrors.add(error);
                    AppLogger.error(error);
                    continue;
                }
                
                // Générer un nom de fichier basé sur le produit et l'index
                String targetName = originalResult.getProductUid() + "_" + 
                                  normalizedImages.size() + "_" + 
                                  image.source().toLowerCase();
                
                // Normaliser l'image
                String normalizedFileName = normalizationService.normalizeImage(originalImagePath, targetName);
                
                // Créer un nouveau ScrapedImage avec le fichier normalisé
                normalizedImages.add(new ScrapedImage(
                    normalizedFileName,
                    image.sourceUrl(),
                    image.source()
                ));
                
                AppLogger.info("image_scraping", "Image normalisée: " + image.filename() + 
                              " -> " + normalizedFileName);
                
            } catch (IOException e) {
                String error = "Erreur normalisation " + image.filename() + ": " + e.getMessage();
                normalizationErrors.add(error);
                AppLogger.error(error);
                // Conserver l'image originale en cas d'erreur
                normalizedImages.add(image);
            } catch (Exception e) {
                String error = "Erreur inattendue normalisation " + image.filename() + ": " + e.getMessage();
                normalizationErrors.add(error);
                AppLogger.error(error);
                // Conserver l'image originale en cas d'erreur
                normalizedImages.add(image);
            }
        }
        
        AppLogger.info("image_scraping", "Normalisation terminée: " + normalizedImages.size() + 
                      " images, " + (normalizationErrors.size() - originalResult.getErrors().size()) + " erreurs de normalisation");
        
        return new ScrapingResult(
            originalResult.getProductName(),
            originalResult.getManufacturer(),
            originalResult.getProductUid(),
            normalizedImages,
            normalizationErrors,
            originalResult.isSuccess() && !normalizedImages.isEmpty()
        );
    }
    
    /**
     * Vérifie si un produit a déjà des images
     */
    private boolean hasExistingImages(String productUid) {
        if (productUid == null || productUid.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Vérifier dans le dossier photos
            Path photosPath = mediasPath.resolve("photos");
            if (Files.exists(photosPath)) {
                return Files.list(photosPath)
                    .anyMatch(file -> file.getFileName().toString().startsWith(productUid));
            }
            
            // Vérifier dans le dossier scraped
            Path scrapedPath = mediasPath.resolve("scraped");
            if (Files.exists(scrapedPath)) {
                return Files.list(scrapedPath)
                    .anyMatch(file -> file.getFileName().toString().startsWith(productUid));
            }
            
        } catch (IOException e) {
            AppLogger.error("Erreur vérification images existantes: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Résultat d'une exécution de processus
     */
    private record ProcessResult(int exitCode, String output) {}
    
    /**
     * Image scrapée
     */
    public record ScrapedImage(String filename, String sourceUrl, String source) {}
    
    /**
     * Résultat du scraping pour un produit
     */
    public static class ScrapingResult {
        private final String productName;
        private final String manufacturer;
        private final String productUid;
        private final List<ScrapedImage> scrapedImages;
        private final List<String> errors;
        private final boolean success;
        
        public ScrapingResult(String productName, String manufacturer, String productUid,
                            List<ScrapedImage> scrapedImages, List<String> errors, boolean success) {
            this.productName = productName;
            this.manufacturer = manufacturer;
            this.productUid = productUid;
            this.scrapedImages = scrapedImages;
            this.errors = errors;
            this.success = success;
        }
        
        // Getters
        public String getProductName() { return productName; }
        public String getManufacturer() { return manufacturer; }
        public String getProductUid() { return productUid; }
        public List<ScrapedImage> getScrapedImages() { return scrapedImages; }
        public List<String> getErrors() { return errors; }
        public boolean isSuccess() { return success; }
        
        @Override
        public String toString() {
            return String.format("ScrapingResult{product='%s %s', images=%d, success=%s}", 
                               manufacturer, productName, scrapedImages.size(), success);
        }
    }
    
    /**
     * Résultat du scraping en batch
     */
    public static class BatchScrapingResult {
        private final List<ScrapingResult> results;
        private final List<String> errors;
        private final int totalProcessed;
        private final int totalSuccessful;
        
        public BatchScrapingResult(List<ScrapingResult> results, List<String> errors, 
                                 int totalProcessed, int totalSuccessful) {
            this.results = results;
            this.errors = errors;
            this.totalProcessed = totalProcessed;
            this.totalSuccessful = totalSuccessful;
        }
        
        // Getters
        public List<ScrapingResult> getResults() { return results; }
        public List<String> getErrors() { return errors; }
        public int getTotalProcessed() { return totalProcessed; }
        public int getTotalSuccessful() { return totalSuccessful; }
        public double getSuccessRate() { 
            return totalProcessed > 0 ? (double) totalSuccessful / totalProcessed * 100 : 0; 
        }
        
        @Override
        public String toString() {
            return String.format("BatchScrapingResult{processed=%d, successful=%d, rate=%.1f%%}", 
                               totalProcessed, totalSuccessful, getSuccessRate());
        }
    }
}