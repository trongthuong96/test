package com.example.appointmentscheduler.service;

import com.google.zxing.WriterException;

import java.io.IOException;

public interface BarcodeService {
    byte[] genarateBarcodeImage(Long barcodeContent) throws WriterException, IOException;

    String generateBarcodeImageAndSave(Long barcodeContent) throws WriterException, IOException;

    String scanBarcode(String barcodeId);

    long generate9DigitBarcode();
}
