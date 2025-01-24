package com.hackintoshsa.services;

import com.hackintoshsa.implementation.UserRepositoryImpl;
import com.hackintoshsa.implementation.WatchLaterRepositoryImpl;
import com.hackintoshsa.models.User;
import com.hackintoshsa.models.WatchLater;
import com.hackintoshsa.repositories.UserRepository;
import com.hackintoshsa.repositories.WatchLaterRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.java.Log;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.net.http.HttpResponse;
import java.util.*;

@Log
@ApplicationScoped
public class WatchLaterService {

    @Inject
    WatchLaterRepositoryImpl watchLaterRepository;

    @Inject
    UserRepositoryImpl userRepository;
    public Map<String, Object> addMovieToWatchLater(String userId, WatchLater movie) {
        Map<String, Object> response = new HashMap<>();


        // Retrieve the user from the database using ObjectId
        User user = userRepository.findById(new ObjectId(userId));
        
        if (user == null) {
            response.put("status", 404);
            response.put("message", "User not found.");
            return response;
        }

        // Initialize the watchLaterMovies list if it's null
        if (user.getWatchLaterMovieIds() == null) {
            user.setWatchLaterMovieIds(new ArrayList<>());

        }


        for (Integer existingMovieId : user.getWatchLaterMovieIds()) {
            if (existingMovieId != null && existingMovieId.equals(movie.getMovieId())) {
                response.put("status", 409);
                response.put("message", "Movie already exists in your Watch Later list.");
                return response;
            }
        }

        // Set movie details and persist to the database
        movie.setUserId(new ObjectId(userId));
        movie.setCreatedAt(new Date());
        movie.setUpdatedAt(new Date());
        watchLaterRepository.persist(movie);

        // Add the movie's ObjectId to the user's Watch Later list
        user.getWatchLaterMovieIds().add(movie.getMovieId());
        userRepository.update(user);

        // Respond with success
        response.put("status", 200);
        response.put("message", "Movie added to Watch Later successfully.");
        response.put("data", movie);
        return response;
    }

    public Map<String, Object> listAllByUser(String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            ObjectId userObjectId = new ObjectId(userId);

            log.warning("Fetching WatchLater items for userId: " + userId);

            // Fetch the watch later list from the repository using the user ID
            List<WatchLater> watchLaterList = watchLaterRepository.listAllByUserId(userObjectId);


            // Check if the list is empty and populate the response accordingly
            if (watchLaterList != null && !watchLaterList.isEmpty()) {
                response.put("status", "success");
                response.put("data", watchLaterList);
                response.put("message", "Successfully fetched all watch later items");
                response.put("statusCode", 200);
            } else {
                response.put("status", "not_found");
                response.put("StatusCode", 404);
                response.put("message", "No watch later items found for this user.");
            }
        } catch (IllegalArgumentException e) {
            log.warning("Invalid userId: " + userId + " " + e);
            response.put("status", "error");
            response.put("message", "Invalid user ID format.");
            response.put("statusCode", 400);
        } catch (Exception e) {
            log.warning("Error fetching watch later items for userId: " + userId + " " + e);
            response.put("status", "error");
            response.put("message", "An error occurred while fetching watch later items.");
            response.put("statusCode", 500);
        }

        return response;
    }

    public Map<String, Object> deleteMovieFromWatchLater(String movieId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Delete the movie from the WatchLater list based on the movieId
            WatchLater deletedMovie = watchLaterRepository.deleteById(movieId);

            if (deletedMovie != null) {
                response.put("status", "success");
                response.put("statusCode", 200);
                response.put("message", "Movie successfully removed from Watch Later list.");
            } else {
                response.put("status", "not_found");
                response.put("StatusCode", 404);
                response.put("message", "Movie not found in Watch Later list.");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while deleting the movie: " + e.getMessage());
        }

        return response;
    }

}
