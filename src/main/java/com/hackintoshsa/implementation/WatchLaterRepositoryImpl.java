package com.hackintoshsa.implementation;

import com.hackintoshsa.models.WatchLater;
import com.hackintoshsa.repositories.WatchLaterRepository;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WatchLaterRepositoryImpl implements WatchLaterRepository, PanacheMongoRepository<WatchLater> {
    @Override
    public WatchLater findByUserId(Object userId) {
        return find("userId", userId).firstResult();
    }

    @Override
    public WatchLater deleteById(String movieId) {
        return find("movieId", movieId).firstResult();
    }

    @Override
    public WatchLater existsByMovieId(String movieId) {
        return find("movieId", movieId).firstResult();
    }

    @Override
    public WatchLater listAllByMovieId(String movieId) {
        return null;
    }

    @Override
    public List<WatchLater> listAllByUserId(Object userId) {
        return find("userId", userId).list();
    }
}
