#!/bin/bash
# Compilation script that ensures output goes to bin/ directory

echo "=== Compiling Java source files ==="

# Clean bin directory first (optional - uncomment if you want clean builds)
# rm -rf bin/*

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files, ensuring output goes to bin/
echo "Compiling with output directory: bin/"
find src -name "*.java" -exec javac -cp ".:lib/*:src" -d bin {} +

if [ $? -eq 0 ]; then
    echo ""
    echo "=== Compilation successful ==="
    echo "All .class files are in bin/ directory"
    echo ""
    echo "To run the system:"
    echo "  java -cp \".:lib/*:bin\" Main"
else
    echo ""
    echo "=== Compilation failed ==="
    exit 1
fi

