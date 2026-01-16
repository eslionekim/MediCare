package com.example.erp.Stock_move;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.erp.Issue_request.Issue_requestRepository;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockRepository;
import com.example.erp.Stock_move_item.Stock_move_item;
import com.example.erp.Stock_move_item.Stock_move_itemRepository;
import com.example.erp.User_account.User_accountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Stock_moveService {
	private final Stock_moveRepository stock_moveRepository;
	private final ItemRepository itemRepository;
	private final StockRepository stockRepository;
	private final Stock_move_itemRepository stock_move_itemRepository;
	
	//물류->출고리스트
	public List<LogisOutboundDTO> getLogisOutboundList(
			String type,
	        String keyword,
	        LocalDate date) {

	    List<Stock_move> moves = stock_moveRepository.findLogisOutboundMoves();

	    List<LogisOutboundDTO> result = new ArrayList<>();

	    for (Stock_move sm : moves) {

	    	Stock_move_item mi = stock_move_itemRepository.findByStockMoveId(sm.getStock_move_id());
	    	if (mi == null) continue;

	            LogisOutboundDTO dto = new LogisOutboundDTO();

	            // 1) 유형 변환
	            if (sm.getMove_type().equals("transfer")) {
	                dto.setType("불출");
	            } else if (sm.getMove_type().equals("outbound")) {

	                if ("sm_discard".equals(sm.getStatus_code())) {
	                    dto.setType("폐기");
	                } else if ("sm_quantity".equals(sm.getStatus_code())) {
	                    dto.setType("수량조정");
	                }
	            }

	            // 필터
	            if (type != null && !type.isEmpty() && !type.equals(dto.getType())) {
	                continue;
	            }

	            
	            // 2) 품목명
	            Item item = itemRepository.findById(mi.getItem_code()).orElse(null);
	            dto.setItemCode(mi.getItem_code());
	            dto.setItemName(item != null ? item.getName() : "-");

	            // 3) 로트코드
	            dto.setLotCode(mi.getLot_code() != null ? mi.getLot_code() : "-");
	            // 4) 변화수량
	            dto.setQuantity(mi.getQuantity());
	            // 5) 사유
	            dto.setNote(sm.getNote() != null ? sm.getNote() : "-");
	            // 6) 직원ID/직원명 패스
	            dto.setUserId(null);
	            dto.setUserName(null);
	            // 7) 일시
	            dto.setMovedAt(sm.getMoved_at());

	            //keyword 필터 (품목명 OR 로트코드)
	            if (keyword != null && !keyword.isEmpty()) {
	                String k = keyword.toLowerCase();

	                boolean matchItemName =
	                        dto.getItemName() != null &&
	                        dto.getItemName().toLowerCase().contains(k);

	                boolean matchLotCode =
	                        dto.getLotCode() != null &&
	                        dto.getLotCode().toLowerCase().contains(k);

	                boolean matchItemCode =
	                        dto.getItemCode() != null &&
	                        dto.getItemCode().toLowerCase().contains(k);

	                if (!(matchItemName || matchLotCode || matchItemCode)) {
	                    continue;
	                }
	            }


	            //날짜 필터
	            if (date != null && dto.getMovedAt() != null) {
	                if (!dto.getMovedAt().toLocalDate().equals(date)) {
	                    continue;
	                }
	            }
	            
	            result.add(dto);
	    }

	    return result;
	}
	
	//약사->출고리스트
	public List<LogisOutboundDTO> getPharmOutboundList(
			String type,
	        String keyword,
	        LocalDate date) {

	    List<Stock_move> moves = stock_moveRepository.findPharmOutboundMoves();

	    List<LogisOutboundDTO> result = new ArrayList<>();

	    for (Stock_move sm : moves) {

	    	Stock_move_item mi = stock_move_itemRepository.findByStockMoveId(sm.getStock_move_id());

	    	if (mi == null) {
	            System.out.println("[누락확인] Stock_move_item 없음 → Stock_move ID: " + sm.getStock_move_id());
	            continue; // 이 조건 때문에 누락되는 항목이 있는지 확인
	        }


	            LogisOutboundDTO dto = new LogisOutboundDTO();

	            // 1) 유형 변환 (status_code 기준)
	            if ("SM_DONE".equalsIgnoreCase(sm.getStatus_code())) {
	                dto.setType("투약");
	            } else if ("sm_quantity".equalsIgnoreCase(sm.getStatus_code())) {
	                dto.setType("수량조정");
	            } else if ("sm_discard".equalsIgnoreCase(sm.getStatus_code())) {
	                dto.setType("폐기");
	            } else {
	                dto.setType("기타"); // 필요하면 기본값
	            }


	            // 필터
	            if (type != null && !type.isEmpty() && !type.equals(dto.getType())) {
	                continue;
	            }

	            
	            // 2) 품목명
	            Item item = itemRepository.findById(mi.getItem_code()).orElse(null);
	            dto.setItemCode(mi.getItem_code());

	            dto.setItemName(item != null ? item.getName() : "-");

	            // 3) 로트코드
	            dto.setLotCode(mi.getLot_code() != null ? mi.getLot_code() : "-");
	            

	            // 4) 변화수량
	            dto.setQuantity((mi != null) ? mi.getQuantity() : BigDecimal.ZERO);

	            // 5) 사유
	            dto.setNote(sm.getNote() != null ? sm.getNote() : "-");

	            // 6) 직원ID/직원명 패스
	            dto.setUserId(null);
	            dto.setUserName(null);

	            // 7) 일시
	            dto.setMovedAt(sm.getMoved_at());

	            //keyword 필터 (품목명 OR 로트코드)
	            if (keyword != null && !keyword.isEmpty()) {
	                String k = keyword.toLowerCase();

	                boolean matchItemName =
	                        dto.getItemName() != null &&
	                        dto.getItemName().toLowerCase().contains(k);

	                boolean matchLotCode =
	                        dto.getLotCode() != null &&
	                        dto.getLotCode().toLowerCase().contains(k);

	                boolean matchItemCode =
	                        dto.getItemCode() != null &&
	                        dto.getItemCode().toLowerCase().contains(k);

	                if (!(matchItemName || matchLotCode || matchItemCode)) {
	                    continue;
	                }
	            }


	            //날짜 필터
	            if (date != null && dto.getMovedAt() != null) {
	                if (!dto.getMovedAt().toLocalDate().equals(date)) {
	                    continue;
	                }
	            }

	            
	            result.add(dto);
	    }

	    return result;
	}
	
	//원무->출고리스트
	public List<LogisOutboundDTO> getExOutboundList(
			String type,
	        String keyword,
	        LocalDate date) {

	    List<Stock_move> moves = stock_moveRepository.findExOutboundMoves();

	    List<LogisOutboundDTO> result = new ArrayList<>();

	    for (Stock_move sm : moves) {

	    	Stock_move_item mi = stock_move_itemRepository.findByStockMoveId(sm.getStock_move_id());

	    	if (mi == null) continue;
	            LogisOutboundDTO dto = new LogisOutboundDTO();

	            // 1) 유형 변환
	            if (sm.getMove_type().equals("transfer")) {
	                dto.setType("불출");
	            } else if (sm.getMove_type().equals("outbound")) {

	                if ("sm_discard".equals(sm.getStatus_code())) {
	                    dto.setType("폐기");
	                } else if ("sm_quantity".equals(sm.getStatus_code())) {
	                    dto.setType("수량조정");
	                }
	            }
	            // 필터
	            if (type != null && !type.isEmpty() && !type.equals(dto.getType())) {
	                continue;
	            }
	            
	            // 2) 품목명
	            Item item = itemRepository.findById(mi.getItem_code()).orElse(null);
	            dto.setItemCode(mi.getItem_code());
	            dto.setItemName(item != null ? item.getName() : "-");

	            // 3) 로트코드
	            dto.setLotCode(mi.getLot_code() != null ? mi.getLot_code() : "-");

	            // 4) 변화수량
	            dto.setQuantity(mi.getQuantity());

	            // 5) 사유
	            dto.setNote(sm.getNote() != null ? sm.getNote() : "-");

	            // 6) 직원ID/직원명 패스
	            dto.setUserId(null);
	            dto.setUserName(null);

	            // 7) 일시
	            dto.setMovedAt(sm.getMoved_at());

	            //keyword 필터 (품목명 OR 로트코드)
	            if (keyword != null && !keyword.isEmpty()) {
	                String k = keyword.toLowerCase();

	                boolean matchItemName =
	                        dto.getItemName() != null &&
	                        dto.getItemName().toLowerCase().contains(k);

	                boolean matchLotCode =
	                        dto.getLotCode() != null &&
	                        dto.getLotCode().toLowerCase().contains(k);

	                boolean matchItemCode =
	                        dto.getItemCode() != null &&
	                        dto.getItemCode().toLowerCase().contains(k);

	                if (!(matchItemName || matchLotCode || matchItemCode)) {
	                    continue;
	                }
	            }


	            //날짜 필터
	            if (date != null && dto.getMovedAt() != null) {
	                if (!dto.getMovedAt().toLocalDate().equals(date)) {
	                    continue;
	                }
	            }
	            result.add(dto);
	    }

	    return result;
	}

}
