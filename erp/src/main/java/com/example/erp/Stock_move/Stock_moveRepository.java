package com.example.erp.Stock_move;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Stock_moveRepository extends JpaRepository<Stock_move, Long>{

}
