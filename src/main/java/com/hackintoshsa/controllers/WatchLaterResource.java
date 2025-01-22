package com.hackintoshsa.controllers;

import com.hackintoshsa.models.WatchLater;
import com.hackintoshsa.services.WatchLaterService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/watchlater")
public class WatchLaterResource {
    @Inject
    WatchLaterService watchLaterService;

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
    public Response listMovies(@PathParam("userId") String userId){
        return Response.status(Response.Status.OK).entity(watchLaterService.listAllByUser(userId)).build();
    }

    @DELETE
    @Path("/delete/{movieId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteByMovieId(@PathParam("movieId")String movieId){
        return Response.status(Response.Status.OK).entity(watchLaterService.deleteMovieFromWatchLater(movieId)).build();
    }
}
