#!/bin/bash

# Script to manually apply database migrations for DevLabs Backend
# This script helps fix missing database columns

echo "DevLabs Backend - Database Migration Helper"
echo "==========================================="

# Check if we're in the correct directory
if [ ! -f "build.gradle.kts" ]; then
    echo "Error: Please run this script from the devlabs-backend root directory"
    exit 1
fi

echo "Available migration options:"
echo "1. Apply review publication status migration (add is_published column)"
echo "2. Check current database schema for review table"
echo "3. Force recreate database schema (WARNING: This will drop all data)"
echo "4. Run application with schema validation"

read -p "Select option (1-4): " option

case $option in
    1)
        echo "Applying review publication status migration..."
        # Note: You would need to connect to your database and run the SQL
        echo "Please run the following SQL in your database:"
        echo "================================================================"
        cat add-review-publication-status-safe.sql
        echo "================================================================"
        ;;
    2)
        echo "To check the current schema, run this SQL query in your database:"
        echo "SELECT column_name, data_type, is_nullable, column_default"
        echo "FROM information_schema.columns"
        echo "WHERE table_name = 'review' AND table_schema = 'public'"
        echo "ORDER BY ordinal_position;"
        ;;
    3)
        echo "WARNING: This will recreate the entire database schema!"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            echo "Setting hibernate.ddl-auto=create-drop for next run..."
            echo "Please temporarily change spring.jpa.hibernate.ddl-auto=create-drop in application-dev.properties"
            echo "Then run: ./gradlew bootRun"
            echo "REMEMBER to change it back to 'update' afterwards!"
        fi
        ;;
    4)
        echo "Running application with schema validation..."
        ./gradlew bootRun --args="--spring.profiles.active=dev"
        ;;
    *)
        echo "Invalid option selected"
        exit 1
        ;;
esac
