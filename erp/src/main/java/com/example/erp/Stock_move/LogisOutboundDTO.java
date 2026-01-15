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
public class LogisOutboundDTO {
	private String type;          // 불출 / 폐기 / 수량조정
	private String itemCode;
    private String itemName;
    private String lotCode;
    private BigDecimal quantity;
    private String note;
    private String userId;        // null 유지
    private String userName;      // null 유지
    private LocalDateTime movedAt;
}
