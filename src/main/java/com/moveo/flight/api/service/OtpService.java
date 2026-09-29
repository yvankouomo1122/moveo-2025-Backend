package com.moveo.flight.api.service;

import com.moveo.flight.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.moveo.flight.api.service.EmailService;
import com.moveo.flight.api.service.SmsService;

@Service
public class OtpService {

    private final SmsService smsService;
    private final EmailService emailService;

    public OtpService(SmsService smsService, EmailService emailService) {
        this.smsService = smsService;
        this.emailService = emailService;
    }

    public boolean sendOtp(User user, String otp) {
        try{
            String message = "Your OTP for verification is: " + otp;
            // smsService.sendSms(user.getPhoneNumber(), message);
            emailService.sendEmail(user.getEmail(), "OTP Verification", message);
            return true;
        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(OtpService.class);
            log.error("Failed to send OTP for user : {}", user.getEmail(), e);
            return false;
        }
    }
}
