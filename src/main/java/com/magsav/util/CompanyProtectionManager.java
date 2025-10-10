package com.magsav.util;

import com.magsav.model.Company;
import com.magsav.repo.CompanyRepository;
import java.util.Optional;

/**
 * Gestionnaire de protection pour les sociétés système
 * Protège la société "Mag Scène" contre les modifications non autorisées
 */
public class CompanyProtectionManager {
    
    private static final String MAG_SCENE_NAME = "Mag Scène";
    
    /**
     * Vérifie si une société est protégée contre les modifications
     * @param company La société à vérifier
     * @return true si la société est protégée
     */
    public static boolean isProtectedCompany(Company company) {
        if (company == null) return false;
        
        // La société "Mag Scène" est protégée
        return MAG_SCENE_NAME.equals(company.getName()) || 
               company.getType() == Company.CompanyType.OWN_COMPANY;
    }
    
    /**
     * Vérifie si une société est protégée contre les modifications par son ID
     * @param companyId L'ID de la société à vérifier
     * @param companyRepo Le repository pour accéder aux données
     * @return true si la société est protégée
     */
    public static boolean isProtectedCompany(Long companyId, CompanyRepository companyRepo) {
        if (companyId == null || companyRepo == null) return false;
        
        Optional<Company> company = companyRepo.findById(companyId);
        return company.map(CompanyProtectionManager::isProtectedCompany).orElse(false);
    }
    
    /**
     * Vérifie si nous sommes dans le contexte des préférences
     * Cette méthode peut être appelée pour autoriser les modifications dans les préférences
     * @return true si nous sommes dans le contexte des préférences
     */
    public static boolean isInPreferencesContext() {
        // Vérification de la pile des appels pour détecter PreferencesController
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("PreferencesController")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Valide si une modification de société est autorisée
     * @param company La société à modifier
     * @return true si la modification est autorisée
     * @throws CompanyProtectionException si la modification n'est pas autorisée
     */
    public static boolean validateCompanyModification(Company company) throws CompanyProtectionException {
        if (isProtectedCompany(company) && !isInPreferencesContext()) {
            throw new CompanyProtectionException(
                "La société '" + company.getName() + "' ne peut être modifiée que dans les préférences de l'application."
            );
        }
        return true;
    }
    
    /**
     * Valide si une suppression de société est autorisée
     * @param company La société à supprimer
     * @return true si la suppression est autorisée
     * @throws CompanyProtectionException si la suppression n'est pas autorisée
     */
    public static boolean validateCompanyDeletion(Company company) throws CompanyProtectionException {
        if (isProtectedCompany(company)) {
            throw new CompanyProtectionException(
                "La société '" + company.getName() + "' ne peut pas être supprimée car elle est protégée."
            );
        }
        return true;
    }
    
    /**
     * Exception personnalisée pour les violations de protection de société
     */
    public static class CompanyProtectionException extends Exception {
        public CompanyProtectionException(String message) {
            super(message);
        }
        
        public CompanyProtectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}