package com.smartshop.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        try {
            logger.info("Preparing to send OTP email to: {}", to);
            
            // Validate email address
            if (to == null || to.trim().isEmpty()) {
                logger.error("Cannot send email: recipient email is null or empty");
                throw new IllegalArgumentException("Recipient email cannot be null or empty");
            }
            
            // Validate OTP
            if (otp == null || otp.trim().isEmpty()) {
                logger.error("Cannot send email: OTP is null or empty");
                throw new IllegalArgumentException("OTP cannot be null or empty");
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setTo(to);
            helper.setSubject("Smart Shop - Password Reset OTP");
            
            String emailContent = "<div style='font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 5px;'>"
                    + "<h2 style='color: #333;'>Password Reset Request</h2>"
                    + "<p>You have requested to reset your password. Please use the following OTP to complete the process:</p>"
                    + "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 24px; letter-spacing: 5px; margin: 20px 0;'>"
                    + otp
                    + "</div>"
                    + "<p>This OTP is valid for 10 minutes. If you did not request a password reset, please ignore this email.</p>"
                    + "<p>Regards,<br>Smart Shop Team</p>"
                    + "</div>";
            
            helper.setText(emailContent, true);
            
            logger.info("Attempting to send email to: {}", to);
            try {
                mailSender.send(message);
                logger.info("Successfully sent OTP email to: {}", to);
            } catch (Exception e) {
                logger.error("Failed to send email using mailSender: {}", e.getMessage(), e);
                // In development mode, log the OTP for testing
                logger.info("Development mode - OTP for {}: {}", to, otp);
                throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
            }
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
            // In development mode, log the OTP for testing
            logger.info("Development mode - OTP for {}: {}", to, otp);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for email sending: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error sending OTP email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }
} 