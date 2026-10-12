package com.example.karta.activities;

import android.app.AlertDialog;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegName, etRegPhone, etRegPassword, etRegConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this);

        etRegName = findViewById(R.id.etRegName);
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String name = etRegName.getText().toString().trim();
        String phone = etRegPhone.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // Проверки заполнения
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка телефона (простая: только цифры, минимум 10 символов)
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 10) {
            Toast.makeText(this, "Введите корректный номер телефона", Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Пароль должен быть не короче 4 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        User newUser = dbHelper.registerUser(name, digitsOnly, hashedPassword);

        if (newUser == null) {
            Toast.makeText(this, "Пользователь с таким номером уже зарегистрирован", Toast.LENGTH_SHORT).show();
            return;
        }

        showCardCreatedDialog(newUser);
    }

    // Показываем диалог с номером созданной (пока неактивной) карты
    private void showCardCreatedDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Регистрация успешна! 🎉")
                .setMessage("Ваша бонусная карта создана:\n\n" +
                        user.getFormattedCardNumber() +
                        "\n\nКарта будет активирована при первой покупке в магазине.")
                .setCancelable(false)
                .setPositiveButton("Понятно", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .show();
    }
}