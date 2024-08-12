package com.example.appointmentscheduler.controller;

import com.example.appointmentscheduler.security.CustomUserDetails;
import com.example.appointmentscheduler.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Slf4j
@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }
    /**
     * Shows all invoices.
     * @param model
     * @return list of all invoices
     */
    @GetMapping("/all")
    public String showAllInvoices(Model model) {
        model.addAttribute("invoices", invoiceService.getAllInvoices());
        return "invoices/listInvoices";
    }
    /**
     * Changes invoice status to paid.
     * @param invoiceId id of the invoice to be changed
     * @return redirect to the list of all invoices
     */
    @PostMapping("/paid/{invoiceId}")
    public String changeStatusToPaid(@PathVariable("invoiceId") int invoiceId) {
        invoiceService.changeInvoiceStatusToPaid(invoiceId);
        return "redirect:/invoices/all";
    }

    /**
     * Issues invoices for all confirmed appointments.
     * @param model
     * @return redirect to the list of all invoices
     */
    @GetMapping("/issue")
    public String issueInvoicesManually(Model model) {
        invoiceService.issueInvoicesForConfirmedAppointments();
        return "redirect:/invoices/all";
    }

    /**
     * Checks if the user is allowed to download the invoice and if so, generates pdf for the invoice and returns it as a response.
     * @param invoiceId id of the invoice to be downloaded
     * @param currentUser currently logged in user
     * @return response with pdf file as a body
     */
    @GetMapping("/download/{invoiceId}")
    public ResponseEntity<InputStreamResource> downloadInvoice(@PathVariable("invoiceId") int invoiceId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            File invoicePdf = invoiceService.generatePdfForInvoice(invoiceId);
            HttpHeaders respHeaders = new HttpHeaders();
            MediaType mediaType = MediaType.parseMediaType("application/pdf");
            respHeaders.setContentType(mediaType);
            respHeaders.setContentLength(invoicePdf.length());
            respHeaders.setContentDispositionFormData("attachment", invoicePdf.getName());
            InputStreamResource isr = new InputStreamResource(new FileInputStream(invoicePdf));
            return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            log.error("Lỗi khi tạo pdf để tải xuống, lỗi: {} ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/payment-success")
    public ResponseEntity<String> handlePaymentSuccess(@RequestParam("invoiceId") int invoiceId) {
        invoiceService.changeInvoiceStatusToPaid(invoiceId);
        return ResponseEntity.ok("Đã thanh toán thành công. Cảm ơn bạn!");
    }
}
