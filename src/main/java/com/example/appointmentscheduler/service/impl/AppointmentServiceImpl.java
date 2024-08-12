package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.dao.BarcodeRepository;
import com.example.appointmentscheduler.entity.*;
import com.example.appointmentscheduler.exception.AppointmentNotFoundException;
import com.example.appointmentscheduler.service.*;
import com.example.appointmentscheduler.dao.AppointmentRepository;
import com.example.appointmentscheduler.dao.ChatMessageRepository;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.entity.user.provider.Provider;
import com.example.appointmentscheduler.model.DayPlan;
import com.example.appointmentscheduler.model.TimePeroid;
import com.example.appointmentscheduler.util.PdfGeneratorUtil;
import com.google.zxing.WriterException;
import com.google.zxing.oned.Code128Reader;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final int NUMBER_OF_ALLOWED_CANCELATIONS_PER_MONTH = 1;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final WorkService workService;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationService notificationService;
    private final JwtTokenServiceImpl jwtTokenService;
    @Autowired
    private BarcodeService barcodeService;
    @Autowired
    private PdfGeneratorUtil pdfGeneratorUtil;
    @Autowired
    private BarcodeRepository barcodeRepository;


    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, UserService userService, WorkService workService, ChatMessageRepository chatMessageRepository, NotificationService notificationService, JwtTokenServiceImpl jwtTokenService) {
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.workService = workService;
        this.chatMessageRepository = chatMessageRepository;
        this.notificationService = notificationService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * phuong thuc updateAppointment()
     * @author PT
     * @description Lưu appointment vào database
     * @update 2024/03/9
     */
    @Override
    public void updateAppointment(Appointment appointment)
    {
        appointmentRepository.save(appointment);
    }

    @Override
    @PostAuthorize("returnObject.provider.id == principal.id or returnObject.customer.id == principal.id or hasRole('ADMIN') ")
    public Appointment getAppointmentByIdWithAuthorization(int id) {
        return getAppointmentById(id);
    }

    @Override
    public Appointment getAppointmentById(int id) {
        return appointmentRepository.findById(id)
                .orElseThrow(AppointmentNotFoundException::new);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public void deleteAppointmentById(int id) {
        appointmentRepository.deleteById(id);
    }

    /**
     * phuong thuc getAppointmentByCustomerId()
     * // va getAppointmentByProviderId()
     * // duoc thuc hien boi nguoi dung co id tuong ung voi id hien tai
     */
    @Override
    @PreAuthorize("#customerId == principal.id")
    public List<Appointment> getAppointmentByCustomerId(int customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    @Override
    @PreAuthorize("#providerId == principal.id")
    public List<Appointment> getAppointmentByProviderId(int providerId) {
        return appointmentRepository.findByProviderId(providerId);
    }

    @Override
    public List<Appointment> getAppointmentsByProviderAtDay(int providerId, LocalDate day) {
        return appointmentRepository.findByProviderIdWithStartInPeroid(providerId, day.atStartOfDay(), day.atStartOfDay().plusDays(1));
    }

    @Override
    public List<Appointment> getAppointmentsByCustomerAtDay(int providerId, LocalDate day) {
        return appointmentRepository.findByCustomerIdWithStartInPeroid(providerId, day.atStartOfDay(), day.atStartOfDay().plusDays(1));
    }

    @Override
    public List<TimePeroid> getAvailableHours(int providerId, int customerId, int workId, LocalDate date) {
        Provider p = userService.getProviderById(providerId);
        WorkingPlan workingPlan = p.getWorkingPlan();
        DayPlan selectedDay = workingPlan.getDay(date.getDayOfWeek().toString().toLowerCase());

        List<Appointment> providerAppointments = getAppointmentsByProviderAtDay(providerId, date);
        List<Appointment> customerAppointments = getAppointmentsByCustomerAtDay(customerId, date);

        List<TimePeroid> availablePeroids = selectedDay.timePeroidsWithBreaksExcluded();
        availablePeroids = excludeAppointmentsFromTimePeroids(availablePeroids, providerAppointments);

        availablePeroids = excludeAppointmentsFromTimePeroids(availablePeroids, customerAppointments);
        return calculateAvailableHours(availablePeroids, workService.getWorkById(workId));
    }

    @Override
    public void createNewAppointment(int workId, int providerId, int customerId, LocalDateTime start) {
        if (isAvailable(workId, providerId, customerId, start)) {
            Appointment appointment = new Appointment();
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            appointment.setCustomer(userService.getCustomerById(customerId));
            appointment.setProvider(userService.getProviderById(providerId));
            Work work = workService.getWorkById(workId);
            appointment.setWork(work);
            appointment.setStart(start);
            appointment.setEnd(start.plusMinutes(work.getDuration()));
//            appointment.setBarcodeId(123456789L);
            // Generate 9-digit barcode ID based on the number of days
            // Generate a random 9-digit barcode ID
//            long randomBarcodeId = barcodeService.generate9DigitBarcode();
            long randomBarcodeId = System.currentTimeMillis();
            appointment.setBarcodeId(randomBarcodeId);

//            appointmentRepository.save(appointment);
            // Chuyển đổi barcode_id thành hình ảnh và lưu vào file
            try {
                barcodeService.genarateBarcodeImage(appointment.getBarcodeId());
                String barcodeImagePath = barcodeService.generateBarcodeImageAndSave(appointment.getBarcodeId());
                // Lưu đường dẫn vào cột barcode_image
                appointment.setBarcodeImage(barcodeImagePath);
            } catch (IOException | WriterException e) {
                throw new RuntimeException("Error generating and saving barcode image", e);
            }
            // Save the appointment to with the updated barcode image to the database
            appointmentRepository.save(appointment);
            notificationService.newNewAppointmentScheduledNotification(appointment, true);
        } else {
            throw new RuntimeException();
        }

    }

    @Override
    public void addMessageToAppointmentChat(int appointmentId, int authorId, ChatMessage chatMessage) {
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);
        if (appointment.getProvider().getId() == authorId || appointment.getCustomer().getId() == authorId) {
            chatMessage.setAuthor(userService.getUserById(authorId));
            chatMessage.setAppointment(appointment);
            chatMessage.setCreatedAt(LocalDateTime.now());
            chatMessageRepository.save(chatMessage);
            notificationService.newChatMessageNotification(chatMessage, true);
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Không được phép");
        }
    }

    @Override
    public List<TimePeroid> calculateAvailableHours(List<TimePeroid> availableTimePeroids, Work work) {
        ArrayList<TimePeroid> availableHours = new ArrayList();
        for (TimePeroid peroid : availableTimePeroids) {
            TimePeroid workPeroid = new TimePeroid(peroid.getStart(), peroid.getStart().plusMinutes(work.getDuration()));
            while (workPeroid.getEnd().isBefore(peroid.getEnd()) || workPeroid.getEnd().equals(peroid.getEnd())) {
                availableHours.add(new TimePeroid(workPeroid.getStart(), workPeroid.getStart().plusMinutes(work.getDuration())));
                workPeroid.setStart(workPeroid.getStart().plusMinutes(work.getDuration()));
                workPeroid.setEnd(workPeroid.getEnd().plusMinutes(work.getDuration()));
            }
        }
        return availableHours;
    }

    @Override
    public List<TimePeroid> excludeAppointmentsFromTimePeroids(List<TimePeroid> peroids, List<Appointment> appointments) {

        List<TimePeroid> toAdd = new ArrayList();
        Collections.sort(appointments);
        for (Appointment appointment : appointments) {
            for (TimePeroid peroid : peroids) {
                if ((appointment.getStart().toLocalTime().isBefore(peroid.getStart()) || appointment.getStart().toLocalTime().equals(peroid.getStart())) && appointment.getEnd().toLocalTime().isAfter(peroid.getStart()) && appointment.getEnd().toLocalTime().isBefore(peroid.getEnd())) {
                    peroid.setStart(appointment.getEnd().toLocalTime());
                }
                if (appointment.getStart().toLocalTime().isAfter(peroid.getStart()) && appointment.getStart().toLocalTime().isBefore(peroid.getEnd()) && appointment.getEnd().toLocalTime().isAfter(peroid.getEnd()) || appointment.getEnd().toLocalTime().equals(peroid.getEnd())) {
                    peroid.setEnd(appointment.getStart().toLocalTime());
                }
                if (appointment.getStart().toLocalTime().isAfter(peroid.getStart()) && appointment.getEnd().toLocalTime().isBefore(peroid.getEnd())) {
                    toAdd.add(new TimePeroid(peroid.getStart(), appointment.getStart().toLocalTime()));
                    peroid.setStart(appointment.getEnd().toLocalTime());
                }
            }
        }
        peroids.addAll(toAdd);
        Collections.sort(peroids);
        return peroids;
    }

    @Override
    public List<Appointment> getCanceledAppointmentsByCustomerIdForCurrentMonth(int customerId) {
        return appointmentRepository.findByCustomerIdCanceledAfterDate(customerId, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
    }

    @Override
    public void updateUserAppointmentsStatuses(int userId) {
        for (Appointment appointment : appointmentRepository.findScheduledByUserIdWithEndBeforeDate(LocalDateTime.now(), userId)) {
            appointment.setStatus(AppointmentStatus.FINISHED);
            updateAppointment(appointment);
        }

        for (Appointment appointment : appointmentRepository.findFinishedByUserIdWithEndBeforeDate(LocalDateTime.now().minusDays(1), userId)) {

            appointment.setStatus(AppointmentStatus.INVOICED);
            updateAppointment(appointment);
        }
    }

    @Override
    public void updateAllAppointmentsStatuses() {
        appointmentRepository.findScheduledWithEndBeforeDate(LocalDateTime.now())
                .forEach(appointment -> {
                    appointment.setStatus(AppointmentStatus.FINISHED);
                    updateAppointment(appointment);
                    if (LocalDateTime.now().minusDays(1).isBefore(appointment.getEnd())) {
                        notificationService.newAppointmentFinishedNotification(appointment, true);
                    }
                });

        appointmentRepository.findFinishedWithEndBeforeDate(LocalDateTime.now().minusDays(1))
                .forEach(appointment -> {
                    appointment.setStatus(AppointmentStatus.CONFIRMED);
                    updateAppointment(appointment);
                });
    }

    @Override
    public void cancelUserAppointmentById(int appointmentId, int userId) {
        Appointment appointment = appointmentRepository.getOne(appointmentId);
        if (appointment.getCustomer().getId() == userId || appointment.getProvider().getId() == userId) {
            appointment.setStatus(AppointmentStatus.CANCELED);
            User canceler = userService.getUserById(userId);
            appointment.setCanceler(canceler);
            appointment.setCanceledAt(LocalDateTime.now());
            appointmentRepository.save(appointment);
            if (canceler.equals(appointment.getCustomer())) {
                notificationService.newAppointmentCanceledByCustomerNotification(appointment, true);
            } else if (canceler.equals(appointment.getProvider())) {
                notificationService.newAppointmentCanceledByProviderNotification(appointment, true);
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Authorization error");
        }


    }

    /**
     * this method is used to check if customer is allowed to reject appointment
     * @param userId
     * @param appointmentId
     * @return true if customer is allowed to reject appointment, false otherwise
     */
    @Override
    public boolean isCustomerAllowedToRejectAppointment(int userId, int appointmentId) {
        User user = userService.getUserById(userId);
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);

        return appointment.getCustomer().equals(user) && appointment.getStatus().equals(AppointmentStatus.FINISHED) && !LocalDateTime.now().isAfter(appointment.getEnd().plusDays(1));
    }

    /**
     * this method is used to request appointment rejection by customer
     * @param appointmentId
     * @param customerId
     * @return true if request was successful, false otherwise
     */
    @Override
    public boolean requestAppointmentRejection(int appointmentId, int customerId) {
        // check if customer is allowed to reject appointment
        if (isCustomerAllowedToRejectAppointment(customerId, appointmentId)) {
            // get appointment by id
            Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);
            // set status of appointment to rejection requested
            appointment.setStatus(AppointmentStatus.REJECTION_REQUESTED);
            // notify provider about rejection request
            notificationService.newAppointmentRejectionRequestedNotification(appointment, true);
            // update appointment
            updateAppointment(appointment);
            return true;
        } else {
            // if customer is not allowed to reject appointment, return false
            return false;
        }

    }

    /**
     * this method use token to get appointment id and customer id
     * @param token
     * @return true if request was successful, false otherwise
     */
    @Override
    public boolean requestAppointmentRejection(String token) {
        if (jwtTokenService.validateToken(token)) {
            int appointmentId = jwtTokenService.getAppointmentIdFromToken(token);
            int customerId = jwtTokenService.getCustomerIdFromToken(token);
            return requestAppointmentRejection(appointmentId, customerId);
        }
        return false;
    }

    /**
     * this method is used to check if provider is allowed to accept appointment rejection
     * @param providerId
     * @param appointmentId
     * @return true if provider is allowed to accept appointment rejection, false otherwise
     */
    @Override
    public boolean isProviderAllowedToAcceptRejection(int providerId, int appointmentId) {
        User user = userService.getUserById(providerId);
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);

        return appointment.getProvider().equals(user) && appointment.getStatus().equals(AppointmentStatus.REJECTION_REQUESTED);
    }
    /**
     * this method is used to accept appointment rejection by provider
     * @param appointmentId
     * @param customerId
     * @return true if request was successful, false otherwise
     */
    @Override
    public boolean acceptRejection(int appointmentId, int customerId) {
        if (isProviderAllowedToAcceptRejection(customerId, appointmentId)) {
            Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);
            appointment.setStatus(AppointmentStatus.REJECTED);
            updateAppointment(appointment);
            notificationService.newAppointmentRejectionAcceptedNotification(appointment, true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * this method use token to get appointment id and provider id
     * @param token
     * @return true if request was successful, false otherwise
     */
    @Override
    public boolean acceptRejection(String token) {
        if (jwtTokenService.validateToken(token)) {
            int appointmentId = jwtTokenService.getAppointmentIdFromToken(token);
            int providerId = jwtTokenService.getProviderIdFromToken(token);
            return acceptRejection(appointmentId, providerId);
        }
        return false;
    }

    @Override
    public String getCancelNotAllowedReason(int userId, int appointmentId) {
        User user = userService.getUserById(userId);
        Appointment appointment = getAppointmentByIdWithAuthorization(appointmentId);

        if (user.hasRole("ROLE_ADMIN")) {
            return "Chỉ có bác sĩ hoặc bên đặt lịch được phép hủy lịch hẹn";
        }

        if (appointment.getProvider().equals(user)) {
            if (!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)) {
                return "Chỉ có thể hủy cuộc hẹn đang ở trạng thái SCHEDULED.";
            } else {
                return null;
            }
        }

        if (appointment.getCustomer().equals(user)) {
            if (!appointment.getStatus().equals(AppointmentStatus.SCHEDULED)) {
                return "Chỉ có thể hủy cuộc hẹn đang ở trạng thái SCHEDULED.";
            } else if (LocalDateTime.now().plusDays(1).isAfter(appointment.getStart())) {
                return "Các cuộc hẹn sẽ diễn ra trong vòng 24 giờ tới không thể bị hủy";
            } else if (!appointment.getWork().getEditable()) {
                return "Chỉ Bác sĩ mới được hủy cuộc hẹn này";
            } else if (getCanceledAppointmentsByCustomerIdForCurrentMonth(userId).size() >= NUMBER_OF_ALLOWED_CANCELATIONS_PER_MONTH) {
                return "Bạn không thể hủy lịch hẹn này vì bạn đã hủy quá số lần cho phép trong tháng này.";
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public int getNumberOfCanceledAppointmentsForUser(int userId) {
        return appointmentRepository.findCanceledByUser(userId).size();
    }

    @Override
    public int getNumberOfScheduledAppointmentsForUser(int userId) {
        return appointmentRepository.findScheduledByUserId(userId).size();
    }

    @Override
    public boolean isAvailable(int workId, int providerId, int customerId, LocalDateTime start) {
        if (!workService.isWorkForCustomer(workId, customerId)) {
            return false;
        }
        Work work = workService.getWorkById(workId);
        TimePeroid timePeroid = new TimePeroid(start.toLocalTime(), start.toLocalTime().plusMinutes(work.getDuration()));
        return getAvailableHours(providerId, customerId, workId, start.toLocalDate()).contains(timePeroid);
    }

    @Override
    public List<Appointment> getConfirmedAppointmentsByCustomerId(int customerId) {
        return appointmentRepository.findConfirmedByCustomerId(customerId);
    }

    @Override
    public File generatePdfForAppointment(int appointmentId) throws DocumentException {
        // Lấy thông tin cuộc hẹn từ kho lưu trữ dựa trên ID
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            // Xử lý khi không tìm thấy cuộc hẹn
            return null;
        }

        // Tạo và trả về tệp PDF từ cuộc hẹn sử dụng utiliy pdfGeneratorUtil
        return pdfGeneratorUtil.generatePdfFromAppointment(appointment);
    }

    @Override
    public void updateAppointmentStatusAfterBarcodeScan(String barcode) {
        // Quét mã vạch để lấy thông tin cuộc hẹn
        Long barcodeId = Long.parseLong(barcode);
        Appointment appointment = barcodeRepository.findByBarcodeId(barcodeId);

        if (appointment != null ) {
            if(appointment.getStatus() == AppointmentStatus.SCHEDULED){
                // Cập nhật trạng thái của cuộc hẹn thành CONFIRMED
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                // Lưu cập nhật vào cơ sở dữ liệu
                appointmentRepository.save(appointment);
            } else {
                System.out.println("Cuộc hẹn đã được xác nhận hoặc đã bị hủy");
            }
        } else {
            throw new RuntimeException("Không tìm thấy thông tin liên quan đến mã vạch " + barcodeId);
        }

    }
}
