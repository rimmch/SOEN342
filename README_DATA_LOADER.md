# DataLoader Testing Guide

## Overview
The DataLoader automatically loads train route data from `eu_rail_network.csv` into the MySQL database on application startup.

## Prerequisites

1. **MySQL Server** must be running
2. **Database created**: Run `src/db/schema.sql` to create the database and tables
3. **MySQL JDBC Driver**: Download `mysql-connector-java.jar` (or `mysql-connector-j-*.jar`)

## Setup Steps

### 1. Create the Database
```bash
mysql -u root < src/db/schema.sql
```

Or manually:
```sql
mysql -u root
source src/db/schema.sql;
```

### 2. Download MySQL JDBC Driver
Download from: https://dev.mysql.com/downloads/connector/j/
Place `mysql-connector-java.jar` in the project root directory.

### 3. Compile the Code
```bash
cd src
javac -cp ".:../mysql-connector-java.jar" persistence/*.java
```

### 4. Run the Test
```bash
# From project root
java -cp ".:mysql-connector-java.jar:src" persistence.TestDataLoader
```

### 5. Or Run Main Application
The Main class will automatically load data on startup:
```bash
java -cp ".:mysql-connector-java.jar:src" Main
```

## Testing Checklist

- [ ] MySQL server is running
- [ ] Database `train_system` exists
- [ ] Tables STATION, ROUTE, ROUTE_DAY exist
- [ ] CSV file `eu_rail_network.csv` is in project root
- [ ] MySQL JDBC driver is in classpath
- [ ] Code compiles without errors
- [ ] TestDataLoader runs successfully
- [ ] Data is inserted into database (check with SQL queries)

## Verify Data Loading

After running, verify data was loaded:

```sql
mysql -u root train_system

-- Check station count
SELECT COUNT(*) FROM STATION;

-- Check route count  
SELECT COUNT(*) FROM ROUTE;

-- Check route days count
SELECT COUNT(*) FROM ROUTE_DAY;

-- Sample data
SELECT * FROM STATION LIMIT 5;
SELECT * FROM ROUTE LIMIT 5;
SELECT * FROM ROUTE_DAY LIMIT 10;
```

## Troubleshooting

### "MySQL JDBC Driver not found"
- Download `mysql-connector-java.jar` and place in project root
- Or add it to your IDE's classpath

### "Database connection failed"
- Check MySQL is running: `mysql -u root -e "SELECT 1;"`
- Verify connection settings in `Database.java`:
  - URL: `jdbc:mysql://localhost:3306/train_system`
  - User: `root`
  - Password: (empty by default)

### "Table not found"
- Run `src/db/schema.sql` to create tables

### "CSV file not found"
- Ensure `eu_rail_network.csv` is in the project root directory
- Check file path in `Main.java` (should be `"eu_rail_network.csv"`)

### "Error processing row"
- Check CSV format matches expected columns
- Verify no special characters causing parsing issues
- Check database constraints (foreign keys, etc.)

## CSV File Format

Expected columns (comma-separated):
1. Route ID
2. Departure City
3. Arrival City
4. Departure Time (HH:MM format)
5. Arrival Time (HH:MM format)
6. Train Type
7. Days of Operation (e.g., "Mon,Wed,Fri", "Daily", "Fri-Sun")
8. First Class ticket rate (in euro)
9. Second Class ticket rate (in euro)

## Features

- **Automatic delimiter detection**: Handles comma or semicolon separated CSV
- **Duplicate prevention**: Uses `getOrCreateStation()` to avoid duplicate stations
- **Transaction support**: Rolls back on error, commits on success
- **Batch commits**: Commits every 100 rows for better performance
- **Error handling**: Continues processing even if individual rows fail
- **Caching**: Station cache prevents repeated database queries



