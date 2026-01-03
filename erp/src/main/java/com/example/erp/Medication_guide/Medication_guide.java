package com.example.erp.Medication_guide;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medication_guide")
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Medication_guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guide_id; //복약지도 번호

    @Column(nullable = false)
    private Long item_code; //물품 코드

    @Lob
    private String description; //설명

    @Lob
    private String guidance; //복약 안내

    private LocalDateTime updated_at;

    private Boolean is_active = true; //사용여부
}

