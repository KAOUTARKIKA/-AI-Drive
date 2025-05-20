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
import android.util.Log;
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

    private static final String TAG = "AlertDetection";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationManager;
    private ApiService apiService;
    private SessionManager sessionManager;

    // Réduire les seuils pour faciliter le déclenchement
    private static final float HARSH_BRAKING_THRESHOLD = -8.0f; // au lieu de -8.0f
    private static final float EXCESSIVE_ACCELERATION_THRESHOLD = 10.0f; // au lieu de 6.0f
    private static final float DANGEROUS_TURN_THRESHOLD = 5.0f; // au lieu de 5.0f
    private static final float EXCESSIVE_SPEED_THRESHOLD = 15.0f; // au lieu de 30.0f

    // Dernières valeurs de localisation
    private Location lastLocation;
    private float currentSpeed;

    // Valeurs du gyroscope
    private float rotationX, rotationY, rotationZ;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");

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
        Log.d(TAG, "Service onStartCommand");

        // Dans onCreate() ou onStartCommand()
        try {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Erreur permission: " + e.getMessage());
        }
        // Enregistrer les listeners
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Accéléromètre enregistré");
        }

        // Enregistrer le listener pour le gyroscope
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Gyroscope enregistré");
        }

        // Enregistrer le listener pour la localisation
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // intervalle minimum en ms
                    1,    // distance minimum en mètres
                    this
            );
            Log.d(TAG, "GPS enregistré");
        } catch (SecurityException e) {
            Log.e(TAG, "Erreur permission GPS: " + e.getMessage());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        Log.d(TAG, "Service onDestroy");
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
                Log.d(TAG, "Freinage brusque détecté: " + y);
                sendAlert("HARSH_BRAKING", "Freinage brusque détecté", "HIGH");
            }

            // Détecter les accélérations excessives
            if (y > EXCESSIVE_ACCELERATION_THRESHOLD) {
                Log.d(TAG, "Accélération excessive détectée: " + y);
                sendAlert("EXCESSIVE_ACCELERATION", "Accélération excessive détectée", "MEDIUM");
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            rotationX = event.values[0];
            rotationY = event.values[1];
            rotationZ = event.values[2];

            // Détecter les virages dangereux (rotation rapide autour de l'axe Z)
            float rotationMagnitude = Math.abs(rotationZ);
            if (rotationMagnitude > DANGEROUS_TURN_THRESHOLD) {
                Log.d(TAG, "Virage dangereux détecté: " + rotationMagnitude);
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
            Log.d(TAG, "Vitesse excessive détectée: " + currentSpeed);
            sendAlert("EXCESSIVE_SPEED", "Vitesse excessive détectée", "HIGH");
        }
    }

    private void sendAlert(String type, String description, String severity) {
        Log.d(TAG, "Tentative d'envoi d'alerte: " + type);

        // Vérifier si l'utilisateur est connecté et a un véhicule actif
        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "Impossible d'envoyer l'alerte: utilisateur non connecté");
            return;
        }

        if (sessionManager.getActiveVehicleId() == -1) {
            Log.e(TAG, "Impossible d'envoyer l'alerte: aucun véhicule actif");
            return;
        }

        // Créer l'alerte avec le format adapté au backend
        AlertModel alert = new AlertModel();
        alert.setType(type);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setStatus("NEW");
        alert.setVehicleId(sessionManager.getActiveVehicleId());
        alert.setUserId(sessionManager.getUserId());
        alert.setNotes("Détecté par AI-Drive mobile app");
        // Vous pouvez structurer ces données selon vos besoins
        String jsonData = "{\"appVersion\":\"1.0.0\",\"deviceModel\":\"" +
                android.os.Build.MODEL + "\",\"androidVersion\":\"" +
                android.os.Build.VERSION.RELEASE + "\"}";
        alert.setData(jsonData);

        // Ajouter les données de localisation si disponibles
        if (lastLocation != null) {
            // Définir l'objet LocationModel
            AlertModel.LocationModel location = new AlertModel.LocationModel(
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude()
            );
            alert.setLocation(location);

            // Définir aussi directement latitude et longitude
            alert.setLatitude(lastLocation.getLatitude());
            alert.setLongitude(lastLocation.getLongitude());

            Log.d(TAG, "Localisation ajoutée: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
        } else {
            Log.w(TAG, "Aucune donnée de localisation disponible pour cette alerte");
        }

        // Log pour déboguer les valeurs de l'alerte
        Log.d(TAG, "Alerte à envoyer: type=" + alert.getType()
                + ", vehicleId=" + alert.getVehicleId()
                + ", severity=" + alert.getSeverity()
                + ", lat/long=" + (alert.getLatitude() != null ? alert.getLatitude() : "null")
                + "/" + (alert.getLongitude() != null ? alert.getLongitude() : "null"));

        // Envoyer l'alerte au serveur
        String token = sessionManager.getToken();
        apiService.createAlert(token, alert).enqueue(new Callback<AlertModel>() {
            @Override
            public void onResponse(Call<AlertModel> call, Response<AlertModel> response) {
                if (response.isSuccessful()) {
                    // Alerte envoyée avec succès
                    Log.d(TAG, "✅ Alerte envoyée avec succès: " + type + " (ID: " +
                            (response.body() != null ? response.body().getId() : "inconnu") + ")");
                    Toast.makeText(AlertDetectionService.this, "Alerte envoyée: " + type, Toast.LENGTH_SHORT).show();
                } else {
                    // Erreur lors de l'envoi de l'alerte
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Corps vide";
                        Log.e(TAG, "❌ Erreur envoi alerte: " + response.code() + " - " + errorBody);
                        Log.e(TAG, "URL appelée: " + call.request().url());
                        Log.e(TAG, "Headers: " + call.request().headers());
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erreur envoi alerte: " + response.code());
                    }
                }
            }
            @Override
            public void onFailure(Call<AlertModel> call, Throwable t) {
                // Erreur lors de l'envoi de l'alerte
                Log.e(TAG, "❌ Échec connexion: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }
}