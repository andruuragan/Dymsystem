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

public class CartAdapter
        extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> items;
    private final Runnable onCartChanged;

    public CartAdapter(
            Context context,
            List<CartItem> items,
            Runnable onCartChanged
    ) {
        this.context = context;
        this.items = items;
        this.onCartChanged = onCartChanged;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_cart,
                        parent,
                        false
                );

        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder holder,
            int position
    ) {

        CartItem cartItem = items.get(position);
        Product product = cartItem.getProduct();

        holder.textProductName.setText(
                product.getName()
        );



        holder.textProductPrice.setText(
                String.format(
                        "%.0f грн / шт.",
                        product.getPrice()
                )
        );

        holder.textQuantity.setText(
                String.valueOf(
                        cartItem.getQuantity()
                )
        );

        updateItemTotal(
                holder,
                cartItem
        );

        // Уменьшить количество
        holder.buttonMinus.setOnClickListener(v -> {

            int quantity =
                    cartItem.getQuantity();

            if (quantity > 1) {

                cartItem.setQuantity(
                        quantity - 1
                );

                notifyItemChanged(position);

                onCartChanged.run();
            }
        });

        // Увеличить количество
        holder.buttonPlus.setOnClickListener(v -> {

            cartItem.setQuantity(
                    cartItem.getQuantity() + 1
            );

            notifyItemChanged(position);

            onCartChanged.run();
        });

        // Удалить позицию
        holder.buttonRemove.setOnClickListener(v -> {

            int currentPosition =
                    holder.getBindingAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            CartItem item =
                    items.get(currentPosition);

            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Удалить товар?")
                    .setMessage(item.getProduct().getName())
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить", (dialog, which) -> {

                        items.remove(currentPosition);

                        notifyItemRemoved(currentPosition);

                        onCartChanged.run();
                    })
                    .show();
        });
    }

    private void updateItemTotal(
            CartViewHolder holder,
            CartItem cartItem
    ) {

        holder.textItemTotal.setText(
                String.format(
                        "%.0f грн",
                        cartItem.getTotal()
                )
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder
            extends RecyclerView.ViewHolder {

        TextView textProductName;

        TextView textProductPrice;
        TextView textQuantity;
        TextView textItemTotal;

        Button buttonMinus;
        Button buttonPlus;

        ImageButton buttonRemove;

        public CartViewHolder(
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