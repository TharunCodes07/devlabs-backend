#!/bin/bash

# Quick fix script for is_published column issues
# Run this if the automatic migration doesn't work

echo "Applying database fix for is_published column..."

# You can modify these connection details as needed
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="devlabs"
DB_USER="postgres"

# Prompt for password
echo "Enter PostgreSQL password for user $DB_USER:"
read -s DB_PASSWORD

# Run the migration SQL
PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -d $DB_NAME -U $DB_USER -f fix-is-published-null-values.sql

if [ $? -eq 0 ]; then
    echo "✅ Database migration completed successfully!"
    echo "You can now restart your application and test the endpoints."
else
    echo "❌ Database migration failed. Please check your connection details and try again."
    echo "Alternative: Copy the SQL from fix-is-published-null-values.sql and run it manually in your database client."
fi
