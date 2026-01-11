package com.example.erp.Issue_request;

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
public class logisRequestDTO {
	private String type;          // 구매요청 / 신규등록
    private String itemType;      // 종류
    private String itemName;      // 품목명
    private BigDecimal qty;       // 수량
    private BigDecimal price;     // 가격
    private String note;          // 요청사유
    private String userId;        // 직원ID
    private String userName;      // 직원명
    private LocalDateTime requestedAt; // 일시
    private String statusName;    // 상태명
}
