# Iteration 3 Requirements Verification

## ✅ Use Case: Load Records
**Requirement:** "The system loads routes from a database (csv file) and keeps them in a catalog of routes in working memory."

**Implementation Status:** ✅ **COMPLETE**
- **Database Loading:** `DataLoader.loadRoutes()` loads CSV into MySQL database (STATION, ROUTE, ROUTE_DAY tables)
- **Memory Catalog:** Routes loaded into `allRoutes` list in `Main.java` (line 31)
- **RouteCatalogue:** Available class with `loadRoutesFromCSV()` method
- **Location:** `src/persistence/DataLoader.java`, `src/Main.java:23-32`

## ✅ Use Case: Search for Connections
**Requirement:** "A client enters criteria for a trip. The system consults the routes catalog and suggests viable connections, if any exist. In doing that the system presents direct connections (corresponding to direct routes) or indirect connections (1-stop and 2-stop, if any), which are computed from the routes available."

**Implementation Status:** ✅ **COMPLETE**
- **Client Input:** `Main.searchForConnections()` collects origin, destination, travel date
- **Routes Catalog:** System consults `allRoutes` list (in-memory catalog)
- **Direct Connections:** Implemented (1 leg) - `Main.findConnections()` line 350-364
- **1-stop Connections:** Implemented (2 legs) - line 366-397
- **2-stop Connections:** Implemented (3 legs) - line 399-448
- **Location:** `src/Main.java:81-140, 347-454`

## ✅ Use Case: Book a Trip
**Requirement:** "A client is able to search, identify and select a desired connection and proceed to book a trip."

**Implementation Status:** ✅ **COMPLETE**
- **Search:** Available via menu option 1
- **Identify & Select:** User selects connection number from search results
- **Book Trip:** `BookingService.bookGroupTrip()` creates Trip with reservations
- **Location:** `src/Main.java:142-250`, `src/service/BookingService.java`

## ✅ Use Case: View Trips
**Requirement:** "A client should be able to enter their last name and id and view all their current trips (i.e. for today's or future connections) and past trips, where the latter are placed in some 'history collection', also viewable by the client."

**Implementation Status:** ✅ **COMPLETE**
- **Client Input:** User enters last name and ID
- **Current Trips:** `Client.getCurrentTrips()` filters using `Trip.isFuture()` (today and future)
- **Past Trips:** `Client.getPastTrips()` filters using `Trip.isPast()` (history collection)
- **Location:** `src/Main.java:252-310`, `src/model/Client.java:45-55`

## ✅ Iteration 3: Persistence Requirement
**Requirement:** "In this iteration we introduce a non-functional requirement: Persistence. You must support this requirement through a Relational Database, and document this provision with a Data Model (Tables) while clearly identifying any and all keys."

**Implementation Status:** ✅ **COMPLETE**
- **Relational Database:** MySQL database (`train_system`)
- **Data Model:** Documented in `src/db/schema.sql`
- **Tables:** STATION, ROUTE, ROUTE_DAY, CLIENT, CONNECTION, CONNECTION_LEG, TRIP
- **Primary Keys:** All tables have AUTO_INCREMENT INT primary keys
- **Foreign Keys:** All foreign keys defined with ON DELETE RESTRICT
- **Composite Keys:** ROUTE_DAY (route_id, day_of_week), CONNECTION_LEG (connection_id, seq_no)
- **Unique Constraints:** STATION.code, CLIENT.gov_id
- **Location:** `src/db/schema.sql`, `src/persistence/`

## ✅ Iteration 3: Layover Policy
**Requirement:** "The system should avoid suggesting connections with unconditional layover durations. You must build some policy (feel free to build your own), such as: 'During the day it can be OK to have a layover for 1-2 hours. However, we don't want a layover for more than 30 minutes when after hours.'"

**Implementation Status:** ✅ **COMPLETE**
- **LayoverPolicy Class:** `src/model/LayoverPolicy.java`
- **Daytime Policy:** 1-2 hours (60-120 minutes) during 6:00-22:00
- **After Hours Policy:** Maximum 30 minutes during 22:00-6:00
- **Enforcement:** 
  - `Connection.respectsLayoverPolicy()` uses `LayoverPolicy.isAcceptableLayover()`
  - Applied in search (`Main.findConnections()`)
  - Applied in booking (`BookingService.bookGroupTrip()`)
- **Location:** `src/model/LayoverPolicy.java`, `src/model/Connection.java:104-127`

## ✅ Iteration 3: Trip Unique Numerical ID
**Requirement:** "Once created, a trip is assigned a unique numerical ID."

**Implementation Status:** ✅ **COMPLETE**
- **ID Type:** `long` (numerical)
- **Generation:** `AtomicLong idGenerator` starting at 1
- **Uniqueness:** Thread-safe atomic counter ensures uniqueness
- **Location:** `src/model/Trip.java:9-25`

## 📋 Additional Features Implemented

### Database Persistence for Bookings
- **TripRepository:** Persists trips, clients, connections to database
- **Automatic Saving:** Bookings automatically saved to database
- **Location:** `src/persistence/TripRepository.java`, `src/service/BookingService.java:78-87`

### Interactive Console Interface
- **Menu System:** User-friendly menu for all operations
- **Location:** `src/Main.java:49-79`

### SearchCriteria & RouteCatalogue
- **SearchCriteria:** Encapsulates search parameters
- **RouteCatalogue:** Maintains route catalog with search functionality
- **Location:** `src/model/SearchCriteria.java`, `src/repository/RouteCatalogue.java`

## 🎯 Alignment with Domain Model

All classes match the Iteration 3 class diagram:
- ✅ Station, Route, Connection, Trip, Reservation, Ticket, Client
- ✅ Money, DaySet, TrainType, TicketClass
- ✅ LayoverPolicy (with all constants and methods)
- ✅ RouteCatalogue (Route Catalog)
- ✅ SearchCriteria
- ✅ BookingService, TripRepository (persistence layer)

## ✅ Summary

**All Iteration 3 requirements are fully implemented and verified.**

