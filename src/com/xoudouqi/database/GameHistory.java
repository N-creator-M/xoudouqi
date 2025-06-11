package com.xoudouqi.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameHistory {
	private static final String DB_URL = "jdbc:sqlite:lib/xoudouqi.db";

    public GameHistory() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS history (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "player1 TEXT," +
                     "player2 TEXT," +
                     "winner TEXT," +
                     "moves TEXT," +
                     "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                     ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveGame(String player1, String player2, String winner, List<String> moves) {
        String sql = "INSERT INTO history (player1, player2, winner, moves) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player1);
            pstmt.setString(2, player2);
            pstmt.setString(3, winner);
            pstmt.setString(4, String.join(";", moves));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printHistory() {
        String sql = "SELECT * FROM history ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("=== Historique des parties ===");
            while (rs.next()) {
                System.out.println("Partie #" + rs.getInt("id"));
                System.out.println("  Joueur 1: " + rs.getString("player1"));
                System.out.println("  Joueur 2: " + rs.getString("player2"));
                System.out.println("  Gagnant: " + rs.getString("winner"));
                System.out.println("  Coups: " + rs.getString("moves"));
                System.out.println("  Date: " + rs.getString("timestamp"));
                System.out.println("---------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
