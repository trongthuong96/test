package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenServiceImpl implements JwtTokenService {

    private String jwtSecret;

    public JwtTokenServiceImpl(@Value(value = "${app.jwtSecret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    /**
     * Generates token for appointment rejection.
     * @param appointment appointment to be rejected
     * @return token
     */
    @Override
    public String generateAppointmentRejectionToken(Appointment appointment) {
        Date expiryDate = convertLocalDateTimeToDate(appointment.getEnd().plusDays(1));
        return Jwts.builder()
                .claim("appointmentId", appointment.getId())
                .claim("customerId", appointment.getCustomer().getId())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    /**
     * Generates token for appointment acceptance.
     * @param appointment appointment to be accepted
     * @return token
     */
    @Override
    public String generateAcceptRejectionToken(Appointment appointment) {
        return Jwts.builder()
                .claim("appointmentId", appointment.getId())
                .claim("providerId", appointment.getProvider().getId())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("Error while token {} validation, error is {}", token, e.getMessage());
        }
        return false;

    }

    @Override
    public int getAppointmentIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return (int) claims.get("appointmentId");
    }

    @Override
    public int getCustomerIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        Integer customerId = claims.get("customerId", Integer.class);
        if (customerId == null) {
            throw new IllegalArgumentException("Token does not contain customerId claim");
        }
        return customerId;
        //return (int) claims.get("customerId");
    }

    @Override
    public int getProviderIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return (int) claims.get("providerId");
    }

    @Override
    public Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.of("Europe/Warsaw");
        ZoneOffset zoneOffSet = zone.getRules().getOffset(localDateTime);
        Instant instant = localDateTime.toInstant(zoneOffSet);
        return Date.from(instant);
    }

    @Override
    public String generateCustomerToken(Customer customer) {
        Date expiryDate = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        return Jwts.builder()
                .claim("customerId", customer.getId())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

}
