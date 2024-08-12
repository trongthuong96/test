package com.example.appointmentscheduler.service;

public interface ScheduledTasksService {
    void updateAllAppointmentsStatuses();

    void issueInvoicesForCurrentMonth();
}
