package com.example.erp.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
	private BigDecimal totalAvailableQty; // 총 가용 재고
    private BigDecimal convertedRequestQty; // 요청 환산 수량
    private String baseUnit; // 정, ml 등

    private List<LotcodeDTO> lotList;
}
