package com.example.dymsystem;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.database.Cursor;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private Cursor latestInvoiceCursor;
    private InvoiceAdapter latestInvoiceAdapter;
    private RecyclerView recyclerLatestInvoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        recyclerLatestInvoices =
                findViewById(
                        R.id.recyclerLatestInvoices
                );

        recyclerLatestInvoices.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadLatestInvoices();

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

        findViewById(R.id.cardInvoices).setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    InvoicesActivity.class
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
    private void loadLatestInvoices() {

        if (latestInvoiceCursor != null &&
                !latestInvoiceCursor.isClosed()) {

            latestInvoiceCursor.close();
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        latestInvoiceCursor =
                dbHelper.getLatestInvoices();

        latestInvoiceAdapter =
                new InvoiceAdapter(
                        this,
                        latestInvoiceCursor,
                        this::deleteLatestInvoice,
                        true
                );

        recyclerLatestInvoices.setAdapter(
                latestInvoiceAdapter
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recyclerLatestInvoices != null) {
            loadLatestInvoices();
        }
    }

    @Override
    protected void onDestroy() {

        if (latestInvoiceCursor != null &&
                !latestInvoiceCursor.isClosed()) {

            latestInvoiceCursor.close();
        }

        super.onDestroy();
    }

    private void deleteLatestInvoice(long invoiceId) {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        android.database.sqlite.SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        db.delete(
                "invoices",
                "id = ?",
                new String[]{
                        String.valueOf(invoiceId)
                }
        );

        db.close();

        loadLatestInvoices();
    }
}