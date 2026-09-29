package com.example.dymsystem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClientAdapter
        extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private final List<Client> clients;
    private final Context context;

    public ClientAdapter(
            Context context,
            List<Client> clients
    ) {
        this.context = context;
        this.clients = clients;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_client,
                        parent,
                        false
                );

        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ClientViewHolder holder,
            int position
    ) {
        Client client = clients.get(position);

        if (client.getId() == -1) {

            // Специальная карточка "Без клиента"

            holder.textClientName.setText(
                    "Без клиента"
            );

            holder.textClientPhone.setVisibility(
                    View.VISIBLE
            );

            holder.textClientPhone.setText(
                    "Накладные без указанного клиента"
            );

            holder.buttonEditClient.setVisibility(
                    View.GONE
            );

            holder.buttonDeleteClient.setVisibility(
                    View.GONE
            );

            holder.buttonViewClient.setOnClickListener(v -> {

                Intent intent = new Intent(
                        context,
                        UnassignedInvoicesActivity.class
                );

                context.startActivity(intent);
            });


        } else {

            // Обычный клиент

            holder.buttonEditClient.setVisibility(
                    View.VISIBLE
            );

            holder.buttonDeleteClient.setVisibility(
                    View.VISIBLE
            );

            holder.textClientName.setText(
                    client.getName()
            );

            if (client.getPhone() == null ||
                    client.getPhone().isEmpty()) {

                holder.textClientPhone.setVisibility(
                        View.GONE
                );

            } else {

                holder.textClientPhone.setVisibility(
                        View.VISIBLE
                );

                holder.textClientPhone.setText(
                        client.getPhone()
                );
            }

            holder.buttonViewClient.setOnClickListener(v -> {

                Intent intent = new Intent(
                        context,
                        ClientDetailsActivity.class
                );

                intent.putExtra(
                        "client_id",
                        client.getId()
                );

                intent.putExtra(
                        "client_name",
                        client.getName()
                );

                context.startActivity(intent);
            });
            holder.buttonEditClient.setOnClickListener(v -> {

                Intent intent = new Intent(
                        context,
                        AddClientActivity.class
                );

                intent.putExtra(
                        "client_id",
                        client.getId()
                );

                context.startActivity(intent);
            });
            holder.buttonDeleteClient.setOnClickListener(v -> {

                new android.app.AlertDialog.Builder(context)
                        .setTitle("Удалить клиента?")
                        .setMessage(
                                "Клиент будет удалён. Его накладные останутся сохранёнными."
                        )
                        .setNegativeButton(
                                "Отмена",
                                null
                        )
                        .setPositiveButton(
                                "Удалить",
                                (dialog, which) -> {

                                    InvoiceDbHelper dbHelper =
                                            new InvoiceDbHelper(context);

                                    android.database.sqlite.SQLiteDatabase db =
                                            dbHelper.getWritableDatabase();

                                    db.delete(
                                            "clients",
                                            "id = ?",
                                            new String[]{
                                                    String.valueOf(client.getId())
                                            }
                                    );

                                    db.close();

                                    clients.remove(position);

                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(
                                            position,
                                            clients.size()
                                    );

                                    android.widget.Toast.makeText(
                                            context,
                                            "Клиент удалён",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                }
                        )
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    static class ClientViewHolder
            extends RecyclerView.ViewHolder {

        TextView textClientName;
        TextView textClientPhone;

        ImageButton buttonViewClient;
        ImageButton buttonEditClient;
        ImageButton buttonDeleteClient;

        public ClientViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            textClientName =
                    itemView.findViewById(
                            R.id.textClientName
                    );

            textClientPhone =
                    itemView.findViewById(
                            R.id.textClientPhone
                    );

            buttonViewClient =
                    itemView.findViewById(
                            R.id.buttonViewClient
                    );
            buttonEditClient =
                    itemView.findViewById(
                            R.id.buttonEditClient
                    );

            buttonDeleteClient =
                    itemView.findViewById(
                            R.id.buttonDeleteClient
                    );
        }
    }
}