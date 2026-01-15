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
public class Issue_Request_psDTO { //약사,원무->불출요청
	private String itemCode;
    private BigDecimal qty;
    private String note;
}
