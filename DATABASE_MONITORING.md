# Database Monitoring Guide

## Overview

The system now persists all bookings to the MySQL database. You can monitor database changes in real-time as you use the system.

## How to Monitor Database Changes

### Option 1: Real-time Monitoring Script (Recommended)

**Terminal 1 - Run the monitoring script:**
```bash
./monitor_database.sh
```

This script will:
- Display CLIENT, CONNECTION, CONNECTION_LEG, and TRIP tables
- Refresh every 2 seconds
- Show the latest 10 records from each table
- Display summary counts

**Terminal 2 - Run the application:**
```bash
java -cp ".:lib/*:bin" Main
```

As you make bookings in the application, you'll see them appear in the monitoring terminal in real-time!

### Option 2: Manual MySQL Queries

Open a MySQL terminal:
```bash
mysql -u root train_system
```

Then run queries to see the data:
```sql
-- View all clients
SELECT * FROM CLIENT;

-- View all connections
SELECT * FROM CONNECTION;

-- View connection legs
SELECT * FROM CONNECTION_LEG ORDER BY connection_id, seq_no;

-- View all trips with client names
SELECT t.trip_id, c.nameFirst, c.nameLast, t.travel_date, t.class_type, t.price 
FROM TRIP t 
JOIN CLIENT c ON t.client_id = c.client_id;

-- Get counts
SELECT 
    (SELECT COUNT(*) FROM CLIENT) as clients,
    (SELECT COUNT(*) FROM CONNECTION) as connections,
    (SELECT COUNT(*) FROM TRIP) as trips;
```

### Option 3: Quick Status Check

Run this command to see current database state:
```bash
./view_database.sh
```

## What Gets Saved to Database

When you book a trip, the following are saved:

1. **CLIENT** - Each traveler is saved as a client (if not already exists)
   - nameFirst, nameLast, gov_id, age

2. **CONNECTION** - The connection route is saved
   - total_duration_min, total_price, legs_count

3. **CONNECTION_LEG** - Each leg of the connection
   - connection_id, seq_no, route_id, leg_duration_min, leg_price

4. **TRIP** - The booking record
   - client_id, connection_id, booking_date, travel_date, class_type, price

## Example Workflow

1. **Start monitoring:**
   ```bash
   ./monitor_database.sh
   ```

2. **In another terminal, start the application:**
   ```bash
   java -cp ".:lib/*:bin" Main
   ```

3. **Use the application:**
   - Search for connections
   - Book a trip
   - Watch the database update in real-time!

4. **Stop monitoring:**
   - Press `Ctrl+C` in the monitoring terminal

## Troubleshooting

If you don't see database updates:
- Check that MySQL is running: `mysql -u root -e "SELECT 1;"`
- Verify database exists: `mysql -u root -e "USE train_system; SHOW TABLES;"`
- Check for errors in the application console (database connection issues will be logged)

