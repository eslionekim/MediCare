package com.example.erp.Issue_request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Issue_requestDTO {
	// ===== 기존 리스트용 =====
    private String itemType;          // 분류
    private String itemName;          // 물품명
    private BigDecimal requestedQty;  // 요청수량
    private String packUnitName;      // 포장단위
    private LocalDateTime requestedAt;// 요청일시
    private String statusCode;        // 상태 (코드 or 명칭)

    // ===== 상세보기용 (추가) =====
    private Long issueRequestId;      // 불출요청 ID
    private String departmentName;    // 부서명
    private String itemCode;          // 물품코드
}
