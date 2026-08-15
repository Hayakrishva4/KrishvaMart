package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.dao.impl.JdbcUserDAO;
import com.krishva.krishvamart.model.User;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcUserDAOTest {

    private HikariDataSource dataSource;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSource.create();
        userDAO = new JdbcUserDAO(dataSource);
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }

    @Test
    void insertAndFindById_roundTripsAllFields() throws Exception {
        User user = new User();
        user.setName("Divya Buyer");
        user.setEmail("divya@example.com");
        user.setPasswordHash("hashed-value");
        user.setRole(User.Role.BUYER);

        User saved = userDAO.insert(user);
        assertTrue(saved.getId() > 0);

        Optional<User> found = userDAO.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Divya Buyer", found.get().getName());
        assertEquals("divya@example.com", found.get().getEmail());
        assertEquals(User.Role.BUYER, found.get().getRole());
    }

    @Test
    void findByEmail_returnsEmptyWhenNoMatch() throws Exception {
        Optional<User> found = userDAO.findByEmail("nobody@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_returnsInsertedUsers() throws Exception {
        User a = new User();
        a.setName("A");
        a.setEmail("a@example.com");
        a.setPasswordHash("x");
        a.setRole(User.Role.BUYER);
        userDAO.insert(a);

        User b = new User();
        b.setName("B");
        b.setEmail("b@example.com");
        b.setPasswordHash("y");
        b.setRole(User.Role.SELLER);
        userDAO.insert(b);

        assertEquals(2, userDAO.findAll().size());
    }
}
