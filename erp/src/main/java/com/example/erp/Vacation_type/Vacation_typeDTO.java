package com.example.erp.Vacation_type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Vacation_typeDTO { //의사-> 스케줄 조회-> 휴가리스트-> 검색창->분류
    private String typeCode;
    private String typeName;
}

