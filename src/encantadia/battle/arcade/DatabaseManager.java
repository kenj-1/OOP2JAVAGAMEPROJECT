package encantadia.battle.arcade;

import java.sql.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton manager for all Arcade Leaderboard database operations.
 * Uses ArcadeDBConnection for all MySQL connections.
 *
 * Required MySQL setup — run this once in XAMPP (phpMyAdmin):
 *
 *   CREATE DATABASE IF NOT EXISTS Leaderboard;
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    private DatabaseManager() {
        initDB();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  INIT
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Creates the leaderboard table inside the Leaderboard database if it does not already exist.
     */
    private void initDB() {
        String sql = "CREATE TABLE IF NOT EXISTS arcade_leaderboard ("
                + "id                 INT AUTO_INCREMENT PRIMARY KEY,"
                + "player_name        VARCHAR(3)  NOT NULL UNIQUE,"
                + "time_taken_seconds INT         NOT NULL,"
                + "damage_dealt       INT         NOT NULL,"
                + "damage_received    INT         NOT NULL,"
                + "trophy_earned      BOOLEAN     NOT NULL"
                + ")";

        Connection conn = ArcadeDBConnection.getConnection();
        if (conn == null) {
            logger.log(Level.SEVERE, "initDB: could not obtain a connection.");
            return;
        }

        try (conn; Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "initDB failed!", e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  QUERY: isNameTaken
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the given 3-letter tag already exists in the leaderboard.
     */
    public boolean isNameTaken(String name) {
        String sql = "SELECT id FROM arcade_leaderboard WHERE player_name = ?";

        Connection conn = ArcadeDBConnection.getConnection();
        if (conn == null) {
            logger.log(Level.SEVERE, "isNameTaken: could not obtain a connection.");
            return false;
        }

        try (conn; PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "isNameTaken failed!", e);
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  INSERT: saveRecord
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Inserts a new leaderboard entry.
     *
     * @param name         3-letter player tag (must be unique)
     * @param timeSecs     total run time in seconds
     * @param dmgDealt     total damage dealt throughout the run
     * @param dmgReceived  total damage received throughout the run
     * @param trophy       true if the player cleared the Arcade Tower
     */
    public void saveRecord(String name, int timeSecs, int dmgDealt, int dmgReceived, boolean trophy) {
        String sql = "INSERT INTO arcade_leaderboard "
                + "(player_name, time_taken_seconds, damage_dealt, damage_received, trophy_earned) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection conn = ArcadeDBConnection.getConnection();
        if (conn == null) {
            logger.log(Level.SEVERE, "saveRecord: could not obtain a connection.");
            return;
        }

        try (conn; PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, timeSecs);
            pstmt.setInt(3, dmgDealt);
            pstmt.setInt(4, dmgReceived);
            pstmt.setBoolean(5, trophy);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "saveRecord failed!", e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SELECT: getLeaderboardData
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Returns all leaderboard rows sorted according to the chosen sort type.
     *
     * @param sortType one of: "Speedrunner", "Aggressor", "Tank", "Alphabetical"
     * @return rows ready to be fed directly into a DefaultTableModel
     */
    public Vector<Vector<Object>> getLeaderboardData(String sortType) {
        Vector<Vector<Object>> data = new Vector<>();

        String orderBy;
        switch (sortType) {
            case "Alphabetical": orderBy = "player_name ASC";        break;
            case "Aggressor":    orderBy = "damage_dealt DESC";       break;
            case "Tank":         orderBy = "damage_received DESC";    break;
            case "Speedrunner":  // fall-through to default
            default:             orderBy = "time_taken_seconds ASC";  break;
        }

        String sql = "SELECT * FROM arcade_leaderboard ORDER BY " + orderBy;

        Connection conn = ArcadeDBConnection.getConnection();
        if (conn == null) {
            logger.log(Level.SEVERE, "getLeaderboardData: could not obtain a connection.");
            return data;
        }

        try (conn;
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            int rank = 1;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rank++);
                row.add(rs.getString("player_name"));

                int time = rs.getInt("time_taken_seconds");
                row.add(String.format("%02d:%02d", time / 60, time % 60));

                row.add(rs.getInt("damage_dealt"));
                row.add(rs.getInt("damage_received"));
                row.add(rs.getBoolean("trophy_earned") ? "👑 CLEAR" : "💀 FALLEN");
                data.add(row);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "getLeaderboardData failed!", e);
        }

        return data;
    }
}