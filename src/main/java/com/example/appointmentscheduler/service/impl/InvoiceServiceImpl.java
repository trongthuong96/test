package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.security.CustomUserDetails;
import com.example.appointmentscheduler.service.*;
import com.example.appointmentscheduler.dao.InvoiceRepository;
import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.AppointmentStatus;
import com.example.appointmentscheduler.entity.Invoice;
import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.util.PdfGeneratorUtil;
import com.google.zxing.WriterException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final NotificationService notificationService;
    private final QRCodeService qrCodeService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, PdfGeneratorUtil pdfGeneratorUtil, UserService userService, AppointmentService appointmentService, NotificationService notificationService, QRCodeService qrCodeService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfGeneratorUtil = pdfGeneratorUtil;
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Generates invoice number in the format: FV/YYYY/MM/NNN
     * where YYYY is the current year, MM is the current month and NNN is the number of invoices issued in the current month.
     * @return invoice number
     */
    //Done: change invoice number
    @Override
    public String generateInvoiceNumber() {
        List<Invoice> invoices = invoiceRepository.findAllIssuedInCurrentMonth(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
        int nextInvoiceNumber = invoices.size() + 1;
        LocalDateTime today = LocalDateTime.now();
        return "HD/" + today.getYear() + "/" + today.getMonthValue() + "/" + nextInvoiceNumber;
    }
    /**
     * Creates new invoice and saves it to the database.
     * @param invoice invoice to be saved
     */
    @Override
    public void createNewInvoice(Invoice invoice) {
        invoiceRepository.save(invoice);
    }
    /**
     * Returns invoice by appointment id.
     * @param appointmentId appointment id
     * @return invoice
     */
    @Override
    public Invoice getInvoiceByAppointmentId(int appointmentId) {
        return invoiceRepository.findByAppointmentId(appointmentId);
    }
    /**
     * Returns invoice by invoice id.
     * @param invoiceId invoice id
     * @return invoice
     */
    @Override
    public Invoice getInvoiceById(int invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(RuntimeException::new);
    }
    /**
     * Returns all invoices (just user has role admin can see all invoices).
     * @return list of invoices
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Generates PDF document for invoice.
     * @param invoiceId invoice id
     * @return PDF document
     */
    @Override
    public File generatePdfForInvoice(int invoiceId) {
        // Lấy thông tin người dùng hiện tại từ Spring Security Context
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Lấy hóa đơn từ kho lưu trữ dựa trên ID
        Invoice invoice = invoiceRepository.getOne(invoiceId);
        // Kiểm tra xem người dùng có quyền tải xuống hóa đơn hay không
        if (!isUserAllowedToDownloadInvoice(currentUser, invoice)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        // Tạo và trả về tệp PDF từ hóa đơn sử dụng utiliy pdfGeneratorUtil
        return pdfGeneratorUtil.generatePdfFromInvoice(invoice);
    }
    /**
     * Checks if user is allowed to download invoice.
     * @param user user
     * @param invoice invoice
     * @return true if user is allowed to download invoice, false otherwise
     */
    @Override
    public boolean isUserAllowedToDownloadInvoice(CustomUserDetails user, Invoice invoice) {
        int userId = user.getId();
        // Nếu người dùng có vai trò ADMIN, cho phép tải xuống mà không kiểm tra tiếp
        if (user.hasRole("ROLE_ADMIN")) {
            return true;
        }
        // Kiểm tra từng cuộc hẹn trong hóa đơn
        for (Appointment a : invoice.getAppointments()) {
            // Nếu người dùng là nhà cung cấp hoặc khách hàng trong bất kỳ cuộc hẹn nào, cho phép tải xuống
            if (a.getProvider().getId() == userId || a.getCustomer().getId() == userId) {
                return true;
            }
        }
        // Nếu không phải là ADMIN và không liên quan đến bất kỳ cuộc hẹn nào, không cho phép tải xuống
        return false;
    }
    /**
     * Changes invoice status to paid.
     * @param invoiceId invoice id
     */
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void changeInvoiceStatusToPaid(int invoiceId) {
        Invoice invoice = invoiceRepository.getOne(invoiceId);
        invoice.setStatus("paid");
        // TODO: delete QR code for invoice
//        qrCodeService.deleteQRCodeForInvoice(invoice);
        invoiceRepository.save(invoice);
    }
    /**
     * Issues invoices for confirmed appointments.
     */
    @Transactional
    @Override
    public void issueInvoicesForConfirmedAppointments() {
        //Note: this method is transactional, so if something goes wrong, no invoices will be issued
        List<Customer> customers = userService.getAllCustomers();
        for (Customer customer : customers) {
            //get all confirmed appointments for customer
            List<Appointment> appointmentsToIssueInvoice = appointmentService.getConfirmedAppointmentsByCustomerId(customer.getId());
            if (!appointmentsToIssueInvoice.isEmpty()) { //if there are any confirmed appointments
                //change status of all appointments to invoiced
                for (Appointment a : appointmentsToIssueInvoice) {
                    // TODO: maybe change this to batch update
                    // set appointment status to invoiced
                    a.setStatus(AppointmentStatus.INVOICED);
                    appointmentService.updateAppointment(a);
                }
                Invoice invoice = new Invoice(generateInvoiceNumber(), "issued", LocalDateTime.now(), appointmentsToIssueInvoice);
                invoiceRepository.save(invoice);
                // TODO: Add QR code generation
//                try {
//                    updateQRCodeForInvoice(invoice.getId()); // add and save QR code to invoice
//                } catch (WriterException | IOException e) {
//                    e.printStackTrace();
//                }
                notificationService.newInvoice(invoice, true);
            }

        }
    }
    /**
     * Generates QR code for invoice and updates the invoice with the QR code data.
     */
    @Override
    public void updateQRCodeForInvoice(int invoiceId) throws WriterException, IOException {
        Invoice invoice = getInvoiceById(invoiceId);
        // generate QR code for invoice
        String qrCodeData = qrCodeService.createInvoiceQRCode(invoice);
        // save QR code image to file and get the path
        String qrImagePath = qrCodeService.generateQRCodeImageAndSave(qrCodeData);
        // update invoice with QR code path and data
        invoice.setQrCodeData(qrCodeData);
        invoice.setQrCodePath(qrImagePath);
        invoiceRepository.save(invoice);
    }


}
