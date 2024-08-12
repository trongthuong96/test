package com.example.appointmentscheduler.service;


import com.example.appointmentscheduler.entity.Work;
import com.example.appointmentscheduler.entity.user.Role;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.entity.user.customer.CorporateCustomer;
import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.entity.user.customer.RetailCustomer;
import com.example.appointmentscheduler.entity.user.provider.Provider;
import com.example.appointmentscheduler.model.ChangePasswordForm;
import com.example.appointmentscheduler.model.UserForm;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface UserService {
    /*
     * User
     * */
    boolean userExists(String userName);

    User getUserById(int userId);

    User getUserByUsername(String userName);

    List<User> getUsersByRoleName(String roleName);

    List<User> getAllUsers();

    void deleteUserById(int userId);

    void updateUserPassword(ChangePasswordForm passwordChangeForm);

    /*
     * Provider
     * */
    Provider getProviderById(int providerId);

    List<Provider> getProvidersWithRetailWorks();

    List<Provider> getProvidersWithCorporateWorks();

    List<Provider> getProvidersByWork(Work work);

    List<Provider> getAllProviders();

    void saveNewProvider(UserForm userForm);

    void updateProviderProfile(UserForm updateData);

    Collection<Role> getRolesForProvider();

    /*
     * Customer
     * */
    Customer getCustomerById(int customerId);

    List<Customer> getAllCustomers();

    /*
     * RetailCustomer
     * */
    RetailCustomer getRetailCustomerById(int retailCustomerId);

    void saveNewRetailCustomer(UserForm userForm) throws IOException, WriterException;

    void updateRetailCustomerProfile(UserForm updateData);

    void saveTemporaryUser(UserForm userForm);

    UserForm getTemporaryUser(String email);

    Collection<Role> getRolesForRetailCustomer();

    /*
     * CorporateCustomer
     * */
    CorporateCustomer getCorporateCustomerById(int corporateCustomerId);

    List<RetailCustomer> getAllRetailCustomers();

    void saveNewCorporateCustomer(UserForm userForm) throws IOException, WriterException;

    void updateCorporateCustomerProfile(UserForm updateData);

    Collection<Role> getRoleForCorporateCustomers();


}

