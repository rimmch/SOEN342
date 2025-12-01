package persistence;

import model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for persisting trips, clients, connections, and reservations to the database.
 */
public class TripRepository {
    private java.sql.Connection dbConnection;

    public TripRepository() throws SQLException {
        this.dbConnection = Database.getConnection();
    }

    /**
     * Saves a trip and all its related data to the database.
     * This includes: Client, Connection, ConnectionLegs, and Trip records.
     */
    public void saveTrip(Trip trip, List<TravelerInfo> travelers, TicketClass ticketClass) throws SQLException {
        try {
            // Save connection first (if not already saved)
            int connectionId = saveConnection(trip.getConnection()); // model.Connection
            
            // Save each traveler as a client and create a trip record
            for (int i = 0; i < travelers.size(); i++) {
                TravelerInfo traveler = travelers.get(i);
                String lastName = extractLastName(traveler.getFullName());
                
                // Save or get client
                int clientId = saveOrGetClient(lastName, traveler.getId(), traveler.getFullName(), traveler.getAge());
                
                // Save trip record
                saveTripRecord(trip, clientId, connectionId, ticketClass, i == 0);
            }
            
            dbConnection.commit();
        } catch (SQLException e) {
            dbConnection.rollback();
            throw e;
        }
    }

    /**
     * Saves a connection to the database and returns its ID.
     * If the connection already exists, returns the existing ID.
     */
    private int saveConnection(model.Connection conn) throws SQLException {
        List<Route> routes = conn.getRoutes();
        
        // Check if connection already exists by matching routes
        String checkSql = "SELECT connection_id FROM CONNECTION WHERE " +
                         "total_duration_min = ? AND total_price = ? AND legs_count = ?";
        
        try (PreparedStatement checkStmt = dbConnection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, conn.getTotalDurationMinutes());
            checkStmt.setBigDecimal(2, conn.getTotalPriceSecondClass().getAmount());
            checkStmt.setInt(3, conn.getNumberOfTransfers() + 1);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int existingId = rs.getInt("connection_id");
                    // Verify the legs match
                    if (connectionLegsMatch(existingId, routes)) {
                        return existingId;
                    }
                }
            }
        }
        
        // Insert new connection
        String insertSql = "INSERT INTO CONNECTION (total_duration_min, total_price, legs_count) VALUES (?, ?, ?)";
        int connectionId;
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, conn.getTotalDurationMinutes());
            stmt.setBigDecimal(2, conn.getTotalPriceSecondClass().getAmount());
            stmt.setInt(3, conn.getNumberOfTransfers() + 1);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    connectionId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get connection ID");
                }
            }
        }
        
        // Insert connection legs
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            int routeId = getRouteIdFromDatabase(route);
            
            if (routeId == -1) {
                String errorMsg = String.format(
                    "Route not found in database. Looking for: %s (%s) -> %s (%s) at %s-%s",
                    route.getDepartureStation().getName(),
                    route.getDepartureStation().getCode(),
                    route.getArrivalStation().getName(),
                    route.getArrivalStation().getCode(),
                    route.getDepartureTime(),
                    route.getArrivalTime()
                );
                throw new SQLException(errorMsg);
            }
            
            String legSql = "INSERT INTO CONNECTION_LEG (connection_id, seq_no, route_id, leg_duration_min, leg_price) " +
                           "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement legStmt = dbConnection.prepareStatement(legSql)) {
                legStmt.setInt(1, connectionId);
                legStmt.setInt(2, i + 1);
                legStmt.setInt(3, routeId);
                legStmt.setInt(4, route.getDurationMinutes());
                legStmt.setBigDecimal(5, route.getPriceSecondClass().getAmount());
                legStmt.executeUpdate();
            }
        }
        
        return connectionId;
    }

    /**
     * Checks if connection legs match the given routes.
     */
    private boolean connectionLegsMatch(int connectionId, List<Route> routes) throws SQLException {
        String sql = "SELECT route_id, seq_no FROM CONNECTION_LEG WHERE connection_id = ? ORDER BY seq_no";
        List<Integer> dbRouteIds = new ArrayList<>();
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setInt(1, connectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dbRouteIds.add(rs.getInt("route_id"));
                }
            }
        }
        
        if (dbRouteIds.size() != routes.size()) {
            return false;
        }
        
        for (int i = 0; i < routes.size(); i++) {
            int routeId = getRouteIdFromDatabase(routes.get(i));
            if (routeId != dbRouteIds.get(i)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Gets route ID from database by matching station and times.
     */
    private int getRouteIdFromDatabase(Route route) throws SQLException {
        String sql = "SELECT r.route_id FROM ROUTE r " +
                    "JOIN STATION s1 ON r.origin_station_id = s1.station_id " +
                    "JOIN STATION s2 ON r.destination_station_id = s2.station_id " +
                    "WHERE s1.code = ? AND s2.code = ? " +
                    "AND r.departure_time = ? AND r.arrival_time = ? " +
                    "LIMIT 1";
        
        String originCode = route.getDepartureStation().getCode();
        String destCode = route.getArrivalStation().getCode();
        Time depTime = Time.valueOf(route.getDepartureTime());
        Time arrTime = Time.valueOf(route.getArrivalTime());
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, originCode);
            stmt.setString(2, destCode);
            stmt.setTime(3, depTime);
            stmt.setTime(4, arrTime);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int routeId = rs.getInt("route_id");
                    System.out.println("  Found route in DB: " + routeId + " for " + originCode + "->" + destCode);
                    return routeId;
                } else {
                    System.err.println("  Route not found in DB: " + originCode + "->" + destCode + " at " + depTime + "-" + arrTime);
                    // Try to find similar routes for debugging
                    debugFindSimilarRoutes(originCode, destCode, depTime, arrTime);
                }
            }
        }
        return -1;
    }
    
    private void debugFindSimilarRoutes(String originCode, String destCode, Time depTime, Time arrTime) {
        try {
            String debugSql = "SELECT r.route_id, s1.code as origin, s2.code as dest, r.departure_time, r.arrival_time " +
                            "FROM ROUTE r " +
                            "JOIN STATION s1 ON r.origin_station_id = s1.station_id " +
                            "JOIN STATION s2 ON r.destination_station_id = s2.station_id " +
                            "WHERE (s1.code = ? OR s2.code = ?) " +
                            "LIMIT 5";
            try (PreparedStatement stmt = dbConnection.prepareStatement(debugSql)) {
                stmt.setString(1, originCode);
                stmt.setString(2, destCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("  Similar routes in DB:");
                        do {
                            System.err.println("    Route " + rs.getInt("route_id") + ": " + 
                                             rs.getString("origin") + "->" + rs.getString("dest") + 
                                             " at " + rs.getTime("departure_time") + "-" + rs.getTime("arrival_time"));
                        } while (rs.next());
                    }
                }
            }
        } catch (SQLException e) {
            // Ignore debug errors
        }
    }

    /**
     * Saves or retrieves a client from the database.
     */
    private int saveOrGetClient(String lastName, String govId, String firstName, int age) throws SQLException {
        // Check if client exists
        String checkSql = "SELECT client_id FROM CLIENT WHERE gov_id = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(checkSql)) {
            stmt.setString(1, govId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("client_id");
                }
            }
        }
        
        // Insert new client
        String insertSql = "INSERT INTO CLIENT (nameFirst, nameLast, gov_id, age) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, govId);
            stmt.setInt(4, age);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get client ID");
                }
            }
        }
    }

    /**
     * Saves a trip record to the database.
     */
    private void saveTripRecord(Trip trip, int clientId, int connectionId, TicketClass ticketClass, boolean isFirstTraveler) throws SQLException {
        // Only save one trip record per trip (not per traveler)
        if (!isFirstTraveler) {
            return;
        }
        
        String sql = "INSERT INTO TRIP (client_id, connection_id, booking_date, travel_date, class_type, price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Money price = (ticketClass == TicketClass.FIRST_CLASS) ? 
                     trip.getConnection().getTotalPriceFirstClass() : 
                     trip.getConnection().getTotalPriceSecondClass();
        
        // Convert TicketClass enum to database format: FIRST_CLASS -> FIRST, SECOND_CLASS -> SECOND
        String classTypeDb = (ticketClass == TicketClass.FIRST_CLASS) ? "FIRST" : "SECOND";
        
        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, connectionId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setDate(4, Date.valueOf(trip.getTravelDate()));
            stmt.setString(5, classTypeDb);
            stmt.setBigDecimal(6, price.getAmount());
            stmt.executeUpdate();
        }
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Unknown";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}

