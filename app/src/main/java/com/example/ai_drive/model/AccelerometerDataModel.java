package com.example.ai_drive.model;

import java.time.LocalDateTime;

public class AccelerometerDataModel {
    private float x;
    private float y;
    private float z;
    private String deviceId;
    private String timestamp;

    public AccelerometerDataModel(float x, float y, float z, String deviceId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.deviceId = deviceId;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters et Setters
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
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