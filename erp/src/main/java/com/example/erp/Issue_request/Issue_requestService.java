package com.example.erp.Issue_request;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Issue_requestService {
	 private final Issue_requestRepository issue_requestRepository;

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
}
