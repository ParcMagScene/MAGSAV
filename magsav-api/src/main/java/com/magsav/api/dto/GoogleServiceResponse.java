package com.magsav.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO pour les réponses des services Google dans MAGSAV 1.3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleServiceResponse {
    
    private boolean success;
    private String message;
    private Object data;
    private Map<String, Object> details;
    private String errorCode;
    private LocalDateTime timestamp;
    
    public GoogleServiceResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public GoogleServiceResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public GoogleServiceResponse(boolean success, String message, Object data) {
        this(success, message);
        this.data = data;
    }
    
    // Factory methods pour faciliter la création
    public static GoogleServiceResponse success(String message) {
        return new GoogleServiceResponse(true, message);
    }
    
    public static GoogleServiceResponse success(String message, Object data) {
        return new GoogleServiceResponse(true, message, data);
    }
    
    public static GoogleServiceResponse error(String message) {
        return new GoogleServiceResponse(false, message);
    }
    
    public static GoogleServiceResponse error(String message, String errorCode) {
        GoogleServiceResponse response = new GoogleServiceResponse(false, message);
        response.setErrorCode(errorCode);
        return response;
    }
    
    // Getters et Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}