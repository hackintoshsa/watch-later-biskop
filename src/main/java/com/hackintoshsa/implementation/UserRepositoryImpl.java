package com.hackintoshsa.implementation;

import com.hackintoshsa.models.User;
import com.hackintoshsa.repositories.UserRepository;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository, PanacheMongoRepository<User> {

    // Method to find a user by email
    @Override
    public User findOneByEmail(String username) {
        return find("email", username).firstResult();
    }

    // Method to find a user by email and password token (used for reset)
    @Override
    public User findResetPasswordToken(String email, String passwordToken) {
        return find("email = ?1 and passwordToken = ?2", email, passwordToken).firstResult();
    }
}
