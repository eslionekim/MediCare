package com.example.erp.Dispense_item;

import java.math.BigDecimal;
import java.util.List;

import com.example.erp.Dispense.DispenseLotDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dispense_itemPopupDTO { //약사->조제리스트->조제 팝업-> 하단
	private String itemCode; // 아이템코드(검증용)
    private String itemName; // 약품명

    private String dosage;   // "1회 dose / 1일 frequency회 / days일"

    private BigDecimal stockQty; //재고
    private String status; // "미완료"
    
    private BigDecimal requiredQty; //총 수량(검증용)
    private List<DispenseLotDTO> lots;
}
