package com.example.dymsystem;

public class InvoiceEditItem {

    private long productId;
    private String productName;
    private double price;
    private int quantity;

    public InvoiceEditItem(
            long productId,
            String productName,
            double price,
            int quantity
    ) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

    public long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotal() {
        return price * quantity;
    }
}