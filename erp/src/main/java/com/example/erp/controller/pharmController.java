package com.example.erp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.Chart;
import com.example.erp.Chart.ChartRepository;
import com.example.erp.Chart_diseases.Chart_diseases;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Diseases_code.Diseases_code;
import com.example.erp.Dispense.Dispense;
import com.example.erp.Dispense.DispenseCompleteRequest;
import com.example.erp.Dispense.DispenseLotDTO;
import com.example.erp.Dispense.DispensePopupDTO;
import com.example.erp.Dispense.DispenseRepository;
import com.example.erp.Dispense.DispenseService;
import com.example.erp.Dispense_item.Dispense_itemPopupDTO;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Fee_item.Fee_itemRepository;
import com.example.erp.Insurance_code.Insurance_code;
import com.example.erp.Insurance_code.Insurance_codeRepository;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Medication_guide.MedicationGuidePopupDTO;
import com.example.erp.Medication_guide.MedicationItemDTO;
import com.example.erp.Medication_guide.Medication_guide;
import com.example.erp.Medication_guide.Medication_guideRepository;
import com.example.erp.Patient.Patient;
import com.example.erp.Patient.PatientRepository;
import com.example.erp.Prescription.Prescription;
import com.example.erp.Prescription.PrescriptionDTO;
import com.example.erp.Prescription.PrescriptionRepository;
import com.example.erp.Prescription_item.Prescription_item;
import com.example.erp.Prescription_item.Prescription_itemRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.Visit.Visit;
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Warehouse.Warehouse;
import com.example.erp.Warehouse.WarehouseRepository;

