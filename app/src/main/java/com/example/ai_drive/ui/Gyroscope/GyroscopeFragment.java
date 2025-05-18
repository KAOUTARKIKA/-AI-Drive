package com.example.ai_drive.ui.Gyroscope;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.ai_drive.R;
import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.GyroscopeDataModel;
import com.example.ai_drive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GyroscopeFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscope;
    private TextView tvRotX, tvRotY, tvRotZ, tvStatus;
    private float[] rotationValues = new float[3];
    private ApiService apiService;
    private String deviceId;
    private long lastUploadTime = 0;
    private static final long UPLOAD_INTERVAL = 5000; // Intervalle de 5 secondes entre les envois
    private SessionManager sessionManager;

    public GyroscopeFragment() {
        // Required empty public constructor
    }

    public static GyroscopeFragment newInstance() {
        return new GyroscopeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialiser le SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
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
        View root = inflater.inflate(R.layout.fragment_gyroscope, container, false);

        // Initialiser les TextViews
        tvRotX = root.findViewById(R.id.tv_gyroscope_x);
        tvRotY = root.findViewById(R.id.tv_gyroscope_y);
        tvRotZ = root.findViewById(R.id.tv_gyroscope_z);
        tvStatus = root.findViewById(R.id.tv_gyroscope_status);

        // Vérifier si le gyroscope est disponible
        if (gyroscope == null) {
            tvStatus.setText("Gyroscope non disponible sur cet appareil");
        } else {
            tvStatus.setText("Gyroscope prêt");
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Enregistrer le listener lorsque le fragment est visible
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Valeurs de rotation en rad/s
            rotationValues[0] = event.values[0]; // Rotation autour de l'axe X
            rotationValues[1] = event.values[1]; // Rotation autour de l'axe Y
            rotationValues[2] = event.values[2]; // Rotation autour de l'axe Z

            // Mettre à jour l'interface utilisateur
            tvRotX.setText(String.format("Rotation X: %.2f rad/s", rotationValues[0]));
            tvRotY.setText(String.format("Rotation Y: %.2f rad/s", rotationValues[1]));
            tvRotZ.setText(String.format("Rotation Z: %.2f rad/s", rotationValues[2]));

            // Afficher le mouvement principal de rotation
            String movement = determineRotationMovement(rotationValues);
            tvStatus.setText("Mouvement: " + movement);

            // Envoyer les données au serveur toutes les UPLOAD_INTERVAL ms
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUploadTime > UPLOAD_INTERVAL) {
                lastUploadTime = currentTime;
                uploadGyroscopeData(rotationValues[0], rotationValues[1], rotationValues[2]);
            }
        }
    }

    private void uploadGyroscopeData(float rotX, float rotY, float rotZ) {
        GyroscopeDataModel data = new GyroscopeDataModel(rotX, rotY, rotZ, deviceId);

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

        // Récupérer le token d'authentification
        String token = sessionManager.getToken();

        apiService.saveGyroscopeData(token, data).enqueue(new Callback<GyroscopeDataModel>() {
            @Override
            public void onResponse(Call<GyroscopeDataModel> call, Response<GyroscopeDataModel> response) {
                if (response.isSuccessful()) {
                    // Données enregistrées avec succès
                    // Vous pouvez ajouter un indicateur dans l'UI si vous le souhaitez
                } else {
                    // Gérer l'erreur
                    if (response.code() == 401) {
                        // Token expiré ou invalide
                        sessionManager.logout();
                        Toast.makeText(requireContext(), "Session expirée, veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Erreur d'envoi gyroscope: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GyroscopeDataModel> call, Throwable t) {
                // Gérer l'échec de la connexion
                Toast.makeText(requireContext(), "Échec de connexion gyroscope: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Rien à faire ici pour cet exemple
    }

    private String determineRotationMovement(float[] values) {
        float absX = Math.abs(values[0]);
        float absY = Math.abs(values[1]);
        float absZ = Math.abs(values[2]);

        float maxValue = Math.max(Math.max(absX, absY), absZ);

        if (maxValue < 0.1) {
            return "Stationnaire";
        }

        if (maxValue == absX) {
            return values[0] > 0 ? "Rotation axe X positif" : "Rotation axe X négatif";
        } else if (maxValue == absY) {
            return values[1] > 0 ? "Rotation axe Y positif" : "Rotation axe Y négatif";
        } else {
            return values[2] > 0 ? "Rotation axe Z positif" : "Rotation axe Z négatif";
        }
    }
}