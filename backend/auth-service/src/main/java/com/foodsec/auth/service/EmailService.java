package com.foodsec.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications.
 *
 * Provides asynchronous email sending for:
 * - Email verification
 * - Password reset
 * - MFA setup notifications
 * - Security alerts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@foodsecuritynet.com}")
    private String fromAddress;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Sends email verification link
     *
     * @param to recipient email address
     * @param username recipient username
     * @param verificationToken verification token
     */
    @Async
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Verify Your FoodSecurityNet Account");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Thank you for registering with FoodSecurityNet.\n\n" +
                "Please verify your email address by clicking the link below:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not create an account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "FoodSecurityNet Team",
                username, verificationUrl
            ));

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    /**
     * Sends password reset link
     *
     * @param to recipient email address
     * @param username recipient username
     * @param resetToken password reset token
     */
    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Reset Your FoodSecurityNet Password");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "We received a request to reset your password.\n\n" +
                "Click the link below to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request a password reset, please ignore this email and contact support if you have concerns.\n\n" +
                "Best regards,\n" +
                "FoodSecurityNet Team",
                username, resetUrl
            ));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    /**
     * Sends MFA setup notification
     *
     * @param to recipient email address
     * @param username recipient username
     */
    @Async
    public void sendMfaEnabledNotification(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Two-Factor Authentication Enabled");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Two-factor authentication has been enabled on your FoodSecurityNet account.\n\n" +
                "Your account is now more secure. You will need to enter a verification code from your authenticator app when logging in.\n\n" +
                "If you did not enable two-factor authentication, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "FoodSecurityNet Team",
                username
            ));

            mailSender.send(message);
            log.info("MFA enabled notification sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send MFA enabled notification to: {}", to, e);
        }
    }

    /**
     * Sends security alert notification
     *
     * @param to recipient email address
     * @param username recipient username
     * @param alertMessage the security alert message
     */
    @Async
    public void sendSecurityAlert(String to, String username, String alertMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Security Alert - FoodSecurityNet");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "We detected unusual activity on your FoodSecurityNet account:\n\n" +
                "%s\n\n" +
                "If this was you, no action is needed. Otherwise, please secure your account immediately by changing your password.\n\n" +
                "Best regards,\n" +
                "FoodSecurityNet Team",
                username, alertMessage
            ));

            mailSender.send(message);
            log.info("Security alert sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send security alert to: {}", to, e);
        }
    }

    /**
     * Sends welcome email to new users
     *
     * @param to recipient email address
     * @param username recipient username
     */
    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("Welcome to FoodSecurityNet!");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Welcome to FoodSecurityNet! We're excited to have you join our community.\n\n" +
                "FoodSecurityNet connects people to fight food insecurity through donation coordination, " +
                "volunteer matching, and community support.\n\n" +
                "To get started:\n" +
                "1. Complete your profile\n" +
                "2. Explore available opportunities\n" +
                "3. Connect with your community\n\n" +
                "If you have any questions, our support team is here to help.\n\n" +
                "Best regards,\n" +
                "FoodSecurityNet Team",
                username
            ));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }
}
