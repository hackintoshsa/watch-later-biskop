package com.hackintoshsa.security;

import com.hackintoshsa.models.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@ApplicationScoped
public class Config {
    @ConfigProperty(name = "jwt.secret")
    String jwtSecret;
    @ConfigProperty(name = "quarkus.mailer.from")
    String fromEmailAddr;
    @ConfigProperty(name="app.baseUrl")
    String baseUrl;



    @Inject
    Mailer mailer;

    public String encodePassword(String password){
        return  BcryptUtil.bcryptHash(password);
    }
    public boolean decodePassword(String rawPassword, String encodedPassword){
        return BcryptUtil.matches(rawPassword, encodedPassword);
    }

    public String generateToken(User user){
        long expirationTime = 1000 * 60 * 60; // 1 hour in milliseconds

        return Jwt.subject(user.getEmail())
                .expiresAt(new Date(System.currentTimeMillis() + expirationTime).toInstant())
                .issuedAt(new Date().toInstant())
                .signWithSecret(jwtSecret);
    }

    //Not Working ---(*_0)--- decided to your javamail workings perfectly fine
    public void sendResetPasswordEmail(String email, String token) throws Exception{
        List<String> sendToEmails = new ArrayList<>();
        sendToEmails.add(email);
        String resetLink = baseUrl + "/reset-password?token=" + token;
        String emailContent = String.format(
                "<p>You are receiving this because you (or someone else) have requested the reset of the password for your account.</p>" +
                        "<p>Please click on the following link, or paste this into your browser to complete the process:</p>" +
                        "<a href='%s'>Reset Password</a>" +
                        "<p>If you did not request this, please ignore this email and your password will remain unchanged.</p>",
                resetLink
        );

        Mail mail = Mail.withHtml(email, "Biskop - Password Reset Request", emailContent);
        mail.setFrom(fromEmailAddr);
        mail.setTo(sendToEmails);
        mail.setText(emailContent);
        mailer.send(mail);
    }


}
