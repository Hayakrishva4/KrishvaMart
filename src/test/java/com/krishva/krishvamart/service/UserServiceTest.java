package com.krishva.krishvamart.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.UnauthorizedException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.PasswordUtil;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    public void register_allowsAdminSelfSignup() throws Exception {
        when(userDAO.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(userDAO.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register("Admin User", "admin@example.com", "password123", "ADMIN");

        assertEquals("admin@example.com", saved.getEmail());
        assertEquals(User.Role.ADMIN, saved.getRole());
        verify(userDAO).insert(any(User.class));
    }

    @Test
    public void register_rejectsInvalidRole() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.register("Name", "a@example.com", "password123", "SUPERUSER"));
        assertNotNull(ex);
    }

    @Test
    public void register_rejectsShortPassword() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.register("Name", "a@example.com", "short", "BUYER"));
        assertNotNull(ex);
    }

    @Test
    public void register_rejectsInvalidEmail() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.register("Name", "not-an-email", "password123", "BUYER"));
        assertNotNull(ex);
    }

    @Test
    public void register_rejectsDuplicateEmail() throws Exception {
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.of(new User()));
        ConflictException ex = assertThrows(ConflictException.class,
                () -> userService.register("Name", "a@example.com", "password123", "BUYER"));
        assertNotNull(ex);
    }

    @Test
    public void register_hashesPasswordBeforePersisting() throws Exception {
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(userDAO.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register("Name", "a@example.com", "password123", "BUYER");

        assertEquals("a@example.com", saved.getEmail());
        assertEquals(User.Role.BUYER, saved.getRole());
        org.junit.jupiter.api.Assertions.assertNotEquals("password123", saved.getPasswordHash());
        verify(userDAO).insert(any(User.class));
    }

    @Test
    public void login_rejectsWrongPassword() throws Exception {
        User existing = new User();
        existing.setEmail("a@example.com");
        existing.setPasswordHash(PasswordUtil.hash("correct-password"));
        existing.setRole(User.Role.BUYER);
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> userService.login("a@example.com", "wrong-password"));
        assertNotNull(ex);
    }

    @Test
    public void login_succeedsWithCorrectPassword() throws Exception {
        User existing = new User();
        existing.setEmail("a@example.com");
        existing.setPasswordHash(PasswordUtil.hash("correct-password"));
        existing.setRole(User.Role.BUYER);
        when(userDAO.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

        User result = userService.login("a@example.com", "correct-password");
        assertEquals("a@example.com", result.getEmail());
    }
}
