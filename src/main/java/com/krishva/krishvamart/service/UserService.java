package com.krishva.krishvamart.service;

import com.krishva.krishvamart.dao.UserDAO;
import com.krishva.krishvamart.exception.AppException;
import com.krishva.krishvamart.exception.ConflictException;
import com.krishva.krishvamart.exception.UnauthorizedException;
import com.krishva.krishvamart.exception.ValidationException;
import com.krishva.krishvamart.model.User;
import com.krishva.krishvamart.util.PasswordUtil;
import com.krishva.krishvamart.util.ValidationUtil;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String name, String email, String password, String roleRaw) throws AppException {
        if (ValidationUtil.isBlank(name)) {
            throw new ValidationException("name", "Name is required");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("email", "A valid email is required");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("password", "Password must be at least 8 characters");
        }
        User.Role role = parseSignupRole(roleRaw);

        if (userDAO.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole(role);
        return userDAO.insert(user);
    }

    public User login(String email, String password) throws AppException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            throw new ValidationException("email", "Email and password are required");
        }
        User user = userDAO.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return user;
    }

    public java.util.List<User> listAll() throws AppException {
        return userDAO.findAll();
    }

    private User.Role parseSignupRole(String roleRaw) throws ValidationException {
        if (ValidationUtil.isBlank(roleRaw)) {
            throw new ValidationException("role", "Role is required");
        }
        String normalized = roleRaw.trim().toUpperCase();
        if (normalized.equals("ADMIN")) {
            throw new ValidationException("role", "Admin accounts cannot self-register");
        }
        try {
            return User.Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("role", "Role must be BUYER or SELLER");
        }
    }
}
