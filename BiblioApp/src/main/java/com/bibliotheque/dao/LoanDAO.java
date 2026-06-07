package com.bibliotheque.dao;

import com.bibliotheque.model.Loan;
import com.bibliotheque.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public boolean createLoan(int userId, int bookId) {
        String sql = "INSERT INTO loans (user_id, book_id, loan_date, due_date, status) " +
                     "VALUES (?, ?, date('now'), date('now', '+14 days'), 'ACTIVE')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean returnLoan(int loanId) {
        String sql = "UPDATE loans SET return_date = date('now'), status = 'RETURNED' WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = """
            SELECT l.*, u.full_name as member_name, b.title as book_title, b.author as book_author
            FROM loans l
            JOIN users u ON l.user_id = u.id
            JOIN books b ON l.book_id = b.id
            ORDER BY l.loan_date DESC
        """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) loans.add(mapLoan(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return loans;
    }

    public List<Loan> getLoansByUser(int userId) {
        List<Loan> loans = new ArrayList<>();
        String sql = """
            SELECT l.*, u.full_name as member_name, b.title as book_title, b.author as book_author
            FROM loans l
            JOIN users u ON l.user_id = u.id
            JOIN books b ON l.book_id = b.id
            WHERE l.user_id = ?
            ORDER BY l.loan_date DESC
        """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) loans.add(mapLoan(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return loans;
    }

    public List<Loan> getActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = """
            SELECT l.*, u.full_name as member_name, b.title as book_title, b.author as book_author
            FROM loans l
            JOIN users u ON l.user_id = u.id
            JOIN books b ON l.book_id = b.id
            WHERE l.status = 'ACTIVE'
            ORDER BY l.due_date ASC
        """;
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) loans.add(mapLoan(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return loans;
    }

    public boolean hasActiveLoan(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE user_id=? AND book_id=? AND status='ACTIVE'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    public int getActiveLoansCount() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM loans WHERE status='ACTIVE'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    public Loan getLoanById(int id) {
        String sql = """
            SELECT l.*, u.full_name as member_name, b.title as book_title, b.author as book_author
            FROM loans l
            JOIN users u ON l.user_id = u.id
            JOIN books b ON l.book_id = b.id
            WHERE l.id = ?
        """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapLoan(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Loan mapLoan(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setId(rs.getInt("id"));
        l.setUserId(rs.getInt("user_id"));
        l.setBookId(rs.getInt("book_id"));
        l.setLoanDate(rs.getString("loan_date"));
        l.setDueDate(rs.getString("due_date"));
        l.setReturnDate(rs.getString("return_date"));
        l.setStatus(rs.getString("status"));
        l.setMemberName(rs.getString("member_name"));
        l.setBookTitle(rs.getString("book_title"));
        l.setBookAuthor(rs.getString("book_author"));
        return l;
    }
}
