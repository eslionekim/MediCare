package com.example.erp.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.example.erp.Issue_request.Issue_Request_psDTO;
import com.example.erp.Issue_request.Issue_requestService;
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
import com.example.erp.Staff_profile.MyPageDTO;
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeDTO;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockRepository;
import com.example.erp.Stock.StockService;
import com.example.erp.Stock_move.LogisOutboundDTO;
import com.example.erp.Stock_move.StockInRequestDTO;
import com.example.erp.Stock_move.Stock_move;
import com.example.erp.Stock_move.Stock_moveRepository;
import com.example.erp.Stock_move.Stock_moveService;
import com.example.erp.Stock_move_item.Stock_move_item;
import com.example.erp.Stock_move_item.Stock_move_itemRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;
import com.example.erp.Vacation.Vacation;
import com.example.erp.Vacation.VacationDTO;
import com.example.erp.Vacation.VacationRepository;
import com.example.erp.Vacation_type.Vacation_type;
import com.example.erp.Vacation_type.Vacation_typeDTO;
import com.example.erp.Vacation_type.Vacation_typeRepository;
import com.example.erp.Visit.Visit;
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Warehouse.Warehouse;
import com.example.erp.Warehouse.WarehouseRepository;
import com.example.erp.Work_schedule.ScheduleCalendarDTO;
import com.example.erp.Work_schedule.Work_scheduleRepository;
import com.example.erp.Work_schedule.Work_scheduleService;
import com.example.erp.notification.NotificationService;
import jakarta.transaction.Transactional;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class pharmController {

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;

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
	private final StockService stockService;
	private final Stock_moveService stock_moveService;
	private final Stock_moveRepository stock_moveRepository;
	private final Stock_move_itemRepository stock_move_itemRepository;
	private final WarehouseRepository warehouseRepository;
	private final DispenseService dispenseService;
	private final Issue_requestService issue_requestService;
	private final Work_scheduleRepository work_scheduleRepository;
	private final Work_scheduleService work_scheduleService;
	private final VacationRepository vacationRepository;
	private final Vacation_typeRepository vacation_typeRepository;
	private final Staff_profileRepository staff_profileRepository;
	
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
	
	// 약사 -> 전체 LOT 조회 (초기 표시용) by 은서
	@GetMapping("/pharm/lots")
	@ResponseBody
	public List<Map<String, Object>> getAllDrugLots() {
	    LocalDate today = LocalDate.now();

	    // 물류창고에 있는 모든 Stock
	    List<Stock> stocks = stockRepository.findAll()
	            .stream()
	            .filter(s -> s.getQuantity().compareTo(BigDecimal.ZERO) > 0)
	            .filter(s -> {
	                Warehouse w = warehouseRepository.findById(s.getWarehouse_code()).orElse(null);
	                return w != null && "약제창고".equals(w.getName());
	            })
	            .collect(Collectors.toList());

	    List<Map<String, Object>> result = new ArrayList<>();

	    for (Stock s : stocks) {
	        Warehouse w = warehouseRepository.findById(s.getWarehouse_code()).orElse(null);
	        if (w == null) continue;

	        Map<String, Object> lot = new HashMap<>();
	        lot.put("stock_id", s.getStock_id());
	        lot.put("lot_code", s.getLot_code());
	        lot.put("created_at", s.getCreated_at());
	        lot.put("location", w.getLocation() + "-" + w.getZone());
	        lot.put("expiry_date", s.getExpiry_date());
	        lot.put("outbound_deadline", s.getOutbound_deadline());
	        lot.put("quantity", s.getQuantity());

	        // 상태 계산
	        String status = "안전";
	        if (s.getOutbound_deadline() != null && !s.getOutbound_deadline().isAfter(today)
	                && (s.getExpiry_date() == null || s.getExpiry_date().isAfter(today))) {
	            status = "임박";
	        } else if (s.getExpiry_date() != null && !s.getExpiry_date().isAfter(today)) {
	            status = "만료";
	        }
	        lot.put("status", status);

	        result.add(lot);
	    }

	    return result;
	}
	
	//약사-> 전체 재고 현황 by 은서
	@GetMapping("/pharm/pharmItem")
	public String pharmItem(
			@RequestParam(name = "itemType",required = false) String itemType,
	        @RequestParam(name = "status",required = false) String filterStatus,
	        @RequestParam(name = "keyword",required = false) String keyword,
	        Model model) {
		List<Item> items = itemRepository.findActiveItemsInDrugWarehouse();

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> itemList = new ArrayList<>();

        for (Item item : items) {
            // "약제창고" warehouse만 필터
        	List<Stock> stocks = stockRepository
        	        .findDrugWarehouseStockByItemCode(item.getItem_code());


            BigDecimal currentStock = stocks.stream()
                    .map(Stock::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal availableStock = stocks.stream()
                    .filter(s -> s.getOutbound_deadline() == null || !s.getOutbound_deadline().isBefore(today))
                    .map(Stock::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String status = availableStock.compareTo(item.getSafety_stock()) <= 0
                    ? "부족"
                    : "안전";


            Map<String, Object> map = new HashMap<>();
            map.put("item_code", item.getItem_code());
            map.put("item_type", item.getItem_type());
            map.put("name", item.getName());
            map.put("current_stock", currentStock);
            map.put("safety_stock", item.getSafety_stock());
            map.put("available_stock", availableStock);
            map.put("status", status);

            itemList.add(map);
        }
        
        List<String> statusList = itemList.stream()
                .map(m -> (String) m.get("status"))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 4. 검색 조건 적용 (AND 조건)
        List<Map<String, Object>> filteredList = itemList.stream()
                // 상태
                .filter(m -> filterStatus == null || filterStatus.isBlank()
                        || filterStatus.equals(m.get("status")))
                // 키워드 (품목코드 OR 품목명)
                .filter(m -> {
                    if (keyword == null || keyword.isBlank()) return true;
                    String kw = keyword.toLowerCase();
                    return m.get("item_code").toString().toLowerCase().contains(kw)
                        || m.get("name").toString().toLowerCase().contains(kw);
                })
                .collect(Collectors.toList());

        // 5. Model 전달
        model.addAttribute("filteredList", filteredList);
        model.addAttribute("statusList", statusList);

        // 검색값 유지
        model.addAttribute("selectedItemType", itemType);
        model.addAttribute("selectedStatus", filterStatus);
        model.addAttribute("keyword", keyword);

        model.addAttribute("itemList", filteredList);
        return "pharm/pharmItem";
    }
	
	//약사->전체재고현황->상세보기 by 은서
    @GetMapping("/pharm/item/{itemCode}/lots")
    @ResponseBody
    public List<Map<String, Object>> getItemLots(@PathVariable("itemCode") String itemCode) {
        LocalDate today = LocalDate.now();

        List<Stock> stocks = stockRepository
    	        .findDrugWarehouseStockByItemCode(itemCode);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Stock s : stocks) {
            Warehouse w = warehouseRepository.findById(s.getWarehouse_code()).orElse(null);
            if (w == null) continue;

            Map<String, Object> lot = new HashMap<>();
            lot.put("stock_id", s.getStock_id());  
            lot.put("lot_code", s.getLot_code());
            lot.put("created_at", s.getCreated_at());
            lot.put("location", w.getLocation() + "-" + w.getZone());
            lot.put("expiry_date", s.getExpiry_date());
            lot.put("outbound_deadline", s.getOutbound_deadline());
            lot.put("quantity", s.getQuantity());

            // 상태 계산
            String status = "안전";
            if (s.getOutbound_deadline() != null && !s.getOutbound_deadline().isAfter(today)
                    && (s.getExpiry_date() == null || s.getExpiry_date().isAfter(today))) {
                status = "임박";
            } else if (s.getExpiry_date() != null && !s.getExpiry_date().isAfter(today)) {
                status = "만료";
            }
            lot.put("status", status);

            result.add(lot);
        }

        return result;
    }
    
 // 약사->전체재고->로트리스트->입고등록->위치 by 은서
    @GetMapping("/pharm/warehouse/locations")
    @ResponseBody
    public List<String> getLocations() {
        return warehouseRepository.findDistinctPharmLocations();
    }

    // 약사->전체재고->로트리스트->입고등록->구간 by 은서
    @GetMapping("/pharm/warehouse/zones")
    @ResponseBody
    public List<String> getZones(@RequestParam("location") String location) {
        return warehouseRepository.findDistinctZonesByPharmLocation(location);
    }
    
    // 약사->전체재고->로트리스트->입고등록 버튼 by 은서
    @PostMapping("/pharm/stock/in")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> stockIn(
    		@RequestParam("stock_move_id")  Long stock_move_id,
    		@RequestParam("item_code") String item_code,
            @RequestParam("location") String location,
            @RequestParam("zone") String zone,
            @RequestParam("lot_code") String lot_code,
            @RequestParam("quantity") int quantity,
            @RequestParam(value = "expiry_date", required = false) LocalDate expiry_date,
            @RequestParam(value = "outbound_deadline", required = false) LocalDate outbound_deadline,
            @RequestParam("created_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate created_at
    ) {
    	//창고 찾기
    	Warehouse warehouse = warehouseRepository
    	        .findWarehouse("약제창고", location, zone)
    	        .orElseThrow(() -> new RuntimeException("창고 정보 없음"));

    	//재고 추가
        Stock stock = new Stock();
        stock.setWarehouse_code(warehouse.getWarehouse_code());
        stock.setItem_code(item_code);
        stock.setLot_code(lot_code);
        stock.setQuantity(BigDecimal.valueOf(quantity));
        stock.setExpiry_date(expiry_date);
        stock.setOutbound_deadline(outbound_deadline);
        stock.setCreated_at(created_at.atStartOfDay());

        stockRepository.save(stock);

        Stock_move move = stock_moveRepository.findById(stock_move_id)
                .orElseThrow(() -> new RuntimeException("입고요청 없음"));


        // 기존 요청을 실제 입고로 확정
        move.setTo_warehouse_code(warehouse.getWarehouse_code());
        move.setStatus_code("SM_IN");

        return ResponseEntity.ok("입고 완료");
    }
    
    //약사->불출리스트
    @GetMapping("/pharm/stock/in/requests")
    @ResponseBody
    public List<StockInRequestDTO> getStockInRequests(@RequestParam("itemCode") String itemCode) {
        return stock_move_itemRepository.findPendingDrugStockInList(itemCode);
    }

    
    @GetMapping("/pharm/item/{itemCode}/price") // 입고등록->단가조회
    @ResponseBody
    public BigDecimal getItemPrice(@PathVariable("itemCode") String itemCode) {
        return itemRepository.findById(itemCode)
                .orElseThrow()
                .getUnit_price();
    }

    
    @PostMapping("/pharm/stock/{stockId}/discard") //약사->전체재고현황->폐기 by 은서
    public ResponseEntity<?> discardStock(
            @PathVariable("stockId") Long stockId,
            @RequestParam("reason") String reason,
            @RequestParam(value ="detail",required = false) String detail
    ) 
    {
        stockService.discard(stockId, reason, detail);
        return ResponseEntity.ok("폐기 완료");
    }

  
    @PostMapping("/pharm/stock/{stockId}/adjust")//약사->전체재고현황->수량조정 by 은서
    public ResponseEntity<?> adjustStock(
            @PathVariable("stockId") Long stockId,
            @RequestParam("type") String type,      // 증가 / 감소
            @RequestParam("quantity") int quantity,
            @RequestParam(value ="reason",required = false) String reason,
            @RequestParam(value ="detail",required = false) String detail
    ) {
        stockService.adjust(stockId, type, quantity, reason, detail);
        return ResponseEntity.ok("수량 조정 완료");
    }
    
    //약사->불출요청->수량
    @GetMapping("/pharm/item/{selectedItemCode}/pack-unit-qty")
    @ResponseBody
    public Integer getPackUnitQty(@PathVariable("selectedItemCode") String selectedItemCode) {
        return itemRepository.findById(selectedItemCode)
                .orElseThrow()
                .getPack_unit_qty(); // 필드명 맞게
    }

    
    //약사 -> 불출요청
    @PostMapping("/pharm/issue-request")
    @ResponseBody
    public void createIssueRequest(@RequestBody Issue_Request_psDTO dto) {
        issue_requestService.createIssueRequest(dto);
    }
    
    //약사->출고리스트
    @GetMapping("/pharm/pharmOutbound") 
    public String pharmOutbound(
    		@RequestParam(name = "type",required = false) String type,
            @RequestParam(name = "keyword",required = false) String keyword,
            @RequestParam(name = "date",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {
    	 List<LogisOutboundDTO> allList = stock_moveService.getPharmOutboundList(null, null, null);
    	// 필터 적용 데이터
         List<LogisOutboundDTO> filteredList =
                 stock_moveService.getPharmOutboundList(type, keyword, date);

         // select 옵션은 전체 기준
         Set<String> types = allList.stream()
                 .map(LogisOutboundDTO::getType)
                 .filter(Objects::nonNull)
                 .collect(Collectors.toCollection(LinkedHashSet::new));
    	 
    	 model.addAttribute("outbounds", filteredList);
    	 model.addAttribute("types", types);
         
    	 return "pharm/pharmOutbound";
    }
    
    
    @GetMapping("/pharm/mySchedule") // 약사 -> 스케줄 조회 by 은서
    public String getMySchedulePage(Model model) {
    	String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        return "pharm/mySchedule";
    }
    
    @GetMapping("/pharm/mySchedule/events") //약사 -> 스케줄 조회 -> 근무 스케줄 달력 by 은서
    @ResponseBody
    public List<Map<String, Object>> getMyScheduleEvents(
            @RequestParam(value="year",required = false) int year,
            @RequestParam(value="month",required = false) int month
    ) {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<ScheduleCalendarDTO> list =
                work_scheduleService.getPharmMonthlySchedule(userId, year, month);

        List<Map<String, Object>> events = new ArrayList<>();

        for (ScheduleCalendarDTO item : list) {
            Map<String, Object> event = new HashMap<>();
            
            StringBuilder title = new StringBuilder();
            title.append(item.getWorkName());

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            if (item.getStartTime() != null) {
                title.append("\n출근 ")
                     .append(item.getStartTime().format(timeFormatter));
            }

            if (item.getEndTime() != null) {
                title.append("\n퇴근 ")
                     .append(item.getEndTime().format(timeFormatter));
            }

            if (item.getStatusName() != null) {
                title.append(item.getStatusName());
            }

            
            event.put("title", title.toString());                 // ⭐ work_name
            event.put("start", item.getWorkDate().toString());     // ⭐ yyyy-MM-dd
            event.put("allDay", true);

            events.add(event);
        }

        return events;
    }
    
    @GetMapping("/pharm/mySchedule/vacations") //약사 -> 스케줄 조회 -> 휴가 리스트 by 은서
    @ResponseBody
    public List<VacationDTO> getMyVacations() {
    	String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return vacationRepository.findVacationByUserId(userId)
            .stream()
            .map(v -> new VacationDTO(
                v.getVacation_id(),
                v.getVacation_type().getType_name(),
                v.getStart_date().toString(),
                v.getEnd_date().toString(),
                v.getStatus_code().getName(),
                v.getStatus_code().getStatus_code()
            ))
            .collect(Collectors.toList());
    }
    
    @GetMapping("/pharm/mySchedule/vacation-types") //약사-> 스케줄 조회->휴가리스트 -> 검색창-> 분류 by 은서
    @ResponseBody
    public List<Vacation_typeDTO> getVacationTypes() {
        return vacation_typeRepository.findByIsActiveTrue()
            .stream()
            .map(v -> new Vacation_typeDTO(
                v.getVacation_type_code(),
                v.getType_name()
            ))
            .toList();
    }
    
    @GetMapping("/pharm/mySchedule/vacation-status") //약사-> 스케줄 조회-> 휴가리스트-> 검색창-> 승인여부 by 은서
    @ResponseBody
    public List<Status_codeDTO> getVacationStatus() {
        return status_codeRepository.findByCategoryAndIsActiveTrue("vacation")
            .stream()
            .map(s -> new Status_codeDTO(
                s.getStatus_code(),
                s.getName()
            ))
            .toList();
    }

    @GetMapping("/pharm/mySchedule/vacations/search") //약사-> 스케줄 조회-> 휴가리스트-> 검색창 by 은서
    @ResponseBody
    public List<VacationDTO> searchVacations(
        @RequestParam(value="typeCode",required = false) String typeCode,
        @RequestParam(value="statusCode",required = false) String statusCode,
        @RequestParam(value="date",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return vacationRepository.searchVacations(userId,
                (typeCode == null || typeCode.isBlank()) ? null : typeCode,
                (statusCode == null || statusCode.isBlank()) ? null : statusCode,
                date)
            .stream()
            .map(v -> new VacationDTO(
        		v.getVacation_id(),
                v.getVacation_type().getType_name(),
                v.getStart_date().toString(),
                v.getEnd_date().toString(),
                v.getStatus_code().getName(),
                v.getStatus_code().getStatus_code()
            ))
            .collect(Collectors.toList());
    }

    @PostMapping("/pharm/mySchedule/vacation/{vacationId}/cancel") //약사-> 스케줄 조회-> 휴가리스트 -> 휴가취소 by 은서
    @ResponseBody
    public void cancelVacation(@PathVariable("vacationId") Long vacationId) {
    	Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new RuntimeException("휴가 없음"));

        Status_code cancelStatus = status_codeRepository.findByCode("VAC_CANCEL_REQUESTED")
                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

        vacation.setStatus_code(cancelStatus);
        vacationRepository.save(vacation);
    }
    
 // 약사 -> 휴가 신청 by 은서
    @GetMapping("/pharm/applyVacation")
    public String applyVacation(Model model) {
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        
        return "pharm/applyVacation";
    }
    
    // 약사 -> 휴가 신청 -> 폼 제출 by 은서
    @PostMapping("/pharm/applyVacation")
    @ResponseBody // 반환값을 JSON형태(키-값)로 전달
    public Map<String,Object> applyVacation(@RequestBody Map<String,String> body) {
        Map<String,Object> result = new HashMap<>(); // 키-값 
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String user_id = auth.getName(); //로그인 한 사용자의 user_id

            User_account user = user_accountRepository.findByUser_id(user_id)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));

            Vacation_type vt = vacation_typeRepository.findByTypeName(body.get("type_name")) //type_name으로 vacation_type 찾기
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 휴가분류"));

            Status_code status_code = status_codeRepository.findByCode("VAC_APPROVED_REQUESTED") //코드로 status_code찾기
                    .orElseThrow(() -> new RuntimeException("상태코드 없음"));

            Vacation v = new Vacation();
            v.setUser_account(user); 
            v.setVacation_type(vt);
            v.setStart_date(LocalDate.parse(body.get("start_date")));
            v.setEnd_date(LocalDate.parse(body.get("end_date")));
            v.setStatus_code(status_code);
            v.setReason(body.get("reason"));

            vacationRepository.save(v);
            
            result.put("success", true); // 저장 성공 시
            // 직원이 휴가 신청했을 때 HR에게 알림
            notificationService.notifyHR("휴가 신청", "휴가 신청이 있습니다.");
        } catch(Exception e){
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
  //약사->마이페이지->비밀번호 확인
  	@GetMapping("/pharm/verifyPassword")
  	public String verifyPasswordForm() {
  	    return "pharm/verifyPassword";
  	}

  	@PostMapping("/pharm/verifyPassword")
  	public String verifyPassword(@RequestParam("password") String password,
  	                             Model model) {

  	    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    // User_account repository 주입되어 있어야 함
  	    User_account user = user_accountRepository.findByUser_id(userId)
  	            .orElse(null);

  	    if (user == null) {
  	        model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
  	        return "pharm/verifyPassword";
  	    }

  	    // 평문이면 equals
  	    // 암호화 되어 있으면 matches 사용
  	    if (!passwordEncoder.matches(password, user.getPassword())) {
  	        model.addAttribute("error", "비밀번호가 올바르지 않습니다.");
  	        return "pharm/verifyPassword";
  	    }

  	    // 성공 → 마이페이지 이동
  	    return "redirect:/pharm/pharmMyPage";
  	}

  	
  	@GetMapping("/pharm/pharmMyPage") //의사->마이페이지
      public String doctorMyPage(Model model) {
  		String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    User_account account = user_accountRepository.findById(userId).orElseThrow();

  	    Staff_profile profile =
  	            staff_profileRepository.findByUser_account_User_id(userId)
  	            .orElse(new Staff_profile());   // 없으면 빈 객체

  	    model.addAttribute("account", account);
  	    model.addAttribute("profile", profile);
  		return "pharm/pharmMyPage"; 
      }
  	
  	@PutMapping("/pharm/pharmMyPage")
  	@ResponseBody
  	public String updateDoctor(@RequestBody MyPageDTO dto) {

  	    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    User_account account = user_accountRepository.findById(userId).orElseThrow();

  	    // 비밀번호가 비어있지 않을 때만 변경
  	    if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {

  	        // 비밀번호 중복 검사
  	        if (user_accountRepository.existsByPassword(dto.getPassword())) {
  	            return "이미 사용 중인 비밀번호입니다.";
  	        }

  	      String encodedPassword = passwordEncoder.encode(dto.getPassword());
  	      account.setPassword(encodedPassword);
  	    }

  	    user_accountRepository.save(account);

  	    Staff_profile profile =
  	            staff_profileRepository.findByUser_account_User_id(userId)
  	            .orElse(new Staff_profile());

  	    profile.setLicense_number(dto.getLicense());
  	    profile.setBank_name(dto.getBank());
  	    profile.setBank_account(dto.getAccount());

  	    staff_profileRepository.save(profile);

  	    return "수정 완료";
  	}

}
