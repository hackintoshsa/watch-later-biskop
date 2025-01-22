package com.hackintoshsa.repositories;

import com.hackintoshsa.models.User;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;


public interface UserRepository extends PanacheMongoRepository<User> {

    User findOneByEmail(String username);
    User findResetPasswordToken(String email, String passwordToken);
}
