package com.hackintoshsa.services;

import com.hackintoshsa.implementation.UserRepositoryImpl;
import com.hackintoshsa.models.User;
import com.hackintoshsa.security.Config;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepositoryImpl userRepositoryImpl;

    @Inject
    Config config;

    public Map<String, Object> loginUser(String email, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            response.put("status", 400);
            response.put("message", "Email and password cannot be null or empty.");
            return response;
        }

        // Find user by email
        User existingUser = userRepositoryImpl.findOneByEmail(email);
        if (existingUser == null) {
            response.put("status", 404);
            response.put("message", "User not found.");
            return response;
        }

        // Check if user is verified
        if (!existingUser.getVerified()) {
            response.put("status", 403);
            response.put("message", "Account not verified. Please check your email.");
            return response;
        }

        // Validate password
        boolean isPasswordMatch = config.decodePassword(password, existingUser.getPassword());
        if (!isPasswordMatch) {
            response.put("status", 401);
            response.put("message", "Incorrect password.");
            return response;
        }

        // Generate token and prepare response
        String token = config.generateToken(existingUser.getEmail(), existingUser.id.toString());
        String refreshToken = config.generateRefreshToken(existingUser.getEmail(), existingUser.id.toString());
        existingUser.setPassword(null); // Hide password in response
        response.put("status", 200);
        response.put("message", "Login successful.");
        response.put("data", existingUser);
        response.put("token", token);
        response.put("refreshToken", refreshToken);

        return response;
    }

    public Map<String, Object> registerUser(User user) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
            response.put("status", 400);
            response.put("message", "Invalid user data.");
            return response;
        }

        // Check if user already exists
        User existingUser = userRepositoryImpl.findOneByEmail(user.getEmail());
        if (existingUser != null) {
            response.put("status", 409);
            response.put("message", "User already exists.");
            return response;
        }

        // Encode password and generate token
        String encodedPassword = config.encodePassword(user.getPassword());
        user.setPassword(encodedPassword);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userRepositoryImpl.persist(user);

        // Generate token
        String token = config.generateToken(user.getEmail(), user.id.toString());
        user.setPassword(null); // Hide password in response

        response.put("status", 201);
        response.put("message", "User registered successfully.");
        response.put("data", user);
        response.put("token", token);

        return response;
    }

    public Map<String, Object> forgotPassword(String email) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepositoryImpl.findOneByEmail(email);
        Log.info(user);
        if (user == null) {
            response.put("status", 404);
            response.put("message", "User not found.");
            return response;
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(new Date(System.currentTimeMillis() + 3600000)); // 1 hour expiry
        userRepositoryImpl.persistOrUpdate(user);
        try{

            // Send reset email
            config.sendResetPasswordEmail(user.getEmail(), resetToken);

            response.put("status", 200);
            response.put("message", "Password reset email sent successfully...");
            response.put("token", resetToken);
            response.put("data", user);
            response.put("reset", "info, An e-mail has been sent to " + user.getEmail() + " "+ "with further instructions.");

        } catch (Exception e) {
            Log.error(e);
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    public Map<String, Object> resetPassword(String email, String token, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepositoryImpl.findResetPasswordToken(email, token);

        if (user == null || user.getResetPasswordExpires().before(new Date())) {
            response.put("status", 400);
            response.put("message", "Invalid user or expired token.");
            return response;
        }

        // Update user password
        user.setPassword(config.encodePassword(newPassword)); // Hash password before saving
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);
        user.setUpdatedAt(new Date());
        userRepositoryImpl.persist(user);

        response.put("status", 200);
        response.put("message", "Password has been reset successfully.");
        return response;
    }

    public Map<String, Object> refreshAccessToken(String refreshToken) {
        Map<String, Object> response = new HashMap<>();

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            response.put("status", 400);
            response.put("message", "Invalid or missing refresh token");
            return response;
        }

        String rawToken = refreshToken.substring("Bearer ".length()).trim();

        try {
            // Validate the refresh token
            if (config.validateRefreshToken(rawToken)) {
                // Extract user details from the refresh token
                String userId = config.extractUserIdFromToken(rawToken);
                String email = config.extractEmail(rawToken);

                // Generate a new access token
                String newAccessToken = config.generateToken(email, userId);

                // Prepare the response
                response.put("status", 200);
                response.put("token", newAccessToken);
                return response;
            } else {
                response.put("status", 401);
                response.put("message", "Invalid refresh token");
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Error refreshing access token: " + e.getMessage());
        }

        return response;
    }


}
