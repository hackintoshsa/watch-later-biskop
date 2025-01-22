package com.hackintoshsa.repositories;

import com.hackintoshsa.models.WatchLater;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;


public interface WatchLaterRepository extends PanacheMongoRepository<WatchLater> {
    WatchLater findByUserId(Object userId);
    WatchLater deleteById(String movieId);
    WatchLater existsByMovieId(String movieId);
    WatchLater listAllByMovieId(String movieId);
    List<WatchLater> listAllByUserId(Object userId);
}
