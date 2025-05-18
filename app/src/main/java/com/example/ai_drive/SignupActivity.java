package com.example.ai_drive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.AuthResponseModel;
import com.example.ai_drive.model.SignupRequestModel;
import com.example.ai_drive.model.VehicleModel;
import com.example.ai_drive.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etTelephone;
    private Spinner spinnerVehicles;
    private Button btnSignup;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;

    private List<VehicleModel> vehiclesList;
    private Long selectedVehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialisation des vues
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etTelephone = findViewById(R.id.et_telephone);
        spinnerVehicles = findViewById(R.id.spinner_vehicles);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);

        // Initialisation de l'API Service et SessionManager
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Charger la liste des véhicules
        loadVehicles();

        // Gérer le clic sur le bouton d'inscription
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Gérer le clic sur le lien de connexion
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Retour à l'écran de connexion
            }
        });
    }

    private void loadVehicles() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getAllVehicles().enqueue(new Callback<List<VehicleModel>>() {
            @Override
            public void onResponse(Call<List<VehicleModel>> call, Response<List<VehicleModel>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    vehiclesList = response.body();

                    if (vehiclesList.isEmpty()) {
                        Toast.makeText(SignupActivity.this, "Aucun véhicule disponible", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<String> vehicleNames = new ArrayList<>();
                    for (VehicleModel vehicle : vehiclesList) {
                        vehicleNames.add(vehicle.getBrand() + " " + vehicle.getModel() + " (" + vehicle.getLicensePlate() + ")");
                    }

                    ArrayAdapter<String> vehiclesAdapter = new ArrayAdapter<>(
                            SignupActivity.this,
                            android.R.layout.simple_spinner_item,
                            vehicleNames);

                    vehiclesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerVehicles.setAdapter(vehiclesAdapter);

                    spinnerVehicles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedVehicleId = vehiclesList.get(position).getId();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedVehicleId = null;
                        }
                    });
                } else {
                    Toast.makeText(SignupActivity.this, "Erreur lors du chargement des véhicules", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VehicleModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SignupActivity.this, "Échec de connexion: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String telephone = etTelephone.getText().toString().trim();

        // Validation des champs
        if (username.isEmpty()) {
            etUsername.setError("Veuillez entrer un nom d'utilisateur");
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Veuillez entrer une adresse email valide");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            etPassword.requestFocus();
            return;
        }

        if (telephone.isEmpty()) {
            etTelephone.setError("Veuillez entrer un numéro de téléphone");
            etTelephone.requestFocus();
            return;
        }

        if (selectedVehicleId == null) {
            Toast.makeText(this, "Veuillez sélectionner un véhicule", Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher la barre de progression
        progressBar.setVisibility(View.VISIBLE);

        // Créer l'objet de requête
        SignupRequestModel signupRequest = new SignupRequestModel(username, password, email, selectedVehicleId, telephone);

        // Envoyer la requête d'inscription
        apiService.signup(signupRequest).enqueue(new Callback<AuthResponseModel>() {
            @Override
            public void onResponse(Call<AuthResponseModel> call, Response<AuthResponseModel> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponseModel authResponse = response.body();

                    // Sauvegarder les informations de session
                    sessionManager.createLoginSession(
                            authResponse.getToken(),
                            authResponse.getUsername(),
                            authResponse.getUserId()
                    );

                    // Rediriger vers l'activité principale
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Gérer les erreurs
                    Toast.makeText(SignupActivity.this, "Échec de l'inscription: nom d'utilisateur ou email déjà utilisé", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SignupActivity.this, "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}