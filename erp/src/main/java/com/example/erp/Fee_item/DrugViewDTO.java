package com.example.erp.Fee_item;

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
public class DrugViewDTO {
	// Fee_item (청구)
    private String feeItemCode;
    private String category;
    private String name;
    private int basePrice;

    // Prescription_item (처방)
    private BigDecimal dose;
    private int frequency;
    private int days;
}
