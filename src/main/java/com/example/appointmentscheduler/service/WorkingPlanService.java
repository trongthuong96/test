package com.example.appointmentscheduler.service;

import com.example.appointmentscheduler.entity.WorkingPlan;
import com.example.appointmentscheduler.model.TimePeroid;

public interface WorkingPlanService {
    void updateWorkingPlan(WorkingPlan workingPlan);

    void addBreakToWorkingPlan(TimePeroid breakToAdd, int planId, String dayOfWeek);

    void deleteBreakFromWorkingPlan(TimePeroid breakToDelete, int planId, String dayOfWeek);

    WorkingPlan getWorkingPlanByProviderId(int providerId);
}
