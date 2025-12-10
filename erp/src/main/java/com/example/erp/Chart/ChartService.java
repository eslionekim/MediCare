package com.example.erp.Chart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Visit.Visit;
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Chart.Chart;
import com.example.erp.Chart.ChartRepository;
import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.Claim;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_item;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Diseases_code.Diseases_code;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Fee_item.Fee_itemRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChartService {
	private final ChartRepository chartRepository;
    private final VisitRepository visitRepository; // Visit 조회 위해 추가
    private final Diseases_codeRepository diseases_codeRepository;
    private final Chart_diseasesRepository chart_diseasesRepository;
    private final ClaimRepository claimRepository;
    private final Claim_itemRepository claim_itemRepository;
    private final Fee_itemRepository fee_itemRepository;
    private final Status_codeRepository status_codeRepository;
    
	// 진료 시작 시 기본 차트 생성 by 은서
    public Chart createBasicChart(Long visitId) { 
    	Optional<Chart> existingChart = chartRepository.findByVisitId(visitId);
        if (existingChart.isPresent()) {
            return existingChart.get(); // 이미 있으면 반환
        }
    	
    	Visit visit = visitRepository.findById(visitId) //넘어온 visitId로 visit조회
                 .orElseThrow(
                		 () -> new RuntimeException("해당 방문이 없습니다. visitId=" + visitId));
        Chart chart = new Chart();
        chart.setVisit(visit); //visit 연결
        chart.setUser_account(visit.getUser_account()); //user_id를 위해 연결
        chart.setLocked(false); //잠금 해제 상태
        return chartRepository.save(chart);
    }

    // 의사 -> 차트 저장 by 은서  
    @Transactional
    public void saveAll(Long visit_id,Long chart_id,String subjective,String objective,String assessment,String plan,String note,List<String> diseases_code,List<String> fee_item_code,List<Integer> base_price,List<Integer> quantity) {
    	//soap 저장
        Chart chart = (chart_id == null)? chartRepository.findByVisitId(visit_id).orElseThrow()
                : chartRepository.findById(chart_id).orElseThrow();

        chart.setSubjective(subjective);
        chart.setObjective(objective);
        chart.setAssessment(assessment);
        chart.setPlan(plan);
        chart.setNote(note);
        chart.setUpdated_at(LocalDateTime.now());
        chartRepository.save(chart);

        // --- 기존 상병 삭제 ---
        List<Chart_diseases> existingDiseases = chart_diseasesRepository.findByChart(chart);
        if (!existingDiseases.isEmpty()) {
            chart_diseasesRepository.deleteAll(existingDiseases);
        }
        
        //질병코드 저장
        if (diseases_code != null) { //있으면
            for (String code : diseases_code) { //리스트에서 하나씩 
                if (code == null || code.isEmpty()) continue; // 존재하거나 비어있지 않으면

                Diseases_code d = diseases_codeRepository.findById(code) //코드 찾기
                        .orElseThrow(() -> new RuntimeException("질병코드 없음"));

                Chart_diseases cd = new Chart_diseases(); //차트 진단 코드
                cd.setChart(chart); //차트 저장
                cd.setDiseases_code(d); //질병코드 저장
                chart_diseasesRepository.save(cd);
            }
        }

        // 청구 저장
        Claim claim = claimRepository.findByVisitId(visit_id).orElse(null); //visit_id로 Claim찾기

        if (claim == null) { // Claim없으면 새로 만들기
            claim = new Claim();
            claim.setVisit(chart.getVisit()); 
            claim.setCreated_at(LocalDateTime.now());
            claimRepository.save(claim); // 반드시 먼저 저장
        } else {
            // 기존 청구항목 삭제
            List<Claim_item> existingItems = claim_itemRepository.findByClaim(claim);
            if (!existingItems.isEmpty()) {
                claim_itemRepository.deleteAll(existingItems);
            }
        }

        int totalAmount = 0; 

        // 청구항목 저장
        if (fee_item_code != null) {
            for (int i = 0; i < fee_item_code.size(); i++) { // 배열에 인덱스값 넣어서 하나씩

                if (fee_item_code.get(i) == null || fee_item_code.get(i).isEmpty())
                    continue;

                Fee_item fee = fee_itemRepository.findById(fee_item_code.get(i)) 
                        .orElseThrow(() -> new RuntimeException("수가항목 없음"));

                int price = base_price.get(i); //가격
                int qty = quantity.get(i); // 개수
                int total = price * qty; //수가항목별 총액

                Claim_item item = new Claim_item();
                item.setClaim(claim); //청구항목 세팅
                item.setFee_item(fee); //수가항목 세팅
                item.setUnit_price(price); //가격 세팅
                item.setQuantity(qty); //수량 세팅
                item.setDiscount(0); //할인 기본 세팅
                item.setTotal(total); //수가항목별 총액 세팅

                claim_itemRepository.save(item);
                totalAmount += total; //청구 엔터티 총액에 수가항목별 총액 더하기
            }
        }

        claim.setTotal_amount(totalAmount); //청구 엔터티 총액에 최종 총액 세팅
        claimRepository.save(claim); //최종 저장
        
        // 방문 상태를 완료로 변경
        Status_code completed = status_codeRepository
                .findById("VIS_COMPLETED")
                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

        Visit visit = chart.getVisit(); //차트에서 Visit 엔터티 가져오기
        visit.setStatus_code(completed); //visit의 status_code엔터티를 completed인 걸로 바꾸기
        visitRepository.save(visit);
    }
}
