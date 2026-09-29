package com.example.dymsystem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InvoiceDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "invoices.db";
    private static final int DATABASE_VERSION = 2;

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
    }
}