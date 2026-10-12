package com.example.karta.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.karta.R;
import com.example.karta.activities.LoginActivity;
import com.example.karta.database.DBHelper;
import com.example.karta.models.User;
import com.example.karta.utils.QRCodeGenerator;
import com.example.karta.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView tvAvatarLetter, tvProfileName, tvProfilePhone;
    private TextView tvCardStatusBadge, tvProfileCardNumber;
    private TextView tvProfileBonusBalance, tvProfileStatus;
    private ImageView ivQRCode, ivEditProfile;
    private Button btnLogout;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Привязка элементов интерфейса
        tvAvatarLetter = view.findViewById(R.id.tvAvatarLetter);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        tvCardStatusBadge = view.findViewById(R.id.tvCardStatusBadge);
        tvProfileCardNumber = view.findViewById(R.id.tvProfileCardNumber);
        tvProfileBonusBalance = view.findViewById(R.id.tvProfileBonusBalance);
        tvProfileStatus = view.findViewById(R.id.tvProfileStatus);
        ivQRCode = view.findViewById(R.id.ivQRCode);
        ivEditProfile = view.findViewById(R.id.ivEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        dbHelper = new DBHelper(getContext());
        sessionManager = new SessionManager(getContext());

        loadProfileData();

        ivEditProfile.setOnClickListener(v -> showEditNameDialog());

        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData(); // обновляем данные при каждом возврате на экран
    }

    private void loadProfileData() {
        int userId = sessionManager.getUserId();
        currentUser = dbHelper.getUserById(userId);

        if (currentUser == null) return;

        // Имя и телефон
        tvProfileName.setText(currentUser.getName());
        tvProfilePhone.setText(formatPhone(currentUser.getPhone()));

        // Первая буква имени для аватара
        if (!currentUser.getName().isEmpty()) {
            tvAvatarLetter.setText(String.valueOf(currentUser.getName().charAt(0)).toUpperCase());
        }

        // Номер карты
        tvProfileCardNumber.setText(currentUser.getFormattedCardNumber());

        // Статус карты (активна / не активна)
        if (currentUser.isCardActive()) {
            tvCardStatusBadge.setText("Карта активна");
            tvCardStatusBadge.setBackgroundResource(R.drawable.bg_badge_active);
        } else {
            tvCardStatusBadge.setText("Карта не активирована");
            tvCardStatusBadge.setBackgroundResource(R.drawable.bg_badge_inactive);
        }

        // Баланс и уровень
        tvProfileBonusBalance.setText(currentUser.getBonusBalance() + " Б");
        tvProfileStatus.setText(currentUser.getStatus());

        // Генерация QR-кода на основе номера карты
        generateQRCode(currentUser.getCardNumber());
    }

    private void generateQRCode(String cardNumber) {
        Bitmap qrBitmap = QRCodeGenerator.generateQRCode(cardNumber, 500);
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap);
        }
    }

    // Форматирование телефона для красивого отображения: +7 (999) 123-45-67
    private String formatPhone(String phone) {
        if (phone == null || phone.length() < 10) return phone;

        String last10 = phone.length() > 10 ? phone.substring(phone.length() - 10) : phone;

        return "+7 (" + last10.substring(0, 3) + ") " +
                last10.substring(3, 6) + "-" +
                last10.substring(6, 8) + "-" +
                last10.substring(8, 10);
    }

    // Диалог редактирования имени
    private void showEditNameDialog() {
        if (currentUser == null) return;

        EditText input = new EditText(getContext());
        input.setText(currentUser.getName());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(getResources().getColor(R.color.text_white));
        input.setHint("Введите новое имя");
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(getContext())
                .setTitle("Изменить имя")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        boolean updated = dbHelper.updateUserName(currentUser.getId(), newName);
                        if (updated) {
                            Toast.makeText(getContext(), "Имя обновлено", Toast.LENGTH_SHORT).show();
                            loadProfileData();
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(getContext())
                .setTitle("Выход")
                .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                .setPositiveButton("Выйти", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}