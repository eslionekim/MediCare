package com.example.erp.Dispense;

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
public class DispensePopupDTO { //약사->조제리스트->조제팝업 상단
	private Long prescriptionId; // 처방전id

    private Long patientId; //환자id
    private String patientName; //환자 이름
    private LocalDate birth; //생년월일
    private String gender; //성별

    private LocalDateTime recentVisitAt; //최근 방문일

    private String userId; //의사ID
    private String Name; //의사명

    private String diagnosis; //진단명
    private String departmentName; //진료과

    private List<Dispense_itemPopupDTO> items; // 처방전의 약품리스트
}
