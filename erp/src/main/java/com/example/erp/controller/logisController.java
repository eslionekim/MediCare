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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.ChartService;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Fee_item.Fee_itemRepository;
import com.example.erp.Issue_request.Issue_request;
import com.example.erp.Issue_request.Issue_requestDTO;
import com.example.erp.Issue_request.Issue_requestRepository;
import com.example.erp.Issue_request.Issue_requestService;
import com.example.erp.Issue_request.logisRequestDTO;
import com.example.erp.Issue_request_item.Issue_request_item;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Patient.PatientService;
import com.example.erp.Reservation.ReservationRepository;
import com.example.erp.Staff_profile.MyPageDTO;
import com.example.erp.Staff_profile.Staff_profile;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeDTO;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockDTO;
import com.example.erp.Stock.StockRepository;
import com.example.erp.Stock.StockService;
import com.example.erp.Stock_move.LogisOutboundDTO;
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
import com.example.erp.Visit.TodayVisitDTO;
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Visit.VisitService;
import com.example.erp.Item.Item;
import com.example.erp.Warehouse.Warehouse;
import com.example.erp.Warehouse.WarehouseRepository;
import com.example.erp.Work_schedule.ScheduleCalendarDTO;
import com.example.erp.Work_schedule.Work_scheduleRepository;
import com.example.erp.Work_schedule.Work_scheduleService;
import com.example.erp.notification.NotificationService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class logisController {

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;

    private final Fee_itemRepository fee_itemRepository;

    private final StockService stockService;
    private final StockRepository stockRepository;
    private final Stock_moveRepository stock_moveRepository;
    private final Stock_moveService stock_moveService;
    private final Stock_move_itemRepository stock_move_itemRepository;
	private final Issue_requestService issue_requestService;
	private final Issue_request_itemRepository issue_request_itemRepository;
	private final Issue_requestRepository issue_requestRepository;
	
	private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final VacationRepository vacationRepository;
    private final Vacation_typeRepository vacation_typeRepository;
    private final Work_scheduleService work_scheduleService;
    private final Status_codeRepository status_codeRepository;
    private final User_accountRepository user_accountRepository;
    private final Staff_profileRepository staff_profileRepository;


	@GetMapping("/logis/itemRequest") //물류->불출요청리스트
    public String getTodayVisitList(Model model) {
			List<Issue_requestDTO> list =issue_requestService.getItemRequestTable();

		    model.addAttribute("list", list);
            return "logis/itemRequest"; 
    }
	
	@PostMapping("/logis/itemRequest/status") //물류->불출요청리스트->요청->승인,반려버튼 by 은서
	@ResponseBody
	public ResponseEntity<Void> updateStatus(
	        @RequestParam("issueRequestId") Long issueRequestId,
	        @RequestParam("statusCode") String statusCode
	) {

	    if ("IR_APPROVED".equals(statusCode)) {
	        issue_requestService.approve(issueRequestId);
	    } 
	    else if ("IR_REJECTED".equals(statusCode)) {
	        issue_requestService.reject(issueRequestId);
	    } 
	    else {
	        return ResponseEntity.badRequest().build();
	    }

	    return ResponseEntity.ok().build();
	}
	
	//물류->불출요청리스트-> 상세보기 by 은서
	@GetMapping("/logis/itemRequest/outbound/extra")
	@ResponseBody
	public Map<String, Object> getOutboundExtra(
	        @RequestParam("issueRequestId") Long issueRequestId,
	        @RequestParam("itemCode") String itemCode) {

	    //  stockService에서 기존 출고 정보 가져오기 (lot 리스트 등)
	    StockDTO stockData = stockService.getOutboundExtra(issueRequestId, itemCode);

	    // Issue_request_item에서 요청 환산 수량, 승인 수량 가져오기
	    Issue_request_item iri = issue_request_itemRepository
	            .findByIssueRequestId(issueRequestId)
	            .orElseThrow(() -> new RuntimeException("Issue_request_item 없음"));

	    // Map으로 만들어서 반환
	    Map<String, Object> result = new HashMap<>();
	    result.put("totalAvailableQty", stockData.getTotalAvailableQty()); //총 가용재고
	    result.put("lotList", stockData.getLotList()); //stockId,lotCode,outboundDeadline,availableQty
	    result.put("baseUnit", stockData.getBaseUnit()); //기본 단위

	    result.put("convertedRequestQty", stockData.getConvertedRequestQty()); // 요청 환산 수량
	    result.put("approvedQty", iri.getApproved_qty());          // 승인 수량

	    return result;
	}

	
	// 물류 -> 불출요청리스트 - 부분출고
@PostMapping("/logis/itemRequest/outbound/partial")
@Transactional
public ResponseEntity<String> partialOutbound(@RequestBody Map<String, Object> param) {

    try {
        Long issueRequestId = ((Number) param.get("issueRequestId")).longValue();
        List<Map<String, Object>> lotList =
                (List<Map<String, Object>>) param.get("lotList");

        BigDecimal totalQty = BigDecimal.ZERO; // ✅ 이번 출고 수량 (기본단위)

        // =========================
        // 1. 재고 차감 + 이동 기록
        // =========================
        for (Map<String, Object> lot : lotList) {

            Long stockId = ((Number) lot.get("stockId")).longValue();
            BigDecimal qty = new BigDecimal(lot.get("qty").toString()); // 기본단위

            Stock stock = stockRepository.findById(stockId)
                    .orElseThrow(() -> new RuntimeException("Stock 없음: " + stockId));

            if (stock.getQuantity().compareTo(qty) < 0) {
                throw new RuntimeException("재고 부족");
            }

            stock.setQuantity(stock.getQuantity().subtract(qty));
            stockRepository.save(stock);

            totalQty = totalQty.add(qty); // ✅ 기본단위 누적

            Stock_move move = new Stock_move();
            move.setMove_type("transfer");
            move.setFrom_warehouse_code(stock.getWarehouse_code());
            move.setIssue_request_id(issueRequestId);
            move.setStatus_code("SM_request");
            move.setMoved_at(LocalDateTime.now());
            stock_moveRepository.save(move);

            Item item = itemRepository.findById(stock.getItem_code())
                    .orElseThrow(() -> new RuntimeException("Item 없음"));

            Stock_move_item smi = new Stock_move_item();
            smi.setStock_move_id(move.getStock_move_id());
            smi.setItem_code(stock.getItem_code());
            smi.setLot_code(stock.getLot_code());
            smi.setQuantity(qty);
            smi.setUnit_price(
                    item.getUnit_price().multiply(qty).intValue()
            );
            smi.setExpiry_date(LocalDate.now());

            stock_move_itemRepository.save(smi);
        }

        // =========================
        // 2. Issue_request_item 승인 수량 처리 (🔥 핵심 수정 구간)
        // =========================
        Issue_request_item iri = issue_request_itemRepository
                .findByIssueRequestId(issueRequestId)
                .orElseThrow(() -> new RuntimeException("Issue_request_item 없음"));

        // 👉 Item 조회 (pack_unit_qty 필요)
        Item item = itemRepository.findById(iri.getItem_code())
                .orElseThrow(() -> new RuntimeException("Item 없음"));

        BigDecimal packUnitQty = BigDecimal.valueOf(item.getPack_unit_qty()); // 포장당 기본단위 수량

        // 👉 요청 수량을 기본단위로 변환
        BigDecimal requestedBaseQty =
                iri.getRequested_qty().multiply(packUnitQty);

        // 👉 기존 승인 수량 (기본단위)
        BigDecimal approvedQty =
                iri.getApproved_qty() == null
                        ? BigDecimal.ZERO
                        : iri.getApproved_qty();

        approvedQty = approvedQty.add(totalQty); // 이번 출고 반영

        // ❗ 요청 수량 초과 방지 (기본단위 기준)
        if (approvedQty.compareTo(requestedBaseQty) > 0) {
            throw new RuntimeException("요청 수량 초과 출고");
        }

        iri.setApproved_qty(approvedQty);
        issue_request_itemRepository.save(iri);

        // =========================
        // 3. Issue 상태 처리 (🔥 단위 수정)
        // =========================
        Issue_request issue = issue_requestRepository.findById(issueRequestId)
                .orElseThrow(() -> new RuntimeException("Issue_request 없음"));

        if (approvedQty.compareTo(requestedBaseQty) < 0) {
            issue.setStatus_code("IR_PICKING"); // 부분 출고
        } else {
            issue.setStatus_code("IR_DONE"); // 전량 출고
        }

        issue_requestRepository.save(issue);

        // =========================
        // 4. 응답
        // =========================
        return ResponseEntity.ok(
                issue.getStatus_code().equals("IR_DONE")
                        ? "출고 완료"
                        : "부분 출고 완료"
        );

    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }
}



	//물류-> 전체 재고 현황 by 은서
	@GetMapping("/logis/item")
	public String Item(
			@RequestParam(name = "itemType",required = false) String itemType,
	        @RequestParam(name = "status",required = false) String filterStatus,
	        @RequestParam(name = "keyword",required = false) String keyword,
	        Model model) {
		List<Item> items = itemRepository.findByIsActiveTrue();

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> itemList = new ArrayList<>();

        for (Item item : items) {
            // "물류창고" warehouse만 필터
            List<Stock> stocks = stockRepository.findByItemCode(item.getItem_code())
                    .stream()
                    .filter(s -> {
                        Warehouse w = warehouseRepository.findById(s.getWarehouse_code()).orElse(null);
                        return w != null && "물류창고".equals(w.getName());
                    })
                    .collect(Collectors.toList());

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
     // 3. 필터용 select 옵션 (중복 제거)
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
        return "logis/item";
    }

	//물류->전체재고현황->상세보기 by 은서
    @GetMapping("/logis/item/{itemCode}/lots")
    @ResponseBody
    public List<Map<String, Object>> getItemLots(@PathVariable("itemCode") String itemCode) {
        LocalDate today = LocalDate.now();

        List<Stock> stocks = stockRepository.findByItemCode(itemCode)
                .stream()
                .filter(s -> {
                    Warehouse w = warehouseRepository.findById(s.getWarehouse_code()).orElse(null);
                    return w != null && "물류창고".equals(w.getName());
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
    
    //물류->전체재고현황->신규등록
    @PostMapping("/logis/item/new")
    @Transactional
    @ResponseBody
    public ResponseEntity<?> requestNewItem(@RequestParam Map<String, String> param) {

    	String Code = param.get("code");      // 일반 물품 코드
        String madeCode = param.get("madecode");     // 제조사 물품 코드

        if (Code == null || Code.isBlank()) {
            throw new RuntimeException("일반 물품 코드는 필수입니다.");
        }
        if (madeCode == null || madeCode.isBlank()) {
            throw new RuntimeException("제조사 물품 코드는 필수입니다.");
        }
        /* =======================
         * 1. FeeItem 저장 (여기 판매가로 들어가야하는데 어떡하지,그리고 기타는..?)
         * ======================= */
        Fee_item feeItem = new Fee_item();
        feeItem.setCategory(param.get("item_type"));
        feeItem.setName(param.get("name"));
        feeItem.setBase_price(Integer.parseInt(param.get("base_price")));
        feeItem.set_active(Boolean.parseBoolean(param.get("taxable"))); // 과세=true
        String feeItemCode = param.get("fee_item_code");
        feeItem.setFee_item_code(Code);

        fee_itemRepository.save(feeItem);

        /* =======================
         * 2. Item 저장
         * ======================= */
        Item item = new Item();
        item.setItem_code(madeCode);
        item.setItem_type(param.get("item_type"));
        item.setName(param.get("name"));
        item.setBase_unit(param.get("base_unit"));
        item.setPack_unit_name(param.get("pack_name"));
        item.setPack_unit_qty(Integer.parseInt(param.get("pack_quantity")));
        item.setSafety_stock(new BigDecimal(param.get("safety_stock")));
        item.setUnit_price(new BigDecimal(param.get("base_price")));
        item.setIs_active(false); // 관리자 승인 전
        item.setCreated_at(LocalDateTime.now());
        item.setFee_item_code(Code);
        itemRepository.save(item);
        
        /* =======================
         * 3. Issue_request 생성
         * ======================= */
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Issue_request issueRequest = new Issue_request();
        issueRequest.setDepartment_code("logis");
        issueRequest.setUser_id(userId);
        issueRequest.setRequested_at(LocalDateTime.now());
        issueRequest.setStatus_code("IR_REQUESTED");
        issue_requestRepository.save(issueRequest);


        /* =======================
         * 4. Issue_request_item 생성
         * ======================= */
        Issue_request_item issueRequestItem = new Issue_request_item();
        issueRequestItem.setIssue_request_id(issueRequest.getIssue_request_id()); // FK
        issueRequestItem.setItem_code(madeCode);
        issue_request_itemRepository.save(issueRequestItem);

        return ResponseEntity.ok("신규 등록 요청 완료");
    }
    
    // 물류->전체재고->로트리스트->입고등록->위치 by 은서
    @GetMapping("/logis/warehouse/locations")
    @ResponseBody
    public List<String> getLocations() {
        return warehouseRepository.findDistinctLocations();
    }

    // 물류->전체재고->로트리스트->입고등록->구간 by 은서
    @GetMapping("/logis/warehouse/zones")
    @ResponseBody
    public List<String> getZones(@RequestParam("location") String location) {
        return warehouseRepository.findDistinctZonesByLocation(location);
    }
    
    // 물류->전체재고->로트리스트->입고등록 버튼 by 은서
    @PostMapping("/logis/stock/in")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> stockIn(
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
    	        .findWarehouse("물류창고", location, zone)
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

        // 재고 이동 저장
        Stock_move move = new Stock_move();
        move.setMove_type("INBOUND");
        move.setTo_warehouse_code(stock.getWarehouse_code());
        move.setMoved_at(LocalDateTime.now());
        move.setStatus_code("SM_IN");
        stock_moveRepository.save(move);

        // 단가 조회
        Item item = itemRepository.findById(item_code)
                .orElseThrow(() -> new RuntimeException("품목 정보 없음"));

        // 재고 이동 항목 저장
        Stock_move_item moveItem = new Stock_move_item();
        moveItem.setStock_move_id(move.getStock_move_id());
        moveItem.setItem_code(item_code);
        moveItem.setLot_code(lot_code);
        moveItem.setQuantity(BigDecimal.valueOf(quantity));

        //총 금액
        BigDecimal totalPrice = item.getUnit_price()
                        .multiply(BigDecimal.valueOf(quantity));

        moveItem.setUnit_price(totalPrice.intValue());
        moveItem.setExpiry_date(LocalDate.now());
        stock_move_itemRepository.save(moveItem);
        return ResponseEntity.ok("입고 완료");
    }
    
    @GetMapping("/logis/item/{itemCode}/price") // 입고등록->단가조회
    @ResponseBody
    public BigDecimal getItemPrice(@PathVariable("itemCode") String itemCode) {
        return itemRepository.findById(itemCode)
                .orElseThrow()
                .getUnit_price();
    }

    
    @PostMapping("/logis/stock/{stockId}/discard") //물류->전체재고현황->폐기 by 은서
    public ResponseEntity<?> discardStock(
            @PathVariable("stockId") Long stockId,
            @RequestParam("reason") String reason,
            @RequestParam(value ="detail",required = false) String detail
    ) 
    {
        stockService.discard(stockId, reason, detail);
        return ResponseEntity.ok("폐기 완료");
    }

  
    @PostMapping("/logis/stock/{stockId}/adjust")//물류->전체재고현황->수량조정 by 은서
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

    //물류->요청리스트
    @GetMapping("/logis/logisRequest") 
    public String logisRequest(Model model) {
    	List<logisRequestDTO> list = issue_requestService.getLogisRequests();
        model.addAttribute("requests", list);
        return "logis/logisRequest";
    }

    //물류->출고리스트
    @GetMapping("/logis/logisOutbound") 
    public String logisOutbound(
    		@RequestParam(name = "type",required = false) String type,
            @RequestParam(name = "keyword",required = false) String keyword,
            @RequestParam(name = "date",required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {
    	// 전체 데이터 (필터 X)
        List<LogisOutboundDTO> allList =
                stock_moveService.getLogisOutboundList(null, null, null);

        // 필터 적용 데이터
        List<LogisOutboundDTO> filteredList =
                stock_moveService.getLogisOutboundList(type, keyword, date);

        // select 옵션은 전체 기준
        Set<String> types = allList.stream()
                .map(LogisOutboundDTO::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        model.addAttribute("outbounds", filteredList);
        model.addAttribute("types", types);

        return "logis/logisOutbound";
    }
    
    //스케줄 조회
    
    @GetMapping("/logis/mySchedule") // 물류 -> 스케줄 조회 by 은서
    public String getMySchedulePage(Model model) {
    	String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        return "logis/mySchedule";
    }
    
    @GetMapping("/logis/mySchedule/events") //물류 -> 스케줄 조회 -> 근무 스케줄 달력 by 은서
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
    
    @GetMapping("/logis/mySchedule/vacations") //물류 -> 스케줄 조회 -> 휴가 리스트 by 은서
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
    
    @GetMapping("/logis/mySchedule/vacation-types") //물류-> 스케줄 조회->휴가리스트 -> 검색창-> 분류 by 은서
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
    
    @GetMapping("/logis/mySchedule/vacation-status") //물류-> 스케줄 조회-> 휴가리스트-> 검색창-> 승인여부 by 은서
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

    @GetMapping("/logis/mySchedule/vacations/search") //물류-> 스케줄 조회-> 휴가리스트-> 검색창 by 은서
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

    @PostMapping("/logis/mySchedule/vacation/{vacationId}/cancel") //물류-> 스케줄 조회-> 휴가리스트 -> 휴가취소 by 은서
    @ResponseBody
    public void cancelVacation(@PathVariable("vacationId") Long vacationId) {
    	Vacation vacation = vacationRepository.findById(vacationId)
                .orElseThrow(() -> new RuntimeException("휴가 없음"));

        Status_code cancelStatus = status_codeRepository.findByCode("VAC_CANCEL_REQUESTED")
                .orElseThrow(() -> new RuntimeException("상태코드 없음"));

        vacation.setStatus_code(cancelStatus);
        vacationRepository.save(vacation);
    }
    
 // 물류 -> 휴가 신청 by 은서
    @GetMapping("/logis/applyVacation")
    public String applyVacation(Model model) {
        String user_id = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Vacation> vacation = vacationRepository.findVacationByUserId(user_id);
        List<Vacation_type> vacationTypes = vacation_typeRepository.findAll();
        
        model.addAttribute("vacation", vacation);
        model.addAttribute("vacationTypes", vacationTypes);
        model.addAttribute("username", user_id);
        
        return "logis/applyVacation";
    }
    
    // 물류 -> 휴가 신청 -> 폼 제출 by 은서
    @PostMapping("/logis/applyVacation")
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
    
    //마이페이지
  	@GetMapping("/logis/verifyPassword")
  	public String verifyPasswordForm() {
  	    return "logis/verifyPassword";
  	}

  	@PostMapping("/logis/verifyPassword")
  	public String verifyPassword(@RequestParam("password") String password,
  	                             Model model) {

  	    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    // User_account repository 주입되어 있어야 함
  	    User_account user = user_accountRepository.findByUser_id(userId)
  	            .orElse(null);

  	    if (user == null) {
  	        model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
  	        return "doctor/verifyPassword";
  	    }

  	    // 평문이면 equals
  	    // 암호화 되어 있으면 matches 사용
  	    if (!passwordEncoder.matches(password, user.getPassword())) {
  	        model.addAttribute("error", "비밀번호가 올바르지 않습니다.");
  	        return "logis/verifyPassword";
  	    }

  	    // 성공 → 마이페이지 이동
  	    return "redirect:/logis/logisMyPage";
  	}

  	
  	@GetMapping("/logis/logisMyPage") //의사->마이페이지
      public String doctorMyPage(Model model) {
  		String userId = SecurityContextHolder.getContext().getAuthentication().getName();

  	    User_account account = user_accountRepository.findById(userId).orElseThrow();

  	    Staff_profile profile =
  	            staff_profileRepository.findByUser_account_User_id(userId)
  	            .orElse(new Staff_profile());   // 없으면 빈 객체

  	    model.addAttribute("account", account);
  	    model.addAttribute("profile", profile);
  		return "logis/logisMyPage"; 
      }
  	
  	@PutMapping("/logis/logisMyPage")
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

  	        account.setPassword(dto.getPassword());
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
