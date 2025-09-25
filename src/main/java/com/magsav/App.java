package com.magsav;

import com.magsav.db.DB;
import com.magsav.imports.CSVImporter;
import com.magsav.qr.QRCodeService;
import com.magsav.label.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        log.info("Démarrage MAGSAV 1.1");
        try (var ds = DB.init("jdbc:sqlite:magsav.db")) {
            DB.migrate(ds);
            log.info("Base initialisée");

            if (args.length > 0 && args[0].equals("import")) {
                var importer = new CSVImporter(ds);
                importer.importClients(Path.of("samples/clients.csv"));
                importer.importFournisseurs(Path.of("samples/fournisseurs.csv"));
                log.info("Import CSV terminé");
            }

            var qr = new QRCodeService();
            var out = Path.of("out/qr-demo.png");
            qr.generateToFile("Dossier:12345", 256, out);
            log.info("QR code créé: {}", out.toAbsolutePath());

            var label = new LabelService();
            var pdf = Path.of("out/etiquette-demo.pdf");
            label.createSimpleLabel(pdf, "Dossier #12345", out);
            log.info("PDF étiquette créé: {}", pdf.toAbsolutePath());
        }
    }
}
