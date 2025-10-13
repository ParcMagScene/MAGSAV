package com.magsav.model;

import javafx.beans.property.*;

/**
 * Modèle JavaFX pour la configuration des services Google
 */
public class GoogleServicesConfig {
    
    // Propriétés de base
    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final StringProperty nom = new SimpleStringProperty("Configuration Google par défaut");
    
    // Configuration OAuth2
    private final StringProperty clientId = new SimpleStringProperty("");
    private final StringProperty clientSecret = new SimpleStringProperty("");
    private final StringProperty redirectUri = new SimpleStringProperty("http://localhost:8080/oauth/callback");
    private final StringProperty scopes = new SimpleStringProperty("https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/contacts.readonly");
    
    // Tokens d'authentification
    private final StringProperty accessToken = new SimpleStringProperty("");
    private final StringProperty refreshToken = new SimpleStringProperty("");
    private final StringProperty tokenType = new SimpleStringProperty("Bearer");
    private final LongProperty tokenExpiresIn = new SimpleLongProperty(0);
    
    // Configuration Calendar
    private final StringProperty calendrierPrincipal = new SimpleStringProperty("primary");
    private final BooleanProperty syncCalendarActif = new SimpleBooleanProperty(true);
    private final IntegerProperty intervalleSync = new SimpleIntegerProperty(15); // minutes
    
    // Configuration Gmail
    private final BooleanProperty gmailActif = new SimpleBooleanProperty(true);
    private final StringProperty emailExpéditeur = new SimpleStringProperty("");
    private final StringProperty signatureEmail = new SimpleStringProperty("");
    
    // Configuration Contacts
    private final BooleanProperty contactsActif = new SimpleBooleanProperty(true);
    private final BooleanProperty syncContactsAuto = new SimpleBooleanProperty(false);
    
    // Métadonnées
    private final StringProperty dateCreation = new SimpleStringProperty("");
    private final StringProperty dateModification = new SimpleStringProperty("");
    private final BooleanProperty actif = new SimpleBooleanProperty(true);
    
    // Constructeurs
    public GoogleServicesConfig() {
        // Configuration par défaut
    }
    
    public GoogleServicesConfig(String nom) {
        this.nom.set(nom);
    }
    
    // Méthodes utilitaires
    public boolean isConfigured() {
        return !getClientId().isEmpty() && !getClientSecret().isEmpty();
    }
    
    public boolean hasValidTokens() {
        return !getAccessToken().isEmpty() && !getRefreshToken().isEmpty();
    }
    
    public boolean needsTokenRefresh() {
        return getTokenExpiresIn() > 0 && 
               System.currentTimeMillis() > getTokenExpiresIn() - 300000; // 5 min avant expiration
    }
    
    public String[] getScopesArray() {
        return getScopes().split("\\s+");
    }
    
    // Getters et setters pour les propriétés
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }
    
    public String getClientId() { return clientId.get(); }
    public void setClientId(String clientId) { this.clientId.set(clientId); }
    public StringProperty clientIdProperty() { return clientId; }
    
    public String getClientSecret() { return clientSecret.get(); }
    public void setClientSecret(String clientSecret) { this.clientSecret.set(clientSecret); }
    public StringProperty clientSecretProperty() { return clientSecret; }
    
    public String getRedirectUri() { return redirectUri.get(); }
    public void setRedirectUri(String redirectUri) { this.redirectUri.set(redirectUri); }
    public StringProperty redirectUriProperty() { return redirectUri; }
    
    public String getScopes() { return scopes.get(); }
    public void setScopes(String scopes) { this.scopes.set(scopes); }
    public StringProperty scopesProperty() { return scopes; }
    
    public String getAccessToken() { return accessToken.get(); }
    public void setAccessToken(String accessToken) { this.accessToken.set(accessToken); }
    public StringProperty accessTokenProperty() { return accessToken; }
    
    public String getRefreshToken() { return refreshToken.get(); }
    public void setRefreshToken(String refreshToken) { this.refreshToken.set(refreshToken); }
    public StringProperty refreshTokenProperty() { return refreshToken; }
    
    public String getTokenType() { return tokenType.get(); }
    public void setTokenType(String tokenType) { this.tokenType.set(tokenType); }
    public StringProperty tokenTypeProperty() { return tokenType; }
    
    public long getTokenExpiresIn() { return tokenExpiresIn.get(); }
    public void setTokenExpiresIn(long tokenExpiresIn) { this.tokenExpiresIn.set(tokenExpiresIn); }
    public LongProperty tokenExpiresInProperty() { return tokenExpiresIn; }
    
    public String getCalendrierPrincipal() { return calendrierPrincipal.get(); }
    public void setCalendrierPrincipal(String calendrierPrincipal) { this.calendrierPrincipal.set(calendrierPrincipal); }
    public StringProperty calendrierPrincipalProperty() { return calendrierPrincipal; }
    
    public boolean isSyncCalendarActif() { return syncCalendarActif.get(); }
    public void setSyncCalendarActif(boolean syncCalendarActif) { this.syncCalendarActif.set(syncCalendarActif); }
    public BooleanProperty syncCalendarActifProperty() { return syncCalendarActif; }
    
    public int getIntervalleSync() { return intervalleSync.get(); }
    public void setIntervalleSync(int intervalleSync) { this.intervalleSync.set(intervalleSync); }
    public IntegerProperty intervalleSyncProperty() { return intervalleSync; }
    
    public boolean isGmailActif() { return gmailActif.get(); }
    public void setGmailActif(boolean gmailActif) { this.gmailActif.set(gmailActif); }
    public BooleanProperty gmailActifProperty() { return gmailActif; }
    
    public String getEmailExpéditeur() { return emailExpéditeur.get(); }
    public void setEmailExpéditeur(String emailExpéditeur) { this.emailExpéditeur.set(emailExpéditeur); }
    public StringProperty emailExpéditeurProperty() { return emailExpéditeur; }
    
    public String getSignatureEmail() { return signatureEmail.get(); }
    public void setSignatureEmail(String signatureEmail) { this.signatureEmail.set(signatureEmail); }
    public StringProperty signatureEmailProperty() { return signatureEmail; }
    
    public boolean isContactsActif() { return contactsActif.get(); }
    public void setContactsActif(boolean contactsActif) { this.contactsActif.set(contactsActif); }
    public BooleanProperty contactsActifProperty() { return contactsActif; }
    
    public boolean isSyncContactsAuto() { return syncContactsAuto.get(); }
    public void setSyncContactsAuto(boolean syncContactsAuto) { this.syncContactsAuto.set(syncContactsAuto); }
    public BooleanProperty syncContactsAutoProperty() { return syncContactsAuto; }
    
    public String getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(String dateCreation) { this.dateCreation.set(dateCreation); }
    public StringProperty dateCreationProperty() { return dateCreation; }
    
    public String getDateModification() { return dateModification.get(); }
    public void setDateModification(String dateModification) { this.dateModification.set(dateModification); }
    public StringProperty dateModificationProperty() { return dateModification; }
    
    public boolean isActif() { return actif.get(); }
    public void setActif(boolean actif) { this.actif.set(actif); }
    public BooleanProperty actifProperty() { return actif; }
    
    @Override
    public String toString() {
        return "GoogleServicesConfig{" +
               "id=" + getId() +
               ", nom='" + getNom() + "'" +
               ", configured=" + isConfigured() +
               ", hasTokens=" + hasValidTokens() +
               ", actif=" + isActif() +
               '}';
    }
}