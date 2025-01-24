package com.hackintoshsa.controllers;

import com.hackintoshsa.models.WatchLater;
import com.hackintoshsa.security.Config;
import com.hackintoshsa.services.WatchLaterService;

import io.quarkus.security.Authenticated;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;


@Path("/watchlater")
@Log
public class WatchLaterResource {
    @Inject
    WatchLaterService watchLaterService;

     @Inject
    Config config;

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //public Response watchLater(WatchLater watchLater, @HeaderParam("Authorization")String token){
    public Response watchLater(WatchLater watchLater,@HeaderParam("userId") String userId){
        Map<String, Object> responseMap = new HashMap<>();

//            if (token == null || token.isEmpty()) {
//                responseMap.put("status", "error");
//                responseMap.put("message", "Authorization token is missing or invalid");
//                responseMap.put("statusCode", 401);
//                return Response
//                        .status(Response.Status.UNAUTHORIZED)
//                        .entity(responseMap)
//                        .build();
//            }
            return Response.status(Response.Status.OK).entity(watchLaterService.addMovieToWatchLater(userId,watchLater)).build();


    }

    @GET
    @Path("/list/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response listMovies(@PathParam("userId") String userId, @HeaderParam("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String rawToken = token.substring("Bearer ".length()).trim();
            System.out.println("Extracted Token: " + rawToken);
        }
        System.out.println("Received token from @HeaderParam: " + token );



        Map<String, Object> response = new HashMap<>();
        try {
            log.warning("Received request to list movies for userId: " + userId);
            log.warning("Token: " + token);

            if (token == null || token.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Missing authorization token.");
                response.put("statusCode", 400);
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }


            // Validate the token
            if (!config.validateToken(token)) {
                response.put("status", "error");
                response.put("message", "Invalid or expired token.");
                response.put("statusCode", 401);
                return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
            }
            log.warning("Extracted userId from token: ");


            // Extract userId from the token
            String tokenUserId = config.extractUserIdFromToken(token);
            log.warning("Extracted userId from token: " + tokenUserId);

            // Check if the userId in the path matches the token
            if (!userId.equals(tokenUserId)) {
                response.put("status", "error");
                response.put("message", "Unauthorized access. UserId mismatch.");
                response.put("statusCode", 403);
                return Response.status(Response.Status.FORBIDDEN).entity(response).build();
            }

            // Fetch the watch later list
            Map<String, Object> watchLaterData = watchLaterService.listAllByUser(userId);
            return Response.ok(watchLaterData).build();

        } catch (Exception e) {
            log.warning("Error occurred while processing listMovies request for userId: " + userId + " " + e);

            response.put("status", "error");
            response.put("message", "An internal server error occurred.");
            response.put("statusCode", 500);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }

    @DELETE
    @Path("/delete/{movieId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteByMovieId(@PathParam("movieId")String movieId){
        return Response.status(Response.Status.OK).entity(watchLaterService.deleteMovieFromWatchLater(movieId)).build();
    }



        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Authenticated
        public String secureHello (@HeaderParam("Authorization")String token){
        System.out.println("myToken" + token);

        return "This is a secure endpoint!" + token;

    }
}