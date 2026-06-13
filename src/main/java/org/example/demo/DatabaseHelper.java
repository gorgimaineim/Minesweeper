package org.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    // Update these to match your local MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/minesweeper_db";
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static void initializeDatabase() {
        // 1. Create the database if it doesn't exist
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS minesweeper_db");
        } catch (Exception e) {
            System.err.println("Database creation failed. Check your MySQL credentials: " + e.getMessage());
        }

        // 2. Create the leaderboard table if it doesn't exist
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "difficulty VARCHAR(20), " +
                    "time_seconds INT" +
                    ")";
            stmt.executeUpdate(createTableSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveScore(String difficulty, int timeSeconds) {
        String insertSQL = "INSERT INTO leaderboard (difficulty, time_seconds) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, difficulty);
            pstmt.setInt(2, timeSeconds);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getTop5Scores(String difficulty) {
        List<Integer> scores = new ArrayList<>();
        String querySQL = "SELECT time_seconds FROM leaderboard WHERE difficulty = ? ORDER BY time_seconds ASC LIMIT 5";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, difficulty);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                scores.add(rs.getInt("time_seconds"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scores;
    }
}