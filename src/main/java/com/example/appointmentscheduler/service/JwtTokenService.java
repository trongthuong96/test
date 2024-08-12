package com.example.appointmentscheduler.service;

import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.entity.user.customer.RetailCustomer;

import java.time.LocalDateTime;
import java.util.Date;

public interface JwtTokenService {
    String generateAppointmentRejectionToken(Appointment appointment);

    String generateAcceptRejectionToken(Appointment appointment);

    boolean validateToken(String token);

    int getAppointmentIdFromToken(String token);

    int getCustomerIdFromToken(String token);

    int getProviderIdFromToken(String token);

    Date convertLocalDateTimeToDate(LocalDateTime localDateTime);

    String generateCustomerToken(Customer customer);
}
