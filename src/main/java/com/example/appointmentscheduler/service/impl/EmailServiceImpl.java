package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.ChatMessage;
import com.example.appointmentscheduler.entity.Invoice;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.service.EmailService;
import com.example.appointmentscheduler.util.PdfGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final JwtTokenServiceImpl jwtTokenService;
    private final PdfGeneratorUtil pdfGenaratorUtil;
    private final String baseUrl;

    public EmailServiceImpl(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine, JwtTokenServiceImpl jwtTokenService, PdfGeneratorUtil pdfGenaratorUtil, @Value("${base.url}") String baseUrl) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.jwtTokenService = jwtTokenService;
        this.pdfGenaratorUtil = pdfGenaratorUtil;
        this.baseUrl = baseUrl;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String templateName, Context templateContext, File attachment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            String html = templateEngine.process("email/" + templateName, templateContext);

            helper.setTo(to);
            helper.setFrom("s2thuphuongs2@gmail.com");
            helper.setSubject(subject);
            helper.setText(html, true);

            if (attachment != null) {
                helper.addAttachment("invoice", attachment);
            }

            javaMailSender.send(message);

        } catch (MessagingException e) {
            log.error("Lỗi khi thêm tệp đính kèm vào email, lỗi là {}", e.getLocalizedMessage());
        }

    }

    @Async
    @Override
    public void sendAppointmentFinishedNotification(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        context.setVariable("url", baseUrl + "/appointments/reject?token=" + jwtTokenService.generateAppointmentRejectionToken(appointment));
        sendEmail(appointment.getCustomer().getEmail(), "Hoàn thành tóm tắt cuộc hẹn", "appointmentFinished", context, null);
    }

    @Async
    @Override
    public void sendAppointmentRejectionRequestedNotification(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        context.setVariable("url", baseUrl + "/appointments/acceptRejection?token=" + jwtTokenService.generateAcceptRejectionToken(appointment));
        sendEmail(appointment.getProvider().getEmail(), "Yêu cầu hủy lịch hẹn", "appointmentRejectionRequested", context, null);
    }

    @Async
    @Override
    public void sendNewAppointmentScheduledNotification(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        sendEmail(appointment.getProvider().getEmail(), "Lịch hẹn mới đã được đặt", "newAppointmentScheduled", context, null);
    }

    @Async
    @Override
    public void sendAppointmentCanceledByCustomerNotification(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        context.setVariable("canceler", "customer");
        sendEmail(appointment.getProvider().getEmail(), "Bên đặt lịch đã hủy lịch hẹn", "appointmentCanceled", context, null);
    }

    @Async
    @Override
    public void sendAppointmentCanceledByProviderNotification(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        context.setVariable("canceler", "provider");
        sendEmail(appointment.getCustomer().getEmail(), "Bác sĩ đã hủy lịch hẹn", "appointmentCanceled", context, null);
    }

    @Async
    @Override
    public void sendInvoice(Invoice invoice) {
        Context context = new Context();
        context.setVariable("customer", invoice.getAppointments().get(0).getCustomer().getFirstName() + " " + invoice.getAppointments().get(0).getCustomer().getLastName());
        try {
            File invoicePdf = pdfGenaratorUtil.generatePdfFromInvoice(invoice);
            sendEmail(invoice.getAppointments().get(0).getCustomer().getEmail(), "Hóa đơn cuộc hẹn", "appointmentInvoice", context, invoicePdf);
        } catch (Exception e) {
            log.error("Lỗi khi tạo pdf, lỗi là {}", e.getLocalizedMessage());
        }

    }

    @Async
    @Override
    public void sendAppointment(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        try {
            File appointmentPdf = pdfGenaratorUtil.generatePdfFromAppointment(appointment);

            // Thay đổi tên mẫu thành đường dẫn đến tệp mẫu thực sự
            String templateName = "pdf/appointment";

            sendEmail(appointment.getCustomer().getEmail(), "Chi tiết cuộc hẹn", templateName, context, appointmentPdf);
        } catch (Exception e) {
            log.error("Lỗi khi tạo pdf, lỗi là {}", e.getLocalizedMessage());
        }
    }



    @Async
    @Override
    public void sendAppointmentRejectionAcceptedNotification(Appointment appointment) {
        Context context = new Context();
        context.setVariable("appointment", appointment);
        sendEmail(appointment.getCustomer().getEmail(), "Yêu cầu từ chối đã được chấp nhận.", "appointmentRejectionAccepted", context, null);
    }

    @Async
    @Override
    public void sendNewChatMessageNotification(ChatMessage chatMessage) {
        Context context = new Context();
        User recipent = chatMessage.getAuthor() == chatMessage.getAppointment().getProvider() ? chatMessage.getAppointment().getCustomer() : chatMessage.getAppointment().getProvider();
        context.setVariable("recipent", recipent);
        context.setVariable("appointment", chatMessage.getAppointment());
        context.setVariable("url", baseUrl + "/appointments/" + chatMessage.getAppointment().getId());
        sendEmail(recipent.getEmail(), "Tin nhắn mới", "newChatMessage", context, null);
    }

}
