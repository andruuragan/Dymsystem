package com.example.dymsystem;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddClientActivity extends AppCompatActivity {

    private EditText editClientName;
    private EditText editClientPhone;
    private EditText editClientEmail;
    private EditText editClientAddress;
    private EditText editClientNote;
    private long clientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_client);
        clientId =
                getIntent().getLongExtra(
                        "client_id",
                        -1
                );

        ImageButton buttonBackAddClient =
                findViewById(R.id.buttonBackAddClient);

        buttonBackAddClient.setOnClickListener(v -> finish());

        editClientName =
                findViewById(R.id.editClientName);

        editClientPhone =
                findViewById(R.id.editClientPhone);

        editClientEmail =
                findViewById(R.id.editClientEmail);

        editClientAddress =
                findViewById(R.id.editClientAddress);

        editClientNote =
                findViewById(R.id.editClientNote);

        if (clientId != -1) {
            loadClient(clientId);
        }

        findViewById(R.id.buttonSaveClient)
                .setOnClickListener(v -> saveClient());
    }

    private void saveClient() {

        String name =
                editClientName.getText()
                        .toString()
                        .trim();

        String phone =
                editClientPhone.getText()
                        .toString()
                        .trim();

        String email =
                editClientEmail.getText()
                        .toString()
                        .trim();

        String address =
                editClientAddress.getText()
                        .toString()
                        .trim();

        String note =
                editClientNote.getText()
                        .toString()
                        .trim();

        if (name.isEmpty()) {

            editClientName.setError(
                    "Введите имя клиента"
            );

            editClientName.requestFocus();

            return;
        }

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        SQLiteDatabase db =
                dbHelper.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put("name", name);

        if (phone.isEmpty()) {
            values.putNull("phone");
        } else {
            values.put("phone", phone);
        }

        if (email.isEmpty()) {
            values.putNull("email");
        } else {
            values.put("email", email);
        }

        if (address.isEmpty()) {
            values.putNull("address");
        } else {
            values.put("address", address);
        }

        if (note.isEmpty()) {
            values.putNull("note");
        } else {
            values.put("note", note);
        }

        if (clientId == -1) {

            // =========================
            // НОВЫЙ КЛИЕНТ
            // =========================

            values.put(
                    "created_at",
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            java.util.Locale.getDefault()
                    ).format(new java.util.Date())
            );

            long newClientId =
                    db.insert(
                            "clients",
                            null,
                            values
                    );

            db.close();

            if (newClientId == -1) {

                Toast.makeText(
                        this,
                        "Не удалось сохранить клиента",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Клиент сохранён",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            // =========================
            // РЕДАКТИРОВАНИЕ
            // =========================

            // Получаем старое имя напрямую из этой же БД
            String oldName = null;

            android.database.Cursor cursor =
                    db.query(
                            "clients",
                            new String[]{"name"},
                            "id = ?",
                            new String[]{
                                    String.valueOf(clientId)
                            },
                            null,
                            null,
                            null
                    );

            if (cursor.moveToFirst()) {

                oldName =
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("name")
                        );
            }

            cursor.close();

            if (oldName == null) {

                db.close();

                Toast.makeText(
                        this,
                        "Клиент не найден",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Обновляем клиента
            int updatedRows =
                    db.update(
                            "clients",
                            values,
                            "id = ?",
                            new String[]{
                                    String.valueOf(clientId)
                            }
                    );

            if (updatedRows > 0) {

                // Если имя изменилось,
                // обновляем его и в накладных
                if (!oldName.equals(name)) {

                    ContentValues invoiceValues =
                            new ContentValues();

                    invoiceValues.put(
                            "client",
                            name
                    );

                    db.update(
                            "invoices",
                            invoiceValues,
                            "client = ?",
                            new String[]{
                                    oldName
                            }
                    );
                }
            }

            db.close();

            if (updatedRows == 0) {

                Toast.makeText(
                        this,
                        "Не удалось изменить клиента",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Данные клиента изменены",
                    Toast.LENGTH_SHORT
            ).show();
        }

        finish();
    }

    private void loadClient(long clientId) {

        InvoiceDbHelper dbHelper =
                new InvoiceDbHelper(this);

        Client client =
                dbHelper.getClientById(clientId);

        if (client == null) {
            return;
        }

        editClientName.setText(
                client.getName()
        );

        editClientPhone.setText(
                client.getPhone()
        );

        editClientEmail.setText(
                client.getEmail()
        );

        editClientAddress.setText(
                client.getAddress()
        );

        editClientNote.setText(
                client.getNote()
        );
    }
}