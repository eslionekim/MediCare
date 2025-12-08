package com.example.erp.Fee_item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class Fee_itemController {
	@Autowired
    private Fee_itemRepository fee_itemRepository;
	
	@GetMapping("/doctor/popup/fee") //의사-> 차트 작성 -> 상병정보 -> 검색
    public String popupFee(@RequestParam(value="keyword", required=false) String keyword, Model model) {
													// 사용자가 입력한 검색어
        List<Fee_item> list; // 질병코드 리스트 조회

        if (keyword == null || keyword.isEmpty()) { // 
            list = fee_itemRepository.findAll();   // 전체 목록
        } else {
            list = fee_itemRepository.searchAll(keyword); //질병코드와 한글명 모두 검색
        }

        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);

        return "doctor/popup/feePopup"; //팝업 띄우기
    }
}
