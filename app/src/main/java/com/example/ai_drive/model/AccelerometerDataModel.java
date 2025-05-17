package com.example.ai_drive.model;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

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
        // Format ISO simple sans nanosecondes
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
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

    // Et réajouter les getters/setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


}