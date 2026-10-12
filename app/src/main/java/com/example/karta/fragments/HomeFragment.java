package com.example.karta.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.karta.R;
import com.example.karta.database.DBHelper;
import com.example.karta.models.User;
import com.example.karta.utils.SessionManager;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvStatus, tvBonusCount, tvCardWarning;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvBonusCount = view.findViewById(R.id.tvBonusCount);
        tvCardWarning = view.findViewById(R.id.tvCardWarning);

        dbHelper = new DBHelper(getContext());
        sessionManager = new SessionManager(getContext());

        loadUserData();
        setupClickListeners(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); // обновляем баланс при каждом возврате на экран
    }

    private void loadUserData() {
        int userId = sessionManager.getUserId();
        User user = dbHelper.getUserById(userId);

        if (user != null) {
            tvGreeting.setText("Привет, " + user.getName() + "! 👋");
            tvStatus.setText("🏆 " + user.getStatus() + " уровень");
            tvBonusCount.setText(String.valueOf(user.getBonusBalance()));

            if (!user.isCardActive()) {
                tvCardWarning.setVisibility(View.VISIBLE);
            } else {
                tvCardWarning.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.cardFavorites).setOnClickListener(v ->
                Toast.makeText(getContext(), "Мои любимые товары", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.cardDiscounts).setOnClickListener(v ->
                Toast.makeText(getContext(), "Персональные скидки", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.cardHistory).setOnClickListener(v ->
                Toast.makeText(getContext(), "История начислений", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.cardPartners).setOnClickListener(v ->
                Toast.makeText(getContext(), "Партнёры", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.cardWheel).setOnClickListener(v ->
                Toast.makeText(getContext(), "Колесо призов", Toast.LENGTH_SHORT).show());
    }
}