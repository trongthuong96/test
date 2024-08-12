package com.example.appointmentscheduler.service;

import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.ChatMessage;
import com.example.appointmentscheduler.entity.Invoice;
import org.thymeleaf.context.Context;

import java.io.File;

public interface EmailService {
    void sendEmail(String to, String subject, String templateName, Context templateContext, File attachment);

    void sendAppointmentFinishedNotification(Appointment appointment);

    void sendAppointmentRejectionRequestedNotification(Appointment appointment);

    void sendNewAppointmentScheduledNotification(Appointment appointment);

    void sendAppointmentCanceledByCustomerNotification(Appointment appointment);

    void sendAppointmentCanceledByProviderNotification(Appointment appointment);

    void sendInvoice(Invoice invoice);

    void sendAppointment(Appointment appointment);

    void sendAppointmentRejectionAcceptedNotification(Appointment appointment);

    void sendNewChatMessageNotification(ChatMessage appointment);

}
