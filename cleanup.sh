#!/bin/bash
# Cleanup script to remove duplicate compiled folders and class files

echo "=== Cleaning up duplicate folders and compiled files ==="

# Remove root-level package folders (these should only exist in src/ and bin/)
echo "Removing root-level package folders..."
rm -rf model/ parser/ persistence/ service/ repository/

# Remove .class files from src/ directory
echo "Removing .class files from src/ directory..."
find src/ -name "*.class" -type f -delete

# Remove root-level .class files
echo "Removing root-level .class files..."
rm -f *.class

# Remove TestIteration3.class if it exists
rm -f TestIteration3.class

echo ""
echo "=== Cleanup complete ==="
echo "Compiled files should only be in bin/ directory"
echo ""
echo "To compile correctly, use:"
echo "  javac -cp \".:lib/*:src\" -d bin src/Main.java"
echo ""
echo "Or compile all Java files:"
echo "  find src -name '*.java' -exec javac -cp \".:lib/*:src\" -d bin {} +"

