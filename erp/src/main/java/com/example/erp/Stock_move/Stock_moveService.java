package com.example.erp.Stock_move;

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
	public List<LogisOutboundDTO> getLogisOutboundList() {

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

	            // 2) 품목명
	            Item item = itemRepository.findById(mi.getItem_code()).orElse(null);
	            dto.setItemName(item != null ? item.getName() : "-");

	            // 3) 로트코드
	            //Stock stock = stockRepository.findById(mi.getStock_id()).orElse(null);
	            dto.setLotCode(null);

	            // 4) 변화수량
	            dto.setQuantity(mi.getQuantity());

	            // 5) 사유
	            dto.setNote(sm.getNote() != null ? sm.getNote() : "-");

	            // 6) 직원ID/직원명 패스
	            dto.setUserId(null);
	            dto.setUserName(null);

	            // 7) 일시
	            dto.setMovedAt(sm.getMoved_at());

	            result.add(dto);
	    }

	    return result;
	}

}
