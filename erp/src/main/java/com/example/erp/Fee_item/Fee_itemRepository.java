package com.example.erp.Fee_item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface Fee_itemRepository extends JpaRepository<Fee_item, Long> {
	@Query("SELECT f FROM Fee_item f WHERE f.name LIKE %:keyword%")
	List<Fee_item> searchAll(@Param("keyword") String keyword);
}
