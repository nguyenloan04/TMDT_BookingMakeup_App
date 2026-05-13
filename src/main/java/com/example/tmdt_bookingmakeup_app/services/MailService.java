package com.example.tmdt_bookingmakeup_app.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//
//    @Value("${app.mail.from-address}")
//    private String fromAddress;
//
//    @Value("${app.mail.from-name}")
//    private String fromName;
//
//    @Async
//    public void sendOtpEmail(String to, String otp) {
//        try {
//            Context context = new Context();
//            context.setVariable("email", to);
//            context.setVariable("otp", otp);
//            context.setVariable("expireTime", 5);
//
//            String htmlContent = templateEngine.process("email/otp-verification", context);
//
//            sendHtmlMail(to, "HomeBook - OTP Verification", htmlContent);
//
//            log.info("Email sent successfully to {}", to);
//        } catch (Exception e) {
//            log.error("CRITICAL: Could not send email to {}. Error: {}", to, e.getMessage());
//        }
//    }
//
//
//    private void sendHtmlMail(String to, String subject, String htmlContent) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            throw new RuntimeException("Failed to send email", e);
//        }
//    }
}
