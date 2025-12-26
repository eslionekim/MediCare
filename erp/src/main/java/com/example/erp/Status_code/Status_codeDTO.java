package com.example.erp.Status_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Status_codeDTO { //의사-> 스케줄 조회-> 휴가 리스트 -> 검색창 ->승인여부 by 은서
	private String statusCode;
    private String name;
}
