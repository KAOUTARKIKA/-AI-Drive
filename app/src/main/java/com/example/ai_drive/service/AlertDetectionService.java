package com.example.ai_drive.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.AlertModel;
import com.example.ai_drive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertDetectionService extends Service implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationManager;
    private ApiService apiService;
    private SessionManager sessionManager;

    // Réduire les seuils pour faciliter le déclenchement
    private static final float HARSH_BRAKING_THRESHOLD = -1.0f; // au lieu de -8.0f
    private static final float EXCESSIVE_ACCELERATION_THRESHOLD = 1.0f; // au lieu de 6.0f
    private static final float DANGEROUS_TURN_THRESHOLD = 0.5f; // au lieu de 5.0f
    private static final float EXCESSIVE_SPEED_THRESHOLD = 3.0f; // au lieu de 30.0f

    // Dernières valeurs de localisation
    private Location lastLocation;
    private float currentSpeed;

    // Valeurs du gyroscope
    private float rotationX, rotationY, rotationZ;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialiser les capteurs
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Initialiser le gestionnaire de localisation
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Initialiser l'API et la session
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Enregistrer les listeners
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Enregistrer le listener pour le gyroscope
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Enregistrer le listener pour la localisation
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // intervalle minimum en ms
                    1,    // distance minimum en mètres
                    this
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Désinscrire les listeners
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Détecter les freinages brusques (décélération importante sur l'axe Y)
            if (y < HARSH_BRAKING_THRESHOLD) {
                sendAlert("HARSH_BRAKING", "Freinage brusque détecté", "HIGH");
            }

            // Détecter les accélérations excessives
            if (y > EXCESSIVE_ACCELERATION_THRESHOLD) {
                sendAlert("EXCESSIVE_ACCELERATION", "Accélération excessive détectée", "MEDIUM");
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            rotationX = event.values[0];
            rotationY = event.values[1];
            rotationZ = event.values[2];

            // Détecter les virages dangereux (rotation rapide autour de l'axe Z)
            float rotationMagnitude = Math.abs(rotationZ);
            if (rotationMagnitude > DANGEROUS_TURN_THRESHOLD) {
                sendAlert("DANGEROUS_TURN", "Virage dangereux détecté", "MEDIUM");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Stocker la dernière position connue
        lastLocation = location;

        // Calculer la vitesse en m/s
        currentSpeed = location.getSpeed();

        // Détecter une vitesse excessive
        if (currentSpeed > EXCESSIVE_SPEED_THRESHOLD) {
            sendAlert("EXCESSIVE_SPEED", "Vitesse excessive détectée", "HIGH");
        }
    }

    private void sendAlert(String type, String description, String severity) {
        // Vérifier si l'utilisateur est connecté et a un véhicule actif
        if (!sessionManager.isLoggedIn() || sessionManager.getActiveVehicleId() == -1) {
            return;
        }

        // Créer l'alerte
        AlertModel alert = new AlertModel();
        alert.setType(type);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setVehicleId(sessionManager.getActiveVehicleId());

        // Ajouter les données de localisation si disponibles
        if (lastLocation != null) {
            AlertModel.LocationModel location = new AlertModel.LocationModel(
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude()
            );
            alert.setLocation(location);
        }

        // Envoyer l'alerte au serveur
        String token = sessionManager.getToken();
        apiService.createAlert(token, alert).enqueue(new Callback<AlertModel>() {
            @Override
            public void onResponse(Call<AlertModel> call, Response<AlertModel> response) {
                if (response.isSuccessful()) {
                    // Alerte envoyée avec succès
                    // Éventuellement notifier l'utilisateur
                    // Toast.makeText(AlertDetectionService.this, "Alerte envoyée", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AlertModel> call, Throwable t) {
                // Erreur lors de l'envoi de l'alerte
                // Éventuellement stocker localement pour réessayer plus tard
            }
        });
    }
}