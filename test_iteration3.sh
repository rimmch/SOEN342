#!/bin/bash
# Comprehensive test script for Iteration 3 requirements

echo "=========================================="
echo "ITERATION 3 COMPREHENSIVE TEST"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASSED=0
FAILED=0

# Test function
test_check() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $1"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $1"
        ((FAILED++))
    fi
}

echo "=== TEST 1: Database Schema ==="
echo "Checking if database tables exist..."
mysql -u root -e "USE train_system; SHOW TABLES;" 2>/dev/null | grep -q "STATION" && \
mysql -u root -e "USE train_system; SHOW TABLES;" 2>/dev/null | grep -q "ROUTE" && \
mysql -u root -e "USE train_system; SHOW TABLES;" 2>/dev/null | grep -q "TRIP"
test_check "All required tables exist (STATION, ROUTE, TRIP, etc.)"

echo ""
echo "=== TEST 2: Database Data Loading ==="
STATION_COUNT=$(mysql -u root -e "USE train_system; SELECT COUNT(*) FROM STATION;" 2>/dev/null | tail -1)
ROUTE_COUNT=$(mysql -u root -e "USE train_system; SELECT COUNT(*) FROM ROUTE;" 2>/dev/null | tail -1)
if [ "$STATION_COUNT" -gt 0 ] && [ "$ROUTE_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✓${NC} Data loaded: $STATION_COUNT stations, $ROUTE_COUNT routes"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} No data in database"
    ((FAILED++))
fi

echo ""
echo "=== TEST 3: Code Compilation ==="
./compile.sh > /dev/null 2>&1
test_check "All Java code compiles successfully"

echo ""
echo "=== TEST 4: Class Files ==="
[ -f "bin/model/Trip.class" ] && \
[ -f "bin/model/Connection.class" ] && \
[ -f "bin/model/LayoverPolicy.class" ] && \
[ -f "bin/persistence/TripRepository.class" ] && \
[ -f "bin/repository/RouteCatalogue.class" ]
test_check "All required class files exist"

echo ""
echo "=== TEST 5: Trip ID Generation ==="
# Check if Trip uses AtomicLong for numerical ID
grep -q "AtomicLong" src/model/Trip.java && \
grep -q "long tripId" src/model/Trip.java
test_check "Trip uses unique numerical ID (long, AtomicLong)"

echo ""
echo "=== TEST 6: Layover Policy Implementation ==="
grep -q "LayoverPolicy" src/model/Connection.java && \
grep -q "isAcceptableLayover" src/model/LayoverPolicy.java && \
[ -f "src/model/LayoverPolicy.java" ]
test_check "LayoverPolicy class implemented and used"

echo ""
echo "=== TEST 7: Database Persistence ==="
grep -q "TripRepository" src/service/BookingService.java && \
grep -q "saveTrip" src/persistence/TripRepository.java
test_check "Database persistence implemented (TripRepository)"

echo ""
echo "=== TEST 8: Search for Connections ==="
grep -q "findConnections" src/Main.java && \
grep -q "Direct connections" src/Main.java && \
grep -q "1-stop connections" src/Main.java && \
grep -q "2-stop connections" src/Main.java
test_check "Search for connections implemented (direct, 1-stop, 2-stop)"

echo ""
echo "=== TEST 9: View Trips ==="
grep -q "getCurrentTrips" src/Main.java && \
grep -q "getPastTrips" src/Main.java && \
grep -q "isFuture" src/model/Trip.java && \
grep -q "isPast" src/model/Trip.java
test_check "View trips implemented (current and past)"

echo ""
echo "=== TEST 10: Schema Keys ==="
# Check for primary keys
grep -q "PRIMARY KEY" src/db/schema.sql && \
# Check for foreign keys
grep -q "FOREIGN KEY" src/db/schema.sql && \
# Check for composite keys
grep -q "PRIMARY KEY.*route_id.*day_of_week" src/db/schema.sql
test_check "Database schema has all keys (PK, FK, composite)"

echo ""
echo "=========================================="
echo "TEST SUMMARY"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Failed: $FAILED${NC}"
else
    echo -e "${GREEN}Failed: $FAILED${NC}"
fi
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Run the system: java -cp \".:lib/*:bin\" Main"
    echo "2. Test interactively by:"
    echo "   - Searching for connections"
    echo "   - Booking a trip"
    echo "   - Viewing trips"
    echo "3. Monitor database: ./monitor_database.sh"
    exit 0
else
    echo -e "${RED}✗ Some tests failed. Please review the errors above.${NC}"
    exit 1
fi

