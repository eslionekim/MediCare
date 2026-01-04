package com.example.erp.Chart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
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
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Prescription.Prescription;
import com.example.erp.Prescription.PrescriptionRepository;
import com.example.erp.Prescription_item.Prescription_item;
import com.example.erp.Prescription_item.Prescription_itemRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final PrescriptionRepository prescriptionRepository;
	private final ChartRepository chartRepository;
    private final VisitRepository visitRepository; // Visit 조회 위해 추가
    private final Diseases_codeRepository diseases_codeRepository;
    private final Chart_diseasesRepository chart_diseasesRepository;
    private final ClaimRepository claimRepository;
    private final Claim_itemRepository claim_itemRepository;
    private final Fee_itemRepository fee_itemRepository;
    private final Status_codeRepository status_codeRepository;
    private final ItemRepository itemRepository;
    private final Prescription_itemRepository prescription_itemRepository;
    
    
    @Transactional
    public Chart createBasicChart(Long visitId) { 
        List<Chart> existingCharts = chartRepository.findByVisitIdList(visitId);

        if (!existingCharts.isEmpty()) {
            return existingCharts.get(0);
        }

        Visit visit = visitRepository.findById(visitId)
                 .orElseThrow(() -> new RuntimeException("해당 방문이 없습니다. visitId=" + visitId));
        
        Chart chart = new Chart();
        chart.setVisit(visit);
        chart.setUser_account(visit.getUser_account());
        chart.setLocked(false);

        try {
            return chartRepository.save(chart);
        } catch (DataIntegrityViolationException e) {
            // 다른 요청이 먼저 생성했으면 기존 차트 반환
            return chartRepository.findByVisitIdList(visitId).get(0);
        }
    }



 // 의사 -> 차트 저장 by 은서
    @Transactional
    public void saveAll(
            Long visit_id,
            Long chart_id,
            String subjective,
            String objective,
            String assessment,
            String plan,
            String note,

            // 질병
            List<String> diseases_code,

            // ===== 일반 수가 =====
            List<Long> normal_fee_item_code,
            List<Integer> normal_quantity,

            // ===== 약품 =====
            List<Long> drug_fee_item_code,
            List<Integer> dose,
            List<Integer> times_per_day,
            List<Integer> days
    ) {

        /* =======================
           1. 차트 저장
        ======================== */
        Chart chart = (chart_id == null)
                ? chartRepository.findByVisitId(visit_id).orElseThrow()
                : chartRepository.findById(chart_id).orElseThrow();

        chart.setSubjective(subjective);
        chart.setObjective(objective);
        chart.setAssessment(assessment);
        chart.setPlan(plan);
        chart.setNote(note);
        chart.setUpdated_at(LocalDateTime.now());
        chartRepository.save(chart);

        /* =======================
           2. 기존 질병 삭제
        ======================== */
        List<Chart_diseases> existingDiseases =
                chart_diseasesRepository.findByChart(chart);
        if (!existingDiseases.isEmpty()) {
            chart_diseasesRepository.deleteAll(existingDiseases);
        }

        /* =======================
           3. 질병 저장
        ======================== */
        if (diseases_code != null) {
            for (String code : diseases_code) {

                if (code == null || code.isEmpty()) continue;

                Diseases_code disease = diseases_codeRepository
                        .findById(code)
                        .orElseThrow(() -> new RuntimeException("질병코드 없음"));

                Chart_diseases cd = new Chart_diseases();
                cd.setChart(chart);
                cd.setDiseases_code(disease);
                chart_diseasesRepository.save(cd);
            }
        }

        Visit visit = chart.getVisit();

        /* =======================
           4. 일반 수가 Claim 저장
        ======================== */
        if (normal_fee_item_code != null && !normal_fee_item_code.isEmpty()) {

            Claim normalClaim = new Claim();
            normalClaim.setVisit(visit);
            normalClaim.setCreated_at(LocalDateTime.now());
            claimRepository.save(normalClaim);

            int totalAmount = 0;

            for (int i = 0; i < normal_fee_item_code.size(); i++) {

                if (normal_fee_item_code.get(i) == null) continue;

                Fee_item fee = fee_itemRepository
                        .findById(normal_fee_item_code.get(i))
                        .orElseThrow(() -> new RuntimeException("수가항목 없음"));

                int price = (fee.getBase_price()); // DB 기준
                int qty = normal_quantity.get(i);
                int total = price * qty;

                Claim_item item = new Claim_item();
                item.setClaim(normalClaim);
                item.setFee_item(fee);
                item.setUnit_price(price);
                item.setQuantity(qty);
                item.setDiscount(0);
                item.setTotal(total);

                claim_itemRepository.save(item);
                totalAmount += total;
            }

            normalClaim.setTotal_amount(totalAmount);
            claimRepository.save(normalClaim);
        }

        /* =======================
           5. 약품 Claim 저장
        ======================== */
        if (drug_fee_item_code != null && !drug_fee_item_code.isEmpty()) {

            Claim drugClaim = new Claim();
            drugClaim.setVisit(visit);
            drugClaim.setCreated_at(LocalDateTime.now());
            claimRepository.save(drugClaim);

            int totalAmount = 0;

            for (int i = 0; i < drug_fee_item_code.size(); i++) {

                if (drug_fee_item_code.get(i) == null) continue;

                Fee_item fee = fee_itemRepository
                        .findById(drug_fee_item_code.get(i))
                        .orElseThrow(() -> new RuntimeException("약품 수가 없음"));

                int price = fee.getBase_price();
                int qty = dose.get(i) * times_per_day.get(i) * days.get(i);
                int total = price * qty;

                Claim_item item = new Claim_item();
                item.setClaim(drugClaim);
                item.setFee_item(fee);
                item.setUnit_price(price);
                item.setQuantity(qty);
                item.setDiscount(0);
                item.setTotal(total);

                claim_itemRepository.save(item);
                totalAmount += total;
            }

            drugClaim.setTotal_amount(totalAmount);
            claimRepository.save(drugClaim);
            
            
            // 처방전
            Prescription prescription = new Prescription();
            prescription.setVisit_id(visit.getVisit_id());
            prescription.setStatus_code("VIS_COMPLETED");
            prescription.setPrescribed_at(LocalDateTime.now());
            prescriptionRepository.save(prescription);

            for (int i = 0; i < drug_fee_item_code.size(); i++) {
                if (drug_fee_item_code.get(i) == null) continue;

                Fee_item fee = fee_itemRepository.findById(drug_fee_item_code.get(i))
                        .orElseThrow(() -> new RuntimeException("약품 수가 없음"));

                //item_code 가져오기
                Item item = itemRepository.findByFeeItemCode(fee.getFee_item_code())
                        .orElseThrow(() -> new RuntimeException("아이템 없음"));
                Long item_code = item.getItem_code(); // 실제 Prescription_item에 넣을 값

                BigDecimal doseValue = BigDecimal.valueOf(dose.get(i));
                int freq = times_per_day.get(i);
                int day = days.get(i);
                BigDecimal totalQty = doseValue.multiply(BigDecimal.valueOf(freq * day));

                Prescription_item pItem = new Prescription_item();
                pItem.setPrescription_id(prescription.getPrescription_id());
                pItem.setItem_code(item_code);
                pItem.setDose(doseValue);
                pItem.setFrequency(freq);
                pItem.setDays(day);
                pItem.setTotal_quantity(totalQty);

                prescription_itemRepository.save(pItem);
            }
        

        }

        /* =======================
           6. 방문 상태 완료 처리
        ======================== */
        Status_code completed = status_codeRepository
                .findById("VIS_COMPLETED")
                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

        visit.setStatus_code(completed);
        visitRepository.save(visit);
    }

}
