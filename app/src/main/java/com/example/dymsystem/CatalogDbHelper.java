
package com.example.dymsystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CatalogDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "catalog.db";

    // Версия базы внутри SQLiteOpenHelper.
    private static final int DATABASE_VERSION = 1;

    // Увеличиваем это число, когда обновляем catalog.db в assets.
    private static final int ASSET_DATABASE_VERSION = 2;

    private static final String PREFS_NAME = "catalog_db_update";
    private static final String PREF_VERSION = "asset_database_version";

    private final Context context;

    public CatalogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Структура базы уже находится в assets/catalog.db.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Здесь пока нет изменений структуры таблиц.
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        copyDatabaseIfNeeded();
        return super.getReadableDatabase();
    }

    private void copyDatabaseIfNeeded() {

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        int installedVersion = prefs.getInt(PREF_VERSION, 0);

        File dbFile = context.getDatabasePath(DATABASE_NAME);

        // База уже актуальная — ничего не меняем.
        if (dbFile.exists()
                && installedVersion >= ASSET_DATABASE_VERSION) {
            return;
        }

        File parent = dbFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException(
                    "Не удалось создать папку базы данных");
        }

        // Сначала копируем новую базу во временный файл.
        File tempFile = new File(parent, DATABASE_NAME + ".new");

        try (InputStream input =
                     context.getAssets().open(DATABASE_NAME);
             OutputStream output =
                     new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int length;

            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }

            output.flush();

        } catch (Exception e) {
            tempFile.delete();
            throw new RuntimeException(
                    "Ошибка копирования новой catalog.db", e);
        }

        // Заменяем только базу каталога, не базу клиентов и накладных.
        context.deleteDatabase(DATABASE_NAME);

        if (!tempFile.renameTo(dbFile)) {
            tempFile.delete();
            throw new RuntimeException(
                    "Не удалось установить новую catalog.db");
        }

        // Запоминаем, какая версия базы установлена.
        prefs.edit()
                .putInt(PREF_VERSION, ASSET_DATABASE_VERSION)
                .apply();
    }

    @Override
    public synchronized void close() {
        super.close();
    }
}
