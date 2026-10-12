package com.example.karta.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.karta.models.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "loyalty.db";
    private static final int DB_VERSION = 1;

    // Таблица пользователей
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_PHONE = "phone";
    private static final String COL_PASSWORD = "password";
    private static final String COL_CARD_NUMBER = "card_number";
    private static final String COL_CARD_ACTIVE = "card_active";
    private static final String COL_BONUS_BALANCE = "bonus_balance";
    private static final String COL_STATUS = "status";
    private static final String COL_ROLE = "role";
    private static final String COL_REG_DATE = "reg_date";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_PHONE + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_CARD_NUMBER + " TEXT UNIQUE NOT NULL, " +
                COL_CARD_ACTIVE + " INTEGER DEFAULT 0, " +
                COL_BONUS_BALANCE + " INTEGER DEFAULT 0, " +
                COL_STATUS + " TEXT DEFAULT 'Бронзовый', " +
                COL_ROLE + " TEXT DEFAULT 'user', " +
                COL_REG_DATE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ===================== РЕГИСТРАЦИЯ =====================

    public User registerUser(String name, String phone, String passwordHash) {
        if (isPhoneExists(phone)) {
            return null; // такой телефон уже зарегистрирован
        }

        String cardNumber = generateUniqueCardNumber();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_PHONE, phone);
        values.put(COL_PASSWORD, passwordHash);
        values.put(COL_CARD_NUMBER, cardNumber);
        values.put(COL_CARD_ACTIVE, 0); // карта создаётся неактивной
        values.put(COL_BONUS_BALANCE, 0);
        values.put(COL_STATUS, "Бронзовый");
        values.put(COL_ROLE, "user");
        values.put(COL_REG_DATE, getCurrentDate());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();

        if (id == -1) return null;

        return new User((int) id, name, phone, passwordHash, cardNumber,
                0, 0, "Бронзовый", "user");
    }

    // ===================== ВХОД =====================

    // identifier - это либо телефон, либо номер карты
    public User loginUser(String identifier, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE (" + COL_PHONE + "=? OR " + COL_CARD_NUMBER + "=?) AND " +
                        COL_PASSWORD + "=?",
                new String[]{identifier, identifier, passwordHash});

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    // ===================== ПОЛУЧЕНИЕ ПОЛЬЗОВАТЕЛЯ =====================

    public User getUserById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_ID + "=?",
                new String[]{String.valueOf(id)});

        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    // ===================== АКТИВАЦИЯ КАРТЫ =====================
    // Будет вызываться из админ-панели при первой покупке клиента

    public boolean activateCard(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CARD_ACTIVE, 1);
        int rows = db.update(TABLE_USERS, values, COL_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // ===================== ОБНОВЛЕНИЕ ИМЕНИ =====================

    public boolean updateUserName(int userId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, newName);
        int rows = db.update(TABLE_USERS, values, COL_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // ===================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====================

    private boolean isPhoneExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TABLE_USERS + " WHERE " + COL_PHONE + "=?",
                new String[]{phone});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private boolean isCardNumberExists(String cardNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TABLE_USERS + " WHERE " + COL_CARD_NUMBER + "=?",
                new String[]{cardNumber});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Генерация уникального 12-значного номера карты
    private String generateUniqueCardNumber() {
        Random random = new Random();
        String cardNumber;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();
        } while (isCardNumberExists(cardNumber));
        return cardNumber;
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private User cursorToUser(Cursor cursor) {
        return new User(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CARD_NUMBER)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_CARD_ACTIVE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_BONUS_BALANCE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE))
        );
    }
}