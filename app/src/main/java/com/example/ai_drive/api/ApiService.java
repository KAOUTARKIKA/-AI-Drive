package com.example.ai_drive.api;

import com.example.ai_drive.model.AccelerometerDataModel;
import com.example.ai_drive.model.GPSDataModel;
import com.example.ai_drive.model.GyroscopeDataModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/api/sensor/accelerometer")
    Call<AccelerometerDataModel> saveAccelerometerData(@Body AccelerometerDataModel data);

    @POST("/api/sensor/gps")
    Call<GPSDataModel> saveGPSData(@Body GPSDataModel data);

    @POST("/api/sensor/gyroscope")
    Call<GyroscopeDataModel> saveGyroscopeData(@Body GyroscopeDataModel data);
}