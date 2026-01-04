package com.example.erp.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.example.erp.Issue_request_item.Issue_request_item;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Patient.PatientService;
import com.example.erp.Reservation.ReservationRepository;
import com.example.erp.Staff_profile.Staff_profileRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockDTO;
import com.example.erp.Stock.StockRepository;
import com.example.erp.Stock.StockService;
import com.example.erp.Stock_move.Stock_move;
import com.example.erp.Stock_move.Stock_moveRepository;
import com.example.erp.Visit.TodayVisitDTO;
import com.example.erp.Visit.VisitRepository;
import com.example.erp.Visit.VisitService;
import com.example.erp.Item.Item;
import com.example.erp.Warehouse.Warehouse;
import com.example.erp.Warehouse.WarehouseRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class logisController {

    private final Fee_itemRepository fee_itemRepository;

    private final StockService stockService;
    private final StockRepository stockRepository;
    private final Stock_moveRepository stock_moveRepository;
	private final Issue_requestService issue_requestService;
	private final Issue_request_itemRepository issue_request_itemRepository;
	private final Issue_requestRepository issue_requestRepository;
	
	private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
	
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
	        @RequestParam("itemCode") Long itemCode) {

	    // 1️⃣ stockService에서 기존 출고 정보 가져오기 (lot 리스트 등)
	    StockDTO stockData = stockService.getOutboundExtra(issueRequestId, itemCode);

	    // 2️⃣ Issue_request_item에서 요청 환산 수량, 승인 수량 가져오기
	    Issue_request_item iri = issue_request_itemRepository
	            .findByIssueRequestId(issueRequestId)
	            .orElseThrow(() -> new RuntimeException("Issue_request_item 없음"));

	    // 3️⃣ Map으로 만들어서 반환
	    Map<String, Object> result = new HashMap<>();
	    result.put("totalAvailableQty", stockData.getTotalAvailableQty());
	    result.put("lotList", stockData.getLotList());
	    result.put("baseUnit", stockData.getBaseUnit());

	    result.put("convertedRequestQty", stockData.getConvertedRequestQty()); // 요청 환산 수량
	    result.put("approvedQty", iri.getApproved_qty());          // 승인 수량

	    return result;
	}

	
	//물류->불출요청리스트-부분출고
	@PostMapping("/logis/itemRequest/outbound/partial")
	@Transactional
	public ResponseEntity<String> partialOutbound(@RequestBody Map<String, Object> param) {
	    try {
	        Long issueRequestId = ((Number)param.get("issueRequestId")).longValue();
	        List<Map<String, Object>> lotList = (List<Map<String, Object>>) param.get("lotList");
	        boolean isPartial = param.getOrDefault("isPartial", false).equals(true); // 부분출고 여부
	        
	        BigDecimal totalQty = BigDecimal.ZERO;
	
	        for (Map<String, Object> lot : lotList) {
	        	Long stockId = ((Number) lot.get("stockId")).longValue();
	            BigDecimal qty = new BigDecimal(lot.get("qty").toString());
	
	            Stock stock = stockRepository.findById(stockId)
	                    .orElseThrow(() -> new RuntimeException("Stock 없음: " + stockId));
	
	            stock.setQuantity(stock.getQuantity().subtract(qty));
	            stockRepository.save(stock);
	
	            totalQty = totalQty.add(qty);
	
	            Stock_move move = new Stock_move();
	            move.setMove_type("transfer");
	            move.setFrom_warehouse_code(stock.getWarehouse_code());
	            move.setIssue_request_id(issueRequestId);
	            move.setStatus_code(isPartial ? "SM_DRAFT" : "SM_request");
	            move.setMoved_at(LocalDateTime.now());
	            move.setQuantity("-" + qty); // 빠져나간 수량 기록
	            stock_moveRepository.save(move);
	        }
	
	        Issue_request_item iri = issue_request_itemRepository
	                .findByIssueRequestId(issueRequestId)
	                .orElseThrow(() -> new RuntimeException("Issue_request_item 없음"));
	        
	        if (isPartial) {
	        	
	            // 부분출고: 이전 approved_qty + 이번 출고량
	            iri.setApproved_qty(iri.getApproved_qty().add(totalQty));
	        } else {
	            // 전체출고: 요청 환산 수량 전체로 설정
	            iri.setApproved_qty(iri.getRequested_qty());
	        }
	        issue_request_itemRepository.save(iri);
	
	        Issue_request issue = issue_requestRepository.findById(issueRequestId)
	                .orElseThrow(() -> new RuntimeException("Issue_request 없음"));
	        issue.setStatus_code(isPartial ? "IR_PICKING" : "IR_DONE");
	        issue_requestRepository.save(issue);
	
	        return ResponseEntity.ok(isPartial ? "부분출고 처리 완료" : "전체출고 완료");
	
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(e.getMessage());
	    }
	}

	//물류-> 전체 재고 현황 by 은서
	@GetMapping("/logis/item")
	public String Item(Model model) {
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

        model.addAttribute("itemList", itemList);
        return "logis/item";
    }

	//물류->전체재고현황->상세보기 by 은서
    @GetMapping("/logis/item/{itemCode}/lots")
    @ResponseBody
    public List<Map<String, Object>> getItemLots(@PathVariable("itemCode") Long itemCode) {
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

        /* =======================
         * 1. FeeItem 저장
         * ======================= */
        Fee_item feeItem = new Fee_item();
        feeItem.setCategory(param.get("item_type"));
        feeItem.setName(param.get("name"));
        feeItem.setBase_price(Integer.parseInt(param.get("base_price")));
        feeItem.set_active(Boolean.parseBoolean(param.get("taxable"))); // 과세=true

        fee_itemRepository.save(feeItem);

        Long feeItemCode = feeItem.getFee_item_code(); // 생성된 PK

        /* =======================
         * 2. Item 저장
         * ======================= */
        Item item = new Item();
        item.setItem_type(param.get("item_type"));
        item.setName(param.get("name"));
        item.setBase_unit(param.get("base_unit"));
        item.setPack_unit_name(param.get("pack_name"));
        item.setPack_unit_qty(Integer.parseInt(param.get("pack_quantity")));
        item.setSafety_stock(new BigDecimal(param.get("safety_stock")));
        item.setUnit_price(new BigDecimal(param.get("base_price")));
        item.setIs_active(false); // 관리자 승인 전
        item.setFee_item_code(feeItemCode);
        item.setCreated_at(LocalDateTime.now());

        itemRepository.save(item);

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
    		@RequestParam("item_code") Long item_code,
            @RequestParam("location") String location,
            @RequestParam("zone") String zone,
            @RequestParam("lot_code") String lot_code,
            @RequestParam("quantity") int quantity,
            @RequestParam(value = "expiry_date", required = false) LocalDate expiry_date,
            @RequestParam(value = "outbound_deadline", required = false) LocalDate outbound_deadline,
            @RequestParam("created_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate created_at
    ) {

    	Warehouse warehouse = warehouseRepository
    	        .findWarehouse("물류창고", location, zone)
    	        .orElseThrow(() -> new RuntimeException("창고 정보 없음"));


        Stock stock = new Stock();
        stock.setWarehouse_code(warehouse.getWarehouse_code());
        stock.setItem_code(item_code);
        stock.setLot_code(lot_code);
        stock.setQuantity(BigDecimal.valueOf(quantity));
        stock.setExpiry_date(expiry_date);
        stock.setOutbound_deadline(outbound_deadline);
        stock.setCreated_at(created_at.atStartOfDay());

        stockRepository.save(stock);

        return ResponseEntity.ok("입고 완료");
    }
    
    @GetMapping("/logis/item/{itemCode}/price") // 입고등록->단가조회
    @ResponseBody
    public BigDecimal getItemPrice(@PathVariable("itemCode") Long itemCode) {
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

    
}
