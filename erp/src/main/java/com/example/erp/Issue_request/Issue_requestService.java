package com.example.erp.Issue_request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.erp.Issue_request_item.Issue_request_item;
import com.example.erp.Issue_request_item.Issue_request_itemRepository;
import com.example.erp.Item.Item;
import com.example.erp.Item.ItemRepository;
import com.example.erp.Status_code.Status_code;
import com.example.erp.Status_code.Status_codeRepository;
import com.example.erp.User_account.User_account;
import com.example.erp.User_account.User_accountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Issue_requestService {
	 private final Issue_requestRepository issue_requestRepository;
	 private final Issue_request_itemRepository issue_request_itemRepository;
	 private final ItemRepository itemRepository;
	 private final User_accountRepository user_accountRepository;
	 private final Status_codeRepository status_codeRepository;

	 //물류->불출요청리스트 by 은서
    public List<Issue_requestDTO> getItemRequestTable() {
        return issue_requestRepository.findIssueRequestList();
    }
    
    @Transactional
    //물류->불출요청리스트->요청->승인버튼 by 은서
    public void approve(Long id) {
    	issue_requestRepository.updateStatus(id, "IR_APPROVED");
    }
    
    @Transactional
    //물류->불출요청리스트->요청->반려버튼 by 은서
    public void reject(Long id) {
    	issue_requestRepository.updateStatus(id, "IR_REJECTED");
    }
    
    //물류->요청리스트
    public List<logisRequestDTO> getLogisRequests() {

        List<Issue_request> requests = issue_requestRepository.findByDepartmentCode("logis");
        List<logisRequestDTO> result = new ArrayList<>();

        for (Issue_request ir : requests) {

            Optional<Issue_request_item> items =issue_request_itemRepository.findByIssueRequestId(ir.getIssue_request_id());

            // item이 없는 경우도 대비
            Issue_request_item iri = items.orElse(null);

            Item item = null;
            if (iri != null) {
                item = itemRepository.findById(iri.getItem_code()).orElse(null);
            }

            User_account user = user_accountRepository
                    .findById(ir.getUser_id())
                    .orElse(null);

            Status_code status = status_codeRepository
                    .findById(ir.getStatus_code())
                    .orElse(null);

            logisRequestDTO dto = new logisRequestDTO();

            //유형
            if (iri == null || iri.getRequested_qty() == null) {
                dto.setType("신규등록");
            } else {
                dto.setType("구매요청");
            }

            //종류 / 품목명
            if (item != null) {
                dto.setItemType(item.getItem_type());
                dto.setItemName(item.getName());
            }

            // 수량
            if (iri != null) {
                dto.setQty(iri.getRequested_qty());
            }

            //가격 = 수량 * 단가
            if (iri != null && iri.getRequested_qty() != null && item != null) {
                if (item.getUnit_price() != null) {
                    dto.setPrice(
                        iri.getRequested_qty()
                           .multiply(item.getUnit_price())
                    );
                }
            }

            // 요청사유
            dto.setNote(
                (ir.getNote() == null || ir.getNote().isBlank())
                        ? "-" : ir.getNote()
            );

            // 직원정보
            dto.setUserId(ir.getUser_id());
            dto.setUserName(user != null ? user.getName() : "-");

            //요청일시
            dto.setRequestedAt(ir.getRequested_at());

            //상태명
            dto.setStatusName(status != null ? status.getName() : "-");

            result.add(dto);
        }

        return result;
    }
    
    //약사->불출요청
    @Transactional
    public void createIssueRequest(@RequestBody Issue_Request_psDTO dto) {

        // 로그인 사용자
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 1) Issue_request 생성
        Issue_request req = new Issue_request();
        req.setDepartment_code("PHARM");
        req.setUser_id(userId);
        req.setRequested_at(LocalDateTime.now());
        req.setStatus_code("IR_REQUESTED");

        if (dto.getNote() != null && !dto.getNote().isBlank()) {
            req.setNote(dto.getNote());
        }

        issue_requestRepository.save(req);

        // 2) Issue_request_item 저장
        Issue_request_item itemReq = new Issue_request_item();
        itemReq.setIssue_request_id(req.getIssue_request_id());
        itemReq.setItem_code(dto.getItemCode());
        itemReq.setRequested_qty(dto.getQty());

        issue_request_itemRepository.save(itemReq);
    }
    
  //원무->불출요청
    @Transactional
    public void createIssueExRequest(@RequestBody Issue_Request_psDTO dto) {

        // 로그인 사용자
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // 1) Issue_request 생성
        Issue_request req = new Issue_request();
        req.setDepartment_code("STAFF");
        req.setUser_id(userId);
        req.setRequested_at(LocalDateTime.now());
        req.setStatus_code("IR_REQUESTED");

        if (dto.getNote() != null && !dto.getNote().isBlank()) {
            req.setNote(dto.getNote());
        }

        issue_requestRepository.save(req);

        // 3) Issue_request_item 저장
        Issue_request_item itemReq = new Issue_request_item();
        itemReq.setIssue_request_id(req.getIssue_request_id());
        itemReq.setItem_code(dto.getItemCode());
        itemReq.setRequested_qty(dto.getQty());

        issue_request_itemRepository.save(itemReq);
    }

}
