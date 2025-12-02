package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitService { //의사->금일 진료 리스트
	private final VisitRepository visitRepository;
	
	@Transactional(readOnly=true) //트랜젝션(조회용)
	public List<TodayVisitDTO> getTodayVisitList(){
		LocalDate today = LocalDate.now(); // 오늘 날짜 -> 최종내원일에 쓰임
		LocalDateTime start = today.atStartOfDay();
		LocalDateTime end = today.plusDays(1).atStartOfDay().minusSeconds(1);
		List<Visit> todayVisits = visitRepository.findByVisitDate(start, end); // 금일 진료 리스트 조회
        List<TodayVisitDTO> result = new ArrayList<>(); // 추출한거 담을 결과물
        LocalDateTime first = null; //가장 빠른 시간
        
        // 진료대기 리스트에서 가장 빠른 시간 찾기
        for (Visit v : todayVisits) {
            if ("진료대기".equals(v.getStatus_code().getName())) {
                if (first == null || v.getVisit_datetime().isBefore(first)) {
                	first = v.getVisit_datetime();
                }
            }
        }
        
        for (Visit v : todayVisits) { // 금일 진료 리스트에 담긴거 하나씩 뽑기
            TodayVisitDTO dto = new TodayVisitDTO(); // 금일 진료 컬럼 dto
            dto.setPatient_id(v.getPatient_id().getPatient_id()); // 환자번호
            dto.setName(v.getPatient_id().getName());             // 환자명
            dto.setGender(v.getPatient_id().getGender());         // 성별
            dto.setBirth_date(v.getPatient_id().getBirth_date());// 생년월일
            dto.setVisit_time(v.getVisit_datetime().toLocalTime());// 접수시간
            dto.setVisit_type(v.getVisit_type());                // 진료 구분
            dto.setNote(v.getNote());                             // 내원 사유
            dto.setStatus_name(v.getStatus_code().getName()); // 상태 코드
            
            // 최종 내원일 조회 (오늘 방문 제외)
            LocalDateTime lastVisit = visitRepository.findLastVisitBeforeToday(v.getPatient_id().getPatient_id(), today);
            dto.setLast_visit(lastVisit != null ? lastVisit.toLocalDate() : null); // 초진이면 null

            dto.setFirst(
            	    "진료대기".equals(v.getStatus_code().getName()) &&
            	    v.getVisit_datetime().equals(first)
            );

            
            result.add(dto); // DTO 리스트에 추가
        }

        return result; // 최종 DTO 리스트 반환
	}
}
