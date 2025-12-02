package com.example.erp.Status_code;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Status_codeRepository  extends JpaRepository<Status_code, String> {

}
