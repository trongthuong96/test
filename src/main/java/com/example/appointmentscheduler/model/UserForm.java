package com.example.appointmentscheduler.model;

import com.example.appointmentscheduler.entity.user.Role;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.entity.user.customer.CorporateCustomer;
import com.example.appointmentscheduler.entity.user.customer.RetailCustomer;
import com.example.appointmentscheduler.entity.user.provider.Provider;
import com.example.appointmentscheduler.validation.UniqueUsername;
import com.example.appointmentscheduler.entity.Work;
import com.example.appointmentscheduler.validation.FieldsMatches;
import com.example.appointmentscheduler.validation.groups.*;

import javax.validation.constraints.*;
import java.util.List;

@FieldsMatches(field = "password", matchingField = "matchingPassword", groups = {CreateUser.class})
public class UserForm {

    @NotNull(groups = {UpdateUser.class})
    @Min(value = 1, groups = {UpdateUser.class})
    private int id;

    @UniqueUsername(groups = {CreateUser.class})
    @Size(min = 5, max = 15, groups = {CreateUser.class}, message = "Tên đăng nhập phải có 5-15 chữ cái")
    @NotBlank(groups = {CreateUser.class})
    private String userName;

    @Size(min = 5, max = 15, groups = {CreateUser.class}, message = "Mật khẩu phải có 5-15 chữ cái")
    @NotBlank(groups = {CreateUser.class})
    private String password;

    @Size(min = 5, max = 15, groups = {CreateUser.class}, message = "Mật khẩu phải có 5-15 chữ cái")
    @NotBlank(groups = {CreateUser.class})
    private String matchingPassword;

    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Tên không thể để trống")
    private String firstName;

    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Họ không được để trống")
    private String lastName;

    @Email(groups = {CreateUser.class, UpdateUser.class}, message = "Email không tồn tại!")
    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Email không thể để trống!")
    private String email;

//    @Pattern(groups = {CreateUser.class, UpdateUser.class}, regexp = "[0-9]{9}", message = "Vui lòng nhập số điện thoại di động hợp lệ")
//    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Số điện thoại không được để trống")
    private String mobile;

    @Size(groups = {CreateUser.class, UpdateUser.class}, min = 5, max = 30, message = "Sai số nhà & đường!")
    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Số nhà & đường không được để trống")
    private String street;

//    @Pattern(groups = {CreateUser.class, UpdateUser.class}, regexp = "[0-9]{2}-[0-9]{3}", message = "Please enter valid postcode")
//    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Post code cannot be empty")

    @NotBlank(groups = {CreateUser.class, UpdateUser.class}, message = "Thành phố không thể để trống")
    private String city;

    /*
     * CorporateCustomer only:
     * */
    @NotBlank(groups = {CreateCorporateCustomer.class, UpdateCorporateCustomer.class}, message = "Tổ chức/Bệnh viện không thể để trống")
    private String companyName;

//    @Pattern(groups = {CreateCorporateCustomer.class, UpdateCorporateCustomer.class}, regexp = "[0-9]{10}", message = "Vui lòng điền số VAT hợp lệ")
//    @NotBlank(groups = {CreateCorporateCustomer.class, UpdateCorporateCustomer.class}, message = "Mẫ số VAT không được để trống")
    private String vatNumber;

    private String qrCodePath;

    private String otp;



    /*
     * Provider only:
     * */
    @NotNull(groups = {CreateProvider.class, UpdateProvider.class})
    private List<Work> works;


    public UserForm() {
    }

    public UserForm(User user) {
        this.setId(user.getId());
        this.setUserName(user.getUserName());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setEmail(user.getEmail());
        this.setCity(user.getCity());
        this.setStreet(user.getStreet());
        this.setMobile(user.getMobile());
    }

    public UserForm(Provider provider) {
        this((User) provider);
        this.setWorks(provider.getWorks());
    }

    public UserForm(RetailCustomer retailCustomer) {
        this((User) retailCustomer);
        this.setQrCodePath(retailCustomer.getQrCodePath());
    }

    public UserForm(CorporateCustomer corporateCustomer) {
        this((User) corporateCustomer);
        this.setCompanyName(corporateCustomer.getCompanyName());
        this.setVatNumber(corporateCustomer.getVatNumber());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatchingPassword() {
        return matchingPassword;
    }

    public void setMatchingPassword(String matchingPassword) {
        this.matchingPassword = matchingPassword;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Work> getWorks() {
        return works;
    }

    public void setWorks(List<Work> works) {
        this.works = works;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}