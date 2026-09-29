package com.example.dymsystem;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.content.Intent;

public class ClientsActivity extends AppCompatActivity {

    private RecyclerView recyclerClients;
    private ClientAdapter clientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_clients);

        ImageButton buttonBackClients =
                findViewById(R.id.buttonBackClients);

        buttonBackClients.setOnClickListener(v -> finish());

        findViewById(R.id.buttonAddClient).setOnClickListener(v -> {

            Intent intent = new Intent(
                    ClientsActivity.this,
                    AddClientActivity.class
            );

            startActivity(intent);
        });


        recyclerClients =
                findViewById(R.id.recyclerClients);

        recyclerClients.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadClients();
    }

    private void loadClients() {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        List<Client> clients =
                dbHelper.getAllClients();

        // Специальная карточка для накладных
        // без указанного клиента
        clients.add(
                0,
                new Client(
                        -1,
                        "Без клиента",
                        "Накладные без указанного клиента",
                        null,
                        null,
                        null,
                        null
                )
        );

        clientAdapter =
                new ClientAdapter(
                        this,
                        clients
                );

        recyclerClients.setAdapter(
                clientAdapter
        );
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadClients();
    }
}