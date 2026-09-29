package com.example.dymsystem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UnassignedInvoicesActivity extends AppCompatActivity {

    private InvoiceAdapter invoiceAdapter;
    private Cursor invoiceCursor;

    private TextView textTitle;
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_unassigned_invoices
        );

        ImageButton buttonBack =
                findViewById(
                        R.id.buttonBackUnassignedInvoices
                );

        buttonBack.setOnClickListener(v -> finish());

        textTitle =
                findViewById(
                        R.id.textUnassignedInvoicesTitle
                );

        recycler =
                findViewById(
                        R.id.recyclerUnassignedInvoices
                );

        recycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        loadInvoices();
    }

    private void loadInvoices() {

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        invoiceCursor =
                dbHelper.getInvoicesWithoutClient();

        int invoiceCount =
                invoiceCursor.getCount();

        textTitle.setText(
                "Накладные без клиента (" +
                        invoiceCount +
                        ")"
        );

        invoiceAdapter =
                new InvoiceAdapter(
                        this,
                        invoiceCursor,
                        this::deleteInvoice
                );

        recycler.setAdapter(
                invoiceAdapter
        );
    }

    private void deleteInvoice(long invoiceId) {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        db.delete(
                "invoices",
                "id = ?",
                new String[]{
                        String.valueOf(invoiceId)
                }
        );

        db.close();

        loadInvoices();

        Toast.makeText(
                this,
                "Накладная удалена",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        super.onDestroy();
    }
}