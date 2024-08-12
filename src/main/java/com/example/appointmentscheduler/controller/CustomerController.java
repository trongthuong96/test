package com.example.appointmentscheduler.controller;

import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.model.ChangePasswordForm;
import com.example.appointmentscheduler.model.UserForm;
import com.example.appointmentscheduler.security.CustomUserDetails;
import com.example.appointmentscheduler.service.AppointmentService;
import com.example.appointmentscheduler.service.OTPService;
import com.example.appointmentscheduler.service.UserService;
import com.example.appointmentscheduler.validation.groups.CreateCorporateCustomer;
import com.example.appointmentscheduler.validation.groups.CreateUser;
import com.example.appointmentscheduler.validation.groups.UpdateCorporateCustomer;
import com.example.appointmentscheduler.validation.groups.UpdateUser;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final OTPService otpService;

    public CustomerController(UserService userService, AppointmentService appointmentService, OTPService otpService) {
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.otpService = otpService;
    }

    @GetMapping("/all")
    public String showAllCustomers(Model model) {
        model.addAttribute("customers", userService.getAllCustomers());
        return "users/listCustomers";
    }

    @GetMapping("/{id}")
    public String showCustomerDetails(@PathVariable int id, Model model) {
        Customer customer = userService.getCustomerById(id);
        if (customer.hasRole("ROLE_CUSTOMER_CORPORATE")) {
            if (!model.containsAttribute("user")) {
                model.addAttribute("user", new UserForm(userService.getCorporateCustomerById(id)));
            }
            model.addAttribute("account_type", "customer_corporate");
            model.addAttribute("formActionProfile", "/customers/corporate/update/profile");
        } else if (customer.hasRole("ROLE_CUSTOMER_RETAIL")) {
            if (!model.containsAttribute("user")) {
                model.addAttribute("user", new UserForm(userService.getRetailCustomerById(id)));
            }
            model.addAttribute("account_type", "customer_retail");
            model.addAttribute("formActionProfile", "/customers/retail/update/profile");
        }
        if (!model.containsAttribute("passwordChange")) {
            model.addAttribute("passwordChange", new ChangePasswordForm(id));
        }
        model.addAttribute("formActionPassword", "/customers/update/password");
        model.addAttribute("numberOfScheduledAppointments", appointmentService.getNumberOfScheduledAppointmentsForUser(id));
        model.addAttribute("numberOfCanceledAppointments", appointmentService.getNumberOfCanceledAppointmentsForUser(id));
        return "users/updateUserForm";
    }

    @PostMapping("/corporate/update/profile")
    public String processCorporateCustomerProfileUpdate(@Validated({UpdateUser.class, UpdateCorporateCustomer.class}) @ModelAttribute("user") UserForm user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/customers/" + user.getId();
        }
        userService.updateCorporateCustomerProfile(user);
        return "redirect:/customers/" + user.getId();
    }

    @PostMapping("/retail/update/profile")
    public String processRetailCustomerProfileUpdate(@Validated({UpdateUser.class}) @ModelAttribute("user") UserForm user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/customers/" + user.getId();
        }
        userService.updateRetailCustomerProfile(user);
        return "redirect:/customers/" + user.getId();
    }


    @GetMapping("/new/{customer_type}")
    public String showCustomerRegistrationForm(@PathVariable("customer_type") String customerType, Model model, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser != null) {
            return "redirect:/";
        }
        if (customerType.equals("corporate")) {
            model.addAttribute("account_type", "customer_corporate");
            model.addAttribute("registerAction", "/customers/new/corporate");
        } else if (customerType.equals("retail")) {
            model.addAttribute("account_type", "customer_retail");
            model.addAttribute("registerAction", "/customers/new/retail");
        } else {
            throw new RuntimeException();
        }
        model.addAttribute("user", new UserForm());
        return "users/createUserForm";
    }


    @PostMapping("/new/retail")
    public String processRetailCustomerRegistration(@Validated({CreateUser.class}) @ModelAttribute("user") UserForm userForm, BindingResult bindingResult, Model model) throws IOException, WriterException {
        if (bindingResult.hasErrors()) {
            populateModel(model, userForm, "customer_retail", "/customers/new/retail", null);
            return "users/createUserForm";
        }
        userService.saveTemporaryUser(userForm);
        // Gui OTP va luu thong tin nguoi dung tam thoi
        otpService.generateAndSendOTP(userForm.getEmail());
        // TODO Luu thong tin nguoi dung tam thoi vao Redis hoac noi luu tru tam thoi khac
        return "users/confirmOtpForm";
    }

    @PostMapping("/new/corporate")
    public String processCorporateCustomerRegistration(@Validated({CreateUser.class, CreateCorporateCustomer.class}) @ModelAttribute("user") UserForm userForm, BindingResult bindingResult, Model model) throws IOException, WriterException {
        if (bindingResult.hasErrors()) {
            populateModel(model, userForm, "customer_corporate", "/customers/new/corporate", null);
            return "users/createUserForm";
        }
        // TODO Luu thong tin nguoi dung tam thoi
        userService.saveTemporaryUser(userForm);
        otpService.generateAndSendOTP(userForm.getEmail());
        return "users/confirmOtpForm";
    }


    @PostMapping("/update/password")
    public String processCustomerPasswordUpdate(@Valid @ModelAttribute("passwordChange") ChangePasswordForm passwordChange, BindingResult bindingResult, @AuthenticationPrincipal CustomUserDetails currentUser, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordChange", bindingResult);
            redirectAttributes.addFlashAttribute("passwordChange", passwordChange);
            return "redirect:/customers/" + currentUser.getId();
        }
        userService.updateUserPassword(passwordChange);
        return "redirect:/customers/" + currentUser.getId();
    }

    @PostMapping("/delete")
    public String processDeleteCustomerRequest(@RequestParam("customerId") int customerId) {
        userService.deleteUserById(customerId);
        return "redirect:/customers/all";
    }

    public Model populateModel(Model model, UserForm user, String account_type, String action, String error) {
        model.addAttribute("user", user);
        model.addAttribute("account_type", account_type);
        model.addAttribute("registerAction", action);
        model.addAttribute("registrationError", error);
        return model;
    }

}
