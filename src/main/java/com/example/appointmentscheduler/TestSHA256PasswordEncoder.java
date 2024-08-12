package com.example.appointmentscheduler;

import com.example.appointmentscheduler.security.PasswordEncoderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestSHA256PasswordEncoder {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PasswordEncoderConfig.class);

        // BCryptPasswordEncoder bean
        PasswordEncoder bcryptEncoder = (PasswordEncoder) context.getBean("passwordEncoder");

        // SHA256PasswordEncoder bean
        PasswordEncoder sha256Encoder = (PasswordEncoder) context.getBean("passwordEncoderSHA");

        // Test password
        String password = "qwerty123";

        // Test password BCrypt
        String bcryptEncodedPassword = bcryptEncoder.encode(password);
        System.out.println("BCrypt: " + bcryptEncodedPassword);

        // Test password SHA-256
        String sha256EncodedPassword = sha256Encoder.encode(password);
        System.out.println("SHA-256: " + sha256EncodedPassword);

        // Xac nhan mat khau goc match voi mat khau da ma hoa
        boolean bcryptMatches = bcryptEncoder.matches(password, bcryptEncodedPassword);
        boolean sha256Matches = sha256Encoder.matches(password, sha256EncodedPassword);

        System.out.println("BCrypt matches: " + bcryptMatches);
        System.out.println("SHA-256 matches: " + sha256Matches);

        // Close the context
        context.close();
    }
}
