package com.example.appointmentscheduler.service;

import com.example.appointmentscheduler.entity.Invoice;
import com.google.zxing.WriterException;

import java.io.IOException;

public interface QRCodeService {
    byte[] generateQRCodeImage(String invoiceData) throws WriterException, IOException;
    String generateQRCodeImageAndSave(String invoiceData) throws WriterException, IOException;

    String generateQRCodeFromToken(String token) throws WriterException, IOException;

    String saveImageToFile(String sanitizedInvoiceNumber, byte[] imageBytes) throws IOException;

    String createInvoiceQRCode(Invoice invoice) throws WriterException, IOException;
    void createInvoiceQRCodeForAllInvoices() throws WriterException, IOException;
    void updateSpecificInvoiceQRCode(int invoiceId);
    void deleteQRCodeForInvoice(Invoice invoice);

    void deleteQRCode(String s);
}
