package com.example.karta.models;

public class User {
    private int id;
    private String name;
    private String phone;
    private String password;
    private String cardNumber;
    private int cardActive; // 0 - неактивна, 1 - активна
    private int bonusBalance;
    private String status;
    private String role;

    public User(int id, String name, String phone, String password, String cardNumber,
                int cardActive, int bonusBalance, String status, String role) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.cardNumber = cardNumber;
        this.cardActive = cardActive;
        this.bonusBalance = bonusBalance;
        this.status = status;
        this.role = role;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getCardNumber() { return cardNumber; }
    public int getBonusBalance() { return bonusBalance; }
    public String getStatus() { return status; }
    public String getRole() { return role; }

    public boolean isCardActive() {
        return cardActive == 1;
    }

    // Форматирует номер карты для красивого отображения: 1234 5678 9012
    public String getFormattedCardNumber() {
        if (cardNumber == null || cardNumber.length() != 12) return cardNumber;
        return cardNumber.substring(0, 4) + " " +
                cardNumber.substring(4, 8) + " " +
                cardNumber.substring(8, 12);
    }
}