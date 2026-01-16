package com.example.erp.Stock_move;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockInRequestDTO {
	private Long stockMoveId;
    private String itemName;
    private BigDecimal requestQty; // quantity / pack_unit_qty
    private LocalDateTime movedAt;
}
