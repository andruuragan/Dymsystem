package com.example.dymsystem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CatalogRepository {

    private final CatalogDbHelper dbHelper;

    public CatalogRepository(android.content.Context context) {
        dbHelper = new CatalogDbHelper(context);
    }

    public List<Product> getProducts() {

        List<Product> products = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                "catalog",
                new String[]{
                        "id",
                        "name",
                        "type",
                        "thickness",
                        "grade",
                        "diameter",
                        "casing",
                        "chimneyType",
                        "price",
                        "image_hash"
                },
                null,
                null,
                null,
                null,
                "id ASC"
        );

        try {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String thickness = cursor.getString(cursor.getColumnIndexOrThrow("thickness"));
                int grade = cursor.getInt(cursor.getColumnIndexOrThrow("grade"));
                String diameter = cursor.getString(cursor.getColumnIndexOrThrow("diameter"));
                String casing = cursor.getString(cursor.getColumnIndexOrThrow("casing"));
                String chimneyType = cursor.getString(cursor.getColumnIndexOrThrow("chimneyType"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                String imageHash = cursor.getString(cursor.getColumnIndexOrThrow("image_hash"));

                products.add(new Product(
                        id,
                        name,
                        type,
                        thickness,
                        grade,
                        diameter,
                        casing,
                        chimneyType,
                        price,
                        imageHash
                ));
            }
        } finally {
            cursor.close();
        }

        return products;
    }
}