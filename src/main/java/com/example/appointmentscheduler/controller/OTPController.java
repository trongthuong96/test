package com.example.appointmentscheduler.controller;

import com.example.appointmentscheduler.dao.RoleRepository;
import com.example.appointmentscheduler.dao.user.customer.CorporateCustomerRepository;
import com.example.appointmentscheduler.dao.user.customer.RetailCustomerRepository;
import com.example.appointmentscheduler.entity.user.customer.RetailCustomer;
import com.example.appointmentscheduler.model.UserForm;
import com.example.appointmentscheduler.service.OTPService;
import com.example.appointmentscheduler.service.UserService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@RequestMapping("/otp")
public class OTPController {
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private OTPService otpService;

    @Autowired
    private RetailCustomerRepository retailCustomerRepository;

    @Autowired
    private CorporateCustomerRepository corporateCustomerRepository;

    @Autowired
    private UserService userService;

    public OTPController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/confirm-otp")
    public String confirmOTP(@RequestParam String email, @RequestParam String otp, Model model) throws IOException, WriterException {
        if (otpService.validateOTP(email, otp)) {
            // OTP chính xác, lưu người dùng từ tạm thời vào cơ sở dữ liệu
            UserForm userForm = userService.getTemporaryUser(email);
            if (userForm != null) {
                if(userForm.getCompanyName() != null && userForm.getVatNumber() != null) {
                    userService.saveNewCorporateCustomer(userForm);
                } else {
                    userService.saveNewRetailCustomer(userForm);
                }
                model.addAttribute("createdUserName", userForm.getUserName());
                return "redirect:/login"; // Chuyển hướng đến trang đăng nhập sau khi xác nhận thành công
            } else {
                model.addAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "users/confirmOtpForm";
            }
        } else {
            model.addAttribute("error", "OTP không hợp lệ");
            return "users/confirmOtpForm";
        }
    }
}
