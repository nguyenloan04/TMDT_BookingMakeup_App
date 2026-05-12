package com.example.tmdt_bookingmakeup_app.services;

import com.example.tmdt_bookingmakeup_app.common.enums.VerificationType;
import com.example.tmdt_bookingmakeup_app.dto.response.auth.VerifyResponse;
import com.example.tmdt_bookingmakeup_app.models.auth.Verification;
import com.example.tmdt_bookingmakeup_app.models.user.User;
import com.example.tmdt_bookingmakeup_app.repositories.UserRepository;
import com.example.tmdt_bookingmakeup_app.repositories.VerificationRepository;
import com.example.tmdt_bookingmakeup_app.security.PasswordEncryption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

// TODO: Bật lại khi config mail
// @Service
@Slf4j
public class VerificationService {
    private static final int LIMIT_ATTEMPT = 5;
    private static final int DEFAULT_OTP_LENGTH = 6;

    private int limitAttempt = LIMIT_ATTEMPT;
    private int otpLength = DEFAULT_OTP_LENGTH;

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Autowired
    public VerificationService(VerificationRepository verificationRepository, UserRepository userRepository, MailService mailService) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }


    public VerificationService(int limitAttempt, int otpLength, VerificationRepository verificationRepository, UserRepository userRepository, MailService mailService) {
        this.limitAttempt = limitAttempt;
        this.otpLength = otpLength;
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @Transactional
    public VerifyResponse sendVerificationCode(String email, VerificationType type) {
        try {
            // Does user exists
            User user = userRepository.findByEmail(email);
            if (user == null) {
                return new VerifyResponse(false, "User not found");
            }
            // Does user verified if type = VERIFY_USER
            if (type == VerificationType.VERIFY_USER && user.isVerified()) {
                return new VerifyResponse(false, "User is already verified");
            }
            // Does user have active otp at the same time
            Verification existVerification = verificationRepository.getByEmail(email);
            if (existVerification != null) {
                if (existVerification.getExpiredAt().isAfter(LocalDateTime.now()) && existVerification.getType() == type) {
                    return new VerifyResponse(false, "You already have verification code");
                }
            }

            //Create new verification code
            if (existVerification != null) {
                verificationRepository.delete(existVerification);
            }

            String otp = generateOTP(this.otpLength);
            Verification savedVerification = new Verification();
            savedVerification.setEmail(email);
            savedVerification.setType(type);
            savedVerification.setCode(otp);
            verificationRepository.save(savedVerification);

            //Send mail
            mailService.sendOtpEmail(email, otp);
            return new VerifyResponse(true, "Sent verification code successfully. Check your email to get your verification code.");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new VerifyResponse(false, "Failed when generating verification code");
        }
    }

    @Transactional
    public VerifyResponse verifyUser(String email, String otp) {
        try {
            User targetUser = userRepository.findByEmail(email);
            if (targetUser == null) {
                return new VerifyResponse(false, "User not found");
            }

            VerifyResponse verifyResult = this.verifyCode(email, otp, VerificationType.VERIFY_USER);
            if (verifyResult.result()) {
                targetUser.setVerified(true);
            }
            return verifyResult;
        } catch (Exception e) {
            log.error(e.getMessage());
            return new VerifyResponse(false, "Failed when verifying user");
        }
    }

    @Transactional
    public VerifyResponse verifyResetPassword(String email, String otp, String newPassword) {
        try {
            User targetUser = userRepository.findByEmail(email);
            if (targetUser == null) {
                return new VerifyResponse(false, "User not found");
            }

            VerifyResponse verifyResult = this.verifyCode(email, otp, VerificationType.RESET_PASSWORD);
            if (verifyResult.result()) {
                targetUser.setPassword(PasswordEncryption.hashPassword(newPassword));
            }
            return verifyResult;
        } catch (Exception e) {
            log.error(e.getMessage());
            return new VerifyResponse(false, "Failed when reset your password");
        }
    }

    private VerifyResponse verifyCode(String email, String otpCode, VerificationType type) {
        try {
            //Get current verification by email and type
            Verification targetVerification = verificationRepository.getByEmailAndType(email, type);
            if (targetVerification == null) {
                return new VerifyResponse(false, "Verification code not found");
            }
            //Check attempt
            if (targetVerification.getAttempts() >= this.limitAttempt) {
                verificationRepository.delete(targetVerification);
                return new VerifyResponse(
                        false,
                        "Maximum number of authentication attempts exceeded. Please request new verification code"
                );
            }
            //Check expired time
            if (targetVerification.getExpiredAt().isBefore(LocalDateTime.now())) {
                verificationRepository.delete(targetVerification);
                return new VerifyResponse(false, "Verification code expired");
            }

            if (!targetVerification.getCode().equals(otpCode)) {
                if (targetVerification.getType() == VerificationType.VERIFY_USER) {
                    targetVerification.setAttempts(targetVerification.getAttempts() + 1);
                    verificationRepository.save(targetVerification);
                }
                return new VerifyResponse(false, "Verification code does not match");
            }
            //Success
            verificationRepository.delete(targetVerification);
            return new VerifyResponse(true, "Verified successfully");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new VerifyResponse(false, "Failed when verifying code");
        }
    }

    private static String generateOTP(int length) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }
}
