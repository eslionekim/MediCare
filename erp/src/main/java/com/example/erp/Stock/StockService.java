package com.example.erp.Stock;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Issue_request.Issue_requestRepository;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {
	private final Issue_request_itemRepository issue_request_itemRepository;
	private final StockRepository stockRepository;
	private final ItemRepository itemRepository;
	
	//물류->불출요청리스트-> 출고 폼 by 은서
	@Transactional(readOnly = true)
	public StockDTO getOutboundExtra(Long issueRequestId, Long itemCode) {

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

}
