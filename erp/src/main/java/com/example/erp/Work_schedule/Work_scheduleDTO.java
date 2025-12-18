package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class Work_scheduleDTO {

	@Data
	public static class WorkScheduleSaveRequest { 
	    private String user_id;
	    private String department_code;
	    private List<ScheduleItem> schedules;
	}

	@Data
	public static class ScheduleItem { // 인사 -> 스케줄 부여 -> 저장하고 나서 새로고침 by 은서
	    private LocalDate workDate;
	    private String workTypeCode;
	}
	
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HrDailyScheduleItem { // 인사-> 전체 스케줄 조회 by 은서
        private String departmentName;
        private String userId;
        private String userName;
        private String workName;
        private String statusName;
    }

}
