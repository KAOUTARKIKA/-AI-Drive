package com.example.ai_drive.model;

import java.time.LocalDateTime;

public class GPSDataModel {
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;
    private String deviceId;
    private String timestamp;

    public GPSDataModel(double latitude, double longitude, double altitude, float speed, String deviceId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.deviceId = deviceId;
        this.timestamp = LocalDateTime.now().toString();
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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}