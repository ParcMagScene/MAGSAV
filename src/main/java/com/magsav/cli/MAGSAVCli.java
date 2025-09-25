package com.magsav.cli;

import com.magsav.db.DB;
import com.magsav.config.Config;
import com.magsav.imports.CSVImporter;
import com.magsav.model.DossierSAV;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.service.SAVService;
import com.zaxxer.hikari.HikariDataSource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "magsav",
    description = "Système de gestion SAV MAGSAV",
    subcommands = {
        MAGSAVCli.ImportCommand.class,
        MAGSAVCli.ListerCommand.class,
        MAGSAVCli.RechercheCommand.class,
        MAGSAVCli.StatutCommand.class,
        MAGSAVCli.EtiquetteCommand.class,
        CommandLine.HelpCommand.class
    })
public class MAGSAVCli implements Runnable {
    
    @Option(names = {"-db", "--database"}, description = "Chemin vers la base SQLite", 
            defaultValue = "magsav.db")
    private String dbPath;
    
    @Option(names = {"-o", "--output"}, description = "Dossier de sortie pour QR/PDF", 
            defaultValue = "out")
    private String outputDir;
    
    @Option(names = {"-c", "--config"}, description = "Fichier de configuration application.yml")
    private String configFile;

    // Valeurs issues du fichier de config (après merge avec options CLI)
    private String resolvedDbUrl; // ex: jdbc:sqlite:magsav.db
    private String resolvedOutputDir;
    private List<String> resolvedDatePatterns = new ArrayList<>();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MAGSAVCli()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public void run() {
        // Chargement et fusion configuration quand uniquement commande racine
        try {
            loadAndMergeConfig();
        } catch (Exception e) {
            System.err.println("⚠ Impossible de charger la config: " + e.getMessage());
        }
        System.out.println("MAGSAV 1.1 - Tapez 'help' pour voir les commandes disponibles");
    }

