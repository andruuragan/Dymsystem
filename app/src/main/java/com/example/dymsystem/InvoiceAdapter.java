package com.example.dymsystem;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InvoiceAdapter
        extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    public interface OnInvoiceDeleteListener {
        void onDeleteInvoice(long invoiceId);
    }

    private final Cursor cursor;
    private final Context context;
    private final OnInvoiceDeleteListener deleteListener;
    private final boolean showClientName;
    public InvoiceAdapter(
            Context context,
            Cursor cursor
    ) {
        this(
                context,
                cursor,
                null,
                false
        );
    }

    public InvoiceAdapter(
            Context context,
            Cursor cursor,
            OnInvoiceDeleteListener deleteListener
    ) {
        this(
                context,
                cursor,
                deleteListener,
                false
        );
    }

    public InvoiceAdapter(
            Context context,
            Cursor cursor,
            OnInvoiceDeleteListener deleteListener,
            boolean showClientName
    ) {
        this.context = context;
        this.cursor = cursor;
        this.deleteListener = deleteListener;
        this.showClientName = showClientName;
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_invoice,
                parent,
                false
        );

        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull InvoiceViewHolder holder,
            int position
    ) {

        if (!cursor.moveToPosition(position)) {
            return;
        }

        long id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id")
        );

        String createdAt = cursor.getString(
                cursor.getColumnIndexOrThrow("created_at")
        );

        String name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
        );

        String client = cursor.getString(
                cursor.getColumnIndexOrThrow("client")
        );

        double total = cursor.getDouble(
                cursor.getColumnIndexOrThrow("total")
        );

        if (showClientName) {

            String clientLabel =
                    (client == null || client.isEmpty())
                            ? "Без клиента"
                            : client;

            holder.textInvoiceId.setText(
                    "Накладная №" +
                            id +
                            " (" +
                            clientLabel +
                            ")"
            );

        } else {

            holder.textInvoiceId.setText(
                    "Накладная №" + id
            );
        }

        holder.textInvoiceDate.setText(
                formatDate(createdAt)
        );

        if (name == null || name.isEmpty()) {
            holder.textInvoiceName.setVisibility(View.GONE);
        } else {
            holder.textInvoiceName.setVisibility(View.VISIBLE);
            holder.textInvoiceName.setText(name);
        }

        holder.textInvoiceTotal.setText(
                String.format(
                        java.util.Locale.getDefault(),
                        "%.2f грн",
                        total
                )
        );

        // Открытие накладной
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    context,
                    InvoiceDetailsActivity.class
            );

            intent.putExtra(
                    "invoice_id",
                    id
            );

            context.startActivity(intent);
        });

        // Кнопка "Поделиться"
        holder.buttonShareInvoice.setOnClickListener(v -> {

            try {

                java.io.File file =
                        InvoiceExcelExporter.export(
                                context,
                                id
                        );

                android.net.Uri uri =
                        androidx.core.content.FileProvider.getUriForFile(
                                context,
                                context.getPackageName() + ".fileprovider",
                                file
                        );

                Intent shareIntent =
                        new Intent(Intent.ACTION_SEND);

                shareIntent.setType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );

                shareIntent.putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                );

                shareIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

                Intent chooser =
                        Intent.createChooser(
                                shareIntent,
                                "Поделиться накладной"
                        );

                context.startActivity(chooser);

            } catch (Exception e) {

                android.widget.Toast.makeText(
                        context,
                        "Ошибка создания Excel: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG
                ).show();
            }

        });



        // Кнопка удаления
        if (deleteListener == null) {

            holder.buttonDeleteInvoice.setVisibility(
                    View.GONE
            );

            holder.buttonDeleteInvoice.setOnClickListener(
                    null
            );

        } else {

            holder.buttonDeleteInvoice.setVisibility(
                    View.VISIBLE
            );

            holder.buttonDeleteInvoice.setOnClickListener(v -> {

                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Удалить накладную?")
                        .setMessage(
                                "Накладная №" +
                                        id +
                                        "\n\nЭто действие нельзя отменить."
                        )
                        .setNegativeButton(
                                "Отмена",
                                null
                        )
                        .setPositiveButton(
                                "Удалить",
                                (dialog, which) -> {

                                    deleteListener.onDeleteInvoice(
                                            id
                                    );
                                }
                        )
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    private String formatDate(String dateTime) {

        if (dateTime == null || dateTime.isEmpty()) {
            return "";
        }

        if (dateTime.length() >= 10) {

            String date = dateTime.substring(
                    0,
                    10
            );

            String[] parts = date.split("-");

            if (parts.length == 3) {

                return parts[2] +
                        "." +
                        parts[1] +
                        "." +
                        parts[0];
            }
        }

        return dateTime;
    }

    static class InvoiceViewHolder
            extends RecyclerView.ViewHolder {

        TextView textInvoiceId;
        TextView textInvoiceDate;
        TextView textInvoiceName;
        TextView textInvoiceTotal;
        ImageButton buttonShareInvoice;

        ImageButton buttonDeleteInvoice;

        public InvoiceViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            textInvoiceId =
                    itemView.findViewById(
                            R.id.textInvoiceId
                    );

            textInvoiceDate =
                    itemView.findViewById(
                            R.id.textInvoiceDate
                    );

            textInvoiceName =
                    itemView.findViewById(
                            R.id.textInvoiceName
                    );

            textInvoiceTotal =
                    itemView.findViewById(
                            R.id.textInvoiceTotal
                    );
            buttonShareInvoice =
                    itemView.findViewById(
                            R.id.buttonShareInvoice
                    );


            buttonDeleteInvoice =
                    itemView.findViewById(
                            R.id.buttonDeleteInvoice
                    );
        }
    }
}