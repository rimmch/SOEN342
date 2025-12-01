# Running the Train Reservation System

## Quick Start

1. **Clean up any duplicate folders (if they exist):**
   ```bash
   ./cleanup.sh
   ```

2. **Compile the code:**
   ```bash
   # Option A: Use the compile script (Recommended)
   ./compile.sh
   
   # Option B: Manual compilation
   find src -name "*.java" -exec javac -cp ".:lib/*:src" -d bin {} +
   ```

3. **Run the system:**
   ```bash
   java -cp ".:lib/*:bin" Main
   ```

**Important:** Always use `-d bin` when compiling to prevent duplicate folders from being created at the root level.

## Database Monitoring

The system now persists all bookings to the MySQL database. To monitor database changes in real-time:

**In a separate terminal, run:**
```bash
./monitor_database.sh
```

This will show CLIENT, CONNECTION, and TRIP tables updating as you make bookings. See `DATABASE_MONITORING.md` for more details.

## Interactive Menu Options

The system provides a menu-driven interface with the following options:

### 1. Search for Connections
- Enter origin city/station name
- Enter destination city/station name  
- Enter travel date (YYYY-MM-DD) or press Enter for today
- System will find:
  - Direct connections (0 transfers)
  - 1-stop connections (1 transfer)
  - 2-stop connections (2 transfers)
- Results are sorted by duration (shortest first)
- Only connections that respect the layover policy are shown

### 2. Book a Trip
- Select a connection from previous search results
- Enter travel date
- Select ticket class (First or Second)
- Enter number of travelers
- For each traveler, enter:
  - Full name
  - Age
  - ID (passport/state ID)
- System will create a trip with unique numerical ID
- Each traveler gets a reservation with a ticket

### 3. View My Trips
- Enter your last name and ID
- View:
  - Current/upcoming trips (today and future)
  - Past trips (history)
- See all reservations and tickets for each trip

### 4. Exit
- Exit the system

## Features

- **Database Integration**: Routes are loaded from CSV into MySQL database on startup
- **In-Memory Search**: Routes are also loaded into memory for fast searching
- **Layover Policy**: Connections must respect layover rules:
  - Daytime (6:00-22:00): 1-2 hour layovers
  - After hours (22:00-6:00): 30 minutes or less
- **Unique Trip IDs**: Each trip gets a unique numerical ID
- **Client Management**: System tracks all clients and their trip history

## Example Usage Flow

1. Start the system
2. Select option 1 to search for connections
   - Example: Origin "Paris", Destination "London"
3. Review the found connections
4. Select option 2 to book a trip
   - Choose connection number
   - Enter traveler details
5. Select option 3 to view your trips
   - Enter your last name and ID
   - See all your bookings

