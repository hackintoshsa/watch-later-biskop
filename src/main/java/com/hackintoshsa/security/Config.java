package com.hackintoshsa.security;

import com.hackintoshsa.models.User;
import io.github.cdimascio.dotenv.Dotenv;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.smallrye.jwt.auth.principal.JWTParser;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.jsonwebtoken.Claims;




import io.smallrye.jwt.build.Jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.X509Util;
import org.jose4j.lang.JoseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;


@ApplicationScoped
public class Config {
    @ConfigProperty(name = "jwt.secret")
    String jwtSecret;
    @ConfigProperty(name = "quarkus.mailer.from")
    String fromEmailAddr;
    @ConfigProperty(name="app.baseUrl")
    String baseUrl;

    private static final Logger log = Logger.getLogger(Config.class.getName());

    @Inject
    Mailer mailer;


    @Inject
    JWTParser jwtParser;

    Dotenv dotenv = Dotenv.load();


    public String encodePassword(String password){
        return  BcryptUtil.bcryptHash(password);
    }
    public boolean decodePassword(String rawPassword, String encodedPassword){
        return BcryptUtil.matches(rawPassword, encodedPassword);
    }


    public String generateToken(String email, String userId) {
        try{
            PrivateKey privateKey = loadKeys(dotenv.get("JWT_SIGN_KEY_LOCATION"));
            long expirationTime = 1000 * 60 * 60; // 1 hour for access token expiration

            // Generate JWT signed with the RSA private key (RS256)
            return Jwt.subject(email)
                    .claim("email", email)
                    .claim("id", userId)
                    .expiresAt(new Date(System.currentTimeMillis() + expirationTime).toInstant())
                    .issuedAt(new Date().toInstant())
                    .sign(privateKey); // Sign with the RSA private key
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error generating token: " + e.getMessage(), e);
        }

    }

    public String generateRefreshToken(String email, String userId) {
        try {
            PrivateKey privateKey = loadKeys(dotenv.get("JWT_SIGN_KEY_LOCATION")); // Same private key used for both access and refresh tokens
            long expirationTime = 1000 * 60 * 60 * 24 * 30; // 30 days for refresh token expiration

            // Generate JWT signed with the RSA private key (RS256) for the refresh token
            return Jwt.subject(email)
                    .claim("email", email)
                    .claim("id", userId)
                    .expiresAt(new Date(System.currentTimeMillis() + expirationTime).toInstant())
                    .issuedAt(new Date().toInstant())
                    .sign(privateKey); // Sign with the RSA private key
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating refresh token: " + e.getMessage(), e);
        }
    }

    //Not Working ---(*_0)--- decided to your javamail workings perfectly fine
    public void sendResetPasswordEmail(String email, String token) throws Exception{
        List<String> sendToEmails = new ArrayList<>();
        sendToEmails.add(email);
        String resetLink = dotenv.get("APP_BASE_URL") + "/reset-password?token=" + token;
        String emailContent = String.format(
                "<p>You are receiving this because you (or someone else) have requested the reset of the password for your account.</p>" +
                        "<p>Please click on the following link, or paste this into your browser to complete the process:</p>" +
                        "<a href='%s'>Reset Password</a>" +
                        "<p>If you did not request this, please ignore this email and your password will remain unchanged.</p>",
                resetLink
        );

        Mail mail = Mail.withHtml(email, "Biskop - Password Reset Request", emailContent);
        mail.setFrom(dotenv.get("FROM_EMAIL"));
        mail.setTo(sendToEmails);
        mail.setText(emailContent);
        mailer.send(mail);
    }

    public boolean validateToken(String token) {
        try {
            return validateTokenInternal(token);
        } catch (Exception e) {
            log.warning("Token validation failed for token: " + token + " "+ e);
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            return validateTokenInternal(refreshToken);
        } catch (Exception e) {
            log.warning("Refresh token validation failed for token: " + refreshToken + " "+ e);
            return false;
        }
    }

    private PrivateKey loadKeys(String privateKeyPath) throws Exception {
        // Read the key content
        String keyContent = Files.readString(Paths.get(privateKeyPath))
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Decode the key
        byte[] keyBytes = java.util.Base64.getDecoder().decode(keyContent);

        // Generate a PrivateKey object
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey loadPublicKey(String publicKeyPath) throws Exception {
        // Read the key content
        String keyContent = Files.readString(Paths.get(publicKeyPath))
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // Decode the key
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);

        // Generate a PublicKey object
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }


    public boolean validateTokenInternal(String token) throws Exception {
        PublicKey publicKey = loadPublicKey(dotenv.get("JWT_VERIFY_KEY_LOCATION"));
        if (publicKey == null) {
            System.out.println("Public key not loaded. Please call loadPublicKey() first.");
            return false;
        }

        // Validate the token format
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        token = token.trim();

        // Remove 'Bearer ' prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Validate token structure
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT structure");
        }

        System.out.println("Public " + token);
        try {

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime() // Require expiration claim
                    .setRequireIssuedAt() // Require issued-at claim
                    .setVerificationKey(publicKey) // Set public key for verification
                    .build();

            // Process the token to validate and extract claims
            jwtConsumer.processToClaims(token);
            return true; // Token is valid
        } catch (Exception e) {
            // Token validation failed (e.g., invalid signature, expired, etc.)
            System.out.println("JWT validation failed: " + e.getMessage());
            return false;
        }
    }

    public String extractUserIdFromToken(String token) {
        try {
            return extractAndPrintClaims(token).getStringClaimValue("id"); // Assumes "sub" claim is the userId
        } catch (Exception e) {
            log.warning("Error extracting userId from token: " + token + " "+e);
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            return extractAndPrintClaims(token).getStringClaimValue("email");
        } catch (Exception e) {
            log.warning("Error extracting email from token: " + token + " "+ e);
            return null;
        }
    }

    private JwtClaims extractAndPrintClaims(String token) throws Exception {
        PublicKey publicKey = loadPublicKey(dotenv.get("JWT_VERIFY_KEY_LOCATION"));
        try {
            // Validate the token format
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            token = token.trim();

            // Remove 'Bearer ' prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Build the JwtConsumer
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime() // Require expiration claim
                    .setRequireIssuedAt() // Require issued-at claim
                    .setVerificationKey(publicKey) // Set public key for verification
                    .build();
            return jwtConsumer.processToClaims(token);
        } catch (Exception e) {
            System.err.println("Failed to process token: " + e.getMessage());
           throw e;
        }
    }


}
