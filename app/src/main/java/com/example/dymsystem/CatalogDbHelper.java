package com.example.dymsystem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CatalogDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "catalog.db";
    private static final int DATABASE_VERSION = 1;

    private final Context context;

    public CatalogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // База уже создана и находится в assets.
        // Ничего не создаём здесь.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Пока обновлений структуры нет.
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        copyDatabaseIfNeeded();
        return super.getReadableDatabase();
    }

    private void copyDatabaseIfNeeded() {

        File dbFile = context.getDatabasePath(DATABASE_NAME);

        if (dbFile.exists()) {
            return;
        }

        File parent = dbFile.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (InputStream input = context.getAssets().open(DATABASE_NAME);
             OutputStream output = new FileOutputStream(dbFile)) {

            byte[] buffer = new byte[8192];
            int length;

            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка копирования catalog.db", e);
        }
    }
}