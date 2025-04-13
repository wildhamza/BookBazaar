package com.bookshop.services;

import com.bookshop.db.DatabaseConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement; 
import java.util.stream.Collectors;

public class DatabaseInitializationService {
    
    private static final String INIT_SCRIPT_PATH = "/db/init_database.sql";
    
    public void initializeDatabase() throws Exception {
        try (@SuppressWarnings("deprecation")
        Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String url = conn.getMetaData().getURL();
            if (!url.contains("allowMultiQueries=true")) {
                throw new SQLException("Database URL must include 'allowMultiQueries=true' parameter");
            }
            
            String sqlScript = readSqlScript();
            executeSqlScript(conn, sqlScript);
        }
    }
    
    private void executeSqlScript(Connection conn, String sqlScript) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String[] statements = sqlScript.split(";");
            
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
    
    private String readSqlScript() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(INIT_SCRIPT_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
} 