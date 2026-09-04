package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {

    User insert(User user) throws DataAccessException;

    Optional<User> findById(long id) throws DataAccessException;
    Optional<User> findByEmail(String email) throws DataAccessException;
    List<User> findAll() throws DataAccessException;
}
