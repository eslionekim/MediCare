package com.example.erp.Fee_item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Fee_itemRepository extends JpaRepository<Fee_item, String> {
}
