package com.example.appointmentscheduler.service;

import com.example.appointmentscheduler.security.CustomUserDetails;
import com.example.appointmentscheduler.entity.Invoice;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface InvoiceService {
    void createNewInvoice(Invoice invoice);

    Invoice getInvoiceByAppointmentId(int appointmentId);

    Invoice getInvoiceById(int invoiceId);

    List<Invoice> getAllInvoices();

    void changeInvoiceStatusToPaid(int invoiceId);

    void issueInvoicesForConfirmedAppointments();

    String generateInvoiceNumber();

    File generatePdfForInvoice(int invoiceId);

    boolean isUserAllowedToDownloadInvoice(CustomUserDetails user, Invoice invoice);
    void updateQRCodeForInvoice(int invoiceId) throws WriterException, IOException;
}

