package com.example.ai_drive.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlertModel {
    private Long id;
    private String type;
    private String description;
    private String severity;
    private String status;
    private String timestamp;
    private LocationModel location;
    private String notes;
    private String data;
    private Long vehicleId;

    // Ajouter ces champs
    private Double latitude;
    private Double longitude;


    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // Classe interne pour la localisation
    public static class LocationModel {
        private double latitude;
        private double longitude;

        public LocationModel(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters et Setters
        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

    // Constructeur par défaut
    public AlertModel() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    // Constructeur complet
    public AlertModel(String type, String description, String severity, LocationModel location, Long vehicleId) {
        this.type = type;
        this.description = description;
        this.severity = severity;
        this.status = "NEW";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        this.location = location;
        this.vehicleId = vehicleId;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public LocationModel getLocation() {
        return location;
    }

    //public void setLocation(LocationModel location) {
        //this.location = location;
    //}

    // Dans la méthode setLocation, ajoutez la synchronisation
    public void setLocation(LocationModel location) {
        this.location = location;
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}