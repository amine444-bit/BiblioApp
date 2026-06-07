package com.bibliotheque.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private int year;
    private int quantity;
    private int available;

    public Book() {}

    public Book(int id, String title, String author, String isbn, String genre, int year, int quantity, int available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.year = year;
        this.quantity = quantity;
        this.available = available;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }

    public boolean isAvailable() { return available > 0; }

    @Override
    public String toString() { return title + " - " + author; }
}
