package com.hackintoshsa.controllers;

import com.hackintoshsa.models.User;
import com.hackintoshsa.services.UserService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/api")
public class UserResource {

    @Inject
    UserService userService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(User user) {
            return Response.ok(userService.loginUser(user.getEmail(), user.getPassword())).build();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
      return Response.ok(userService.registerUser(user)).build();
    }

    @POST
    @Path("/forgot-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(User user) {
        return Response.ok(userService.forgotPassword(user.getEmail())).build();
    }

    @POST
    @Path("/reset-password")
    @Blocking
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(User user) {
        return Response.ok(userService.resetPassword(user.getEmail(),user.getResetPasswordToken(),user.getPassword())).build();
    }


}
