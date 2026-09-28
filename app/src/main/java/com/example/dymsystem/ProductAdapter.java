package com.example.dymsystem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final Context context;

    public ProductAdapter(
            Context context,
            List<Product> products,
            OnAddToCartListener onAddToCartListener
    ) {
        this.context = context;
        this.products = products;
        this.onAddToCartListener = onAddToCartListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);

        return new ProductViewHolder(view);
    }
    private final OnAddToCartListener onAddToCartListener;
    public interface OnAddToCartListener {
        void onAddToCart(Product product, int quantity, ImageView imageView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder,
            int position
    ) {
        Product product = products.get(position);

        // Название
        holder.textProductName.setText(product.getName());

        // Характеристики
        String specs = "Ø" + product.getDiameter()
                + " • " + product.getThickness()
                + " • AISI " + product.getGrade();

        holder.textProductSpecs.setText(specs);

        // Цена
        holder.textProductPrice.setText(
                String.format("%.0f грн", product.getPrice())
        );

        // Количество
        holder.quantity = 1;
        holder.textQuantity.setText("1");

        // Итого скрыто при количестве 1
        holder.textProductTotal.setVisibility(View.GONE);

        // Картинка
        loadProductImage(holder.imageProduct, product.getImageHash());

        // Минус
        holder.buttonMinus.setOnClickListener(v -> {

            if (holder.quantity > 1) {

                holder.quantity--;

                holder.textQuantity.setText(
                        String.valueOf(holder.quantity)
                );

                if (holder.quantity == 1) {

                    // Если вернулись к 1 — скрываем итог
                    holder.textProductTotal.setVisibility(View.GONE);

                } else {

                    double total =
                            product.getPrice() * holder.quantity;

                    holder.textProductTotal.setText(
                            String.format("Итого: %.0f грн", total)
                    );
                }
            }
        });

        // Плюс
        holder.buttonPlus.setOnClickListener(v -> {

            holder.quantity++;

            holder.textQuantity.setText(
                    String.valueOf(holder.quantity)
            );

            double total =
                    product.getPrice() * holder.quantity;

            holder.textProductTotal.setText(
                    String.format("Итого: %.0f грн", total)
            );

            // Показываем итог
            holder.textProductTotal.setVisibility(View.VISIBLE);
        });
        holder.buttonAdd.setOnClickListener(v -> {

            onAddToCartListener.onAddToCart(
                    product,
                    holder.quantity,
                    holder.imageProduct
            );
        });
    }

    private void loadProductImage(ImageView imageView, String imageHash) {

        if (imageHash == null || imageHash.isEmpty()) {
            return;
        }

        String fileName = imageHash + ".webp";

        try {
            InputStream inputStream =
                    context.getAssets().open("images/" + fileName);

            Drawable drawable = Drawable.createFromStream(
                    inputStream,
                    fileName
            );

            imageView.setImageDrawable(drawable);

            inputStream.close();

        } catch (Exception e) {
            // Если изображения нет — оставляем стандартное изображение.
            imageView.setImageResource(
                    com.example.dymsystem.R.drawable.ic_launcher_foreground
            );
        }
    }
    public void updateList(List<Product> newProducts) {

        products.clear();
        products.addAll(newProducts);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView imageProduct;
        TextView textProductName;
        TextView textProductSpecs;
        TextView textProductPrice;
        TextView textProductTotal;
        TextView textQuantity;


        Button buttonMinus;
        Button buttonPlus;

        View buttonAdd;

        int quantity = 1;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductSpecs = itemView.findViewById(R.id.textProductSpecs);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textProductTotal = itemView.findViewById(R.id.textProductTotal);
            textQuantity = itemView.findViewById(R.id.textQuantity);

            buttonMinus = itemView.findViewById(R.id.buttonMinus);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);

            buttonAdd = itemView.findViewById(R.id.buttonAdd);
        }
    }
}