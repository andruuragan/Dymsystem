package com.example.dymsystem;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Открываем выбор товаров по нажатию
        // на кнопку "СОЗДАТЬ НАКЛАДНУЮ"
        findViewById(R.id.cardCreateInvoice).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ProductSelectionActivity.class
            );

            startActivity(intent);
        });

        // Открываем клиентов
        findViewById(R.id.cardClients).setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    ClientsActivity.class
            );

            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }
}