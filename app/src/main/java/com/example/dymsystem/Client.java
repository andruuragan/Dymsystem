package com.example.dymsystem;

public class Client {

    private long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String note;
    private String createdAt;

    public Client(
            long id,
            String name,
            String phone,
            String email,
            String address,
            String note,
            String createdAt
    ) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.note = note;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}