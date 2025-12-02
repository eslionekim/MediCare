package com.example.erp.Staff_profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.erp.Role_code.Role_code;

@Repository
public interface Staff_profileRepository extends JpaRepository<Role_code, String> {

}
