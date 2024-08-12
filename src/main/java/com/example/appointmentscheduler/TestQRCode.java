package com.example.appointmentscheduler;

import com.example.appointmentscheduler.entity.Appointment;
import com.example.appointmentscheduler.entity.Invoice;
import com.example.appointmentscheduler.entity.Work;
import com.example.appointmentscheduler.service.InvoiceService;
import com.example.appointmentscheduler.service.impl.InvoiceServiceImpl;
import com.example.appointmentscheduler.service.impl.QRCodeServiceImpl;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestQRCode {
    public static void main(String[] args) {
        QRCodeServiceImpl qrCodeService = new QRCodeServiceImpl();
        InvoiceServiceImpl invoiceService = new InvoiceServiceImpl(null, null, null, null, null, qrCodeService);

        // Tạo một danh sách các cuộc hẹn mẫu
        List<Appointment> appointments = new ArrayList<>();
        Work work1 = new Work();
        work1.setPrice(100.0);
        Appointment appointment1 = new Appointment();
        appointment1.setId(1);
        appointment1.setWork(work1);
        appointments.add(appointment1);

        Work work2 = new Work();
        work2.setPrice(150.0);
        Appointment appointment2 = new Appointment();
        appointment2.setId(2);
        appointment2.setWork(work2);
        appointments.add(appointment2);

        // Cuộc hẹn
        for (Appointment appointment : appointments) {
            System.out.println(appointment.getStatus() + " - " + appointment.getWork().getPrice());
        }
        List<Invoice> invoices = new ArrayList<>();

        // Tạo một hóa đơn mẫu
        Invoice invoice = new Invoice("HD/2024/06/001", "issued", LocalDateTime.now(), appointments);
        // Hóa đơn
        System.out.println(invoice.getNumber() + " - " + invoice.getStatus() + " - " + invoice.getIssued() + " - " + invoice.getTotalAmount());

        // Tạo hoá đơn để modified
        Invoice invoice2 = new Invoice("HD/2024/06/002", "issued", LocalDateTime.now(), appointments);
        invoice2.setId(2);
        try {
            // Tạo mã QR cho hóa đơn và lưu kết quả vào file
            String qrCodePath = qrCodeService.createInvoiceQRCode(invoice);
            String qrCodePath2 = qrCodeService.createInvoiceQRCode(invoice2);
            System.out.println("QR Code generated and saved at: " + qrCodePath);
            System.out.println("QR Code 2 generated and saved at: " + qrCodePath2);
            // Lưu QR Code 2 vào database
            invoice2.setQrCodePath(qrCodePath2);
            invoice2.setQrCodeData("QR Code 2 data");
            System.out.println("QR Code 2 saved to database");
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }

        try {
            // Cập nhật mã QR cho hóa đơn
            invoiceService.updateQRCodeForInvoice(1);
            System.out.println("QR Code updated for invoice: " + invoice.getId());
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}
