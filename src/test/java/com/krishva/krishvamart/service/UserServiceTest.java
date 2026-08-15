package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.UnauthorizedException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    void register_rejectsAdminSelfSignup() {
        assertThrows(ValidationException.class,
                () -> userService.register("Name", "a@example.com", "password123", "ADMIN"));
    }

    @Test
    void register_rejectsShortPassword() {
        assertThrows(ValidationException.class,
                () -> userService.register("Name", "a@example.com", "short", "BUYER"));
    }

    @Test
    void register_rejectsInvalidEmail() {
        assertThrows(ValidationException.class,
                () -> userService.register("Name", "not-an-email", "password123", "BUYER"));
    }

    @Test
    void register_rejectsDuplicateEmail() throws Exception {
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.of(new User()));
        assertThrows(ConflictException.class,
                () -> userService.register("Name", "a@example.com", "password123", "BUYER"));
    }

    @Test
    void register_hashesPasswordBeforePersisting() throws Exception {
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(userDAO.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register("Name", "a@example.com", "password123", "BUYER");

        assertEquals("a@example.com", saved.getEmail());
        assertEquals(User.Role.BUYER, saved.getRole());
        org.junit.jupiter.api.Assertions.assertNotEquals("password123", saved.getPasswordHash());
        verify(userDAO).insert(any(User.class));
    }

    @Test
    void login_rejectsWrongPassword() throws Exception {
        User existing = new User();
        existing.setEmail("a@example.com");
        existing.setPasswordHash(PasswordUtil.hash("correct-password"));
        existing.setRole(User.Role.BUYER);
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

        assertThrows(UnauthorizedException.class, () -> userService.login("a@example.com", "wrong-password"));
    }

    @Test
    void login_succeedsWithCorrectPassword() throws Exception {
        User existing = new User();
        existing.setEmail("a@example.com");
        existing.setPasswordHash(PasswordUtil.hash("correct-password"));
        existing.setRole(User.Role.BUYER);
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

        User result = userService.login("a@example.com", "correct-password");
        assertEquals("a@example.com", result.getEmail());
    }
}
