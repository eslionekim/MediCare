package com.example.erp.Warehouse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "warehouse") //창고
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Warehouse {

    @Id
    @Column(length = 50)
    private String warehouse_code; //창고코드

    @Column(nullable = false, length = 100)
    private String name; //창고명

    @Column(length = 255)
    private String location; //위치

    @Column(length = 50)
    private String zone; //구간

    @Column(nullable = false)
    private Boolean is_active = true;
}

