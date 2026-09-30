package com.example.dymsystem;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InvoicesActivity extends AppCompatActivity {

    private InvoiceAdapter invoiceAdapter;
    private Cursor invoiceCursor;

    private TextView textTitle;
    private RecyclerView recycler;

    private AutoCompleteTextView editClient;

    private TextInputEditText editDateFrom;
    private TextInputEditText editDateTo;

    private final List<String> clientNames =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_invoices
        );

        ImageButton buttonBack =
                findViewById(
                        R.id.buttonBackInvoices
                );

        buttonBack.setOnClickListener(
                v -> finish()
        );

        textTitle =
                findViewById(
                        R.id.textInvoicesTitle
                );

        recycler =
                findViewById(
                        R.id.recyclerInvoices
                );

        editClient =
                findViewById(
                        R.id.editClient
                );

        editDateFrom =
                findViewById(
                        R.id.editDateFrom
                );

        editDateTo =
                findViewById(
                        R.id.editDateTo
                );

        recycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        editDateFrom.setOnClickListener(
                v -> showDatePicker(editDateFrom)
        );

        editDateTo.setOnClickListener(
                v -> showDatePicker(editDateTo)
        );

        findViewById(
                R.id.buttonApplyInvoiceFilter
        ).setOnClickListener(
                v -> applyFilters()
        );

        findViewById(
                R.id.buttonResetInvoiceFilter
        ).setOnClickListener(
                v -> resetFilters()
        );

        loadClientDropdown();

        loadInvoices();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recycler != null) {
            loadClientDropdown();
            loadInvoices();
        }
    }

    private void loadClientDropdown() {

        clientNames.clear();

        clientNames.add("Все");
        clientNames.add("Без клиента");

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        List<Client> clients =
                dbHelper.getAllClients();

        for (Client client : clients) {
            clientNames.add(client.getName());
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        clientNames
                );

        editClient.setAdapter(adapter);

        if (editClient.getText().toString().isEmpty()) {
            editClient.setText(
                    "Все",
                    false
            );
        }
    }

    private void loadInvoices() {

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        invoiceCursor =
                db.query(
                        "invoices",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "created_at DESC"
                );

        showInvoices();
    }

    private void applyFilters() {

        String selectedClient =
                editClient.getText()
                        .toString()
                        .trim();

        String dateFrom =
                editDateFrom.getText()
                        .toString()
                        .trim();

        String dateTo =
                editDateTo.getText()
                        .toString()
                        .trim();

        if (dateFrom.isEmpty() &&
                dateTo.isEmpty() &&
                selectedClient.isEmpty()) {

            loadInvoices();
            return;
        }

        if (!dateFrom.isEmpty() &&
                !dateTo.isEmpty()) {

            String sqlDateFrom =
                    convertDisplayDateToSql(dateFrom);

            String sqlDateTo =
                    convertDisplayDateToSql(dateTo);

            if (sqlDateFrom.isEmpty() ||
                    sqlDateTo.isEmpty()) {

                Toast.makeText(
                        this,
                        "Некорректная дата",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (sqlDateFrom.compareTo(sqlDateTo) > 0) {

                Toast.makeText(
                        this,
                        "Дата от не может быть позже даты до",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
        }

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        StringBuilder selection =
                new StringBuilder();

        List<String> selectionArgs =
                new ArrayList<>();

        // Фильтр клиента
        if (!selectedClient.isEmpty() &&
                !selectedClient.equals("Все")) {

            if (selectedClient.equals("Без клиента")) {

                selection.append(
                        "(client IS NULL OR client = '')"
                );

            } else {

                selection.append(
                        "client = ?"
                );

                selectionArgs.add(
                        selectedClient
                );
            }
        }

        // Фильтр даты от
        if (!dateFrom.isEmpty()) {

            if (selection.length() > 0) {
                selection.append(" AND ");
            }

            selection.append(
                    "created_at >= ?"
            );

            selectionArgs.add(
                    convertDisplayDateToSql(dateFrom)
                            + " 00:00:00"
            );
        }

        // Фильтр даты до
        if (!dateTo.isEmpty()) {

            if (selection.length() > 0) {
                selection.append(" AND ");
            }

            selection.append(
                    "created_at <= ?"
            );

            selectionArgs.add(
                    convertDisplayDateToSql(dateTo)
                            + " 23:59:59"
            );
        }

        invoiceCursor =
                db.query(
                        "invoices",
                        null,
                        selection.length() > 0
                                ? selection.toString()
                                : null,
                        selectionArgs.toArray(
                                new String[0]
                        ),
                        null,
                        null,
                        "created_at DESC"
                );

        showInvoices();
    }

    private void resetFilters() {

        editClient.setText(
                "Все",
                false
        );

        editDateFrom.setText("");
        editDateTo.setText("");

        loadInvoices();
    }

    private void showInvoices() {

        int invoiceCount =
                invoiceCursor.getCount();

        textTitle.setText(
                "Накладные (" +
                        invoiceCount +
                        ")"
        );

        invoiceAdapter =
                new InvoiceAdapter(
                        this,
                        invoiceCursor,
                        this::deleteInvoice,
                        true
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
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
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

    @Override
    protected void onDestroy() {

        if (invoiceCursor != null &&
                !invoiceCursor.isClosed()) {

            invoiceCursor.close();
        }

        super.onDestroy();
    }
}