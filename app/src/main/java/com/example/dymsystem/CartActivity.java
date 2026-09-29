package com.example.dymsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import androidx.activity.OnBackPressedCallback;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private TextView textCartTotal;

    private CartAdapter cartAdapter;
    private EditText editCartDiscount;
    private EditText editCartInvoiceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);

        ImageButton buttonBackCart =
                findViewById(R.id.buttonBackCart);

        recyclerCart =
                findViewById(R.id.recyclerCart);

        textCartTotal =
                findViewById(R.id.textCartTotal);
        editCartDiscount =
                findViewById(R.id.editCartDiscount);

        editCartInvoiceName =
                findViewById(R.id.editCartInvoiceName);

        editCartDiscount.addTextChangedListener(
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
                        updateCartTotal();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        MaterialButton buttonAddCartProduct =
                findViewById(R.id.buttonAddCartProduct);

        MaterialButton buttonClearCart =
                findViewById(R.id.buttonClearCart);

        MaterialButton buttonSaveCart =
                findViewById(R.id.buttonSaveCart);

        // Кнопка назад
        buttonBackCart.setOnClickListener(v -> finish());

        // Список корзины
        recyclerCart.setLayoutManager(
                new LinearLayoutManager(this)
        );

        cartAdapter = new CartAdapter(
                this,
                CartManager.getInstance().getItems(),
                this::updateCartTotal
        );

        recyclerCart.setAdapter(cartAdapter);

        updateCartTotal();

        // Добавить товар
        buttonAddCartProduct.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CartActivity.this,
                    ProductSelectionActivity.class
            );

            startActivity(intent);
        });

        // Очистить корзину
        buttonClearCart.setOnClickListener(v -> {

            if (CartManager.getInstance().getItems().isEmpty()) {
                Toast.makeText(
                        this,
                        "Накладная уже пуста",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Очистить накладную?")
                    .setMessage("Все добавленные товары будут удалены.")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Очистить", (dialog, which) -> {

                        CartManager.getInstance().clear();

                        cartAdapter.notifyDataSetChanged();

                        updateCartTotal();
                    })
                    .show();
        });

        // Сохранить
        buttonSaveCart.setOnClickListener(v -> {

            long invoiceId = saveInvoiceToDatabase();

            if (invoiceId != -1) {

                saveInvoiceToExcel(invoiceId);

                Toast.makeText(
                        this,
                        "Накладная сохранена №" + invoiceId,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void updateCartTotal() {

        double total =
                CartManager.getInstance().getTotalPrice();

        double discount = 0;

        String discountText =
                editCartDiscount.getText()
                        .toString()
                        .trim();

        if (!discountText.isEmpty()) {

            try {

                discount =
                        Double.parseDouble(
                                discountText.replace(",", ".")
                        );

            } catch (NumberFormatException e) {

                discount = 0;
            }
        }

        if (discount < 0) {
            discount = 0;
        }

        if (discount > 100) {
            discount = 100;
        }

        double finalTotal =
                total - (total * discount / 100);

        textCartTotal.setText(
                String.format(
                        "Итого: %.0f грн",
                        finalTotal
                )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (cartAdapter != null) {

            cartAdapter.notifyDataSetChanged();

            updateCartTotal();
        }
    }
    private long saveInvoiceToDatabase() {

        CartManager cartManager = CartManager.getInstance();

        if (cartManager.getItems().isEmpty()) {
            Toast.makeText(
                    this,
                    "Корзина пуста",
                    Toast.LENGTH_SHORT
            ).show();

            return -1;
        }

        double subtotal = cartManager.getTotalPrice();

        double discount = 0;

        String discountText =
                editCartDiscount.getText().toString().trim();

        if (!discountText.isEmpty()) {
            try {
                discount = Double.parseDouble(
                        discountText.replace(",", ".")
                );
            } catch (NumberFormatException ignored) {
            }
        }

        if (discount < 0) discount = 0;
        if (discount > 100) discount = 100;

        double total =
                subtotal - (subtotal * discount / 100);

        EditText editCartClient =
                findViewById(R.id.editCartClient);

        String client =
                editCartClient.getText().toString().trim();
        String invoiceName =
                editCartInvoiceName.getText()
                        .toString()
                        .trim();

        String createdAt =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                ).format(new Date());

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        db.beginTransaction();

        long invoiceId;

        try {

            // Создаём накладную
            ContentValues invoiceValues =
                    new ContentValues();

            invoiceValues.put(
                    "created_at",
                    createdAt
            );

            if (client.isEmpty()) {
                invoiceValues.putNull("client");
            } else {
                invoiceValues.put(
                        "client",
                        client
                );
            }

            if (invoiceName.isEmpty()) {
                invoiceValues.putNull("name");
            } else {
                invoiceValues.put(
                        "name",
                        invoiceName
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

            invoiceId = db.insertOrThrow(
                    "invoices",
                    null,
                    invoiceValues
            );

            // Добавляем товары
            for (CartItem item : cartManager.getItems()) {

                Product product =
                        item.getProduct();

                ContentValues itemValues =
                        new ContentValues();

                itemValues.put(
                        "invoice_id",
                        invoiceId
                );

                itemValues.put(
                        "product_id",
                        product.getId()
                );

                itemValues.put(
                        "product_name",
                        product.getName()
                );

                itemValues.put(
                        "price",
                        product.getPrice()
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

        return invoiceId;
    }

    private void saveInvoiceToExcel(long invoiceId) {
        try {

            CartManager cartManager =
                    CartManager.getInstance();

            if (cartManager.getItems().isEmpty()) {
                Toast.makeText(
                        this,
                        "Корзина пуста",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String invoiceName =
                    editCartInvoiceName.getText()
                            .toString()
                            .trim();

            if (invoiceName.isEmpty()) {
                invoiceName = "Накладная";
            }

// Убираем символы, которые нельзя использовать в имени файла
            invoiceName = invoiceName.replaceAll(
                    "[\\\\/:*?\"<>|]",
                    "_"
            );

            String fileName =
                    invoiceId + "_" +
                            invoiceName +
                            ".xlsx";

            // Папка Documents
            File folder =
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS
                    );

            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file =
                    new File(folder, fileName);

            FileOutputStream fos =
                    new FileOutputStream(file);

            Workbook wb =
                    new Workbook(
                            fos,
                            "Dymsystem",
                            "1.0"
                    );

            Worksheet ws =
                    wb.newWorksheet("Накладна");

            // Дата
            ws.range(2, 0, 2, 1).merge();

            String date =
                    new SimpleDateFormat(
                            "dd.MM.yyyy",
                            Locale.getDefault()
                    ).format(new Date());

            ws.value(
                    2,
                    0,
                    "Дата: " + date
            );

            // Клиент
            EditText editCartClient =
                    findViewById(R.id.editCartClient);

            String client =
                    editCartClient
                            .getText()
                            .toString()
                            .trim();

            if (client.isEmpty()) {
                ws.value(
                        2,
                        2,
                        "Клієнт: __________________"
                );
            } else {
                ws.value(
                        2,
                        2,
                        "Клієнт: " + client
                );
            }

            ws.range(2, 2, 2, 3).merge();

            // Заголовок таблицы
            int startRow = 4;
            int row = startRow + 1;

            ws.value(startRow, 0, "№");
            ws.value(startRow, 1, "Найменування");
            ws.value(startRow, 2, "Ціна");
            ws.value(startRow, 3, "Кіл-ть");
            ws.value(startRow, 4, "Сума");

            ws.range(startRow, 0, startRow, 4)
                    .style()
                    .bold()
                    .horizontalAlignment("center")
                    .fillColor("D9EAF7")
                    .borderStyle("thin")
                    .set();

            // Товары
            for (int i = 0;
                 i < cartManager.getItems().size();
                 i++) {

                CartItem item =
                        cartManager.getItems().get(i);

                Product product =
                        item.getProduct();

                ws.value(
                        row,
                        0,
                        i + 1
                );

                ws.value(
                        row,
                        1,
                        product.getName()
                );

                ws.value(
                        row,
                        2,
                        product.getPrice()
                );

                ws.value(
                        row,
                        3,
                        item.getQuantity()
                );

                int excelRow =
                        row + 1;

                ws.formula(
                        row,
                        4,
                        "C" + excelRow +
                                "*D" + excelRow
                );

                row++;
            }

            // Диапазоны таблицы
            int firstItemExcelRow =
                    startRow + 2;

            int lastItemExcelRow =
                    row;

            ws.range(
                            startRow + 1,
                            0,
                            row - 1,
                            0
                    ).style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow + 1,
                            2,
                            row - 1,
                            2
                    ).style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow + 1,
                            3,
                            row - 1,
                            3
                    ).style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow + 1,
                            4,
                            row - 1,
                            4
                    ).style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow,
                            0,
                            row - 1,
                            4
                    ).style()
                    .borderStyle("thin")
                    .set();

            // Итого
            row++;

            ws.value(
                    row,
                    1,
                    "Разом:"
            );

            int totalExcelRow =
                    row + 1;

            ws.formula(
                    row,
                    4,
                    "SUM(E" +
                            firstItemExcelRow +
                            ":E" +
                            lastItemExcelRow +
                            ")"
            );

            ws.range(
                            row,
                            1,
                            row,
                            4
                    ).style()
                    .bold()
                    .borderStyle("thin")
                    .set();

            // Скидка
            double discount = 0;

            try {

                String discountText =
                        editCartDiscount
                                .getText()
                                .toString()
                                .trim();

                if (!discountText.isEmpty()) {
                    discount =
                            Double.parseDouble(
                                    discountText.replace(",", ".")
                            );
                }

            } catch (Exception ignored) {
            }

            if (discount < 0) {
                discount = 0;
            }

            if (discount > 100) {
                discount = 100;
            }

            if (discount > 0) {

                row++;

                int discountExcelRow =
                        row + 1;

                ws.value(
                        row,
                        1,
                        "Знижка в %:"
                );

                ws.value(
                        row,
                        3,
                        discount
                );

                ws.formula(
                        row,
                        4,
                        "E" +
                                totalExcelRow +
                                "*D" +
                                discountExcelRow +
                                "/100"
                );

                ws.range(
                                row,
                                1,
                                row,
                                4
                        ).style()
                        .bold()
                        .borderStyle("thin")
                        .set();

                row++;

                ws.value(
                        row,
                        1,
                        "Разом зі знижкою:"
                );

                ws.formula(
                        row,
                        4,
                        "E" +
                                totalExcelRow +
                                "-E" +
                                discountExcelRow
                );

                ws.range(
                                row,
                                1,
                                row,
                                4
                        ).style()
                        .bold()
                        .borderStyle("thin")
                        .set();
            }

            // Ширина колонок
            ws.width(0, 6);
            ws.width(1, 45);
            ws.width(2, 10);
            ws.width(3, 8);
            ws.width(4, 15);

            wb.finish();
            fos.close();

            MediaScannerConnection.scanFile(
                    this,
                    new String[]{
                            file.getAbsolutePath()
                    },
                    null,
                    null
            );

            Toast.makeText(
                    this,
                    "Excel сохранён:\n" +
                            file.getAbsolutePath(),
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Ошибка Excel:\n" +
                            e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}