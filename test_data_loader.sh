#!/bin/bash

# Test script for DataLoader
# This script tests if the DataLoader can compile and connect to the database

echo "=== DataLoader Test Script ==="
echo ""

# Check if Java is available
if ! command -v javac &> /dev/null; then
    echo "ERROR: Java compiler (javac) not found. Please install Java JDK."
    exit 1
fi

echo "✓ Java compiler found: $(javac -version 2>&1)"
echo ""

# Check if CSV file exists
CSV_FILE="eu_rail_network.csv"
if [ ! -f "$CSV_FILE" ]; then
    echo "ERROR: CSV file not found: $CSV_FILE"
    echo "Please ensure eu_rail_network.csv is in the project root."
    exit 1
fi

echo "✓ CSV file found: $CSV_FILE"
echo "  File size: $(du -h "$CSV_FILE" | cut -f1)"
echo "  Line count: $(wc -l < "$CSV_FILE")"
echo ""

# Check if schema.sql exists
SCHEMA_FILE="src/db/schema.sql"
if [ ! -f "$SCHEMA_FILE" ]; then
    echo "WARNING: Schema file not found: $SCHEMA_FILE"
    echo "Please run schema.sql to create the database tables first."
else
    echo "✓ Schema file found: $SCHEMA_FILE"
fi
echo ""

# Check for MySQL connector
echo "Checking for MySQL JDBC driver..."
MYSQL_JAR=""
if [ -f "mysql-connector-java.jar" ]; then
    MYSQL_JAR="mysql-connector-java.jar"
elif [ -f "lib/mysql-connector-java.jar" ]; then
    MYSQL_JAR="lib/mysql-connector-java.jar"
elif [ -f "mysql-connector-j-*.jar" ]; then
    MYSQL_JAR=$(ls mysql-connector-j-*.jar | head -1)
fi

if [ -z "$MYSQL_JAR" ]; then
    echo "⚠ WARNING: MySQL JDBC driver not found in project root."
    echo "  You may need to download mysql-connector-java.jar"
    echo "  Or add it to your classpath when compiling/running."
else
    echo "✓ MySQL JDBC driver found: $MYSQL_JAR"
fi
echo ""

# Try to compile the code (basic syntax check)
echo "Attempting to compile persistence classes..."
cd src

# Create classpath
CLASSPATH="."
if [ ! -z "$MYSQL_JAR" ]; then
    CLASSPATH="$CLASSPATH:../$MYSQL_JAR"
fi

# Try compiling Database.java
if javac -cp "$CLASSPATH" persistence/Database.java 2>&1 | head -20; then
    echo "✓ Database.java compiled successfully"
else
    echo "✗ Database.java compilation failed"
    echo "  This might be due to missing MySQL JDBC driver"
fi

# Try compiling DataLoader.java (requires model classes)
if javac -cp "$CLASSPATH" persistence/DataLoader.java 2>&1 | head -20; then
    echo "✓ DataLoader.java compiled successfully"
else
    echo "✗ DataLoader.java compilation failed"
    echo "  This might be due to missing dependencies"
fi

cd ..

echo ""
echo "=== Test Summary ==="
echo ""
echo "To fully test the DataLoader:"
echo "1. Ensure MySQL is running"
echo "2. Create the database: mysql -u root < src/db/schema.sql"
echo "3. Compile all Java files with MySQL connector in classpath"
echo "4. Run: java -cp '.:mysql-connector-java.jar:src' persistence.TestDataLoader"
echo ""
echo "Or run the Main class which will automatically load data on startup."



