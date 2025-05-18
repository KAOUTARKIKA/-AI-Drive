package com.example.ai_drive.ui.GPS;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ai_drive.R;
import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.GPSDataModel;
import com.example.ai_drive.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSFragment extends Fragment implements LocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private LocationManager locationManager;
    private TextView tvLatitude, tvLongitude, tvAltitude, tvSpeed, tvStatus;
    private ApiService apiService;
    private String deviceId;
    private SessionManager sessionManager;

    public GPSFragment() {
        // Required empty public constructor
    }

    public static GPSFragment newInstance() {
        return new GPSFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialiser le LocationManager
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

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
        View root = inflater.inflate(R.layout.fragment_g_p_s, container, false);

        // Initialiser les TextViews
        tvLatitude = root.findViewById(R.id.tv_gps_latitude);
        tvLongitude = root.findViewById(R.id.tv_gps_longitude);
        tvAltitude = root.findViewById(R.id.tv_gps_altitude);
        tvSpeed = root.findViewById(R.id.tv_gps_speed);
        tvStatus = root.findViewById(R.id.tv_gps_status);

        // Vérifier et demander les permissions
        checkLocationPermission();

        return root;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            tvStatus.setText("En attente de permission...");
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Vérifier si le GPS est activé
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                tvStatus.setText("GPS et réseau désactivés. Veuillez activer la localisation.");
                return;
            }

            tvStatus.setText("Recherche de position GPS...");

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000,   // Intervalle minimum en ms
                        1,      // Distance minimum en mètres
                        this);
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        1,
                        this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                tvStatus.setText("Permission refusée. Impossible d'accéder au GPS.");
                Toast.makeText(requireContext(), "Permission GPS refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Mettre à jour l'interface utilisateur avec les nouvelles données GPS
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        float speed = location.getSpeed() * 3.6f; // Conversion en km/h

        tvLatitude.setText(String.format("Latitude: %.6f°", latitude));
        tvLongitude.setText(String.format("Longitude: %.6f°", longitude));
        tvAltitude.setText(String.format("Altitude: %.1f m", altitude));
        tvSpeed.setText(String.format("Vitesse: %.1f km/h", speed));
        tvStatus.setText("Position GPS trouvée");

        // Envoyer les données au serveur
        uploadGPSData(latitude, longitude, altitude, speed);
    }

    private void uploadGPSData(double latitude, double longitude, double altitude, float speed) {
        GPSDataModel data = new GPSDataModel(latitude, longitude, altitude, speed, deviceId);

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

        apiService.saveGPSData(token, data).enqueue(new Callback<GPSDataModel>() {
            @Override
            public void onResponse(Call<GPSDataModel> call, Response<GPSDataModel> response) {
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
                        Toast.makeText(requireContext(), "Erreur d'envoi GPS: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GPSDataModel> call, Throwable t) {
                // Gérer l'échec de la connexion
                Toast.makeText(requireContext(), "Échec de connexion GPS: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        tvStatus.setText("GPS activé");
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        tvStatus.setText("GPS désactivé");
    }
}