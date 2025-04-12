package com.bookshop.repositories;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic repository interface defining common data access operations.
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public interface Repository<T, ID> {
    
    /**
     * Find all entities.
     * 
     * @return List of all entities
     * @throws SQLException If a database error occurs
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Find an entity by its ID.
     * 
     * @param id The entity ID
     * @return The entity, or null if not found
     * @throws SQLException If a database error occurs
     */
    T findById(ID id) throws SQLException;
    
    /**
     * Save a new entity.
     * 
     * @param entity The entity to save
     * @return The ID of the newly saved entity
     * @throws SQLException If a database error occurs
     */
    ID save(T entity) throws SQLException;
    
    /**
     * Update an existing entity.
     * 
     * @param entity The entity to update
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean update(T entity) throws SQLException;
    
    /**
     * Delete an entity by its ID.
     * 
     * @param id The entity ID
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    boolean delete(ID id) throws SQLException;
} 