import jakarta.transaction.Transactional;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class pharmController {

    private final Medication_guideRepository medication_guideRepository;
	private final PrescriptionRepository prescriptionRepository;
	private final Prescription_itemRepository prescription_itemRepository;
	private final VisitRepository visitRepository;
	private final ItemRepository itemRepository;
	private final Fee_itemRepository fee_itemRepository;
	private final Insurance_codeRepository insurance_codeRepository;
	private final DispenseRepository dispenseRepository;
	private final Status_codeRepository status_codeRepository;
	private final User_accountRepository user_accountRepository;
	private final ChartRepository chartRepository;
	private final Chart_diseasesRepository chart_diseasesRepository;
	private final StockRepository stockRepository;
	private final WarehouseRepository warehouseRepository;
	private final DispenseService dispenseService;
	
	//약사->조제 리스트 by 은서
	@GetMapping("/pharm/todayPrescription")
	public String todayPrescription(Model model) {
		List<PrescriptionDTO> dtoList = prescriptionRepository.findAll().stream().map(p -> {
	        PrescriptionDTO dto = new PrescriptionDTO();
	        dto.setPrescriptionId(p.getPrescription_id());
	        
	        Visit visit = visitRepository.findById(p.getVisit_id()).orElse(null);
	        Patient patient = visit.getPatient();
	        dto.setPatientName(patient != null ? patient.getName() : "-");       
	        dto.setPrescribedAt(p.getPrescribed_at());
	        
	        List<Prescription_item> items = prescription_itemRepository.findByPrescriptionId(p.getPrescription_id());
	        if (!items.isEmpty()) {
	            Item firstItem = itemRepository.findById(items.get(0).getItem_code()).orElse(null);
	            int restCount = items.size() - 1;
	            dto.setPrescriptionSummary(firstItem != null 
	                ? firstItem.getName() + (restCount > 0 ? " 외 " + restCount + "개" : "")
	                : (restCount > 0 ? "외 " + restCount + "개" : "-"));
	        }

	        //처방id로 조제 얻기-> 조제 엔터티 있는지 확인
	        Optional<Dispense> dispenseOpt = dispenseRepository.findByPrescriptionId(p.getPrescription_id());

	        if (dispenseOpt.isEmpty()) {
	            dto.setDispenseStatus("조제 대기");
	            dto.setDispenser("-");
	            dto.setStatusCode("NONE");
	        } else {
	            Dispense ds = dispenseOpt.get();
	            // 상태 코드 허용 체크
	            if (List.of("DIS_STOP","DIS_READY","DIS_ING").contains(ds.getStatus_code())) {
	                Optional<Status_code> status = status_codeRepository.findByCode(ds.getStatus_code());
	                dto.setDispenseStatus(status.map(Status_code::getName).orElse("-"));

	                User_account user = user_accountRepository.findById(ds.getUser_id()).orElse(null);
	                dto.setDispenser(user != null ? user.getName() : "-");
	                dto.setStatusCode(ds.getStatus_code());
	            } else {
	                // 허용되지 않은 상태 코드면 아예 dto 반환하지 않음
	                return null; 
	            }
	        }
	        return dto;
	    })
		.filter(Objects::nonNull) // <- null 제거
        .toList();

	    model.addAttribute("prescriptions", dtoList);
	    return "pharm/todayPrescription";
	}
	
	@GetMapping("/pharm/todayPrescription/popup/{prescriptionId}") //약제->조제리스트->조제팝업
	public String getDispensePopup(@PathVariable("prescriptionId") Long prescriptionId,Model model) { //처방전으로
		//처방전id를 통해 처방전 찾기
	    Prescription prescription = prescriptionRepository.findById(prescriptionId).orElseThrow();

	    //처방전->방문id->visit 찾기
	    Visit visit = visitRepository.findById(prescription.getVisit_id()).orElseThrow();
	    //visit->patient 찾기
	    Patient patient = visit.getPatient();
	    //팝업 상단 정보
	    DispensePopupDTO dto = new DispensePopupDTO();
	    //처방id 세팅
	    dto.setPrescriptionId(prescriptionId);
	    //환자id세팅
	    dto.setPatientId(patient.getPatient_id());
	    //환자명 세팅
	    dto.setPatientName(patient.getName());
	    //생년월일 세팅
	    dto.setBirth(patient.getBirth_date());
	    //의사id세팅
	    dto.setUserId(visit.getUser_account().getUser_id());
	    //이름 세팅
	    dto.setName(visit.getUser_account().getName());
	    //진료과 세팅
	    dto.setDepartmentName(visit.getDepartment().getName());
	    //차트 찾기
	    Chart chart = chartRepository.findByVisitId(visit.getVisit_id()).orElse(null);
	    //진단명 세팅
	    String diagnosisName = "-";
	    if (chart != null) {
	        List<Chart_diseases> diseasesList =
	            chart_diseasesRepository.findByChartOrderByPrimary(chart);

	        if (!diseasesList.isEmpty()) {
	            Diseases_code dc = diseasesList.get(0).getDiseases_code();
	            diagnosisName = dc.getName_kor();
	        }
	    }

	    dto.setDiagnosis(diagnosisName);

	    // 최근 방문일
	    Optional<LocalDateTime> recent =
	        prescriptionRepository.findLatestPrescribedAtByPatient(patient.getPatient_id());
	    dto.setRecentVisitAt(recent.orElse(null));

	    // 처방 항목
	    List<Prescription_item> items =
	        prescription_itemRepository.findByPrescriptionId(prescriptionId);

	    List<Dispense_itemPopupDTO> itemDTOs = items.stream().map(pi -> {
	    	Dispense_itemPopupDTO i = new Dispense_itemPopupDTO();

	        Item item = itemRepository.findById(pi.getItem_code()).orElse(null);
	        i.setItemCode(pi.getItem_code());
	        i.setItemName(item != null ? item.getName() : "-");

	        // 로트 리스트 세팅
	        List<DispenseLotDTO> lotDTOs =
	        	    stockRepository.findPharmLots(pi.getItem_code());
        	i.setLots(lotDTOs);


	        //처방량 세팅
	        i.setDosage(
	            "1회"+pi.getDose().intValue() + " / 1일" +
	            pi.getFrequency() + "회 / " +
	            pi.getDays() + "일"
	        );

	        //재고 세팅
	        BigDecimal totalStock =
	            stockRepository.sumAvailableStock(pi.getItem_code());
	        i.setStockQty(totalStock);

	        //상태 세팅
	        i.setStatus("미완료");
	        
	        // 총 처방갯수 세팅
	        BigDecimal requiredQty =
	                pi.getDose()
	                  .multiply(BigDecimal.valueOf(pi.getFrequency()))
	                  .multiply(BigDecimal.valueOf(pi.getDays()));

	        i.setRequiredQty(requiredQty);


	        return i;
	    }).toList();

	    dto.setItems(itemDTOs);
	    
	    model.addAttribute("popup", dto);
	    return "pharm/dispensePopup";
	}
	
	// 약사->조제리스트->조제대기->조제 만들기 by 은서
	@PostMapping("/pharm/todayPrescription/start/{prescriptionId}")
	@ResponseBody
	@Transactional
	public void startDispense(@PathVariable("prescriptionId") Long prescriptionId) {

	    // 이미 조제중이면 insert 안 함
	    if (dispenseRepository.existsByPrescriptionId(prescriptionId)) {
	        return;
	    }

	    String userId = SecurityContextHolder
	            .getContext()
	            .getAuthentication()
	            .getName();

	    Dispense dispense = new Dispense();
	    dispense.setPrescription_id(prescriptionId);
	    dispense.setUser_id(userId);
	    dispense.setStatus_code("DIS_ING");
	    dispense.setDispensed_at(LocalDateTime.now());

	    dispenseRepository.save(dispense);
	}
	
	// 약사-> 조제리스트->조제팝업->닫기 버튼 by은서
	@DeleteMapping("/dispense/{prescriptionId}")
	public ResponseEntity<Void> deleteDispense(@PathVariable("prescriptionId") Long prescriptionId) {

	    dispenseRepository.deleteByPrescriptionId(prescriptionId);

	    return ResponseEntity.ok().build();
	}
	
	//약사->조제완료 by 은서
	@PostMapping("/pharm/dispense/complete")
	@ResponseBody
	public ResponseEntity<?> completeDispense(@RequestBody DispenseCompleteRequest request) {
	    try {
	        dispenseService.completeDispense(request);
	        return ResponseEntity.ok("조제 완료 처리 완료");
	    } catch (IllegalArgumentException e) {
	        // 예외 메시지 그대로 내려주기
	        return ResponseEntity
	                .badRequest() // 400
	                .body("조제 실패: " + e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("조제 실패(서버 오류): " + e.getMessage());
	    }
	}
	
	// 약사->투약 팝업
	@GetMapping("/pharm/todayPrescription/guide/{prescriptionId}")
	public String getMedicationGuidePopup(@PathVariable("prescriptionId") Long prescriptionId,Model model) {

	    MedicationGuidePopupDTO dto = new MedicationGuidePopupDTO(); //복약지도

	    // 1) prescription
	    Prescription prescription = prescriptionRepository.findById(prescriptionId).orElseThrow();

	    // 2) visit / patient
	    Visit visit = visitRepository.findById(prescription.getVisit_id()).orElseThrow();
	    Patient patient = visit.getPatient();

	    dto.setPrescriptionId(prescriptionId);
	    dto.setPatientName(patient.getName());
	    dto.setBirth(patient.getBirth_date());
	    dto.setGender(patient.getGender());

	    // 3) dispense 정보
	    Dispense dispense = dispenseRepository.findByPrescriptionId(prescriptionId).orElseThrow();
	    dto.setDispensedAt(dispense.getDispensed_at());

	    User_account pharm = user_accountRepository.findById(dispense.getUser_id()).orElse(null);
	    dto.setPharmacistName(pharm != null ? pharm.getName() : "-");

	    // 4) item 리스트
	    List<Prescription_item> items = prescription_itemRepository.findByPrescriptionId(prescriptionId);

	    List<MedicationItemDTO> itemDTOs = new ArrayList<>();

	    BigDecimal total = BigDecimal.ZERO;
	    BigDecimal taxTotal = BigDecimal.ZERO;

	    for (Prescription_item pi : items) {

	        MedicationItemDTO mi = new MedicationItemDTO();

	        Item item = itemRepository.findById(pi.getItem_code()).orElse(null);
	        Fee_item fee = fee_itemRepository.findById(item.getFee_item_code()).orElse(null);
	        Medication_guide mg = medication_guideRepository.findByItemCode(item.getItem_code()).orElse(null);

	        mi.setName(item.getName());
	        mi.setDose(pi.getDose());
	        mi.setFrequency(pi.getFrequency());
	        mi.setDays(pi.getDays());
	        mi.setGuidance(mg != null ? mg.getGuidance() : "-");
	        mi.setDescription(mg != null ? mg.getDescription() : "-");

	        itemDTOs.add(mi);

	        // 금액 계산
	        if (fee != null) {
	        	// base_price × dose × frequency × days
	            BigDecimal qtyMultiplier =
	            		pi.getDose()
                        .multiply(BigDecimal.valueOf(pi.getFrequency()))
                        .multiply(BigDecimal.valueOf(pi.getDays()));

	            BigDecimal itemTotal =
	                    BigDecimal.valueOf(fee.getBase_price())
	                            .multiply(qtyMultiplier);

	            // 총액
	            total = total.add(itemTotal);

	            // 과세 총액 (is_active == true 인 경우만)
	            if (fee.is_active()) {
	                taxTotal = taxTotal.add(itemTotal);
	                }
	        }
	    }

	    dto.setItems(itemDTOs);

	    // 금액 세팅
	    dto.setTotalAmount(total);
	    dto.setTaxAmount(taxTotal);
	    dto.setNonTaxAmount(total.subtract(taxTotal));

	    // 보험 할인
	    Insurance_code insurance = visit.getInsurance_code();

	    BigDecimal patientAmount = BigDecimal.ZERO;

	    if (insurance != null) {
	    	patientAmount = taxTotal.multiply(
	        		BigDecimal.valueOf(insurance.getDiscount_rate())
	        );
	    }

	    dto.setInsurerAmount(total.subtract(patientAmount));
	    dto.setPatientAmount(patientAmount);
	    dto.setDiscountRate(insurance.getDiscount_rate());
	    dto.setInsuranceName(insurance.getName());

	    model.addAttribute("guide", dto);

	    return "pharm/medicationGuidePopup";
	}
	
	// 약사->투약 팝업
	@PostMapping("/pharm/dispense/complete/{prescriptionId}")
	@Transactional
	public ResponseEntity<String> completeDispense(@PathVariable("prescriptionId") Long prescriptionId) {

	    // 1) dispense 조회
	    Dispense dispense = dispenseRepository
	            .findByPrescriptionId(prescriptionId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 처방에 대한 조제 정보가 없습니다."));

	    // 2) 상태코드 변경
	    dispense.setStatus_code("DIS_DONE");

	    // 3) 저장 (변경 감지로 flush)
	    dispenseRepository.save(dispense);  // @Transactional이면 굳이 필요 없음

	    return ResponseEntity.ok("DIS_DONE 업데이트 완료");
	}


}
