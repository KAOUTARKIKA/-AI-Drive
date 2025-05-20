package com.example.ai_drive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager2 featuresViewPager;
    private TabLayout indicator;
    private MaterialButton getStartedButton;
    private MaterialButton createAccountButton;
    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView sloganTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialiser les vues
        featuresViewPager = findViewById(R.id.features_view_pager);
        indicator = findViewById(R.id.indicator);
        getStartedButton = findViewById(R.id.get_started_button);
        createAccountButton = findViewById(R.id.create_account_button);
        logoImageView = findViewById(R.id.logo_imageview);
        appNameTextView = findViewById(R.id.app_name_textview);
        sloganTextView = findViewById(R.id.slogan_textview);

        // Configurer le ViewPager des fonctionnalités
        setupFeaturesPager();

        // Animer les éléments de la page
        animateContent();

        // Configurer les écouteurs de clics
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviguer vers la page de connexion
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Naviguer vers la page d'inscription
                Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void setupFeaturesPager() {
        // Créer les éléments de fonctionnalités
        List<FeatureAdapter.FeatureItem> features = new ArrayList<>();

        features.add(new FeatureAdapter.FeatureItem(
                R.drawable.ic_accelerometer_z,
                "Monitoring Accéléromètre",
                "Analysez les forces G et détectez les accélérations brusques pour une conduite plus douce et économique en carburant."
        ));

        features.add(new FeatureAdapter.FeatureItem(
                R.drawable.ic_rotation_z,
                "Données Gyroscope",
                "Mesurez les rotations de votre véhicule pour détecter les virages brusques et améliorer votre technique de conduite."
        ));

        features.add(new FeatureAdapter.FeatureItem(
                R.drawable.ic_latitude,
                "Suivi GPS en temps réel",
                "Visualisez votre trajet avec précision et obtenez des statistiques détaillées sur votre style de conduite."
        ));

        // Créer et configurer l'adaptateur
        FeatureAdapter adapter = new FeatureAdapter(this, features);
        featuresViewPager.setAdapter(adapter);

        // Animation de défilement
        featuresViewPager.setPageTransformer((page, position) -> {
            float absPosition = Math.abs(position);
            page.setScaleY(0.85f + (1 - absPosition) * 0.15f);
        });

        // Configurer l'indicateur
        new TabLayoutMediator(indicator, featuresViewPager,
                (tab, position) -> {
                    // Pas besoin de titre pour les tabs
                }).attach();
    }

    private void animateContent() {
        // Charger les animations
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Définir les délais pour chaque élément
        new Handler().postDelayed(() -> appNameTextView.startAnimation(slideUp), 300);
        new Handler().postDelayed(() -> sloganTextView.startAnimation(slideUp), 500);
        new Handler().postDelayed(() -> featuresViewPager.setVisibility(View.VISIBLE), 700);

        // Animation de battement pour le logo
        Animation pulse = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        pulse.setDuration(1500);
        logoImageView.startAnimation(pulse);
    }
}