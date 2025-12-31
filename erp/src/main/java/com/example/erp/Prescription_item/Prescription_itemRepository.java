package com.example.erp.Prescription_item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Prescription_itemRepository extends JpaRepository<Prescription_item, Long>{

}
