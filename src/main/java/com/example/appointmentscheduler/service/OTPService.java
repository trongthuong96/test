package com.example.appointmentscheduler.service;

public interface OTPService {
    String generateAndSendOTP(String email);

    void sendOTPEmail(String email, String otp);

    boolean validateOTP(String email, String otp);
}
