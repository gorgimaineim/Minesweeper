package org.example.demo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String URL      = "jdbc:mysql://localhost:3306/minesweeper_db";
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String USER     = "root";
    private static final String PASSWORD = "password";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS minesweeper_db");
        } catch (Exception e) {
            System.err.println("DB creation failed: " + e.getMessage());
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS leaderboard (" +
                            "  id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  difficulty VARCHAR(20)," +
                            "  time_seconds INT)"
            );
        } catch (Exception e) { e.printStackTrace(); }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS user_settings (" +
                            "  setting_key VARCHAR(100) PRIMARY KEY," +
                            "  setting_value VARCHAR(255))"
            );
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void saveScore(String difficulty, int timeSeconds) {
        String sql = "INSERT INTO leaderboard (difficulty, time_seconds) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, difficulty);
            pstmt.setInt(2, timeSeconds);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<Integer> getTop5Scores(String difficulty) {
        List<Integer> scores = new ArrayList<>();
        String sql = "SELECT time_seconds FROM leaderboard WHERE difficulty = ? " +
                "ORDER BY time_seconds ASC LIMIT 5";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, difficulty);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) scores.add(rs.getInt("time_seconds"));
        } catch (Exception e) { e.printStackTrace(); }
        return scores;
    }

    public static void saveSetting(String key, String value) {
        String sql = "INSERT INTO user_settings (setting_key, setting_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static String loadSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM user_settings WHERE setting_key = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("setting_value");
        } catch (Exception e) { e.printStackTrace(); }
        return defaultValue;
    }
}