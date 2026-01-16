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
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.ChartRepository;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Dispense.DispenseRepository;
import com.example.erp.Dispense.DispenseService;
import com.example.erp.Fee_item.Fee_itemRepository;
import com.example.erp.Insurance_code.Insurance_codeRepository;
import com.example.erp.Issue_request.Issue_Request_psDTO;
import com.example.erp.Issue_request.Issue_requestService;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Medication_guide.Medication_guideRepository;
import com.example.erp.Prescription.PrescriptionRepository;
import com.example.erp.Prescription_item.Prescription_itemRepository;
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
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Warehouse.Warehouse;
import com.example.erp.Warehouse.WarehouseRepository;
import com.example.erp.Work_schedule.ScheduleCalendarDTO;
import com.example.erp.Work_schedule.Work_scheduleService;
import com.example.erp.notification.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class staffContoller {

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
	private final VacationRepository vacationRepository;
	private final Vacation_typeRepository vacation_typeRepository;
	private final Work_scheduleService work_scheduleService;
	
	
	// 원무-> 전체 재고 현황 by 은서
		@GetMapping("/staff/staffItem")
		public String pharmItem(
				@RequestParam(name = "itemType",required = false) String itemType,
		        @RequestParam(name = "status",required = false) String filterStatus,
		        @RequestParam(name = "keyword",required = false) String keyword,
		        Model model) {
			List<Item> items = itemRepository.findActiveItemsInExWarehouse();

	        LocalDate today = LocalDate.now();
	        List<Map<String, Object>> itemList = new ArrayList<>();

	        for (Item item : items) {
	            // "약제창고" warehouse만 필터
	        	List<Stock> stocks = stockRepository
	        	        .findExWarehouseStockByItemCode(item.getItem_code());


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

	        List<String> itemTypeList = itemList.stream()
	                .map(m -> (String) m.get("item_type"))
	                .distinct()
	                .sorted()
	                .collect(Collectors.toList());

	        List<String> statusList = itemList.stream()
	                .map(m -> (String) m.get("status"))
	                .distinct()
	                .sorted()
	                .collect(Collectors.toList());

	        // 4. 검색 조건 적용 (AND 조건)
	        List<Map<String, Object>> filteredList = itemList.stream()
	                // 종류
	                .filter(m -> itemType == null || itemType.isBlank()
	                        || itemType.equals(m.get("item_type")))
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
	        model.addAttribute("itemTypeList", itemTypeList);
	        model.addAttribute("statusList", statusList);

	        // 검색값 유지
	        model.addAttribute("selectedItemType", itemType);
	        model.addAttribute("selectedStatus", filterStatus);
	        model.addAttribute("keyword", keyword);

	        model.addAttribute("itemList", filteredList);
	        return "staff/staffItem";
	    }
		
		//원무->전체재고현황->상세보기 by 은서
	    @GetMapping("/staff/item/{itemCode}/lots")
	    @ResponseBody
	    public List<Map<String, Object>> getItemLots(@PathVariable("itemCode") String itemCode) {
	        LocalDate today = LocalDate.now();

	        List<Stock> stocks = stockRepository
	    	        .findExWarehouseStockByItemCode(itemCode);

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
	    
	 // 원무->전체재고->로트리스트->입고등록->위치 by 은서
	    @GetMapping("/staff/warehouse/locations")
	    @ResponseBody
	    public List<String> getLocations() {
	        return warehouseRepository.findDistinctExLocations();
	    }

	    // 원무->전체재고->로트리스트->입고등록->구간 by 은서
	    @GetMapping("/staff/warehouse/zones")
	    @ResponseBody
	    public List<String> getZones(@RequestParam("location") String location) {
	        return warehouseRepository.findDistinctZonesByExLocation(location);
	    }
	    
	    // 원무->전체재고->로트리스트->입고등록 버튼 by 은서
	    @PostMapping("/staff/stock/in")
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
	    	        .findWarehouse("원무창고", location, zone)
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
	    @GetMapping("/staff/stock/in/requests")
	    @ResponseBody
	    public List<StockInRequestDTO> getStockInRequests(@RequestParam("itemCode") String itemCode) {
	    	return stock_move_itemRepository.findPendingExStockInList(itemCode);
	    }
	    
	    @GetMapping("/staff/item/{itemCode}/price") // 입고등록->단가조회
	    @ResponseBody
	    public BigDecimal getItemPrice(@PathVariable("itemCode") String itemCode) {
	        return itemRepository.findById(itemCode)
	                .orElseThrow()
	                .getUnit_price();
	    }

	    
	    @PostMapping("/staff/stock/{stockId}/discard") //원무->전체재고현황->폐기 by 은서
	    public ResponseEntity<?> discardStock(
	            @PathVariable("stockId") Long stockId,
	            @RequestParam("reason") String reason,
	            @RequestParam(value ="detail",required = false) String detail
	    ) 
	    {
	        stockService.discard(stockId, reason, detail);
	        return ResponseEntity.ok("폐기 완료");
	    }

	  
	    @PostMapping("/staff/stock/{stockId}/adjust")//원무->전체재고현황->수량조정 by 은서
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
	    
	    //원무->전체재고현황->불출요청
	    @GetMapping("/api/item/{itemCode}")
	    @ResponseBody
	    public Item getItem(@PathVariable String itemCode) {
	        return itemRepository.findById(itemCode)
	                .orElseThrow(() -> new RuntimeException("Item not found"));
	    }
	    
	    //원무 -> 불출요청
	    @PostMapping("/staff/issue-request")
	    @ResponseBody
	    public void createIssueRequest(@RequestBody Issue_Request_psDTO dto) {
	        issue_requestService.createIssueExRequest(dto);
	    }
	    
	    //원무->출고리스트
	    @GetMapping("/staff/staffOutbound") 
	    public String staffOutbound(
	    		@RequestParam(name = "type",required = false) String type,
	            @RequestParam(name = "keyword",required = false) String keyword,
	            @RequestParam(name = "date",required = false)
	            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	            LocalDate date,
	            Model model) {
	    	 List<LogisOutboundDTO> allList = stock_moveService.getExOutboundList(null, null, null);
	    	// 필터 적용 데이터
	         List<LogisOutboundDTO> filteredList =
	                 stock_moveService.getExOutboundList(type, keyword, date);

	         // select 옵션은 전체 기준
	         Set<String> types = allList.stream()
	                 .map(LogisOutboundDTO::getType)
	                 .filter(Objects::nonNull)
	                 .collect(Collectors.toCollection(LinkedHashSet::new));

	         model.addAttribute("outbounds", filteredList);
	         model.addAttribute("types", types);
	        return "staff/staffOutbound";
	    }
	    

	    @GetMapping("/staff/mySchedule") // 원무 -> 스케줄 조회 by 은서
	    public String getMySchedulePage(Model model) {
	    	String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
	        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
	        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
	        
	        model.addAttribute("vacation", vacation);
	        model.addAttribute("vacationTypes", vacationTypes);
	        model.addAttribute("username", user_id);
	        return "staff/mySchedule";
	    }
	    
	    @GetMapping("/staff/mySchedule/events") //원무 -> 스케줄 조회 -> 근무 스케줄 달력 by 은서
	    @ResponseBody
	    public List<Map<String, Object>> getMyScheduleEvents(
	            @RequestParam(value="year",required = false) int year,
	            @RequestParam(value="month",required = false) int month
	    ) {
	        String userId = SecurityContextHolder.getContext()
	                .getAuthentication().getName();

	        List<ScheduleCalendarDTO> list =
	                work_scheduleService.getStaffMonthlySchedule(userId, year, month);

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
	    
	    @GetMapping("/staff/mySchedule/vacations") //원무 -> 스케줄 조회 -> 휴가 리스트 by 은서
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
	    
	    @GetMapping("/staff/mySchedule/vacation-types") //원무-> 스케줄 조회->휴가리스트 -> 검색창-> 분류 by 은서
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
	    
	    @GetMapping("/staff/mySchedule/vacation-status") //원무-> 스케줄 조회-> 휴가리스트-> 검색창-> 승인여부 by 은서
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

	    @GetMapping("/staff/mySchedule/vacations/search") //원무-> 스케줄 조회-> 휴가리스트-> 검색창 by 은서
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

	    @PostMapping("/staff/mySchedule/vacation/{vacationId}/cancel") //원무-> 스케줄 조회-> 휴가리스트 -> 휴가취소 by 은서
	    @ResponseBody
	    public void cancelVacation(@PathVariable("vacationId") Long vacationId) {
	    	Vacation vacation = vacationRepository.findById(vacationId)
	                .orElseThrow(() -> new RuntimeException("휴가 없음"));

	        Status_code cancelStatus = status_codeRepository.findByCode("VAC_CANCEL_REQUESTED")
	                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

	        vacation.setStatus_code(cancelStatus);
	        vacationRepository.save(vacation);
	    }
	    
	 // 원무 -> 휴가 신청 by 은서
	    @GetMapping("/staff/applyVacation")
	    public String applyVacation(Model model) {
	        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
	        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
	        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
	        
	        model.addAttribute("vacation", vacation);
	        model.addAttribute("vacationTypes", vacationTypes);
	        model.addAttribute("username", user_id);
	        
	        return "staff/applyVacation";
	    }
	    
	    // 원무 -> 휴가 신청 -> 폼 제출 by 은서
	    @PostMapping("/staff/applyVacation")
	    @ResponseBody // 반환값을 JSON형태(키-값)로 전달
	    public Map<String,Object> applyVacation(@RequestBody Map<String,String> body) {
	        Map<String,Object> result = new HashMap<>(); // 키-값 
	        try {
	            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	            String user_id = auth.getName(); //로그인 한 사용자의 user_id

	            User_account user = new User_account(); 
	            user.setUser_id(user_id); //새로 생성한 객체에 user_id 설정

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
	            notificationService.notifyHR("휴가 신청", "직원 " + user_id + "님이 휴가를 신청했습니다.");
	        } catch(Exception e){
	            result.put("success", false);
	            result.put("message", e.getMessage());
	        }
	        return result;
	    }
}
