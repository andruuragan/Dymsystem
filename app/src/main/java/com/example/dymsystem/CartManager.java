package com.example.dymsystem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;

    private final List<CartItem> items;

    private CartManager() {
        items = new ArrayList<>();
    }

    public static CartManager getInstance() {

        if (instance == null) {
            instance = new CartManager();
        }

        return instance;
    }

    public void addProduct(Product product, int quantity) {

        for (CartItem item : items) {

            if (item.getProduct().getId() == product.getId()) {

                item.setQuantity(
                        item.getQuantity() + quantity
                );

                return;
            }
        }

        items.add(
                new CartItem(product, quantity)
        );
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalQuantity() {

        int total = 0;

        for (CartItem item : items) {
            total += item.getQuantity();
        }

        return total;
    }

    public double getTotalPrice() {

        double total = 0;

        for (CartItem item : items) {
            total += item.getTotal();
        }

        return total;
    }

    public void removeProduct(int productId) {

        items.removeIf(
                item -> item.getProduct().getId() == productId
        );
    }

    public void clear() {
        items.clear();
    }
}