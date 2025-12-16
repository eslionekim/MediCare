package com.example.erp.Work_schedule;

import java.time.LocalDate;
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
	public static class ScheduleItem {
	    private LocalDate workDate;
	    private String workTypeCode;
	}
	
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkTypeItem {
    	private LocalDate workDate;
        private String workTypeCode;
        private String workName;

        
        public WorkTypeItem(String workTypeCode, String workName) {
            this.workTypeCode = workTypeCode;
            this.workName = workName;
        }
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
