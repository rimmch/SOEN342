#!/bin/bash
# Real-time database monitoring script
# Run this in a separate terminal to watch database changes

echo "=========================================="
echo "DATABASE MONITOR - Real-time Updates"
echo "=========================================="
echo "Watching for changes in CLIENT, CONNECTION, and TRIP tables..."
echo "Press Ctrl+C to stop"
echo ""

# Function to display current state
show_database_state() {
    clear
    echo "=========================================="
    echo "DATABASE MONITOR - $(date '+%Y-%m-%d %H:%M:%S')"
    echo "=========================================="
    echo ""
    
    echo "=== CLIENT TABLE ==="
    mysql -u root train_system -e "SELECT client_id, nameFirst, nameLast, gov_id, age FROM CLIENT ORDER BY client_id DESC LIMIT 10;" 2>/dev/null || echo "No clients yet"
    echo ""
    
    echo "=== CONNECTION TABLE ==="
    mysql -u root train_system -e "SELECT connection_id, total_duration_min, total_price, legs_count FROM CONNECTION ORDER BY connection_id DESC LIMIT 10;" 2>/dev/null || echo "No connections yet"
    echo ""
    
    echo "=== CONNECTION_LEG TABLE (Latest) ==="
    mysql -u root train_system -e "SELECT cl.connection_id, cl.seq_no, cl.route_id, cl.leg_duration_min, cl.leg_price FROM CONNECTION_LEG cl ORDER BY cl.connection_id DESC, cl.seq_no LIMIT 10;" 2>/dev/null || echo "No connection legs yet"
    echo ""
    
    echo "=== TRIP TABLE ==="
    mysql -u root train_system -e "SELECT t.trip_id, c.nameLast, t.travel_date, t.class_type, t.price FROM TRIP t JOIN CLIENT c ON t.client_id = c.client_id ORDER BY t.trip_id DESC LIMIT 10;" 2>/dev/null || echo "No trips yet"
    echo ""
    
    echo "=== SUMMARY COUNTS ==="
    mysql -u root train_system -e "SELECT 
        (SELECT COUNT(*) FROM CLIENT) as clients,
        (SELECT COUNT(*) FROM CONNECTION) as connections,
        (SELECT COUNT(*) FROM TRIP) as trips;" 2>/dev/null
    echo ""
    echo "Refreshing in 2 seconds... (Press Ctrl+C to stop)"
}

# Main monitoring loop
while true; do
    show_database_state
    sleep 2
done

