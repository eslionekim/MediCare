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
		Optional<Work_schedule> lastWork = work_scheduleRepository.findMostRecentWork(userId);
	    // 마지막 출근 기록이 있고, end_time이 null이면 아직 퇴근 안 함
	    return lastWork.isPresent() && lastWork.get().getEnd_time() == null;
    }

}
