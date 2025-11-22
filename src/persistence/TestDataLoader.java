package persistence;

import java.io.IOException;
import java.sql.*;

/**
 * Test class to verify DataLoader functionality.
 * Tests loading data from CSV file into database.
 */
public class TestDataLoader {
    
    public static void main(String[] args) {
        System.out.println("=== Testing DataLoader ===");
        
        // Test 1: Check database connection
        System.out.println("\n1. Testing database connection...");
        try {
            Connection conn = Database.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("   ✓ Database connection successful");
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("   ✗ Database connection failed: " + e.getMessage());
            System.err.println("   Make sure MySQL is running and train_system database exists.");
            return;
        }
        
        // Test 2: Check if tables exist
        System.out.println("\n2. Checking database tables...");
        try (Connection conn = Database.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "STATION", null);
            if (tables.next()) {
                System.out.println("   ✓ STATION table exists");
            } else {
                System.err.println("   ✗ STATION table not found. Please run schema.sql first.");
                return;
            }
            
            tables = meta.getTables(null, null, "ROUTE", null);
            if (tables.next()) {
                System.out.println("   ✓ ROUTE table exists");
            } else {
                System.err.println("   ✗ ROUTE table not found. Please run schema.sql first.");
                return;
            }
            
            tables = meta.getTables(null, null, "ROUTE_DAY", null);
            if (tables.next()) {
                System.out.println("   ✓ ROUTE_DAY table exists");
            } else {
                System.err.println("   ✗ ROUTE_DAY table not found. Please run schema.sql first.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("   ✗ Error checking tables: " + e.getMessage());
            return;
        }
        
        // Test 3: Try loading data from CSV
        System.out.println("\n3. Testing CSV file loading...");
        String csvPath = "eu_rail_network.csv"; // Use relative path from project root
        
        // Check if file exists
        java.io.File csvFile = new java.io.File(csvPath);
        if (!csvFile.exists()) {
            System.err.println("   ✗ CSV file not found at: " + csvPath);
            System.err.println("   Current directory: " + System.getProperty("user.dir"));
            System.err.println("   Please ensure eu_rail_network.csv is in the project root.");
            return;
        }
        System.out.println("   ✓ CSV file found: " + csvFile.getAbsolutePath());
        
        // Try loading a small sample first
        System.out.println("\n4. Loading data from CSV...");
        try {
            // Clear existing data for clean test (optional - comment out if you want to keep data)
            // clearTestData();
            
            DataLoader.loadRoutes(csvPath);
            System.out.println("   ✓ Data loading completed successfully!");
            
            // Verify data was inserted
            System.out.println("\n5. Verifying inserted data...");
            try (Connection conn = Database.getConnection()) {
                // Count stations
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM STATION")) {
                    if (rs.next()) {
                        int stationCount = rs.getInt("count");
                        System.out.println("   ✓ Stations in database: " + stationCount);
                    }
                }
                
                // Count routes
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM ROUTE")) {
                    if (rs.next()) {
                        int routeCount = rs.getInt("count");
                        System.out.println("   ✓ Routes in database: " + routeCount);
                    }
                }
                
                // Count route days
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM ROUTE_DAY")) {
                    if (rs.next()) {
                        int routeDayCount = rs.getInt("count");
                        System.out.println("   ✓ Route days in database: " + routeDayCount);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("   ✗ File reading error: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("   ✗ Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("   ✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Complete ===");
    }
    
    /**
     * Helper method to clear test data (optional).
     * Uncomment if you want to start fresh each time.
     */
    private static void clearTestData() {
        try (Connection conn = Database.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM ROUTE_DAY");
                stmt.executeUpdate("DELETE FROM ROUTE");
                stmt.executeUpdate("DELETE FROM STATION");
                conn.commit();
                System.out.println("   Cleared existing test data");
            }
        } catch (SQLException e) {
            System.err.println("   Warning: Could not clear test data: " + e.getMessage());
        }
    }
}



