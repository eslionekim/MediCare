package com.example.erp.batch;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Vacation.VacationRepository;
import com.example.erp.Work_schedule.Work_schedule;
import com.example.erp.Work_schedule.Work_scheduleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class WorkScheduleStatusBatch {

    private final Work_scheduleRepository workScheduleRepository;
    private final VacationRepository vacationRepository;
    private final Status_codeRepository statusCodeRepository;

    //@Scheduled(cron = "0 10 0 * * *") // 매일 00:10
    @Scheduled(cron = "*/10 * * * * *") // 10초마다
    public void updateScheduleStatus() {    	
        LocalDate targetDate = LocalDate.now().minusDays(1);

        Status_code SCH_CANCELLED = statusCodeRepository.findById("SCH_CANCELLED").orElseThrow();
        Status_code SCH_COMPLETED = statusCodeRepository.findById("SCH_COMPLETED").orElseThrow();
        Status_code SCH_LATE = statusCodeRepository.findById("SCH_LATE").orElseThrow();
        Status_code SCH_ABSENT = statusCodeRepository.findById("SCH_ABSENT").orElseThrow();

        List<Work_schedule> schedules =
                workScheduleRepository.findTargetSchedules(targetDate);

        for (Work_schedule ws : schedules) {

            // 1️⃣ 휴가
            boolean isVacation = vacationRepository.existsApprovedVacation(
                    ws.getUser_account().getUser_id(),
                    ws.getWork_date()
            );

            if (isVacation) {
                ws.setStatus_code(SCH_CANCELLED);
                continue;
            }

            LocalTime start = ws.getStart_time();
            LocalTime end = ws.getEnd_time();
            LocalTime typeStart = ws.getWork_type().getStart_time();
            LocalTime typeEnd = ws.getWork_type().getEnd_time();

            // 4️⃣ 무단결석 (null)
            if (start == null && end == null) {
                ws.setStatus_code(SCH_ABSENT);
                continue;
            }

            // 2️⃣ 근무완료
            if (!start.isAfter(typeStart) && !end.isBefore(typeEnd)) {
                ws.setStatus_code(SCH_COMPLETED);
            }
            // 3️⃣ 무단지각
            else if (start.isAfter(typeStart) && !end.isBefore(typeEnd)) {
                ws.setStatus_code(SCH_LATE);
            }
            // 4️⃣ 무단결석
            else {
                ws.setStatus_code(SCH_ABSENT);
            }
        }
    }
}

