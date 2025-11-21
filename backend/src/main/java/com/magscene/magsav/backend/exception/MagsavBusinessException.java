package com.magscene.magsav.backend.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception métier personnalisée pour MAGSAV.
 * Permet de lever des erreurs spécifiques au domaine avec des codes d'erreur précis.
 * 
 * @author MAGSAV Team
 * @since 3.0
 */
public class MagsavBusinessException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Map<String, Object> details;

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Constructeur avec message simple
     */
    public MagsavBusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST, null);
    }

    /**
     * Constructeur avec statut HTTP
     */
    public MagsavBusinessException(String errorCode, String message, HttpStatus httpStatus) {
        this(errorCode, message, httpStatus, null);
    }

    /**
     * Constructeur complet
     */
    public MagsavBusinessException(String errorCode, String message, HttpStatus httpStatus, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    /**
     * Factory methods pour les erreurs courantes
     */
    public static class Factory {
        
        public static MagsavBusinessException equipmentNotFound(Long id) {
            return new MagsavBusinessException(
                "EQUIPMENT_NOT_FOUND",
                "Équipement non trouvé avec l'ID: " + id,
                HttpStatus.NOT_FOUND,
                Map.of("equipmentId", id)
            );
        }

        public static MagsavBusinessException serviceRequestNotFound(Long id) {
            return new MagsavBusinessException(
                "SERVICE_REQUEST_NOT_FOUND", 
                "Demande de service non trouvée avec l'ID: " + id,
                HttpStatus.NOT_FOUND,
                Map.of("serviceRequestId", id)
            );
        }

        public static MagsavBusinessException invalidQrCode(String qrCode) {
            return new MagsavBusinessException(
                "INVALID_QR_CODE",
                "Code QR invalide: " + qrCode,
                HttpStatus.BAD_REQUEST,
                Map.of("qrCode", qrCode)
            );
        }

        public static MagsavBusinessException equipmentAlreadyExists(String serialNumber) {
            return new MagsavBusinessException(
                "EQUIPMENT_ALREADY_EXISTS",
                "Un équipement avec ce numéro de série existe déjà: " + serialNumber,
                HttpStatus.CONFLICT,
                Map.of("serialNumber", serialNumber)
            );
        }

        public static MagsavBusinessException invalidDateRange(String startDate, String endDate) {
            return new MagsavBusinessException(
                "INVALID_DATE_RANGE",
                "Plage de dates invalide: la date de fin doit être après la date de début",
                HttpStatus.BAD_REQUEST,
                Map.of("startDate", startDate, "endDate", endDate)
            );
        }

        public static MagsavBusinessException equipmentNotAvailable(Long equipmentId, String period) {
            return new MagsavBusinessException(
                "EQUIPMENT_NOT_AVAILABLE",
                "Équipement non disponible pour la période demandée",
                HttpStatus.CONFLICT,
                Map.of("equipmentId", equipmentId, "period", period)
            );
        }
    }
}