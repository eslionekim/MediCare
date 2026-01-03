package com.example.erp.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LotcodeDTO {
	private Long stockId;
	private String lotCode;
    private LocalDate outboundDeadline;
    private BigDecimal availableQty;
}
