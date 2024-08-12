package com.example.appointmentscheduler.dao;

import com.example.appointmentscheduler.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String roleName);
}
