package com.hackintoshsa.models;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

@MongoEntity(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends PanacheMongoEntity {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Boolean verified = false;
    private String resetPasswordToken;
    private Date resetPasswordExpires;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
    private List<Integer> watchLaterMovieIds;

}
