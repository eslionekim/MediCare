package com.example.erp.Work_schedule;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Work_scheduleService {
    private final Work_scheduleRepository work_scheduleRepository;

	public List<ScheduleCalendarDTO> getDoctorMonthlySchedule(String userId, int year, int month) {

	    return work_scheduleRepository.findDoctorMonthlySchedule(userId, year, month);
	}

}
