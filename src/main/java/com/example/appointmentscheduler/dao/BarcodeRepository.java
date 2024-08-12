package com.example.appointmentscheduler.dao;

import com.example.appointmentscheduler.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BarcodeRepository extends JpaRepository<Appointment, Long> {

    @Query("select a from Appointment a where a.barcodeId = ?1")
    Appointment findByBarcodeId(@Param("barcodeId") Long barcodeId);
}