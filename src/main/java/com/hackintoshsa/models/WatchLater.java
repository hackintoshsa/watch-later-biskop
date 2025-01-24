package com.hackintoshsa.models;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@MongoEntity(collection = "watch-later", database = "biskop")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchLater extends PanacheMongoEntity {


    private Integer movieId; // The ID from the Movie Database API
    private String title;
    private String overview;
    private String poster_path;
    private String backdrop_path;
    private String release_date;
    private String media_type;
    private Boolean video;
    private ObjectId userId;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();


}
