package com.example.appointmentscheduler.entity;

import java.util.ResourceBundle;
public enum AppointmentStatus {
    SCHEDULED,
    FINISHED,
    CONFIRMED,
    INVOICED,
    CANCELED,
    DENIED,
    REJECTION_REQUESTED,
    REJECTED,
    EXCHANGE_REQUESTED;
    private static final ResourceBundle messages = ResourceBundle.getBundle("messages", new java.util.Locale("vi"));

    public String getDisplayName() {
        return messages.getString("appointment.status." + this.name());
    }
}

