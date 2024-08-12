package com.example.appointmentscheduler.service.impl;

import com.example.appointmentscheduler.dao.RoleRepository;
import com.example.appointmentscheduler.dao.user.UserRepository;
import com.example.appointmentscheduler.dao.user.customer.CorporateCustomerRepository;
import com.example.appointmentscheduler.dao.user.customer.CustomerRepository;
import com.example.appointmentscheduler.dao.user.customer.RetailCustomerRepository;
import com.example.appointmentscheduler.dao.user.provider.ProviderRepository;
import com.example.appointmentscheduler.entity.Work;
import com.example.appointmentscheduler.entity.WorkingPlan;
import com.example.appointmentscheduler.entity.user.Role;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.entity.user.customer.CorporateCustomer;
import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.entity.user.customer.RetailCustomer;
import com.example.appointmentscheduler.entity.user.provider.Provider;
import com.example.appointmentscheduler.model.ChangePasswordForm;
import com.example.appointmentscheduler.model.UserForm;
import com.example.appointmentscheduler.service.JwtTokenService;
import com.example.appointmentscheduler.service.OTPService;
import com.example.appointmentscheduler.service.QRCodeService;
import com.example.appointmentscheduler.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final ProviderRepository providerRepository;
    private final CustomerRepository customerRepository;
    private final CorporateCustomerRepository corporateCustomerRepository;
    private final RetailCustomerRepository retailCustomerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final QRCodeService qrCodeService;

    private final JwtTokenService jwtTokenService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OTPService otpService;
    @Autowired
    private RedisTemplate<String, UserForm> redisTemplate;

    public UserServiceImpl(ProviderRepository providerRepository, CustomerRepository customerRepository, CorporateCustomerRepository corporateCustomerRepository, RetailCustomerRepository retailCustomerRepository, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, QRCodeService qrCodeService, JwtTokenService jwtTokenService) {
        this.providerRepository = providerRepository;
        this.customerRepository = customerRepository;
        this.corporateCustomerRepository = corporateCustomerRepository;
        this.retailCustomerRepository = retailCustomerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.qrCodeService = qrCodeService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public boolean userExists(String userName) {
        return userRepository.findByUserName(userName).isPresent();
    }

    @Override
    @PreAuthorize("#userId == principal.id")
    public User getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    // phuong thuc getCustomerById duoc thuc hien neu id cua customer duoc truyen vao = id hien tai va user co role la admin
    @Override
    @PreAuthorize("#customerId == principal.id or hasRole('ADMIN')")
    public Customer getCustomerById(int customerId) {
        return customerRepository.getOne(customerId);
    }

    @Override
    public Provider getProviderById(int providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    @Override
    @PreAuthorize("#retailCustomerId == principal.id or hasRole('ADMIN')")
    public RetailCustomer getRetailCustomerById(int retailCustomerId) {
        return retailCustomerRepository.findById(retailCustomerId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

    }

    @Override
    @PreAuthorize("#corporateCustomerId == principal.id or hasRole('ADMIN')")
    public CorporateCustomer getCorporateCustomerById(int corporateCustomerId) {
        return corporateCustomerRepository.findById(corporateCustomerId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Provider> getAllProviders() {
        return providerRepository.findAll();
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public List<RetailCustomer> getAllRetailCustomers() {
        return retailCustomerRepository.findAll();
    }


    @Override
    public User getUserByUsername(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    @Override
    public List<User> getUsersByRoleName(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(int userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<Provider> getProvidersWithRetailWorks() {
        return providerRepository.findAllWithRetailWorks();
    }

    @Override
    public List<Provider> getProvidersWithCorporateWorks() {
        return providerRepository.findAllWithCorporateWorks();
    }

    @Override
    public List<Provider> getProvidersByWork(Work work) {
        return providerRepository.findByWorks(work);
    }

    @Override
    @PreAuthorize("#passwordChangeForm.id == principal.id")
    public void updateUserPassword(ChangePasswordForm passwordChangeForm) {
        User user = userRepository.getOne(passwordChangeForm.getId());
        user.setPassword(passwordEncoder.encode(passwordChangeForm.getPassword()));
        userRepository.save(user);
    }

    @Override
    @PreAuthorize("#updateData.id == principal.id or hasRole('ADMIN')")
    public void updateProviderProfile(UserForm updateData) {
        Provider provider = providerRepository.getOne(updateData.getId());
        provider.update(updateData);
        providerRepository.save(provider);
    }

    @Override
    @PreAuthorize("#updateData.id == principal.id or hasRole('ADMIN')")
    public void updateRetailCustomerProfile(UserForm updateData) {
        RetailCustomer retailCustomer = retailCustomerRepository.getOne(updateData.getId());
        retailCustomer.update(updateData);
        retailCustomerRepository.save(retailCustomer);

    }

    @Override
    @PreAuthorize("#updateData.id == principal.id or hasRole('ADMIN')")
    public void updateCorporateCustomerProfile(UserForm updateData) {
        CorporateCustomer corporateCustomer = corporateCustomerRepository.getOne(updateData.getId());
        corporateCustomer.update(updateData);
        corporateCustomerRepository.save(corporateCustomer);

    }

    @Override
    public void saveNewRetailCustomer(UserForm userForm) throws IOException, WriterException {
        RetailCustomer retailCustomer = new RetailCustomer(userForm, passwordEncoder.encode(userForm.getPassword()), getRolesForRetailCustomer());
        retailCustomerRepository.save(retailCustomer); // Luu nguoi dung truoc moi co id tao token
        // Tao ma token cho nguoi dung moi
        String token = jwtTokenService.generateCustomerToken(retailCustomer);
        String qrCodePath = qrCodeService.generateQRCodeFromToken(token).replaceAll("src/main/resources/static/", "");
        // Lưu đường dẫn mã QR vào cơ sở dữ liệu
        retailCustomer.setQrCodePath(qrCodePath);
        retailCustomerRepository.save(retailCustomer);
//        generateAndSaveQRCode(retailCustomer);
    }
//    private void generateAndSaveQRCode(User user) throws IOException, WriterException {
//        String token = jwtTokenService.generateCustomerToken(user);
//        String qrCodePath = qrCodeService.generateQRCodeFromToken(token);
//        user.setQrCodePath(qrCodePath);
//        userRepository.save(user);
//    }

    @Override
    public void saveNewCorporateCustomer(UserForm userForm) throws IOException, WriterException {
        CorporateCustomer corporateCustomer = new CorporateCustomer(userForm, passwordEncoder.encode(userForm.getPassword()), getRoleForCorporateCustomers());
//        // Tao ma token cho nguoi dung moi
//        String token = jwtTokenService.generateUserToken(corporateCustomer);
//        String qrCodePath = qrCodeService.generateQRCodeFromToken(token);
//        // Lưu đường dẫn mã QR vào cơ sở dữ liệu
//        corporateCustomer.setQrCodePath(qrCodePath);
        corporateCustomerRepository.save(corporateCustomer);
    }

    @Override
    public void saveNewProvider(UserForm userForm) {
        WorkingPlan workingPlan = WorkingPlan.generateDefaultWorkingPlan();
        Provider provider = new Provider(userForm, passwordEncoder.encode(userForm.getPassword()), getRolesForProvider(), workingPlan);
        providerRepository.save(provider);
    }
    // Tao methods luu tam thoi de luu thong tin nguoi dung vao Redis truoc khi xac nhan OTP
    @Override
    public void saveTemporaryUser(UserForm userForm) {
        try {
            String userFormJson = objectMapper.writeValueAsString(userForm);
            logger.info("Lưu UserForm vào Redis: {}", userFormJson);
            redisTemplate.opsForValue().set(userForm.getEmail(), userForm, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.error("Lỗi khi tuần tự hóa UserForm", e);
        }
    }

    @Override
    public UserForm getTemporaryUser(String email) {
        UserForm userForm = redisTemplate.opsForValue().get(email);
        try {
            String userFormJson = objectMapper.writeValueAsString(userForm);
            logger.info("Truy xuất biểu mẫu người dùng từ Redis: {}", userFormJson);
        } catch (Exception e) {
            logger.error("Lỗi khi tuần tự hóa UserForm", e);
        }
        return userForm;
    }
    // Tao methods lay cac role cho nguoi dung
    @Override
    public Collection<Role> getRolesForRetailCustomer() {
        HashSet<Role> roles = new HashSet();
        roles.add(roleRepository.findByName("ROLE_CUSTOMER_RETAIL"));
        roles.add(roleRepository.findByName("ROLE_CUSTOMER"));
        return roles;
    }

    @Override
    public Collection<Role> getRoleForCorporateCustomers() {
        HashSet<Role> roles = new HashSet();
        roles.add(roleRepository.findByName("ROLE_CUSTOMER_CORPORATE"));
        roles.add(roleRepository.findByName("ROLE_CUSTOMER"));
        return roles;
    }

    @Override
    public Collection<Role> getRolesForProvider() {
        HashSet<Role> roles = new HashSet();
        roles.add(roleRepository.findByName("ROLE_PROVIDER"));
        return roles;
    }
}

