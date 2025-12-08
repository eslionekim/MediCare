package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodayVisitDTO { // 의사 -> 금일 진료 리스트
	private Long visit_id;
	private LocalTime visit_time; // 접수시간-> visit_datetime에서 시간만 추출
	private Long patient_id; // 환자id
	private String name; // 환자명
	private String gender; // 성별
    private LocalDate birth_date; //생년월일
    private String visit_type; // 진료 구분 (초진/재진)
    private LocalDate last_visit;   // 최종 내원일(초진이면 null)
    private String note; //내원 사유 
    private String status_name; //상태
    private boolean first; // 접수시작
    private Long chart_id; // 새로 저장해서 불러올 chart_id
}
