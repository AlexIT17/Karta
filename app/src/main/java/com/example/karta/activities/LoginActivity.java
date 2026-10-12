package com.example.karta.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.karta.R;
import com.example.karta.database.DBHelper;
import com.example.karta.models.User;
import com.example.karta.utils.PasswordUtils;
import com.example.karta.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginIdentifier, etLoginPassword;
    private Button btnLogin;
    private TextView tvGoRegister;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Если пользователь уже вошёл ранее — сразу открываем главный экран
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etLoginIdentifier = findViewById(R.id.etLoginIdentifier);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String identifier = etLoginIdentifier.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(identifier) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = dbHelper.loginUser(identifier, hashedPassword);

        if (user != null) {
            sessionManager.saveUserSession(user.getId());
            Toast.makeText(this, "Добро пожаловать, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Неверный телефон/карта или пароль", Toast.LENGTH_SHORT).show();
        }
    }
}