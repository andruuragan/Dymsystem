package com.example.dymsystem;

public class Product {

    private final int id;
    private final String name;
    private final String type;
    private final String thickness;
    private final int grade;
    private final String diameter;
    private final String casing;
    private final String chimneyType;
    private final double price;
    private final String imageHash;

    public Product(
            int id,
            String name,
            String type,
            String thickness,
            int grade,
            String diameter,
            String casing,
            String chimneyType,
            double price,
            String imageHash
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.thickness = thickness;
        this.grade = grade;
        this.diameter = diameter;
        this.casing = casing;
        this.chimneyType = chimneyType;
        this.price = price;
        this.imageHash = imageHash;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getThickness() {
        return thickness;
    }

    public int getGrade() {
        return grade;
    }

    public String getDiameter() {
        return diameter;
    }

    public String getCasing() {
        return casing;
    }

    public String getChimneyType() {
        return chimneyType;
    }

    public double getPrice() {
        return price;
    }

    public String getImageHash() {
        return imageHash;
    }
}