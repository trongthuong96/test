package com.example.appointmentscheduler.service;

import com.google.zxing.WriterException;

import java.io.IOException;

public interface PaymentService {

    String generateMoMoQRCode(String orderId) throws WriterException, IOException, IOException, WriterException;
}
