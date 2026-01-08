package com.example.erp.Dispense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DispenseCompleteRequest {//조제완료
	private Long prescriptionId;
    private List<DispenseItemRequest> items;

    @Data
    public static class DispenseItemRequest {
        private String itemCode;
        private List<LotRequest> lots;
    }

    @Data
    public static class LotRequest {
        private Long stockId;
        private BigDecimal quantity;
    }
}
