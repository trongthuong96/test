package com.example.appointmentscheduler.dao;

import com.example.appointmentscheduler.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QRCodeRepository extends JpaRepository<User, Long> {

}
