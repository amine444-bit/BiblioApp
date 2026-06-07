package com.bibliotheque.util;

import java.sql.*;

public class DatabaseUtil {

    private static final String DB_URL = "jdbc:sqlite:bibliotheque.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('ADMIN','MEMBER')),
                    full_name TEXT NOT NULL,
                    email TEXT,
                    created_at TEXT DEFAULT (datetime('now'))
                )
            """);

            // Books table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    isbn TEXT UNIQUE,
                    genre TEXT,
                    year INTEGER,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    available INTEGER NOT NULL DEFAULT 1
                )
            """);

            // Loans table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS loans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    loan_date TEXT DEFAULT (date('now')),
                    due_date TEXT,
                    return_date TEXT,
                    status TEXT DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','RETURNED','OVERDUE')),
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (book_id) REFERENCES books(id)
                )
            """);

            // Insert default admin if not exists
            stmt.execute("""
                INSERT OR IGNORE INTO users (username, password, role, full_name, email)
                VALUES ('admin', 'admin123', 'ADMIN', 'Administrateur', 'admin@biblio.tn')
            """);

            // Insert sample books
            stmt.execute("""
                INSERT OR IGNORE INTO books (title, author, isbn, genre, year, quantity, available)
                VALUES
                ('Le Petit Prince', 'Antoine de Saint-Exupéry', '978-2-07-040850-4', 'Roman', 1943, 3, 3),
                ('L''Étranger', 'Albert Camus', '978-2-07-036024-7', 'Roman', 1942, 2, 2),
                ('Les Misérables', 'Victor Hugo', '978-2-07-040962-4', 'Roman', 1862, 2, 2),
                ('Germinal', 'Émile Zola', '978-2-07-040327-1', 'Roman', 1885, 1, 1),
                ('Madame Bovary', 'Gustave Flaubert', '978-2-07-036024-8', 'Roman', 1857, 2, 2)
            """);

            System.out.println("✅ Base de données initialisée avec succès.");

        } catch (SQLException e) {
            System.err.println("❌ Erreur initialisation BD: " + e.getMessage());
        }
    }
}
