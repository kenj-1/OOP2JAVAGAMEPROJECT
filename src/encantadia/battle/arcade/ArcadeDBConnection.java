package encantadia.battle.arcade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for establishing MySQL database connections
 * for the Arcade Leaderboard via XAMPP.
 */
public class ArcadeDBConnection {

    // CHANGED: Database name is now "Leaderboard"
    private static final String URL      = "jdbc:mysql://localhost:3306/Leaderboard";
    private static final String USER     = "root";
    private static final String PASSWORD = ""; // Default XAMPP password is empty
    private static final Logger logger   = Logger.getLogger(ArcadeDBConnection.class.getName());

    /**
     * Returns a live connection to the Leaderboard database.
     * Returns null if the connection fails.
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            logger.log(Level.SEVERE, "Arcade database connection failed!", e);
            return null;
        }
    }
}