package com.example.erp.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.example.erp.Stock_move_item.Stock_move_item;
import com.example.erp.Stock_move_item.Stock_move_itemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
	private final Issue_request_itemRepository issue_request_itemRepository;
	private final StockRepository stockRepository;
	private final Stock_moveRepository stock_moveRepository;
	private final ItemRepository itemRepository;
	private final Stock_move_itemRepository stock_move_itemRepository;
	
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

	    //재고 이동 생성
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
	    
	    // item_code로 Item 조회
	    Item item = itemRepository.findById(stock.getItem_code())
	            .orElseThrow(() -> new RuntimeException("Item 없음"));

	    Integer unitPrice = item.getUnit_price().intValue();         
	    BigDecimal qty = stock.getQuantity();                    
	    Integer totalAmount = qty.intValue() * unitPrice;      

	    
	    // 재고 이동 항목 생성
	    Stock_move_item moveItem = new Stock_move_item();
	    moveItem.setStock_move_id(move.getStock_move_id());                     
	    moveItem.setItem_code(stock.getItem_code());     
	    moveItem.setLot_code(stock.getLot_code());        
	    moveItem.setQuantity(stock.getQuantity());
	    moveItem.setUnit_price(totalAmount);
	    stock_move_itemRepository.save(moveItem);

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

	    //재고 이동 
	    Stock_move move = new Stock_move();
	    move.setMove_type("outbound");
	    move.setFrom_warehouse_code(stock.getWarehouse_code());
	    move.setMoved_at(LocalDateTime.now());
	    move.setStatus_code("sm_quantity");
	    stock_moveRepository.save(move);
	    
	    stockRepository.save(stock);
	    
	    // Item 조회 (단가 가져오기)
	    Item item = itemRepository.findById(stock.getItem_code())
	            .orElseThrow(() -> new RuntimeException("Item 없음"));

	    // Integer 단가
	    Integer unitPrice = item.getUnit_price().intValue();
	    
	    // 재고 이동 항목 생성
	    Stock_move_item moveItem = new Stock_move_item();
	    moveItem.setStock_move_id(move.getStock_move_id());
	    moveItem.setItem_code(stock.getItem_code());
	    moveItem.setLot_code(stock.getLot_code());
	    moveItem.setQuantity(BigDecimal.valueOf(movedQty));
	    Integer totalPrice = unitPrice * movedQty;
	    moveItem.setUnit_price(totalPrice);
	    moveItem.setExpiry_date(LocalDate.now());

	    stock_move_itemRepository.save(moveItem);
	}



}
