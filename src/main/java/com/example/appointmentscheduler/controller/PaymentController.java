package com.example.appointmentscheduler.controller;

import com.example.appointmentscheduler.entity.Invoice;
import com.example.appointmentscheduler.service.InvoiceService;
import com.example.appointmentscheduler.service.PaymentService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/momo/{invoiceId}")
    public String getMoMoPaymentQRCode(@PathVariable("invoiceId") int invoiceId, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        try {
            String qrCodePath = paymentService.generateMoMoQRCode(String.valueOf(invoiceId));
            model.addAttribute("qrCodePath", qrCodePath);
            model.addAttribute(  "invoice", invoice);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate QR code for payment.");
        }
        return "invoices/momo_payment";  // Tên file HTML để hiển thị QR code
    }

        @GetMapping("/momo/callback")
        public String handleMoMoCallback(@RequestParam("orderId") String orderId, @RequestParam("resultCode") int resultCode, Model model) {
            if (resultCode == 0) { //  0 là mã kết quả cho việc thanh toán thành công
                int invoiceId = Integer.parseInt(orderId);
                invoiceService.changeInvoiceStatusToPaid(invoiceId);
                model.addAttribute("message", "Thanh toán thành công!");
            } else {
                model.addAttribute("message", "Thanh toán thất bại!");
            }
            return "invoices/payment_result";  // Tên file HTML để hiển thị kết quả thanh toán
        }
    }


