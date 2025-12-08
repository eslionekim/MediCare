package com.example.erp.Vacation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VacationRepository extends JpaRepository<Vacation, Long> {
	@Query("SELECT v FROM Vacation v JOIN FETCH v.user_account u JOIN FETCH v.vacation_type vt JOIN FETCH v.status_code sc LEFT JOIN FETCH u.staff_profile sp LEFT JOIN FETCH sp.department d")
	List<Vacation> vacationList();
}
