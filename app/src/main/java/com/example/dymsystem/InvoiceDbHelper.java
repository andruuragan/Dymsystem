package com.example.dymsystem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class InvoiceDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "invoices.db";
    private static final int DATABASE_VERSION = 3;

    public InvoiceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Таблица накладных
        db.execSQL(
                "CREATE TABLE invoices (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "created_at TEXT NOT NULL, " +
                        "client TEXT, " +
                        "name TEXT, " +
                        "discount REAL NOT NULL DEFAULT 0, " +
                        "subtotal REAL NOT NULL, " +
                        "total REAL NOT NULL" +
                        ")"
        );

        // Товары внутри накладной
        db.execSQL(
                "CREATE TABLE invoice_items (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "invoice_id INTEGER NOT NULL, " +
                        "product_id INTEGER NOT NULL, " +
                        "product_name TEXT NOT NULL, " +
                        "price REAL NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "total REAL NOT NULL, " +
                        "FOREIGN KEY(invoice_id) REFERENCES invoices(id) ON DELETE CASCADE" +
                        ")"
        );

        //Клиенты

        db.execSQL(
                "CREATE TABLE clients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL UNIQUE, " +
                        "phone TEXT, " +
                        "email TEXT, " +
                        "address TEXT, " +
                        "note TEXT, " +
                        "created_at TEXT NOT NULL" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE invoices ADD COLUMN name TEXT"
            );
        }

        if (oldVersion < 3) {
            db.execSQL(
                    "CREATE TABLE clients (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL UNIQUE, " +
                            "phone TEXT, " +
                            "email TEXT, " +
                            "address TEXT, " +
                            "note TEXT, " +
                            "created_at TEXT NOT NULL" +
                            ")"
            );
        }

    }
    public List<Client> getAllClients() {

        List<Client> clients = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "clients",
                null,
                null,
                null,
                null,
                null,
                "name COLLATE NOCASE ASC"
        );

        while (cursor.moveToNext()) {

            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            String phone = cursor.getString(
                    cursor.getColumnIndexOrThrow("phone")
            );

            String email = cursor.getString(
                    cursor.getColumnIndexOrThrow("email")
            );

            String address = cursor.getString(
                    cursor.getColumnIndexOrThrow("address")
            );

            String note = cursor.getString(
                    cursor.getColumnIndexOrThrow("note")
            );

            String createdAt = cursor.getString(
                    cursor.getColumnIndexOrThrow("created_at")
            );

            clients.add(
                    new Client(
                            id,
                            name,
                            phone,
                            email,
                            address,
                            note,
                            createdAt
                    )
            );
        }

        cursor.close();
        db.close();

        return clients;
    }
    public Client getClientById(long clientId) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "clients",
                null,
                "id = ?",
                new String[]{
                        String.valueOf(clientId)
                },
                null,
                null,
                null
        );

        Client client = null;

        if (cursor.moveToFirst()) {

            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
            );

            String phone = cursor.getString(
                    cursor.getColumnIndexOrThrow("phone")
            );

            String email = cursor.getString(
                    cursor.getColumnIndexOrThrow("email")
            );

            String address = cursor.getString(
                    cursor.getColumnIndexOrThrow("address")
            );

            String note = cursor.getString(
                    cursor.getColumnIndexOrThrow("note")
            );

            String createdAt = cursor.getString(
                    cursor.getColumnIndexOrThrow("created_at")
            );

            client = new Client(
                    id,
                    name,
                    phone,
                    email,
                    address,
                    note,
                    createdAt
            );
        }

        cursor.close();
        db.close();

        return client;
    }
    public Cursor getInvoicesByClient(String clientName) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                "invoices",
                null,
                "client = ?",
                new String[]{
                        clientName
                },
                null,
                null,
                "created_at DESC"
        );
    }

    public Cursor getInvoicesByClientAndDate(
            String clientName,
            String dateFrom,
            String dateTo
    ) {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                "invoices",
                null,
                "client = ? AND created_at >= ? AND created_at < ?",
                new String[]{
                        clientName,
                        dateFrom + " 00:00:00",
                        dateTo + " 23:59:59"
                },
                null,
                null,
                "created_at DESC"
        );
    }
    public Cursor getInvoicesWithoutClient() {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                "invoices",
                null,
                "client IS NULL OR client = ''",
                null,
                null,
                null,
                "created_at DESC"
        );
    }

    public Cursor getLatestInvoices() {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                "invoices",
                null,
                null,
                null,
                null,
                null,
                "created_at DESC",
                "5"
        );
    }

}