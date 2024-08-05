package com.example.teabot;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // Hash a password using BCrypt
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    // Checks if inputted plain text password matches a hashed password
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
