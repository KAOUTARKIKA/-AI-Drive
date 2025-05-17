package com.example.ai_drive.api;

import com.example.ai_drive.model.AccelerometerDataModel;
import com.example.ai_drive.model.AuthResponseModel;
import com.example.ai_drive.model.GPSDataModel;
import com.example.ai_drive.model.GyroscopeDataModel;
import com.example.ai_drive.model.LoginRequestModel;
import com.example.ai_drive.model.SignupRequestModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/auth/login")
    Call<AuthResponseModel> login(@Body LoginRequestModel loginRequest);

    @POST("/api/auth/signup")
    Call<AuthResponseModel> signup(@Body SignupRequestModel signupRequest);

    @POST("/api/sensor/accelerometer")
    Call<AccelerometerDataModel> saveAccelerometerData(
            @Header("Authorization") String token,
            @Body AccelerometerDataModel data);

    @POST("/api/sensor/gps")
    Call<GPSDataModel> saveGPSData(
            @Header("Authorization") String token,
            @Body GPSDataModel data);

    @POST("/api/sensor/gyroscope")
    Call<GyroscopeDataModel> saveGyroscopeData(
            @Header("Authorization") String token,
            @Body GyroscopeDataModel data);
}