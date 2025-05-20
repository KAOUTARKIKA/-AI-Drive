package com.example.ai_drive;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.ai_drive.api.ApiClient;
import com.example.ai_drive.api.ApiService;
import com.example.ai_drive.model.AuthResponseModel;
import com.example.ai_drive.model.LoginRequestModel;
import com.example.ai_drive.model.VehicleModel;
import com.example.ai_drive.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation des vues
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);

        // Initialisation de l'API Service et SessionManager
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Vérifier si l'utilisateur est déjà connecté
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Gérer le clic sur le bouton de connexion
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Gérer le clic sur le lien d'inscription
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation des champs
        if (username.isEmpty()) {
            etUsername.setError("Veuillez entrer votre nom d'utilisateur");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Veuillez entrer votre mot de passe");
            etPassword.requestFocus();
            return;
        }

        // Afficher la barre de progression
        progressBar.setVisibility(View.VISIBLE);

        // Créer l'objet de requête
        LoginRequestModel loginRequest = new LoginRequestModel(username, password);

        // Envoyer la requête d'authentification
        apiService.login(loginRequest).enqueue(new Callback<AuthResponseModel>() {
            // Dans onResponse après l'authentification réussie
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
                             // Ajout de l'ID du véhicule
                    );

                    // Récupérer et définir automatiquement un véhicule actif
                    loadAndSetDefaultVehicle(authResponse.getToken());

                    // Rediriger vers l'activité principale
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Gérer les erreurs
                    Toast.makeText(LoginActivity.this, "Échec de la connexion: identifiants incorrects", Toast.LENGTH_LONG).show();
                }
            }

            // Ajouter cette nouvelle méthode pour charger et définir un véhicule par défaut
            private void loadAndSetDefaultVehicle(String token) {
                // Récupérer le token formaté
                String formattedToken = "Bearer " + token;

                // Appeler l'API pour récupérer les véhicules de l'utilisateur
                apiService.getUserVehicles(formattedToken).enqueue(new Callback<List<VehicleModel>>() {
                    @Override
                    public void onResponse(Call<List<VehicleModel>> call, Response<List<VehicleModel>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Définir le premier véhicule comme actif
                            VehicleModel defaultVehicle = response.body().get(0);
                            sessionManager.setActiveVehicle(defaultVehicle.getId());
                            Toast.makeText(LoginActivity.this,
                                    "Véhicule par défaut: " + defaultVehicle.getBrand() + " " + defaultVehicle.getModel(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<VehicleModel>> call, Throwable t) {
                        // En cas d'échec, on ne bloque pas le flux principal
                        Toast.makeText(LoginActivity.this,
                                "Impossible de charger un véhicule par défaut, veuillez en sélectionner un manuellement.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<AuthResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}