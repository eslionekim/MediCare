package com.example.erp.Work_schedule;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

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

	 // 여기서 WorkType용 DTO 추가
    @Data
    public static class WorkTypeItem {
        private String workTypeCode;
        private String workName;

        public WorkTypeItem(String workTypeCode, String workName) {
            this.workTypeCode = workTypeCode;
            this.workName = workName;
        }
    }
}
