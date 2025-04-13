package com.bookshop.repositories;

import java.sql.SQLException;
import java.util.List;

public interface Repository<T, ID> {
    
    List<T> findAll() throws SQLException;
    
    T findById(ID id) throws SQLException;
    
    ID save(T entity) throws SQLException;
    
    boolean update(T entity) throws SQLException;
    
    boolean delete(ID id) throws SQLException;
} 