package com.example.erp.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Chart.ChartService;
import com.example.erp.Chart_diseases.Chart_diseasesRepository;
import com.example.erp.Claim.ClaimRepository;
import com.example.erp.Claim_item.Claim_itemRepository;
import com.example.erp.Diseases_code.Diseases_codeRepository;
import com.example.erp.Issue_request.Issue_request;
import com.example.erp.Issue_request.Issue_requestDTO;
import com.example.erp.Issue_request.Issue_requestRepository;
import com.example.erp.Issue_request.Issue_requestService;
import com.example.erp.Issue_request_item.Issue_request_item;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class logisController {

    private final StockService stockService;
    private final StockRepository stockRepository;
    private final Stock_moveRepository stock_moveRepository;
	private final Issue_requestService issue_requestService;
	private final Issue_request_itemRepository issue_request_itemRepository;
	private final Issue_requestRepository issue_requestRepository;
	
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
	            move.setStatus_code("SM_request");
	            move.setMoved_at(LocalDateTime.now());
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


}
