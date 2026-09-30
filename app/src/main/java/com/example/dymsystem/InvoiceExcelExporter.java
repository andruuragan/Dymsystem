package com.example.dymsystem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceExcelExporter {

    public static File export(
            Context context,
            long invoiceId
    ) throws Exception {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(context);

        SQLiteDatabase db =
                dbHelper.getReadableDatabase();

        // Получаем накладную
        Cursor invoiceCursor =
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

        if (!invoiceCursor.moveToFirst()) {

            invoiceCursor.close();
            db.close();

            throw new Exception(
                    "Накладная не найдена"
            );
        }

        String client =
                invoiceCursor.getString(
                        invoiceCursor.getColumnIndexOrThrow(
                                "client"
                        )
                );

        String invoiceName =
                invoiceCursor.getString(
                        invoiceCursor.getColumnIndexOrThrow(
                                "name"
                        )
                );

        double discount =
                invoiceCursor.getDouble(
                        invoiceCursor.getColumnIndexOrThrow(
                                "discount"
                        )
                );

        String createdAt =
                invoiceCursor.getString(
                        invoiceCursor.getColumnIndexOrThrow(
                                "created_at"
                        )
                );

        invoiceCursor.close();

        // Название файла
        String safeInvoiceName =
                invoiceName;

        if (safeInvoiceName == null ||
                safeInvoiceName.isEmpty()) {

            safeInvoiceName = "Накладная";
        }

        safeInvoiceName =
                safeInvoiceName.replaceAll(
                        "[\\\\/:*?\"<>|]",
                        "_"
                );

        String fileName =
                invoiceId +
                        "_" +
                        safeInvoiceName +
                        ".xlsx";

        // Временная папка приложения.
        // Она подходит для последующей передачи
        // через Android Share.
        File folder =
                new File(
                        context.getCacheDir(),
                        "invoices"
                );

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file =
                new File(
                        folder,
                        fileName
                );

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

        // =========================
        // Дата
        // =========================

        ws.range(2, 0, 2, 1).merge();

        String displayDate =
                formatInvoiceDate(createdAt);

        ws.value(
                2,
                0,
                "Дата: " + displayDate
        );

        // =========================
        // Клиент
        // =========================

        ws.range(2, 2, 2, 3).merge();

        if (client == null ||
                client.isEmpty()) {

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

        // =========================
        // Заголовок таблицы
        // =========================

        int startRow = 4;
        int row = startRow + 1;

        ws.value(startRow, 0, "№");
        ws.value(startRow, 1, "Найменування");
        ws.value(startRow, 2, "Ціна");
        ws.value(startRow, 3, "Кіл-ть");
        ws.value(startRow, 4, "Сума");

        ws.range(
                        startRow,
                        0,
                        startRow,
                        4
                )
                .style()
                .bold()
                .horizontalAlignment("center")
                .fillColor("D9EAF7")
                .borderStyle("thin")
                .set();

        // =========================
        // Товары
        // =========================

        Cursor itemsCursor =
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

        int itemNumber = 1;

        while (itemsCursor.moveToNext()) {

            String productName =
                    itemsCursor.getString(
                            itemsCursor.getColumnIndexOrThrow(
                                    "product_name"
                            )
                    );

            double price =
                    itemsCursor.getDouble(
                            itemsCursor.getColumnIndexOrThrow(
                                    "price"
                            )
                    );

            int quantity =
                    itemsCursor.getInt(
                            itemsCursor.getColumnIndexOrThrow(
                                    "quantity"
                            )
                    );

            ws.value(
                    row,
                    0,
                    itemNumber
            );

            ws.value(
                    row,
                    1,
                    productName
            );

            ws.value(
                    row,
                    2,
                    price
            );

            ws.value(
                    row,
                    3,
                    quantity
            );

            int excelRow =
                    row + 1;

            ws.formula(
                    row,
                    4,
                    "C" +
                            excelRow +
                            "*D" +
                            excelRow
            );

            row++;
            itemNumber++;
        }

        itemsCursor.close();

        // =========================
        // Форматирование таблицы
        // =========================

        if (row > startRow + 1) {

            ws.range(
                            startRow + 1,
                            0,
                            row - 1,
                            0
                    )
                    .style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow + 1,
                            2,
                            row - 1,
                            2
                    )
                    .style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow + 1,
                            3,
                            row - 1,
                            3
                    )
                    .style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow + 1,
                            4,
                            row - 1,
                            4
                    )
                    .style()
                    .horizontalAlignment("center")
                    .set();

            ws.range(
                            startRow,
                            0,
                            row - 1,
                            4
                    )
                    .style()
                    .borderStyle("thin")
                    .set();
        }

        // =========================
        // Итого
        // =========================

        int firstItemExcelRow =
                startRow + 2;

        int lastItemExcelRow =
                row;

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
                )
                .style()
                .bold()
                .borderStyle("thin")
                .set();

        // =========================
        // Скидка
        // =========================

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
                    )
                    .style()
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
                    )
                    .style()
                    .bold()
                    .borderStyle("thin")
                    .set();
        }

        // =========================
        // Ширина колонок
        // =========================

        ws.width(0, 6);
        ws.width(1, 45);
        ws.width(2, 10);
        ws.width(3, 8);
        ws.width(4, 15);

        wb.finish();
        fos.close();
        db.close();

        return file;
    }

    private static String formatInvoiceDate(
            String dateTime
    ) {

        if (dateTime == null ||
                dateTime.isEmpty()) {

            return new SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
            ).format(new Date());
        }

        try {

            Date date =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                    ).parse(dateTime);

            return new SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
            ).format(date);

        } catch (Exception e) {

            return dateTime;
        }
    }
}