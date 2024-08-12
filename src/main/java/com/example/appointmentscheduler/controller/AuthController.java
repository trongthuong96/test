package com.example.appointmentscheduler.controller;

import com.example.appointmentscheduler.dao.user.UserRepository;
import com.example.appointmentscheduler.dao.user.customer.CustomerRepository;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.model.QRLoginRequest;
import com.example.appointmentscheduler.security.CustomUserDetails;
import com.example.appointmentscheduler.security.JwtAuthenticationResponse;
import com.example.appointmentscheduler.service.JwtTokenService;
import com.example.appointmentscheduler.service.QRCodeService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.io.IOException;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    QRCodeService qrCodeService;

    @PostMapping("/login/qr")
    public ResponseEntity<?> authenticateUserWithQRCode(@RequestBody QRLoginRequest qrLoginRequest) throws IOException, WriterException {
        String token = qrLoginRequest.getToken();
        if (!jwtTokenService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        int userId = jwtTokenService.getCustomerIdFromToken(token);
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        CustomUserDetails userDetails = CustomUserDetails.create(customer);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        qrCodeService.deleteQRCode("src/main/resources/static/" + customer.getQrCodePath()); // Delete old QR code
        String jwt = jwtTokenService.generateCustomerToken(customer); // Generating new token
        String qrCodePath = qrCodeService.generateQRCodeFromToken(jwt).replaceAll("src/main/resources/static/", "");

        // Lưu đường dẫn mã QR vào cơ sở dữ liệu
        customer.setQrCodePath(qrCodePath);
        customerRepository.save(customer);
        // Tra ve jwt cu dunng de dang nhap may la (ko return ve jwt moi)
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }
}