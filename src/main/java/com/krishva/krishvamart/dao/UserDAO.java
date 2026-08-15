package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.User;

import java.util.List;
import java.util.Optional;

/** Data access for the {@code users} table. All SQL lives in the implementation, PreparedStatement only. */
public interface UserDAO {

    /** Persists a new user and returns it with the generated id populated. */
    User insert(User user) throws DataAccessException;

    /** Looks up a user by primary key. */
    Optional<User> findById(long id) throws DataAccessException;

    /** Looks up a user by their unique email address (case-sensitive as stored). */
    Optional<User> findByEmail(String email) throws DataAccessException;

    /** Returns every user, most recently created first. Used by the admin user list (F7). */
    List<User> findAll() throws DataAccessException;
}
