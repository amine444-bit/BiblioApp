package com.bibliotheque.dao;

import com.bibliotheque.model.Book;
import com.bibliotheque.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) books.add(mapBook(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ? OR isbn LIKE ? ORDER BY title";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw);
            ps.setString(3, kw); ps.setString(4, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) books.add(mapBook(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public List<Book> getAvailableBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available > 0 ORDER BY title";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) books.add(mapBook(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return books;
    }

    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, genre, year, quantity, available) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getYear());
            ps.setInt(6, book.getQuantity());
            ps.setInt(7, book.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, genre=?, year=?, quantity=?, available=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getGenre());
            ps.setInt(5, book.getYear());
            ps.setInt(6, book.getQuantity());
            ps.setInt(7, book.getAvailable());
            ps.setInt(8, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean decrementAvailable(int bookId) {
        String sql = "UPDATE books SET available = available - 1 WHERE id = ? AND available > 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean incrementAvailable(int bookId) {
        String sql = "UPDATE books SET available = available + 1 WHERE id = ? AND available < quantity";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int getTotalBooks() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public int getAvailableCount() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(available) FROM books")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("isbn"),
            rs.getString("genre"),
            rs.getInt("year"),
            rs.getInt("quantity"),
            rs.getInt("available")
        );
    }
}
