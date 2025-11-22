package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection manager for MySQL.
 * Provides singleton-style connection management using static methods.
 */
public class Database {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/train_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // Single connection instance (singleton-style)
    private static Connection connection = null;
    
    static {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Make sure mysql-connector-java is in your classpath.", e);
        }
    }
    
    /**
     * Gets a connection to the train_system database.
     * Returns the same connection instance (singleton-style).
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        // Return existing connection if available and valid
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        
        // Create new connection
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        connection.setAutoCommit(false); // Use manual commits for batch operations
        return connection;
    }
    
    /**
     * Gets a connection with custom credentials.
     * 
     * @param user database username
     * @param password database password
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection(String user, String password) throws SQLException {
        return DriverManager.getConnection(DB_URL, user, password);
    }
    
    /**
     * Closes the database connection.
     * 
     * @throws SQLException if closing fails
     */
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}
