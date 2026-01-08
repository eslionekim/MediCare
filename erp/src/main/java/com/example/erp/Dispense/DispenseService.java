package com.example.erp.Dispense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.erp.Dispense.DispenseCompleteRequest.DispenseItemRequest;
import com.example.erp.Dispense_item.Dispense_item;
import com.example.erp.Dispense_item.Dispense_itemRepository;
import com.example.erp.Fee_item.Fee_item;
import com.example.erp.Fee_item.Fee_itemRepository;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Prescription_item.Prescription_item;
import com.example.erp.Prescription_item.Prescription_itemRepository;
import com.example.erp.Stock.Stock;
import com.example.erp.Stock.StockRepository;
import com.example.erp.Stock_move.Stock_move;
import com.example.erp.Stock_move.Stock_moveRepository;
import com.example.erp.Stock_move_item.Stock_move_item;
import com.example.erp.Stock_move_item.Stock_move_itemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DispenseService {

    private final DispenseRepository dispenseRepository;
    private final Dispense_itemRepository dispense_itemRepository;
    private final StockRepository stockRepository;
    private final Stock_moveRepository stock_moveRepository;
    private final Stock_move_itemRepository stock_move_itemRepository;
    private final Fee_itemRepository fee_itemRepository;
    private final ItemRepository itemRepository;
    private final Prescription_itemRepository prescription_itemRepository;

    @Transactional
	public void completeDispense(DispenseCompleteRequest request) {//조제완료
	
	    Long prescriptionId = request.getPrescriptionId();
	
	    // DISPENSE 찾기
	    Dispense dispense = dispenseRepository.findByPrescriptionId(prescriptionId)
	            .orElseThrow(() -> new IllegalArgumentException("Dispense not found"));
	    
	    // Prescription_item 리스트 조회
	    List<Prescription_item> prescriptionItems =
	            prescription_itemRepository.findByPrescriptionId(prescriptionId);
	
	    //  Dispense_item insert
	    for (Prescription_item pi : prescriptionItems) { // 처방항목 순회
	
	        Dispense_item di = new Dispense_item();
	        di.setDispense_id(dispense.getDispense_id());
	        di.setPrescription_item_id(pi.getPrescription_item_id());
	        di.setItem_code(pi.getItem_code());
	        di.setQuantity(pi.getTotal_quantity());
	
	        dispense_itemRepository.save(di);
	    }
	
	    // Stock quantity 감소,Stock_move 생성,Stock_move_item 생성
	    for (DispenseItemRequest itemReq : request.getItems()) {
	
	        String itemCode = itemReq.getItemCode(); //아이템 하나
	
	        for (DispenseCompleteRequest.LotRequest lot : itemReq.getLots()) {
	
	        	Long stockId = lot.getStockId();
	            BigDecimal useQty = lot.getQuantity(); //입력한 수량
	
	            // stock 조회
	            Stock stock = stockRepository.findById(stockId)
	                    .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

	            // stock_id 1개당 Stock_move 1건 생성
	            Stock_move move = new Stock_move();
	            move.setMove_type("OUTBOUND");
	            move.setFrom_warehouse_code(stock.getWarehouse_code());
	            move.setDispense_id(dispense.getDispense_id());
	            move.setClaim_id(prescriptionId);
	            move.setMoved_at(LocalDateTime.now());
	            move.setStatus_code("SM_DONE");
	            stock_moveRepository.save(move);
	            
	            // 수량 감소
	            stock.setQuantity(stock.getQuantity().subtract(useQty));
	            stockRepository.save(stock);
	
	            // Item 에서 fee_item_code 조회
	            Item item = itemRepository.findById(itemCode)
	                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));
	
	            Fee_item fee = fee_itemRepository.findById(item.getFee_item_code())
	                    .orElseThrow(() -> new IllegalArgumentException("해당 약품 수가항목이 존재하지 않습니다."));
	
	            // Stock_move_item insert
	            Stock_move_item smi = new Stock_move_item();
	            smi.setStock_move_id(move.getStock_move_id());
	            smi.setItem_code(itemCode);
	            smi.setLot_code(stock.getLot_code());
	            smi.setQuantity(useQty);
	            smi.setUnit_price(fee.getBase_price());
	
	            stock_move_itemRepository.save(smi);
	        }
	    }
	
	    // Dispense 상태 변경
	    dispense.setStatus_code("DIS_READY");
	    dispenseRepository.save(dispense);
	}
}