    private void loadAndMergeConfig() throws Exception {
        Path cfgPath = configFile != null ? Path.of(configFile) : Path.of("application.yml");
        Config cfg = Config.load(cfgPath);

        // DB URL: priorité option CLI (-db) > config > défaut
        String cfgDb = cfg.get("app.database.url", "jdbc:sqlite:magsav.db");
        if (!cfgDb.startsWith("jdbc:")) {
            // Si simple chemin fourni on suppose sqlite
            cfgDb = "jdbc:sqlite:" + cfgDb;
        }
        if (!dbPath.equals("magsav.db")) { // l'utilisateur a passé -db
            resolvedDbUrl = dbPath.startsWith("jdbc:") ? dbPath : "jdbc:sqlite:" + dbPath;
        } else {
            resolvedDbUrl = cfgDb;
        }

        // Output directory
        String cfgOut = cfg.get("app.output.directory", outputDir);
        if (!outputDir.equals("out")) {
            resolvedOutputDir = outputDir;
        } else {
            resolvedOutputDir = cfgOut;
        }

        // Date formats (liste séparée par virgule simple, ex dans application.yml)
        String cfgFormats = cfg.get("app.import.dateFormats", "dd/MM/yyyy,yyyy-MM-dd,dd-MM-yyyy,d/M/yyyy");
        for (String p : cfgFormats.split("[,;]")) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) resolvedDatePatterns.add(trimmed);
        }
        if (resolvedDatePatterns.isEmpty()) {
            resolvedDatePatterns.add("dd/MM/yyyy");
        }
    }
    
        @Command(name = "import", description = "Importer des données depuis un fichier CSV")
    static class ImportCommand implements Runnable {
        @CommandLine.ParentCommand
        private MAGSAVCli parent;
        @Option(names = {"--clients"}, description = "Import d'un fichier de clients")
        private String clientsFile;
        
        @Option(names = {"--fournisseurs"}, description = "Import d'un fichier de fournisseurs") 
        private String fournisseursFile;
        
        @Option(names = {"--dossiers-sav"}, description = "Import du fichier principal SAV (PRODUIT, N° DE SERIE, PROPRIETAIRE, PANNE, STATUT, DETECTEUR, DATE ENTREE, DATE SORTIE)")
        private String dossiersSavFile;

        @Override
        public void run() {
            String dbUrl = parent != null && parent.resolvedDbUrl != null ? parent.resolvedDbUrl : "jdbc:sqlite:magsav.db";
            try (HikariDataSource ds = DB.init(dbUrl)) {
                DB.migrate(ds);
                List<String> patterns = parent != null ? parent.resolvedDatePatterns : List.of("dd/MM/yyyy","yyyy-MM-dd");
                CSVImporter importer = new CSVImporter(ds, patterns);
                
                if (clientsFile != null) {
                    System.out.println("Import des clients...");
                    importer.importClients(Path.of(clientsFile));
                    System.out.println("✓ Clients importés");
                }
                
                if (fournisseursFile != null) {
                    System.out.println("Import des fournisseurs...");
                    importer.importFournisseurs(Path.of(fournisseursFile));
                    System.out.println("✓ Fournisseurs importés");
                }
                
                if (dossiersSavFile != null) {
                    System.out.println("Import des dossiers SAV...");
                    int imported = importer.importDossiersSAV(Path.of(dossiersSavFile));
                    System.out.printf("✓ %d dossiers SAV importés%n", imported);
                }
                
                if (clientsFile == null && fournisseursFile == null && dossiersSavFile == null) {
                    System.err.println("Aucun fichier spécifié. Utilisez --clients, --fournisseurs ou --dossiers-sav");
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de l'import : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @Command(name = "nouveau", description = "Créer un nouveau dossier SAV")
    void nouveauDossier(
            @Option(names = {"-n", "--nom"}, required = true) String nom,
            @Option(names = {"-p", "--prenom"}) String prenom,
            @Option(names = {"-e", "--email"}, required = true) String email,
            @Option(names = {"-t", "--tel"}) String tel,
            @Option(names = {"-a", "--adresse"}) String adresse,
            @Option(names = {"-m", "--marque"}, required = true) String marque,
            @Option(names = {"--modele"}, required = true) String modele,
            @Option(names = {"-s", "--sn"}, required = true) String sn,
            @Option(names = {"--accessoires"}) String accessoires,
            @Option(names = {"--symptome"}, required = true) String symptome,
            @Option(names = {"-c", "--commentaire"}) String commentaire
    ) {
        try {
            if (resolvedDbUrl == null) loadAndMergeConfig();
        } catch (Exception e) {
            System.err.println("⚠ Config non chargée: " + e.getMessage());
        }
        String dbUrlToUse = resolvedDbUrl != null ? resolvedDbUrl : (dbPath.startsWith("jdbc:") ? dbPath : "jdbc:sqlite:" + dbPath);
        try (var ds = DB.init(dbUrlToUse)) {
            DB.migrate(ds);
            var savService = new SAVService(ds);
            
            var dossierSAV = savService.creerDossierSAV(
                nom, prenom != null ? prenom : "", email, tel, adresse,
                marque, modele, sn, accessoires != null ? accessoires : "",
                symptome, commentaire != null ? commentaire : ""
            );
            
            System.out.printf("Dossier SAV créé - ID: %d%n", dossierSAV.dossier.id());
            System.out.printf("Client: %s %s (%s)%n", 
                dossierSAV.client.prenom(), dossierSAV.client.nom(), dossierSAV.client.email());
            System.out.printf("Appareil: %s %s - SN: %s%n", 
                dossierSAV.appareil.marque(), dossierSAV.appareil.modele(), dossierSAV.appareil.sn());
            
            // Générer automatiquement l'étiquette
            String out = resolvedOutputDir != null ? resolvedOutputDir : outputDir;
            savService.genererEtiquette(dossierSAV.dossier.id(), Paths.get(out));
            System.out.printf("Étiquette générée: %s/etiquette-dossier-%d.pdf%n", 
                out, dossierSAV.dossier.id());
                
        } catch (Exception e) {
            System.err.println("Erreur création dossier: " + e.getMessage());
        }
    }
    
        @Command(name = "lister", description = "Lister les dossiers SAV avec filtres")
    static class ListerCommand implements Runnable {
        @CommandLine.ParentCommand
        private MAGSAVCli parent;
        @Option(names = {"--statut"}, description = "Filtrer par statut (recu, diagnostique, attente_pieces, repare, pret, livre)")
        private String statut;
        
        @Option(names = {"--proprietaire"}, description = "Filtrer par propriétaire")
        private String proprietaire;
        
        @Option(names = {"--produit"}, description = "Filtrer par produit")
        private String produit;

        @Override
        public void run() {
            String dbUrl = parent != null && parent.resolvedDbUrl != null ? parent.resolvedDbUrl : "jdbc:sqlite:magsav.db";
            try (HikariDataSource ds = DB.init(dbUrl)) {
                DB.migrate(ds);
                DossierSAVRepository repo = new DossierSAVRepository(ds);
                List<DossierSAV> dossiers;
                
                if (statut != null) {
                    dossiers = repo.findByStatut(statut);
                } else if (proprietaire != null) {
                    dossiers = repo.findByProprietaire(proprietaire);
                } else {
                    dossiers = repo.findAll();
                }
                
                if (dossiers.isEmpty()) {
                    System.out.println("Aucun dossier trouvé");
                    return;
                }
                
                System.out.printf("%-5s %-20s %-15s %-20s %-15s %-10s %-15s %-12s %-12s%n",
                    "ID", "PRODUIT", "N° SÉRIE", "PROPRIÉTAIRE", "PANNE", "STATUT", "DÉTECTEUR", "ENTRÉE", "SORTIE");
                System.out.println("-".repeat(150));
                
                for (DossierSAV d : dossiers) {
                    System.out.printf("%-5d %-20s %-15s %-20s %-15s %-10s %-15s %-12s %-12s%n",
                        d.id(),
                        truncate(d.produit(), 20),
                        truncate(d.numeroSerie(), 15),
                        truncate(d.proprietaire(), 20),
                        truncate(d.panne(), 15),
                        d.statut(),
                        truncate(d.detecteur(), 15),
                        d.dateEntree() != null ? d.dateEntree().toString() : "",
                        d.dateSortie() != null ? d.dateSortie().toString() : ""
                    );
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la liste : " + e.getMessage());
            }
        }
        
        private String truncate(String str, int maxLen) {
            if (str == null) return "";
            return str.length() > maxLen ? str.substring(0, maxLen - 3) + "..." : str;
        }
    }
    
    @Command(name = "statut", description = "Changer le statut d'un dossier SAV")
    static class StatutCommand implements Runnable {
        @CommandLine.ParentCommand
        private MAGSAVCli parent;
        @Option(names = {"--id"}, required = true, description = "ID du dossier")
        private Long dossierId;
        
        @Option(names = {"--nouveau-statut"}, required = true, description = "Nouveau statut (recu, diagnostique, attente_pieces, repare, pret, livre)")
        private String nouveauStatut;

        @Override
        public void run() {
            String dbUrl = parent != null && parent.resolvedDbUrl != null ? parent.resolvedDbUrl : "jdbc:sqlite:magsav.db";
            try (HikariDataSource ds = DB.init(dbUrl)) {
                DB.migrate(ds);
                DossierSAVRepository repo = new DossierSAVRepository(ds);
                DossierSAV dossier = repo.findById(dossierId);
                
                if (dossier == null) {
                    System.err.printf("Dossier #%d non trouvé%n", dossierId);
                    return;
                }
                
                DossierSAV updated = new DossierSAV(
                    dossier.id(),
                    dossier.produit(),
                    dossier.numeroSerie(),
                    dossier.proprietaire(),
                    dossier.panne(),
                    nouveauStatut,
                    dossier.detecteur(),
                    dossier.dateEntree(),
                    "livre".equals(nouveauStatut) ? LocalDate.now() : dossier.dateSortie(),
                    dossier.createdAt()
                );
                
                repo.update(updated);
                System.out.printf("✓ Statut du dossier #%d changé vers '%s'%n", dossierId, nouveauStatut);
                
            } catch (Exception e) {
                System.err.println("Erreur lors du changement de statut : " + e.getMessage());
            }
        }
    }
    
    @Command(name = "recherche", description = "Rechercher un dossier SAV")
    static class RechercheCommand implements Runnable {
        @CommandLine.ParentCommand
        private MAGSAVCli parent;
        @Option(names = {"--serie"}, description = "Rechercher par numéro de série")
        private String serie;
        
        @Option(names = {"--proprietaire"}, description = "Rechercher par propriétaire")
        private String proprietaire;

        @Override
        public void run() {
            if (serie == null && proprietaire == null) {
                System.err.println("Spécifiez au moins --serie ou --proprietaire");
                return;
            }
            String dbUrl = parent != null && parent.resolvedDbUrl != null ? parent.resolvedDbUrl : "jdbc:sqlite:magsav.db";
            try (HikariDataSource ds = DB.init(dbUrl)) {
                DB.migrate(ds);
                DossierSAVRepository repo = new DossierSAVRepository(ds);
                List<DossierSAV> dossiers = new ArrayList<>();
                
                if (serie != null) {
                    dossiers.addAll(repo.findByNumeroSerie(serie));
                }
                if (proprietaire != null) {
                    dossiers.addAll(repo.findByProprietaire(proprietaire));
                }
                
                // Suppression des doublons
                dossiers = dossiers.stream().distinct().collect(Collectors.toList());
                
                if (dossiers.isEmpty()) {
                    System.out.println("Aucun dossier trouvé");
                    return;
                }
                
                for (DossierSAV d : dossiers) {
                    System.out.printf("Dossier #%d :%n", d.id());
                    System.out.printf("  Produit: %s%n", d.produit());
                    System.out.printf("  N° série: %s%n", d.numeroSerie());
                    System.out.printf("  Propriétaire: %s%n", d.proprietaire());
                    System.out.printf("  Panne: %s%n", d.panne());
                    System.out.printf("  Statut: %s%n", d.statut());
                    System.out.printf("  Détecteur: %s%n", d.detecteur());
                    System.out.printf("  Date entrée: %s%n", d.dateEntree());
                    System.out.printf("  Date sortie: %s%n", d.dateSortie());
                    System.out.println();
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la recherche : " + e.getMessage());
            }
        }
    }
    
    @Command(name = "etiquette", description = "Générer une étiquette pour un dossier")
    static class EtiquetteCommand implements Runnable {
        @CommandLine.ParentCommand
        private MAGSAVCli parent;
        @Option(names = {"--id"}, required = true, description = "ID du dossier")
        private Long dossierId;

        @Override
        public void run() {
            String dbUrl = parent != null && parent.resolvedDbUrl != null ? parent.resolvedDbUrl : "jdbc:sqlite:magsav.db";
            String outDir = parent != null && parent.resolvedOutputDir != null ? parent.resolvedOutputDir : "output";
            try (HikariDataSource ds = DB.init(dbUrl)) {
                DB.migrate(ds);
                DossierSAVRepository repo = new DossierSAVRepository(ds);
                com.magsav.model.DossierSAV dossier = repo.findById(dossierId);
                if (dossier == null) {
                    System.err.printf("Dossier #%d introuvable%n", dossierId);
                    return;
                }
                // Génération QR + étiquette simplifiée adaptée au modèle unifié
                var qrService = new com.magsav.qr.QRCodeService();
                var labelService = new com.magsav.label.LabelService();
                var outputPath = Paths.get(outDir);
                java.nio.file.Files.createDirectories(outputPath);
                var qrPath = outputPath.resolve("qr-dossier-" + dossierId + ".png");
                String qrContent = "MAGSAV:" + dossierId + ":" + dossier.numeroSerie();
                qrService.generateToFile(qrContent, 256, qrPath);
                String titre = "Dossier SAV #" + dossierId + "\n" + dossier.produit() + " - SN:" + dossier.numeroSerie() + "\n" + dossier.proprietaire() + "\nStatut: " + dossier.statut();
                var pdfPath = outputPath.resolve("etiquette-dossier-" + dossierId + ".pdf");
                labelService.createSimpleLabel(pdfPath, titre, qrPath);
                System.out.printf("Étiquette générée: %s%n", pdfPath);
            } catch (Exception e) {
                System.err.println("Erreur génération étiquette: " + e.getMessage());
            }
        }
    }
}