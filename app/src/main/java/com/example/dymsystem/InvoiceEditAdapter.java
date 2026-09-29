package com.example.dymsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class InvoiceEditAdapter
        extends RecyclerView.Adapter<InvoiceEditAdapter.InvoiceEditViewHolder> {

    private final Context context;
    private final List<InvoiceEditItem> items;
    private final Runnable onChanged;

    public InvoiceEditAdapter(
            Context context,
            List<InvoiceEditItem> items,
            Runnable onChanged
    ) {
        this.context = context;
        this.items = items;
        this.onChanged = onChanged;
    }

    @NonNull
    @Override
    public InvoiceEditViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_cart,
                        parent,
                        false
                );

        return new InvoiceEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull InvoiceEditViewHolder holder,
            int position
    ) {

        InvoiceEditItem item =
                items.get(position);

        holder.textProductName.setText(
                item.getProductName()
        );

        holder.textProductPrice.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f грн / шт.",
                        item.getPrice()
                )
        );

        holder.textQuantity.setText(
                String.valueOf(
                        item.getQuantity()
                )
        );

        updateItemTotal(
                holder,
                item
        );

        holder.buttonMinus.setOnClickListener(v -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            InvoiceEditItem currentItem =
                    items.get(currentPosition);

            if (currentItem.getQuantity() > 1) {

                currentItem.setQuantity(
                        currentItem.getQuantity() - 1
                );

                notifyItemChanged(
                        currentPosition
                );

                onChanged.run();
            }
        });

        holder.buttonPlus.setOnClickListener(v -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            InvoiceEditItem currentItem =
                    items.get(currentPosition);

            currentItem.setQuantity(
                    currentItem.getQuantity() + 1
            );

            notifyItemChanged(
                    currentPosition
            );

            onChanged.run();
        });

        holder.buttonRemove.setOnClickListener(v -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition ==
                    RecyclerView.NO_POSITION) {
                return;
            }

            InvoiceEditItem currentItem =
                    items.get(currentPosition);

            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Удалить товар?")
                    .setMessage(
                            currentItem.getProductName()
                    )
                    .setNegativeButton(
                            "Отмена",
                            null
                    )
                    .setPositiveButton(
                            "Удалить",
                            (dialog, which) -> {

                                items.remove(
                                        currentPosition
                                );

                                notifyItemRemoved(
                                        currentPosition
                                );

                                onChanged.run();
                            }
                    )
                    .show();
        });
    }

    private void updateItemTotal(
            InvoiceEditViewHolder holder,
            InvoiceEditItem item
    ) {

        holder.textItemTotal.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f грн",
                        item.getTotal()
                )
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class InvoiceEditViewHolder
            extends RecyclerView.ViewHolder {

        TextView textProductName;
        TextView textProductPrice;
        TextView textQuantity;
        TextView textItemTotal;

        Button buttonMinus;
        Button buttonPlus;

        ImageButton buttonRemove;

        public InvoiceEditViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            textProductName =
                    itemView.findViewById(
                            R.id.textCartProductName
                    );

            textProductPrice =
                    itemView.findViewById(
                            R.id.textCartProductPrice
                    );

            textQuantity =
                    itemView.findViewById(
                            R.id.textCartQuantity
                    );

            textItemTotal =
                    itemView.findViewById(
                            R.id.textCartItemTotal
                    );

            buttonMinus =
                    itemView.findViewById(
                            R.id.buttonCartMinus
                    );

            buttonPlus =
                    itemView.findViewById(
                            R.id.buttonCartPlus
                    );

            buttonRemove =
                    itemView.findViewById(
                            R.id.buttonRemoveCartItem
                    );
        }
    }
}