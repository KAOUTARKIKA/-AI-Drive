package com.example.ai_drive.api;

import com.example.ai_drive.model.AccelerometerDataModel;
import com.example.ai_drive.model.AlertModel;
import com.example.ai_drive.model.AuthResponseModel;
import com.example.ai_drive.model.GPSDataModel;
import com.example.ai_drive.model.GyroscopeDataModel;
import com.example.ai_drive.model.LoginRequestModel;
import com.example.ai_drive.model.SignupRequestModel;
import com.example.ai_drive.model.VehicleModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    // Endpoints pour les véhicules
    @GET("/api/vehicles")
    Call<List<VehicleModel>> getAllVehicles();

    @GET("/api/vehicles/{id}")
    Call<VehicleModel> getVehicleById(@Path("id") Long id);

    @GET("/api/vehicles/user")
    Call<List<VehicleModel>> getUserVehicles(@Header("Authorization") String token);

    @POST("/api/vehicles/{vehicleId}/assign")
    Call<Void> assignVehicleToUser(
            @Path("vehicleId") Long vehicleId,
            @Header("Authorization") String token);

    @POST("/api/vehicles/{vehicleId}/remove")
    Call<Void> removeVehicleFromUser(
            @Path("vehicleId") Long vehicleId,
            @Header("Authorization") String token);


    @POST("/api/alerts")
    Call<AlertModel> createAlert(@Header("Authorization") String token, @Body AlertModel alertModel);

    @GET("/api/alerts/user")
    Call<List<AlertModel>> getUserAlerts(@Header("Authorization") String token);

    @GET("/api/alerts/vehicle/{vehicleId}")
    Call<List<AlertModel>> getVehicleAlerts(@Path("vehicleId") Long vehicleId, @Header("Authorization") String token);


}