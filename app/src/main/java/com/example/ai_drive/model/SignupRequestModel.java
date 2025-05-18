package com.example.ai_drive.model;

public class SignupRequestModel {
    private String username;
    private String password;
    private String email;
    private String telephone;
    private Long vehicleId;

    public SignupRequestModel(String username, String password, String email, Long vehicleId, String telephone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.vehicleId = vehicleId;
        this.telephone = telephone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}