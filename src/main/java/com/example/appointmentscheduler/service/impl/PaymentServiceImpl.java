package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.dao.InvoiceRepository;
import com.example.appointmentscheduler.service.PaymentService;
import com.example.appointmentscheduler.service.QRCodeService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private QRCodeService qrCodeService;

    @Override
    public String generateMoMoQRCode(String orderId) throws IOException, WriterException {

        String momoUrl = "https://me.momo.vn/3GIQTNsxUPsnsEu5IXu8Fq";

        // Tạo QR code từ URL
        byte[] qrCodeImageBytes = qrCodeService.generateQRCodeImage(momoUrl);
        String imagePath = qrCodeService.saveImageToFile(orderId, qrCodeImageBytes);
        return imagePath;
    }

}
