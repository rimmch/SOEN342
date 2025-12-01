#!/bin/bash
# Script to view database tables and data

echo "=========================================="
echo "TRAIN SYSTEM DATABASE VIEWER"
echo "=========================================="
echo ""

echo "=== DATABASE TABLES ==="
mysql -u root -e "USE train_system; SHOW TABLES;" 2>/dev/null

echo ""
echo "=== TABLE RECORD COUNTS ==="
mysql -u root -e "USE train_system; 
SELECT 'STATION' as table_name, COUNT(*) as count FROM STATION
UNION ALL
SELECT 'ROUTE', COUNT(*) FROM ROUTE
UNION ALL
SELECT 'ROUTE_DAY', COUNT(*) FROM ROUTE_DAY
UNION ALL
SELECT 'CLIENT', COUNT(*) FROM CLIENT
UNION ALL
SELECT 'CONNECTION', COUNT(*) FROM CONNECTION
UNION ALL
SELECT 'CONNECTION_LEG', COUNT(*) FROM CONNECTION_LEG
UNION ALL
SELECT 'TRIP', COUNT(*) FROM TRIP;" 2>/dev/null

echo ""
echo "=== SAMPLE STATION DATA (first 5) ==="
mysql -u root -e "USE train_system; SELECT * FROM STATION LIMIT 5;" 2>/dev/null

echo ""
echo "=== SAMPLE ROUTE DATA (first 3) ==="
mysql -u root -e "USE train_system; 
SELECT r.route_id, s1.name as origin, s2.name as destination, 
       r.departure_time, r.arrival_time, r.train_type,
       r.first_class_price, r.second_class_price
FROM ROUTE r 
JOIN STATION s1 ON r.origin_station_id = s1.station_id
JOIN STATION s2 ON r.destination_station_id = s2.station_id
LIMIT 3;" 2>/dev/null

echo ""
echo "=== SAMPLE ROUTE_DAY DATA (first 5) ==="
mysql -u root -e "USE train_system; SELECT * FROM ROUTE_DAY LIMIT 5;" 2>/dev/null

echo ""
echo "=========================================="
echo "To view more data, use:"
echo "  mysql -u root train_system"
echo "Then run SQL queries like:"
echo "  SELECT * FROM STATION;"
echo "  SELECT * FROM ROUTE LIMIT 10;"
echo "=========================================="

