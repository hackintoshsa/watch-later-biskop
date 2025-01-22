package com.hackintoshsa.models;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@MongoEntity(collection = "watch-later")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchLater extends PanacheMongoEntity {


    private Integer movieId; // The ID from the Movie Database API
    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private String releaseDate;
    private String mediaType;
    private Boolean video;
    private ObjectId userId;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();


}
