package com.example.dymsystem;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.ContentValues;
import android.content.Intent;
import android.app.Activity;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AlertDialog;

public class InvoiceDetailsActivity extends AppCompatActivity {

    private long invoiceId;

    private EditText editInvoiceClient;
    private EditText editInvoiceName;
    private EditText editInvoiceDiscount;
    private TextView textInvoiceTotalDetails;

    private final List<InvoiceEditItem> invoiceItems =
            new ArrayList<>();

    private InvoiceEditAdapter invoiceEditAdapter;
    private ActivityResultLauncher<Intent> productSelectionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_invoice_details
        );

        productSelectionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {



                            if (result.getResultCode() != Activity.RESULT_OK) {
                                return;
                            }

                            Intent data = result.getData();

                            if (data == null) {



                                return;
                            }

                            int productId =
                                    data.getIntExtra(
                                            "product_id",
                                            -1
                                    );

                            String productName =
                                    data.getStringExtra(
                                            "product_name"
                                    );

                            double productPrice =
                                    data.getDoubleExtra(
                                            "product_price",
                                            0
                                    );

                            int quantity =
                                    data.getIntExtra(
                                            "product_quantity",
                                            1
                                    );



                            if (productId == -1 ||
                                    productName == null) {
                                return;
                            }

                            addProductToInvoice(
                                    productId,
                                    productName,
                                    productPrice,
                                    quantity
                            );
                        }
                );



        ImageButton buttonBack =
                findViewById(
                        R.id.buttonBackInvoiceDetails
                );

        buttonBack.setOnClickListener(v -> finish());

        invoiceId =
                getIntent().getLongExtra(
                        "invoice_id",
                        -1
                );

        if (invoiceId == -1) {
            finish();
            return;
        }

        editInvoiceClient =
                findViewById(
                        R.id.editInvoiceClient
                );

        editInvoiceName =
                findViewById(
                        R.id.editInvoiceName
                );

        editInvoiceDiscount =
                findViewById(
                        R.id.editInvoiceDiscount
                );

        textInvoiceTotalDetails =
                findViewById(
                        R.id.textInvoiceTotalDetails
                );

        RecyclerView recyclerInvoiceItems =
                findViewById(
                        R.id.recyclerInvoiceItems
                );

        recyclerInvoiceItems.setLayoutManager(
                new LinearLayoutManager(this)
        );

        invoiceEditAdapter =
                new InvoiceEditAdapter(
                        this,
                        invoiceItems,
                        this::updateInvoiceTotal
                );

        recyclerInvoiceItems.setAdapter(
                invoiceEditAdapter
        );

        loadInvoice();
        loadInvoiceItems();
        Button buttonClearInvoice =
                findViewById(
                        R.id.buttonClearInvoice
                );

        Button buttonSaveInvoice =
                findViewById(
                        R.id.buttonSaveInvoice
                );

        Button buttonAddInvoiceProduct =
                findViewById(
                        R.id.buttonAddInvoiceProduct
                );

        buttonAddInvoiceProduct.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            InvoiceDetailsActivity.this,
                            ProductSelectionActivity.class
                    );

            intent.putExtra(
                    "invoice_edit_mode",
                    true
            );

            productSelectionLauncher.launch(intent);
        });



        buttonClearInvoice.setOnClickListener(v -> {

            if (invoiceItems.isEmpty()) {
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Очистить накладную?")
                    .setMessage(
                            "Все товары будут удалены из текущего редактирования."
                    )
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton(
                            "Очистить",
                            (dialog, which) -> {

                                invoiceItems.clear();

                                invoiceEditAdapter.notifyDataSetChanged();

                                updateInvoiceTotal();
                            }
                    )
                    .show();
        });

        buttonSaveInvoice.setOnClickListener(v -> {

            if (invoiceItems.isEmpty()) {

                new AlertDialog.Builder(this)
                        .setTitle("Нет товаров")
                        .setMessage(
                                "Добавьте хотя бы один товар."
                        )
                        .setPositiveButton("OK", null)
                        .show();

                return;
            }

            saveAsNewInvoice();
        });

        editInvoiceDiscount.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        updateInvoiceTotal();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }
    private void addProductToInvoice(
            long productId,
            String productName,
            double productPrice,
            int quantity
    ) {


        // Если такой товар уже есть — увеличиваем количество
        for (int i = 0; i < invoiceItems.size(); i++) {

            InvoiceEditItem item =
                    invoiceItems.get(i);

            if (item.getProductId() == productId) {

                item.setQuantity(
                        item.getQuantity() + quantity
                );

                invoiceEditAdapter.notifyDataSetChanged();

                updateInvoiceTotal();

                return;
            }
        }

        // Если товара ещё нет — добавляем новый
        InvoiceEditItem newItem =
                new InvoiceEditItem(
                        productId,
                        productName,
                        productPrice,
                        quantity
                );

        invoiceItems.add(newItem);

        invoiceEditAdapter.notifyDataSetChanged();

        updateInvoiceTotal();
    }
    private void loadInvoice() {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        Cursor cursor =
                db.query(
                        "invoices",
                        null,
                        "id = ?",
                        new String[]{
                                String.valueOf(invoiceId)
                        },
                        null,
                        null,
                        null
                );

        if (cursor.moveToFirst()) {

            String client =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "client"
                            )
                    );

            String name =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "name"
                            )
                    );

            double discount =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "discount"
                            )
                    );

            TextView title =
                    findViewById(
                            R.id.textInvoiceDetailsTitle
                    );

            title.setText(
                    "Накладная №" + invoiceId
            );

            if (client != null && !client.isEmpty()) {
                editInvoiceClient.setText(client);
            }

            if (name != null && !name.isEmpty()) {
                editInvoiceName.setText(name);
            }

            if (discount > 0) {
                editInvoiceDiscount.setText(
                        String.format(
                                Locale.getDefault(),
                                "%.2f",
                                discount
                        )
                );
            }
        }

        cursor.close();
        db.close();
    }

    private void loadInvoiceItems() {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        Cursor cursor =
                db.query(
                        "invoice_items",
                        null,
                        "invoice_id = ?",
                        new String[]{
                                String.valueOf(invoiceId)
                        },
                        null,
                        null,
                        "id ASC"
                );

        invoiceItems.clear();

        while (cursor.moveToNext()) {

            long productId =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    "product_id"
                            )
                    );

            String productName =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    "product_name"
                            )
                    );

            double price =
                    cursor.getDouble(
                            cursor.getColumnIndexOrThrow(
                                    "price"
                            )
                    );

            int quantity =
                    cursor.getInt(
                            cursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            invoiceItems.add(
                    new InvoiceEditItem(
                            productId,
                            productName,
                            price,
                            quantity
                    )
            );
        }

        cursor.close();
        db.close();

        invoiceEditAdapter.notifyDataSetChanged();

        updateInvoiceTotal();
    }

    private void updateInvoiceTotal() {

        double subtotal = 0;

        for (InvoiceEditItem item : invoiceItems) {
            subtotal += item.getTotal();
        }

        double discount = 0;

        String discountText =
                editInvoiceDiscount.getText()
                        .toString()
                        .trim();

        if (!discountText.isEmpty()) {
            try {
                discount =
                        Double.parseDouble(
                                discountText.replace(",", ".")
                        );
            } catch (NumberFormatException ignored) {
                discount = 0;
            }
        }

        double total =
                subtotal -
                        (subtotal * discount / 100.0);

        textInvoiceTotalDetails.setText(
                String.format(
                        Locale.getDefault(),
                        "Итого: %.2f грн",
                        total
                )
        );
    }
    private void saveAsNewInvoice() {

        double subtotal = 0;

        for (InvoiceEditItem item : invoiceItems) {
            subtotal += item.getTotal();
        }

        double discount = 0;

        String discountText =
                editInvoiceDiscount.getText()
                        .toString()
                        .trim();

        if (!discountText.isEmpty()) {
            try {
                discount =
                        Double.parseDouble(
                                discountText.replace(",", ".")
                        );
            } catch (NumberFormatException ignored) {
                discount = 0;
            }
        }

        double total =
                subtotal -
                        (subtotal * discount / 100.0);

        String client =
                editInvoiceClient.getText()
                        .toString()
                        .trim();

        String name =
                editInvoiceName.getText()
                        .toString()
                        .trim();

        if (client.isEmpty()) {
            client = null;
        }

        if (name.isEmpty()) {
            name = null;
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        db.beginTransaction();

        long newInvoiceId = -1;

        try {

            // Если клиент указан — добавляем его в список клиентов,
            // если такого клиента ещё нет.
            if (client != null) {

                ContentValues clientValues =
                        new ContentValues();

                clientValues.put(
                        "name",
                        client
                );

                clientValues.put(
                        "created_at",
                        new java.text.SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                        ).format(
                                new java.util.Date()
                        )
                );

                db.insertWithOnConflict(
                        "clients",
                        null,
                        clientValues,
                        SQLiteDatabase.CONFLICT_IGNORE
                );
            }

            // Создаём НОВУЮ накладную.
            ContentValues invoiceValues =
                    new ContentValues();

            invoiceValues.put(
                    "created_at",
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                    ).format(
                            new java.util.Date()
                    )
            );

            if (client == null) {
                invoiceValues.putNull("client");
            } else {
                invoiceValues.put(
                        "client",
                        client
                );
            }

            if (name == null) {
                invoiceValues.putNull("name");
            } else {
                invoiceValues.put(
                        "name",
                        name
                );
            }

            invoiceValues.put(
                    "discount",
                    discount
            );

            invoiceValues.put(
                    "subtotal",
                    subtotal
            );

            invoiceValues.put(
                    "total",
                    total
            );

            newInvoiceId =
                    db.insertOrThrow(
                            "invoices",
                            null,
                            invoiceValues
                    );

            // Копируем товары в новую накладную.
            for (InvoiceEditItem item : invoiceItems) {

                ContentValues itemValues =
                        new ContentValues();

                itemValues.put(
                        "invoice_id",
                        newInvoiceId
                );

                itemValues.put(
                        "product_id",
                        item.getProductId()
                );

                itemValues.put(
                        "product_name",
                        item.getProductName()
                );

                itemValues.put(
                        "price",
                        item.getPrice()
                );

                itemValues.put(
                        "quantity",
                        item.getQuantity()
                );

                itemValues.put(
                        "total",
                        item.getTotal()
                );

                db.insertOrThrow(
                        "invoice_items",
                        null,
                        itemValues
                );
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
            db.close();
        }
        final long savedInvoiceId = newInvoiceId;
        new AlertDialog.Builder(this)
                .setTitle("Сохранено")
                .setMessage(
                        "Создана новая накладная №" +
                                savedInvoiceId
                )
                .setPositiveButton(
                        "Открыть",
                        (dialog, which) -> {

                            Intent intent =
                                    new Intent(
                                            this,
                                            InvoiceDetailsActivity.class
                                    );

                            intent.putExtra(
                                    "invoice_id",
                                    savedInvoiceId
                            );

                            startActivity(intent);

                            finish();
                        }
                )
                .setNegativeButton(
                        "Закрыть",
                        (dialog, which) -> finish()
                )
                .show();
    }
}