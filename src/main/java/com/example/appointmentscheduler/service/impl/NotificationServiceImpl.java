package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.dao.NotificationRepository;
import com.example.appointmentscheduler.entity.*;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.service.EmailService;
import com.example.appointmentscheduler.service.NotificationService;
import com.example.appointmentscheduler.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final boolean mailingEnabled;

    public NotificationServiceImpl(@Value("${mailing.enabled}") boolean mailingEnabled, NotificationRepository notificationRepository, UserService userService, EmailService emailService) {
        this.mailingEnabled = mailingEnabled;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public void newNotification(String title, String message, String url, User user) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setUrl(url);
        notification.setCreatedAt(new Date());
        notification.setMessage(message);
        notification.setUser(user);
        notificationRepository.save(notification);
    }


    @Override
    public void markAsRead(int notificationId, int userId) {
        Notification notification = notificationRepository.getOne(notificationId);
        if (notification.getUser().getId() == userId) {
            notification.setRead(true);
            notificationRepository.save(notification);
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
    }

    @Override
    public void markAllAsRead(int userId) {
        List<Notification> notifications = notificationRepository.getAllUnreadNotifications(userId);
        for (Notification notification : notifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    public Notification getNotificationById(int notificationId) {
        return notificationRepository.getOne(notificationId);
    }

    @Override
    public List<Notification> getAll(int userId) {
        return userService.getUserById(userId).getNotifications();
    }

    @Override
    public List<Notification> getUnreadNotifications(int userId) {
        return notificationRepository.getAllUnreadNotifications(userId);
    }

    @Override
    public void newAppointmentFinishedNotification(Appointment appointment, boolean sendEmail) {
        String title = "Lịch hẹn kết thúc";
        String message = "Cuộc hẹn đã kết thúc, bạn có thể từ chối rằng nó đã diễn ra cho đến khi đủ " + appointment.getEnd().plusHours(24).toString();
        String url = "/appointments/" + appointment.getId();
        newNotification(title, message, url, appointment.getCustomer());
        if (sendEmail && mailingEnabled) {
            emailService.sendAppointmentFinishedNotification(appointment);
        }

    }

    @Override
    public void newAppointmentRejectionRequestedNotification(Appointment appointment, boolean sendEmail) {
        String title = "Lịch hẹn được xin phép hủy bỏ";
        String message = appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName() + "xin phép hủy bỏ một cuộc hẹn. Cần có sự chấp thuận của bạn";
        String url = "/appointments/" + appointment.getId();
        newNotification(title, message, url, appointment.getProvider());
        if (sendEmail && mailingEnabled) {
            emailService.sendAppointmentRejectionRequestedNotification(appointment);
        }
    }

    @Override
    public void newNewAppointmentScheduledNotification(Appointment appointment, boolean sendEmail) {
        String title = "Đã lên lịch hẹn mới";
        String message = "Cuộc hẹn mới đã được lên lịch với " + appointment.getCustomer().getFirstName() + " " + appointment.getProvider().getLastName() + " vào lúc " + appointment.getStart().toString();
        String url = "/appointments/" + appointment.getId();
        newNotification(title, message, url, appointment.getProvider());
        if (sendEmail && mailingEnabled) {
            emailService.sendNewAppointmentScheduledNotification(appointment);
            emailService.sendAppointment(appointment);
        }
    }

    @Override
    public void newAppointmentCanceledByCustomerNotification(Appointment appointment, boolean sendEmail) {
        String title = "Lịch hẹn đã bị hủy";
        String message = appointment.getCustomer().getFirstName() + " " + appointment.getCustomer().getLastName() + " hủy lịch hẹn vào lúc " + appointment.getStart().toString();
        String url = "/appointments/" + appointment.getId();
        newNotification(title, message, url, appointment.getProvider());
        if (sendEmail && mailingEnabled) {
            emailService.sendAppointmentCanceledByCustomerNotification(appointment);
        }
    }

    @Override
    public void newAppointmentCanceledByProviderNotification(Appointment appointment, boolean sendEmail) {
        String title = "Lịch hẹn đã bị hủy";
        String message = appointment.getProvider().getFirstName() + " " + appointment.getProvider().getLastName() + " hủy lịch hẹn vào lúc " + appointment.getStart().toString();
        String url = "/appointments/" + appointment.getId();
        newNotification(title, message, url, appointment.getCustomer());
        if (sendEmail && mailingEnabled) {
            emailService.sendAppointmentCanceledByProviderNotification(appointment);
        }
    }

    public void newInvoice(Invoice invoice, boolean sendEmail) {
        String title = "Hóa đơn mới";
        String message = "Hóa đơn mới đã được phát hành";
        String url = "/invoices/" + invoice.getId();
        newNotification(title, message, url, invoice.getAppointments().get(0).getCustomer());
        if (sendEmail && mailingEnabled) {
            emailService.sendInvoice(invoice);
        }
    }

    @Override
    public void newAppointmentRejectionAcceptedNotification(Appointment appointment, boolean sendEmail) {
        String title = "Yêu cầu hủy bỏ đã được chấp nhận";
        String message = "Bác sĩ chấp thuận yêu cầu hủy bỏ lịch hẹn của bạn";
        String url = "/appointments/" + appointment.getId();
        newNotification(title, message, url, appointment.getCustomer());
        if (sendEmail && mailingEnabled) {
            emailService.sendAppointmentRejectionAcceptedNotification(appointment);
        }
    }

    @Override
    public void newChatMessageNotification(ChatMessage chatMessage, boolean sendEmail) {
        String title = "Có tin nhắn mới";
        String message = "Bạn có tin nhắn mới từ " + chatMessage.getAuthor().getFirstName() + " về cuộc hẹn đã được lên lịch vào lúc " + chatMessage.getAppointment().getStart().format(DateTimeFormatter.ofPattern("HH:mm"));
        String url = "/appointments/" + chatMessage.getAppointment().getId();
        newNotification(title, message, url, chatMessage.getAuthor() == chatMessage.getAppointment().getProvider() ? chatMessage.getAppointment().getCustomer() : chatMessage.getAppointment().getProvider());
        if (sendEmail && mailingEnabled) {
            emailService.sendNewChatMessageNotification(chatMessage);
        }
    }

}
