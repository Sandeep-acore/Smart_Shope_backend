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
            
            mailSender.send(message);
            logger.info("OTP email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send OTP email: {}", e.getMessage());
            // In development mode, log the OTP for testing
            logger.info("Development mode - OTP for {}: {}", to, otp);
        }
    }
} 