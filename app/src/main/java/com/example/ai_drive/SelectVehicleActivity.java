package com.example.ai_drive;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.VehicleModel;
import com.example.ai_drive.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectVehicleActivity extends AppCompatActivity {

    private ListView lvVehicles;
    private Button btnCancel;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;
    private List<VehicleModel> vehiclesList;
    private ArrayAdapter<String> vehiclesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_vehicle);

        // Initialiser les vues
        lvVehicles = findViewById(R.id.lv_vehicles);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progress_bar);

        // Initialiser l'API Service et SessionManager
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Configuration du bouton annuler
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Charger les véhicules de l'utilisateur
        loadUserVehicles();

        // Configurer le clic sur les éléments de la liste
        lvVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Définir le véhicule sélectionné comme actif
                Long vehicleId = vehiclesList.get(position).getId();
                sessionManager.setActiveVehicle(vehicleId);
                Toast.makeText(SelectVehicleActivity.this,
                        "Véhicule " + vehiclesList.get(position).getBrand() + " " +
                                vehiclesList.get(position).getModel() + " sélectionné",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadUserVehicles() {
        progressBar.setVisibility(View.VISIBLE);

        // Récupérer le token d'authentification
        String token = sessionManager.getToken();

        apiService.getUserVehicles(token).enqueue(new Callback<List<VehicleModel>>() {
            @Override
            public void onResponse(Call<List<VehicleModel>> call, Response<List<VehicleModel>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    vehiclesList = response.body();

                    if (vehiclesList.isEmpty()) {
                        Toast.makeText(SelectVehicleActivity.this, "Aucun véhicule associé à votre compte", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Créer la liste des noms de véhicules à afficher
                    List<String> vehicleNames = new ArrayList<>();
                    for (VehicleModel vehicle : vehiclesList) {
                        vehicleNames.add(vehicle.getBrand() + " " + vehicle.getModel() +
                                " (" + vehicle.getLicensePlate() + ")");
                    }

                    // Configurer l'adaptateur et l'associer à la ListView
                    vehiclesAdapter = new ArrayAdapter<>(
                            SelectVehicleActivity.this,
                            android.R.layout.simple_list_item_1,
                            vehicleNames);

                    lvVehicles.setAdapter(vehiclesAdapter);

                    // Marquer le véhicule actif (si défini)
                    Long activeVehicleId = sessionManager.getActiveVehicleId();
                    if (activeVehicleId != -1) {
                        for (int i = 0; i < vehiclesList.size(); i++) {
                            if (vehiclesList.get(i).getId().equals(activeVehicleId)) {
                                lvVehicles.setItemChecked(i, true);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(SelectVehicleActivity.this,
                            "Erreur lors du chargement des véhicules: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VehicleModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SelectVehicleActivity.this,
                        "Échec de connexion: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}