package com.krishva.krishvamart.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(
                plainPassword,
                BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(
            String plainPassword,
            String hashed) {

        if (plainPassword == null || hashed == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashed);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}