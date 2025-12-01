# Compilation Guide

## Problem: Duplicate Folders

If you see duplicate folders like `model/`, `parser/`, `persistence/`, `service/` at the root level, these are created when Java compiles without specifying the output directory (`-d bin`).

## Solution

### 1. Clean Up Duplicate Folders

Run the cleanup script:
```bash
./cleanup.sh
```

This will:
- Remove root-level package folders (`model/`, `parser/`, `persistence/`, `service/`, `repository/`)
- Remove `.class` files from `src/` directory
- Remove root-level `.class` files

### 2. Compile Correctly

**Option A: Use the compile script (Recommended)**
```bash
./compile.sh
```

**Option B: Manual compilation**
```bash
# Compile all Java files to bin/ directory
find src -name "*.java" -exec javac -cp ".:lib/*:src" -d bin {} +
```

**Option C: Compile Main.java specifically**
```bash
javac -cp ".:lib/*:src" -d bin src/Main.java
```

### 3. Run the System

```bash
java -cp ".:lib/*:bin" Main
```

## Important Notes

- **Always use `-d bin`** when compiling to ensure output goes to the `bin/` directory
- **Never compile without `-d bin`** - this creates `.class` files in the source directories
- The `.gitignore` file has been updated to ignore duplicate folders and `.class` files in `src/`
- VS Code settings are configured to output compiled files to `bin/`

## Why This Happens

When you compile Java without the `-d` flag:
```bash
javac -cp ".:lib/*:src" src/Main.java  # ❌ WRONG - creates files in src/
```

Java creates `.class` files in the same directory structure as the source files. This is why you see:
- `src/model/Client.java` → `src/model/Client.class` (wrong location)
- Root-level `model/` folder with `.class` files (wrong location)

The correct way:
```bash
javac -cp ".:lib/*:src" -d bin src/Main.java  # ✅ CORRECT - creates files in bin/
```

This ensures:
- `src/model/Client.java` → `bin/model/Client.class` (correct location)

