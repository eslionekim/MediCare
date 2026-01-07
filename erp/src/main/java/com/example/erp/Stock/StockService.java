package com.example.erp.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Issue_request.Issue_requestRepository;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Stock_move.Stock_move;
import com.example.erp.Stock_move.Stock_moveRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
	private final Issue_request_itemRepository issue_request_itemRepository;
	private final StockRepository stockRepository;
	private final Stock_moveRepository stock_moveRepository;
	private final ItemRepository itemRepository;
	
	//물류->불출요청리스트-> 출고 폼 by 은서
	@Transactional(readOnly = true)
	public StockDTO getOutboundExtra(Long issueRequestId, String itemCode) {

	    BigDecimal convertedQty =issue_request_itemRepository.findConvertedQty(issueRequestId, itemCode);
	    BigDecimal totalAvailableQty = stockRepository.findTotalAvailableQty(itemCode);
	    List<LotcodeDTO> lotList = stockRepository.findOutboundLots(itemCode);

	    Item item = itemRepository.findById(itemCode)
	    	    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 itemCode"));


	    StockDTO dto = new StockDTO();
	    dto.setConvertedRequestQty(convertedQty);
	    dto.setTotalAvailableQty(totalAvailableQty);
	    dto.setBaseUnit(item.getBase_unit());
	    dto.setLotList(lotList);

	    return dto;
	}
	
	@Transactional //물류->전체재고현황->폐기 by 은서
	public void discard(Long stockId, String reason, String detail) {

	    Stock stock = stockRepository.findById(stockId)
	        .orElseThrow(() -> new RuntimeException("Stock 없음"));

	    Stock_move move = new Stock_move();
	    move.setMove_type("outbound");
	    move.setFrom_warehouse_code(stock.getWarehouse_code());
	    move.setMoved_at(LocalDateTime.now());
	    move.setStatus_code("sm_discard");

	    String note = reason;
	    if (detail != null && !detail.isBlank()) {
	        note += " (" + detail + ")";
	    }
	    move.setNote(note);

	    stock_moveRepository.save(move);

	    stock.setQuantity(BigDecimal.valueOf(0));
	    stockRepository.save(stock);
	}
	
	@Transactional //물류->전체재고현황->수량조절 by 은서
	public void adjust(
	        Long stockId,
	        String type,
	        int quantity,
	        String reason,
	        String detail
	) {
	    Stock stock = stockRepository.findById(stockId)
	        .orElseThrow(() -> new RuntimeException("Stock 없음"));

	    int movedQty = quantity;
	    if ("감소".equals(type)) {
	        movedQty = -quantity;
	    }

	    Stock_move move = new Stock_move();
	    move.setMove_type("outbound");
	    move.setFrom_warehouse_code(stock.getWarehouse_code());
	    move.setMoved_at(LocalDateTime.now());
	    move.setStatus_code("sm_quantity");
	    //move.setQuantity(String.valueOf(movedQty));

	    String note = type;
	    if (reason != null && !reason.isBlank()) {
	        note += " (" + reason + ")";
	    }
	    move.setNote(note);

	    stock_moveRepository.save(move);

	    BigDecimal delta = BigDecimal.valueOf(movedQty);
	    stock.setQuantity(stock.getQuantity().add(delta));
	    
	    stockRepository.save(stock);
	}



}
