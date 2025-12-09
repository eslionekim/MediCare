// src/main/java/com/example/erp/Patient/VisitHistoryDto.java
package com.example.erp.Patient;

import java.time.LocalDateTime;

public class VisitHistoryDto {

    private Long visitId;
    private LocalDateTime visitDatetime;

    private String departmentCode;
    private Long doctorId;

    private String visitType; // 초진/재진 등
    private String visitRoute; // walk-in / reservation
    private String statusCode; // WAIT, IN_TREAT, DONE ...
    private String note; // 증상/메모

    // --- getter / setter ---
    public Long getVisitId() {
        return visitId;
    }

    public void setVisitId(Long visitId) {
        this.visitId = visitId;
    }

    public LocalDateTime getVisitDatetime() {
        return visitDatetime;
    }

    public void setVisitDatetime(LocalDateTime visitDatetime) {
        this.visitDatetime = visitDatetime;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getVisitRoute() {
        return visitRoute;
    }

    public void setVisitRoute(String visitRoute) {
        this.visitRoute = visitRoute;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
