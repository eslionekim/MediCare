package com.example.erp.Visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
            if ("대기 중".equals(v.getStatus_code().getName())) {
                if (first == null || v.getVisit_datetime().isBefore(first)) {
                	first = v.getVisit_datetime();
                }
            }
        }
        
        for (Visit v : todayVisits) { // 금일 진료 리스트에 담긴거 하나씩 뽑기
            TodayVisitDTO dto = new TodayVisitDTO(); // 금일 진료 컬럼 dto
            dto.setPatient_id(v.getPatient().getPatient_id()); // 환자번호
            dto.setName(v.getPatient().getName());             // 환자명
            dto.setGender(v.getPatient().getGender());         // 성별
            dto.setBirth_date(v.getPatient().getBirth_date());// 생년월일
            dto.setVisit_time(v.getVisit_datetime().toLocalTime());// 접수시간
            dto.setVisit_type(v.getVisit_type());                // 진료 구분
            dto.setNote(v.getNote());                             // 내원 사유
            dto.setStatus_name(v.getStatus_code().getName()); // 상태 코드
            
            // 최종 내원일 조회 (오늘 방문 제외)
            LocalDateTime lastVisit = visitRepository.findLastVisitBeforeToday(v.getPatient().getPatient_id(), today);
            dto.setLast_visit(lastVisit != null ? lastVisit.toLocalDate() : null); // 초진이면 null

            dto.setFirst( "대기 중".equals(v.getStatus_code().getName()) && v.getVisit_datetime().equals(first));
            			// "대기 중"만 선별 && 가장 빠른 visit_datetime인거 저장
            result.add(dto); // DTO 리스트에 추가
        }
        
        Map<String, Integer> statusPriority = Map.of( //상태 우선순위
                "대기 중", 1,
                "진료 중", 2,
                "진료 종료", 3,
                "수납/청구 완료", 4
        );
        
        result.sort( //sort : 정렬
        	    Comparator //비교자
        	        .comparingInt((TodayVisitDTO dto) -> statusPriority.getOrDefault(dto.getStatus_name(), 99)) // 번호 기준
        	        //접근 정수비교                       람다  우선순위      접근  조회            상태             없음 기본
        	        .thenComparing(TodayVisitDTO::getVisit_time) // LocalTime 기준 정렬
        	        // 후 비교        타입            방문시간
        );


        return result; // 최종 DTO 리스트 반환
	}
}
