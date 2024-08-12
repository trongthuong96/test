package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.dao.InvoiceRepository;
import com.example.appointmentscheduler.dao.user.UserRepository;
import com.example.appointmentscheduler.entity.Invoice;
import com.example.appointmentscheduler.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QRCodeServiceImpl implements QRCodeService {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public byte[] generateQRCodeImage(String inputData) throws WriterException, IOException {
        int width = 300;
        int height = 300;
        BitMatrix matrix = new MultiFormatWriter().encode(inputData, BarcodeFormat.QR_CODE, width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0 : 0xFFFFFF);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
    @Override
    public String generateQRCodeImageAndSave(String inputData) throws WriterException, IOException {
        byte[] qrCodeImageBytes = generateQRCodeImage(inputData);
        String extractedInvoiceNumber = inputData.substring(inputData.indexOf("InvoiceNumber:") + 14, inputData.indexOf("Status:"));
        String sanitizedInvoiceNumber = extractedInvoiceNumber.replaceAll("[^a-zA-Z0-9]", "");
        String imagePath = saveImageToFile(sanitizedInvoiceNumber, qrCodeImageBytes);
        return imagePath;
    }

    @Override
    public String generateQRCodeFromToken(String token) throws WriterException, IOException {
        byte[] qrCodeImageBytes = generateQRCodeImage(token);
        String subToken = token.replaceAll("[^a-zA-Z0-9]", "_"); //token.substring(token.indexOf("Token:") + 6, token.indexOf("URL:"));
        // Băm token để tạo chuỗi ngắn hơn và an toàn hơn cho tên file
        String sanitizedToken = Integer.toHexString(subToken.hashCode());
        String imagePath = saveImageToFile(sanitizedToken, qrCodeImageBytes);
        return imagePath;
    }

    @Override
    public String saveImageToFile(String nameFile, byte[] imageBytes) throws IOException {
        String imagePath = "src/main/resources/static/img/qrcodes/" + nameFile + ".png";
        File imageFile = new File(imagePath);
        Files.write(Paths.get(imageFile.getAbsolutePath()), imageBytes);
        return imagePath;
    }
    @Override
    public String createInvoiceQRCode(Invoice invoice) throws WriterException, IOException {
        String invoiceData = generateInvoiceData(invoice);
        return generateQRCodeImageAndSave(invoiceData);
    }

    private String generateInvoiceData(Invoice invoice) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        String formattedDate = invoice.getIssued().format(formatter);

        List<String> appointmentDetails = invoice.getAppointments().stream()
                .map(appointment -> "AppointmentID:" + appointment.getId() + ", WorkPrice:" + appointment.getWork().getPrice())
                .collect(Collectors.toList());

        return "InvoiceID:" + invoice.getId() +
                "\nInvoiceNumber:" + invoice.getNumber() +
                "\nStatus:" + invoice.getStatus() +
                "\nTotalAmount:" + invoice.getTotalAmount() +
                "\nIssuedDate:" + formattedDate +
                "\nAppointments:" + String.join("; ", appointmentDetails) +
                "\nURL:" + "http://localhost:8080/invoice/" + invoice.getId();
    }

    @Override
    public void createInvoiceQRCodeForAllInvoices() throws WriterException, IOException {
        List<Invoice> invoices = invoiceRepository.findAll();
        for (Invoice invoice : invoices) {
            String qrCodePath = createInvoiceQRCode(invoice);
            invoice.setQrCodePath(qrCodePath);
            invoiceRepository.save(invoice);
        }
    }
    @Override
    public void updateSpecificInvoiceQRCode(int invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(RuntimeException::new);
        try {
            updateQRCodeForInvoice(invoice);  // Tạo và lưu mã QR cho hóa đơn
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
    public void updateQRCodeForInvoice(Invoice invoice) throws WriterException, IOException {
        String qrCodeData = createInvoiceQRCode(invoice);
        String qrImagePath = generateQRCodeImageAndSave(qrCodeData);
        invoice.setQrCodeData(qrCodeData);
        invoice.setQrCodePath(qrImagePath);
        invoiceRepository.save(invoice);
    }
    @Override
    public void deleteQRCodeForInvoice(Invoice invoice) {
        try {
            Files.deleteIfExists(Paths.get(invoice.getQrCodePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        invoice.setQrCodePath(null);
        invoice.setQrCodeData(null);
        invoiceRepository.save(invoice);
    }
    @Override
    public void deleteQRCode(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}