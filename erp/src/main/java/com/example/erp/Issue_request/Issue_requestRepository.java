package com.example.erp.Issue_request;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Issue_requestRepository extends JpaRepository<Issue_request, Long>{
	// 물류->불출요청리스트 by 은서
	@Query("""
	    select new com.example.erp.Issue_request.Issue_requestDTO(
	        i.item_type,
	        i.name,
	        iri.requested_qty,
	        i.pack_unit_name,
	        ir.requested_at,
	        sc.name,
	        
	        ir.issue_request_id,
	        d.name,
	        iri.item_code
	    )
	    from Issue_request ir
	    join Issue_request_item iri
	        on ir.issue_request_id = iri.issue_request_id
	    join Department d
			on ir.department_code = d.department_code
	    join Item i
	        on iri.item_code = i.item_code
	    join Status_code sc
	        on ir.status_code = sc.status_code
	    where ir.department_code <> 'logis' 
	    and ir.status_code in ('IR_PICKING','IR_REQUESTED','IR_APPROVED')
	    order by ir.requested_at desc
	""")
	List<Issue_requestDTO> findIssueRequestList();

	// 물류-> 불출요청리스트 -> 요청 상태 ->승인,반려 by 은서
	@Modifying
	@Query("""
	    update Issue_request ir
	    set ir.status_code = :statusCode
	    where ir.issue_request_id = :issueRequestId
	""")
	void updateStatus(@Param("issueRequestId") Long issueRequestId,@Param("statusCode") String statusCode);

	//물류->요청리스트 by은서
	@Query("select ir from Issue_request ir where ir.department_code = :dept")
	List<Issue_request> findByDepartmentCode(@Param("dept") String departmentCode);


}
