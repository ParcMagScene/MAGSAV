package com.magscene.magsav.desktop.view.personnel;

import com.magscene.magsav.desktop.component.EntityDetailView;

import java.util.Map;

/**
 * Fiche de détails spécialisée pour le personnel
 * Hérite du système unifié EntityDetailView
 */
public class PersonnelDetailView extends EntityDetailView {
    
    public PersonnelDetailView() {
        super("Personnel");
    }
    
    /**
     * Initialise la fiche avec les données d'un membre du personnel
     */
    public void setPersonnelData(Map<String, Object> personnelData) {
        // Vider le contenu précédent
        clearDynamicContent();
        
        // Informations principales
        String firstName = (String) personnelData.getOrDefault("firstName", "");
        String lastName = (String) personnelData.getOrDefault("lastName", "");
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) fullName = "Personne sans nom";
        
        String position = (String) personnelData.getOrDefault("position", "");
        setEntityInfo(fullName, position, "Personnel");
        
        // Image par défaut pour personnel
        setDefaultImage("personnel");
        
        // Section Identification
        addInfoRow("Matricule", (String) personnelData.get("employeeNumber"), true);
        addInfoRow("Prénom", firstName);
        addInfoRow("Nom", lastName);
        addInfoRow("Poste", position);
        addInfoRow("Département", (String) personnelData.get("department"));
        addSeparator();
        
        // Section Contact
        addInfoRow("Email", (String) personnelData.get("email"));
        addInfoRow("Téléphone", (String) personnelData.get("phone"));
        addInfoRow("Téléphone mobile", (String) personnelData.get("mobilePhone"));
        addInfoRow("Adresse", (String) personnelData.get("address"));
        addSeparator();
        
        // Section Professionnelle
        String employmentType = (String) personnelData.get("employmentType");
        String status = (String) personnelData.get("status");
        addInfoRow("Type d'emploi", employmentType);
        addInfoRow("Statut", status, true);
        addInfoRow("Date d'embauche", formatDate(personnelData.get("hireDate")));
        addInfoRow("Manager", (String) personnelData.get("manager"));
        addSeparator();
        
        // Section Qualifications
        addInfoRow("Spécialités", formatList(personnelData.get("specialties")));
        addInfoRow("Certifications", formatList(personnelData.get("certifications")));
        addInfoRow("Permis de conduire", (String) personnelData.get("drivingLicense"));
        addInfoRow("Niveau d'expérience", (String) personnelData.get("experienceLevel"));
        addSeparator();
        
        // Section Contractuelle
        addInfoRow("Salaire horaire", formatHourlyRate(personnelData.get("hourlyRate")));
        addInfoRow("Heures par semaine", formatHours(personnelData.get("weeklyHours")));
        addInfoRow("Fin de contrat", formatDate(personnelData.get("contractEndDate")));
        addInfoRow("Dernière évaluation", formatDate(personnelData.get("lastEvaluation")));
        addSeparator();
        
        // Section Disponibilité
        addInfoRow("Disponibilité", (String) personnelData.get("availability"));
        addInfoRow("Jours travaillés", formatDaysWorked(personnelData.get("workDays")));
        addInfoRow("Congés restants", formatDays(personnelData.get("remainingLeave")));
        addInfoRow("Prochaine formation", formatDate(personnelData.get("nextTraining")));
        addSeparator();
        
        // Section Notes
        addInfoRow("Notes", (String) personnelData.get("notes"));
        addInfoRow("Urgence contact", (String) personnelData.get("emergencyContact"));
        
        // QR Code
        if (personnelData.containsKey("id")) {
            String qrData = "PERSONNEL_" + personnelData.get("id");
            generateQRCode(qrData);
        }
        
        // Boutons d'action
        addActionButton("✏️ Modifier", "primary", this::editPersonnel);
        addActionButton("📅 Planning", "secondary", this::viewSchedule);
        addActionButton("📋 Évaluation", "secondary", this::viewEvaluation);
        
        // Boutons conditionnels selon le statut et type
        if ("ACTIF".equals(status)) {
            addActionButton("⏰ Pointage", "success", this::clockInOut);
        }
        
        if ("INTERMITTENT".equals(employmentType)) {
            addActionButton("📄 Contrat", "secondary", this::manageContract);
        }
        
        if ("FORMATION".equals(status)) {
            addActionButton("✅ Valider", "success", this::completeTraining);
        }
    }
    
    /**
     * Formate une liste pour l'affichage
     */
    @SuppressWarnings("unchecked")
    private String formatList(Object list) {
        if (list == null) return null;
        if (list instanceof java.util.List) {
            return String.join(", ", (java.util.List<String>) list);
        }
        return list.toString();
    }
    
    /**
     * Formate le salaire horaire
     */
    private String formatHourlyRate(Object rate) {
        if (rate == null) return null;
        try {
            double r = Double.parseDouble(rate.toString());
            return String.format("%.2f €/h", r);
        } catch (NumberFormatException e) {
            return rate.toString();
        }
    }
    
    /**
     * Formate les heures
     */
    private String formatHours(Object hours) {
        if (hours == null) return null;
        try {
            int h = Integer.parseInt(hours.toString());
            return h + "h";
        } catch (NumberFormatException e) {
            return hours.toString();
        }
    }
    
    /**
     * Formate les jours travaillés
     */
    @SuppressWarnings("unchecked")
    private String formatDaysWorked(Object days) {
        if (days == null) return null;
        if (days instanceof java.util.List) {
            return String.join(", ", (java.util.List<String>) days);
        }
        return days.toString();
    }
    
    /**
     * Formate les jours
     */
    private String formatDays(Object days) {
        if (days == null) return null;
        try {
            int d = Integer.parseInt(days.toString());
            return d + " jours";
        } catch (NumberFormatException e) {
            return days.toString();
        }
    }
    
    /**
     * Formate la date pour l'affichage
     */
    private String formatDate(Object date) {
        if (date == null) return null;
        // TODO: Formatter selon le type de date reçu
        return date.toString();
    }
    
    // Actions spécifiques au personnel
    private void editPersonnel() {
        // TODO: Ouvrir le dialog d'édition
        System.out.println("Édition du personnel");
        close();
    }
    
    private void viewSchedule() {
        // TODO: Ouvrir le planning personnel
        System.out.println("Planning du personnel");
    }
    
    private void viewEvaluation() {
        // TODO: Afficher les évaluations
        System.out.println("Évaluations du personnel");
    }
    
    private void clockInOut() {
        // TODO: Gérer le pointage
        System.out.println("Pointage du personnel");
    }
    
    private void manageContract() {
        // TODO: Gérer le contrat intermittent
        System.out.println("Gestion du contrat intermittent");
    }
    
    private void completeTraining() {
        // TODO: Valider la formation
        System.out.println("Validation de formation");
    }
    
    /**
     * Méthode statique pour créer rapidement une fiche de personnel
     */
    public static PersonnelDetailView createAndShow(Map<String, Object> personnelData) {
        PersonnelDetailView detail = new PersonnelDetailView();
        detail.setPersonnelData(personnelData);
        detail.show();
        return detail;
    }
}