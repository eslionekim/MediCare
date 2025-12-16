package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleCalendarDTO { //의사 -> 스케줄 조회 by 은서
	private LocalDate workDate;
    private String workTypeCode;
    private String workName;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public ScheduleCalendarDTO(String workTypeCode, String workName) { // 인사 -> 스케줄 부여 -> 근무종류 조회 by 은서
        this.workTypeCode = workTypeCode;
        this.workName = workName;
    }
}
