package persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * DataLoader reads CSV file and loads data into STATION, ROUTE, and ROUTE_DAY tables.
 */
public class DataLoader {
    
    // Time formatter not needed as we parse directly to LocalTime
    private Connection connection;
    private Map<String, Integer> stationCache; // city -> station_id
    private char delimiter; // Detected CSV delimiter (comma or semicolon)
    
    /**
     * Constructor initializes connection and station cache.
     */
    public DataLoader() throws SQLException {
        this.connection = Database.getConnection();
        this.stationCache = new HashMap<>();
    }
    
    /**
     * Main method to load routes from CSV file.
     * 
     * @param csvFilePath path to the CSV file
     * @throws IOException if file reading fails
     * @throws SQLException if database operations fail
     */
    public static void loadRoutes(String csvFilePath) throws IOException, SQLException {
        DataLoader loader = new DataLoader();
        try {
            loader.loadDataFromCSV(csvFilePath);
            loader.connection.commit(); // Commit all changes
            System.out.println("Data loading completed successfully.");
        } catch (Exception e) {
            loader.connection.rollback(); // Rollback on error
            throw e;
        } finally {
            loader.close();
        }
    }
    
    /**
     * Loads data from CSV file into database.
     * 
     * @param csvFilePath path to CSV file
     */
    private void loadDataFromCSV(String csvFilePath) throws IOException, SQLException {
        System.out.println("Loading data from: " + csvFilePath);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            // Read and skip header row
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            // Detect delimiter (comma or semicolon)
            delimiter = detectDelimiter(headerLine);
            System.out.println("Detected delimiter: " + (delimiter == ',' ? "comma" : "semicolon"));
            
            int rowCount = 0;
            int successCount = 0;
            String line;
            
            // Process each data row
            while ((line = reader.readLine()) != null) {
                rowCount++;
                
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                try {
                    // Parse and process the row
                    processRouteRow(line);
                    successCount++;
                    
                    // Commit every 100 rows for better performance
                    if (successCount % 100 == 0) {
                        connection.commit();
                        System.out.println("Processed " + successCount + " routes...");
                    }
                } catch (Exception e) {
                    System.err.println("Error processing row " + rowCount + ": " + e.getMessage());
                    // Continue processing other rows
                }
            }
            
            // Final commit
            connection.commit();
            System.out.println("Successfully processed " + successCount + " out of " + rowCount + " routes.");
        }
    }
    
    /**
     * Detects CSV delimiter (comma or semicolon) by analyzing the header line.
     * 
     * @param headerLine the header row of the CSV
     * @return the detected delimiter character
     */
    private char detectDelimiter(String headerLine) {
        int commaCount = 0;
        int semicolonCount = 0;
        
        // Count occurrences of each delimiter (ignoring quoted sections)
        boolean inQuotes = false;
        for (char c : headerLine.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == ',') {
                    commaCount++;
                } else if (c == ';') {
                    semicolonCount++;
                }
            }
        }
        
        // Return the delimiter with more occurrences
        return (semicolonCount > commaCount) ? ';' : ',';
    }
    
    /**
     * Parses a CSV line, handling quoted fields that may contain delimiters.
     * 
     * @param line the CSV line to parse
     * @return array of field values
     */
    private String[] parseCSVLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                // Field separator found
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }
    
    /**
     * Processes a single route row from CSV.
     * Expected columns: Route ID, Departure City, Arrival City, Departure Time,
     * Arrival Time, Train Type, Days of Operation, First Class price, Second Class price
     * 
     * @param line the CSV line to process
     */
    private void processRouteRow(String line) throws SQLException {
        // Parse CSV line
        String[] fields = parseCSVLine(line);
        
        if (fields.length < 9) {
            throw new IllegalArgumentException("Expected at least 9 columns, found: " + fields.length);
        }
        
        // Extract fields (trimming and handling empty values)
        String routeId = fields[0].trim();
        String depCity = fields[1].trim();
        String arrCity = fields[2].trim();
        String depTimeStr = fields[3].trim();
        String arrTimeStr = fields[4].trim();
        String trainType = fields[5].trim();
        String daysOfOp = fields[6].trim();
        String firstPriceStr = fields[7].trim();
        String secondPriceStr = fields[8].trim();
        
        // Validate required fields
        if (depCity.isEmpty() || arrCity.isEmpty() || depTimeStr.isEmpty() || arrTimeStr.isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: departure city, arrival city, or times");
        }
        
        // Get or create stations (prevents duplicates)
        int originStationId = getOrCreateStation(connection, depCity);
        int destinationStationId = getOrCreateStation(connection, arrCity);
        
        // Parse times to LocalTime
        LocalTime departureTime = parseTime(depTimeStr);
        LocalTime arrivalTime = parseTime(arrTimeStr);
        
        // Parse prices
        double firstPrice = Double.parseDouble(firstPriceStr);
        double secondPrice = Double.parseDouble(secondPriceStr);
        
        // Insert route into ROUTE table
        int routeIdInt = insertRoute(originStationId, destinationStationId,
                                    departureTime, arrivalTime, trainType,
                                    firstPrice, secondPrice);
        
        // Insert route days into ROUTE_DAY table
        if (!daysOfOp.isEmpty()) {
            insertRouteDays(routeIdInt, daysOfOp);
        }
    }
    
    /**
     * Gets or creates a station and returns its station_id.
     * Prevents duplicate stations by checking city name in database.
     * Uses caching to avoid repeated database queries.
     * 
     * @param conn database connection
     * @param city city name
     * @return station_id
     */
    public int getOrCreateStation(Connection conn, String city) throws SQLException {
        // Check cache first for performance
        if (stationCache.containsKey(city)) {
            return stationCache.get(city);
        }
        
        // Check if station already exists in database
        String selectSql = "SELECT station_id FROM STATION WHERE city = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int stationId = rs.getInt("station_id");
                stationCache.put(city, stationId); // Cache for future use
                return stationId;
            }
        }
        
        // Station doesn't exist, create it
        String insertSql = "INSERT INTO STATION (name, city, country, code) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            String name = city; // Use city name as station name
            String country = "Unknown"; // Default country (could be enhanced with country detection)
            String code = generateStationCode(city);
            
            stmt.setString(1, name);
            stmt.setString(2, city);
            stmt.setString(3, country);
            stmt.setString(4, code);
            
            stmt.executeUpdate();
            
            // Get the generated station_id
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int stationId = rs.getInt(1);
                stationCache.put(city, stationId); // Cache the new station
                return stationId;
            }
        }
        
        throw new SQLException("Failed to create station for city: " + city);
    }
    
    /**
     * Inserts a route into the ROUTE table.
     * 
     * @param originStationId foreign key to STATION
     * @param destinationStationId foreign key to STATION
     * @param departureTime route departure time
     * @param arrivalTime route arrival time
     * @param trainType type of train
     * @param firstClassPrice first class ticket price
     * @param secondClassPrice second class ticket price
     * @return the generated route_id
     */
    public int insertRoute(int originStationId, int destinationStationId,
                          LocalTime departureTime, LocalTime arrivalTime,
                          String trainType, double firstClassPrice, double secondClassPrice)
                          throws SQLException {
        
        String sql = "INSERT INTO ROUTE (origin_station_id, destination_station_id, " +
                     "departure_time, arrival_time, train_type, first_class_price, second_class_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, originStationId);
            stmt.setInt(2, destinationStationId);
            stmt.setTime(3, Time.valueOf(departureTime)); // Convert LocalTime to SQL Time
            stmt.setTime(4, Time.valueOf(arrivalTime)); // Convert LocalTime to SQL Time
            stmt.setString(5, trainType);
            stmt.setDouble(6, firstClassPrice);
            stmt.setDouble(7, secondClassPrice);
            
            stmt.executeUpdate();
            
            // Get the generated route_id
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        throw new SQLException("Failed to insert route");
    }
    
    /**
     * Inserts route days into ROUTE_DAY table.
     * Parses days string in various formats: "Mon,Wed,Fri", "Mon-Fri", "Daily", "Sat-Sun", etc.
     * 
     * @param routeId foreign key to ROUTE
     * @param daysString days of operation string
     */
    public void insertRouteDays(int routeId, String daysString) throws SQLException {
        daysString = daysString.trim();
        
        // Remove quotes if present
        if (daysString.startsWith("\"") && daysString.endsWith("\"")) {
            daysString = daysString.substring(1, daysString.length() - 1);
        }
        
        // Parse days pattern into array of day numbers (1=Monday, 7=Sunday)
        int[] days = parseDaysOfOperation(daysString);
        
        if (days.length == 0) {
            return; // No valid days found
        }
        
        // Insert each day into ROUTE_DAY table
        String sql = "INSERT INTO ROUTE_DAY (route_id, day_of_week) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE route_id = route_id";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int day : days) {
                stmt.setInt(1, routeId);
                stmt.setInt(2, day); // day_of_week: 1=Monday, 7=Sunday
                stmt.executeUpdate();
            }
        }
    }
    
    /**
     * Parses days of operation string into array of day numbers (1-7).
     * Supports multiple formats:
     * - "Daily" → all 7 days
     * - "Mon-Fri" → Monday through Friday
     * - "Fri-Sun" → Friday through Sunday (wraps around)
     * - "Mon,Wed,Fri" → specific days
     * - "Sat-Sun" → weekend
     * 
     * @param daysStr days of operation string
     * @return array of day numbers (1=Monday, 7=Sunday)
     */
    private int[] parseDaysOfOperation(String daysStr) {
        daysStr = daysStr.trim();
        
        // Handle "Daily" - all 7 days
        if (daysStr.equalsIgnoreCase("Daily")) {
            return new int[]{1, 2, 3, 4, 5, 6, 7};
        }
        
        // Handle range like "Mon-Fri" or "Fri-Sun"
        if (daysStr.contains("-")) {
            String[] parts = daysStr.split("-");
            if (parts.length == 2) {
                int start = dayNameToNumber(parts[0].trim());
                int end = dayNameToNumber(parts[1].trim());
                
                if (start > 0 && end > 0) {
                    java.util.List<Integer> dayList = new java.util.ArrayList<>();
                    
                    if (end >= start) {
                        // Normal range (e.g., Mon-Fri)
                        for (int i = start; i <= end; i++) {
                            dayList.add(i);
                        }
                    } else {
                        // Wraps around (e.g., Fri-Sun)
                        for (int i = start; i <= 7; i++) {
                            dayList.add(i);
                        }
                        for (int i = 1; i <= end; i++) {
                            dayList.add(i);
                        }
                    }
                    
                    return dayList.stream().mapToInt(i -> i).toArray();
                }
            }
        }
        
        // Handle comma-separated list like "Mon,Wed,Fri"
        if (daysStr.contains(",")) {
            String[] parts = daysStr.split(",");
            java.util.List<Integer> dayList = new java.util.ArrayList<>();
            for (String part : parts) {
                int day = dayNameToNumber(part.trim());
                if (day > 0) {
                    dayList.add(day);
                }
            }
            return dayList.stream().mapToInt(i -> i).toArray();
        }
        
        // Single day
        int day = dayNameToNumber(daysStr);
        if (day > 0) {
            return new int[]{day};
        }
        
        return new int[0];
    }
    
    /**
     * Converts day name abbreviation to day number.
     * 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday, 7=Sunday
     * 
     * @param dayName day name (e.g., "Mon", "Monday", "Tue")
     * @return day number (1-7) or 0 if invalid
     */
    private int dayNameToNumber(String dayName) {
        String day = dayName.substring(0, Math.min(3, dayName.length())).toLowerCase();
        switch (day) {
            case "mon": return 1;
            case "tue": return 2;
            case "wed": return 3;
            case "thu": return 4;
            case "fri": return 5;
            case "sat": return 6;
            case "sun": return 7;
            default: return 0;
        }
    }
    
    /**
     * Parses time string to LocalTime.
     * Handles formats like "08:15", "08:15:00", or "08:08 (+1d)".
     * 
     * @param timeStr time string
     * @return LocalTime object
     */
    private LocalTime parseTime(String timeStr) {
        // Remove any additional info like "(+1d)" for next-day arrivals
        timeStr = timeStr.trim();
        if (timeStr.contains("(")) {
            timeStr = timeStr.substring(0, timeStr.indexOf("(")).trim();
        }
        
        // Extract HH:mm part
        String[] parts = timeStr.split(":");
        if (parts.length >= 2) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return LocalTime.of(hour, minute);
        }
        
        throw new IllegalArgumentException("Invalid time format: " + timeStr);
    }
    
    /**
     * Generates a 3-letter station code from city name.
     * Extracts uppercase letters and takes first 3 characters.
     * 
     * @param cityName city name
     * @return 3-letter station code
     */
    private String generateStationCode(String cityName) {
        String letters = cityName.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "XXX";
        }
        return letters.substring(0, Math.min(3, letters.length()));
    }
    
    /**
     * Closes the database connection.
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
