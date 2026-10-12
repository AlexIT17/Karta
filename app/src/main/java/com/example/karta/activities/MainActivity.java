package com.example.karta.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.karta.R;
import com.example.karta.fragments.CatalogFragment;
import com.example.karta.fragments.HomeFragment;
import com.example.karta.fragments.NotificationsFragment;
import com.example.karta.fragments.ProfileFragment;
import com.example.karta.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        // Защита: если пользователь не авторизован - выкидываем на экран входа
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        bottomNav = findViewById(R.id.bottomNav);

        // По умолчанию открываем вкладку "Бонусы" (как на референсе дизайна)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_bonus);
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
    }

    private boolean onNavItemSelected(@NonNull android.view.MenuItem item) {
        Fragment selectedFragment;

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            selectedFragment = new CatalogFragment(); // временно, пока нет отдельного экрана "Главная"
        } else if (itemId == R.id.nav_catalog) {
            selectedFragment = new CatalogFragment();
        } else if (itemId == R.id.nav_bonus) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_notifications) {
            selectedFragment = new NotificationsFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else {
            return false;
        }

        loadFragment(selectedFragment);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}