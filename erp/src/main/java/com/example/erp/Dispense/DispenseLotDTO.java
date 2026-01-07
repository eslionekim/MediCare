package com.example.erp.Dispense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.erp.Dispense_item.Dispense_itemPopupDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DispenseLotDTO {
	private Long stockId;
	private String lotCode;
    private BigDecimal quantity;
    private LocalDate expiryDate;
    private String location;
}
