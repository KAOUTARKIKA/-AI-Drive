package com.example.ai_drive.ui.Accelerometre;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ai_drive.R;
import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.AccelerometerDataModel;
import com.example.ai_drive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccelerometreFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView tvX, tvY, tvZ, tvStatus;
    private ApiService apiService;
    private String deviceId;
    private long lastUploadTime = 0;
    private static final long UPLOAD_INTERVAL = 5000; // Intervalle de 5 secondes entre les envois
    private SessionManager sessionManager;

    public AccelerometreFragment() {
        // Required empty public constructor
    }

    public static AccelerometreFragment newInstance() {
        return new AccelerometreFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialiser le SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Initialiser Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        // Obtenir l'ID de l'appareil
        deviceId = Secure.getString(requireActivity().getContentResolver(), Secure.ANDROID_ID);

        // Initialiser SessionManager
        sessionManager = new SessionManager(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_accelerometre, container, false);

        // Initialiser les TextViews
        tvX = root.findViewById(R.id.tv_accelerometer_x);
        tvY = root.findViewById(R.id.tv_accelerometer_y);
        tvZ = root.findViewById(R.id.tv_accelerometer_z);
        tvStatus = root.findViewById(R.id.tv_accelerometer_status);

        // Vérifier si l'accéléromètre est disponible
        if (accelerometer == null) {
            tvStatus.setText("Accéléromètre non disponible sur cet appareil");
        } else {
            tvStatus.setText("Accéléromètre prêt");
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Enregistrer le listener lorsque le fragment est visible
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Désactiver le listener lorsque le fragment n'est pas visible
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Mettre à jour l'interface utilisateur
            tvX.setText(String.format("X: %.2f m/s²", x));
            tvY.setText(String.format("Y: %.2f m/s²", y));
            tvZ.setText(String.format("Z: %.2f m/s²", z));

            // Déterminer l'orientation approximative de l'appareil
            String orientation = determineOrientation(x, y, z);
            tvStatus.setText("Orientation: " + orientation);

            // Envoyer les données au serveur toutes les UPLOAD_INTERVAL ms
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUploadTime > UPLOAD_INTERVAL) {
                lastUploadTime = currentTime;
                uploadAccelerometerData(x, y, z);
            }
        }
    }

    private void uploadAccelerometerData(float x, float y, float z) {
        AccelerometerDataModel data = new AccelerometerDataModel(x, y, z, deviceId);

        // Récupérer le vehicleId actif et l'associer si présent
        Long activeVehicleId = sessionManager.getActiveVehicleId();
        if (activeVehicleId != -1) {
            data.setVehicleId(activeVehicleId);
        }

        // Vérifier si l'utilisateur est connecté
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Veuillez vous connecter pour envoyer des données", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupérer le token d'authentification et l'afficher dans les logs
        String token = sessionManager.getToken();
        Log.d("AUTH_DEBUG", "Token: " + token); // Vérifiez ce token dans les logs

        apiService.saveAccelerometerData(token, data).enqueue(new Callback<AccelerometerDataModel>() {
            @Override
            public void onResponse(Call<AccelerometerDataModel> call, Response<AccelerometerDataModel> response) {
                if (response.isSuccessful()) {
                    // Données enregistrées avec succès
                    // Vous pouvez ajouter un indicateur dans l'UI si vous le souhaitez
                    Log.d("AUTH_DEBUG", "Envoi réussi");
                } else {
                    // Gérer l'erreur
                    if (response.code() == 401) {
                        // Token expiré ou invalide
                        sessionManager.logout();
                        Toast.makeText(requireContext(), "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
                        Log.e("AUTH_DEBUG", "Erreur 401: Token invalide");
                    } else {
                        try {
                            // Afficher le corps de la réponse d'erreur
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Erreur inconnue";
                            Toast.makeText(requireContext(), "Erreur d'envoi: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                            Log.e("AUTH_DEBUG", "Erreur " + response.code() + ": " + errorBody);
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Erreur d'envoi: " + response.code(), Toast.LENGTH_SHORT).show();
                            Log.e("AUTH_DEBUG", "Erreur " + response.code() + ": " + e.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AccelerometerDataModel> call, Throwable t) {
                // Gérer l'échec de la connexion
                Toast.makeText(requireContext(), "Échec de connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AUTH_DEBUG", "Échec de connexion: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Rien à faire ici pour cet exemple
    }

    private String determineOrientation(float x, float y, float z) {
        if (Math.abs(x) > Math.abs(y) && Math.abs(x) > Math.abs(z)) {
            return x > 0 ? "Incliné à gauche" : "Incliné à droite";
        } else if (Math.abs(y) > Math.abs(x) && Math.abs(y) > Math.abs(z)) {
            return y > 0 ? "Incliné vers le bas" : "Incliné vers le haut";
        } else {
            return z > 0 ? "Face visible" : "Face cachée";
        }
    }
}