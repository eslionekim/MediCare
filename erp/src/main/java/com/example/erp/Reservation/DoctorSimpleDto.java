// src/main/java/com/example/erp/Reservation/DoctorSimpleDto.java
package com.example.erp.Reservation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorSimpleDto {

    private String userId;
    private String name;
}
