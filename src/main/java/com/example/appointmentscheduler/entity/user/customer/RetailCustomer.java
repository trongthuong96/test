package com.example.appointmentscheduler.entity.user.customer;

import com.example.appointmentscheduler.entity.user.Role;
import com.example.appointmentscheduler.model.UserForm;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.Collection;

@Entity
@Table(name = "retail_customers")
@PrimaryKeyJoinColumn(name = "id_customer")
public class RetailCustomer extends Customer {

    public RetailCustomer() {
    }

    public RetailCustomer(UserForm userFormDTO, String encryptedPassword, Collection<Role> roles) {
        super(userFormDTO, encryptedPassword, roles);
    }


}
