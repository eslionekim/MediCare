package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Work_scheduleService {
    private final Work_scheduleRepository work_scheduleRepository;

	public List<ScheduleCalendarDTO> getDoctorMonthlySchedule(String userId, int year, int month) {

	    return work_scheduleRepository.findDoctorMonthlySchedule(userId, year, month);
	}
	
	public boolean hasUnfinishedWork(String userId) { // 로그아웃->퇴근 찍었는지
        return work_scheduleRepository.findUnfinishedWork(userId).isPresent();
    }

}
