package database;

import models.Training;

import java.sql.*;
import java.util.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqlite:FitMood.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS trainings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "type TEXT," +
                "duration INTEGER," +
                "mood INTEGER," +
                "note TEXT" +
                ");";
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertTraining(String date, String type, int duration, int mood, String note) {
        String sql = "INSERT INTO trainings(date,type,duration,mood,note) VALUES(?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, date);
            p.setString(2, type);
            p.setInt(3, duration);
            p.setInt(4, mood);
            p.setString(5, note);
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Training> getAllTrainings() {
        List<Training> list = new ArrayList<>();
        String sql = "SELECT * FROM trainings ORDER BY id DESC";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Training(
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("type"),
                        rs.getInt("duration"),
                        rs.getInt("mood"),
                        rs.getString("note")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Для графіку: агрегація тривалості по датам (якщо кілька - сумує)
    public static LinkedHashMap<String, Integer> getProgressData() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT date, SUM(duration) AS total FROM trainings GROUP BY date ORDER BY date ASC";
        try (Connection c = connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getString("date"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Статистика за місяць (передаємо YYYY-MM)
    public static Map<String, Integer> getMonthlyStats(String monthPrefix) {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) AS cnt, AVG(duration) AS avgd, AVG(mood) AS avgm FROM trainings WHERE date LIKE ?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, monthPrefix + "%");
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    stats.put("count", rs.getInt("cnt"));
                    stats.put("avgDuration", (int)Math.round(rs.getDouble("avgd")));
                    stats.put("avgMood", (int)Math.round(rs.getDouble("avgm")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public static void updateTraining(int id, String date, String type, int duration, int mood, String note) {
        // Зверніть увагу на порядок параметрів у запиті!
        String sql = "UPDATE trainings SET date = ?, type = ?, duration = ?, mood = ?, note = ? WHERE id = ?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, date);
            p.setString(2, type);
            p.setInt(3, duration);
            p.setInt(4, mood);
            p.setString(5, note);
            p.setInt(6, id); // Використовуємо id для ідентифікації
            p.executeUpdate();
            System.out.println("Запис ID " + id + " успішно оновлено.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void deleteTraining(int id) {
        String sql = "DELETE FROM trainings WHERE id = ?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            int rowsAffected = p.executeUpdate();
            System.out.println("Видалено записів: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Пошук по типу або нотатці
    public static List<Training> searchTrainings(String q) {
        List<Training> list = new ArrayList<>();
        String sql = "SELECT * FROM trainings WHERE type LIKE ? OR note LIKE ? ORDER BY id DESC";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            String like = "%" + q + "%";
            p.setString(1, like);
            p.setString(2, like);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    list.add(new Training(
                            rs.getInt("id"),
                            rs.getString("date"),
                            rs.getString("type"),
                            rs.getInt("duration"),
                            rs.getInt("mood"),
                            rs.getString("note")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

