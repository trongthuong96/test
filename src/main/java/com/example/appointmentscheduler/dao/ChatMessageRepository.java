package com.example.appointmentscheduler.dao;

import com.example.appointmentscheduler.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

}
