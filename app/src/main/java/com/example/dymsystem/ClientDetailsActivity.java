package com.example.dymsystem;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.database.Cursor;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Locale;
import com.google.android.material.textfield.TextInputEditText;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

public class ClientDetailsActivity extends AppCompatActivity {

    private TextInputEditText editDateFrom;
    private TextInputEditText editDateTo;
    private Client client;

    private InvoiceAdapter invoiceAdapter;
    private Cursor invoiceCursor;

    private TextView textInvoicesTitle;
    private RecyclerView recyclerClientInvoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client_details);

        ImageButton buttonBack =
                findViewById(R.id.buttonBackClientDetails);

        buttonBack.setOnClickListener(v -> finish());

        long clientId =
                getIntent().getLongExtra(
                        "client_id",
                        -1
                );

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        client =
                dbHelper.getClientById(clientId);
        if (client == null) {
            finish();
            return;
        }

        TextView textTitle =
                findViewById(
                        R.id.textClientDetailsTitle
                );

        TextView textName =
                findViewById(
                        R.id.textDetailName
                );

        TextView textPhone =
                findViewById(
                        R.id.textDetailPhone
                );

        TextView textEmail =
                findViewById(
                        R.id.textDetailEmail
                );

        TextView textAddress =
                findViewById(
                        R.id.textDetailAddress
                );

        TextView textNote =
                findViewById(
                        R.id.textDetailNote
                );

        textTitle.setText(client.getName());

        textName.setText(
                "Имя: " + client.getName()
        );

        textPhone.setText(
                "Телефон: " +
                        valueOrDash(client.getPhone())
        );

        textEmail.setText(
                "Email: " +
                        valueOrDash(client.getEmail())
        );

        textAddress.setText(
                "Адрес: " +
                        valueOrDash(client.getAddress())
        );

        textNote.setText(
                "Примечание: " +
                        valueOrDash(client.getNote())
        );
        textInvoicesTitle =
                findViewById(
                        R.id.textClientInvoicesTitle
                );
        recyclerClientInvoices =
                findViewById(
                        R.id.recyclerClientInvoices
                );

        recyclerClientInvoices.setLayoutManager(
                new LinearLayoutManager(this)
        );
        editDateFrom =
                findViewById(R.id.editDateFrom);

        editDateTo =
                findViewById(R.id.editDateTo);

        editDateFrom.setOnClickListener(
                v -> showDatePicker(editDateFrom)
        );

        findViewById(R.id.buttonApplyDateFilter)
                .setOnClickListener(v -> applyDateFilter());

        findViewById(R.id.buttonResetDateFilter)
                .setOnClickListener(v -> resetDateFilter());

        editDateTo.setOnClickListener(
                v -> showDatePicker(editDateTo)
        );

        loadClientInvoices();
    }

    private void loadClientInvoices() {

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        invoiceCursor =
                dbHelper.getInvoicesByClient(
                        client.getName()
                );

        updateInvoiceList();
    }

    private void loadClientInvoices(
            String dateFrom,
            String dateTo
    ) {

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        invoiceCursor =
                dbHelper.getInvoicesByClientAndDate(
                        client.getName(),
                        dateFrom,
                        dateTo
                );

        updateInvoiceList();
    }

    private void updateInvoiceList() {

        int invoiceCount =
                invoiceCursor.getCount();

        textInvoicesTitle.setText(
                "Накладные клиента (" +
                        invoiceCount +
                        ")"
        );

        invoiceAdapter =
                new InvoiceAdapter(
                        this,
                        invoiceCursor,
                        this::deleteInvoice
                );

        recyclerClientInvoices.setAdapter(
                invoiceAdapter
        );
    }
    private void deleteInvoice(long invoiceId) {

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

        loadClientInvoices();

        Toast.makeText(
                this,
                "Накладная удалена",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void applyDateFilter() {

        String dateFrom =
                editDateFrom.getText()
                        .toString()
                        .trim();

        String dateTo =
                editDateTo.getText()
                        .toString()
                        .trim();

        if (dateFrom.isEmpty() || dateTo.isEmpty()) {

            Toast.makeText(
                    this,
                    "Выберите дату от и дату до",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (dateFrom.compareTo(dateTo) > 0) {

            Toast.makeText(
                    this,
                    "Дата от не может быть позже даты до",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String sqlDateFrom =
                convertDisplayDateToSql(dateFrom);

        String sqlDateTo =
                convertDisplayDateToSql(dateTo);

        loadClientInvoices(
                sqlDateFrom,
                sqlDateTo
        );
    }
    private void resetDateFilter() {

        editDateFrom.setText("");
        editDateTo.setText("");

        loadClientInvoices();
    }
    private String convertDisplayDateToSql(
            String date
    ) {

        String[] parts =
                date.split("\\.");

        if (parts.length != 3) {
            return "";
        }

        return parts[2] +
                "-" +
                parts[1] +
                "-" +
                parts[0];
    }
    private void showDatePicker(
            TextInputEditText target
    ) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d.%02d.%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            target.setText(date);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }
    private String valueOrDash(String value) {

        if (value == null || value.isEmpty()) {
            return "—";
        }

        return value;
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