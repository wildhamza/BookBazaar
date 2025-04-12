package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for initializing the database with required tables and data.
 */
public class DatabaseInitializationService {
    
    private static final String INIT_SCRIPT_PATH = "/db/init_database.sql";
    
    /**
     * Initializes the database by running the initialization script.
     * 
     * @throws Exception If initialization fails
     */
    public void initializeDatabase() throws Exception {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Enable multiple statement execution
            String url = conn.getMetaData().getURL();
            if (!url.contains("allowMultiQueries=true")) {
                throw new SQLException("Database URL must include 'allowMultiQueries=true' parameter");
            }
            
            // Read and execute the SQL script
            String sqlScript = readSqlScript();
            executeSqlScript(conn, sqlScript);
        }
    }
    
    /**
     * Executes a SQL script containing multiple statements.
     * 
     * @param conn The database connection
     * @param sqlScript The SQL script to execute
     * @throws SQLException If execution fails
     */
    private void executeSqlScript(Connection conn, String sqlScript) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Split the script into individual statements
            String[] statements = sqlScript.split(";");
            
            // Execute each statement
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    try {
                        stmt.execute(trimmedStatement);
                    } catch (SQLException e) {
                        System.err.println("Error executing statement: " + trimmedStatement);
                        throw e;
                    }
                }
            }
        }
    }
    
    /**
     * Reads the SQL initialization script from resources.
     * 
     * @return The SQL script as a string
     * @throws Exception If reading the script fails
     */
    private String readSqlScript() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(INIT_SCRIPT_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
} 