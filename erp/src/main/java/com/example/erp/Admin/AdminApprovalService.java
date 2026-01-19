package com.example.erp.Admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminApprovalService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void updatePurchaseStatus(Long requestId, String statusCode) {
        Query query = entityManager.createNativeQuery("""
                update issue_request
                set status_code = :status
                where issue_request_id = :id
                """);
        query.setParameter("status", statusCode);
        query.setParameter("id", requestId);
        query.executeUpdate();
    }

    @Transactional
    public void updateItemActive(String itemCode, boolean active) {
        Query query = entityManager.createNativeQuery("""
                update item
                set is_active = :active
                where item_code = :itemCode
                """);
        query.setParameter("active", active ? 1 : 0);
        query.setParameter("itemCode", itemCode);
        query.executeUpdate();
    }

    @Transactional
    public void updateStockMoveStatus(Long moveId, String statusCode) {
        Query query = entityManager.createNativeQuery("""
                update stock_move
                set status_code = :status
                where stock_move_id = :id
                """);
        query.setParameter("status", statusCode);
        query.setParameter("id", moveId);
        query.executeUpdate();
    }

    public List<PurchaseApprovalRow> loadPurchaseApprovals() {
        Query query = entityManager.createNativeQuery("""
                select ir.issue_request_id,
                       ir.department_code,
                       iri.item_code,
                       iri.requested_qty,
                       ir.status_code,
                       ir.requested_at
                from issue_request ir
                left join issue_request_item iri on ir.issue_request_id = iri.issue_request_id
                order by ir.requested_at desc
                """);
        List<Object[]> rows = query.getResultList();
        List<PurchaseApprovalRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new PurchaseApprovalRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    row[4] != null ? row[4].toString() : "",
                    row[5] != null ? row[5].toString() : ""));
        }
        return results;
    }

    public List<PriceExceptionRow> loadPriceExceptionApprovals() {
        Query query = entityManager.createNativeQuery("""
                select item_code, name, unit_price, safety_stock, is_active
                from item
                where unit_price is not null
                order by unit_price desc
                limit 20
                """);
        List<Object[]> rows = query.getResultList();
        List<PriceExceptionRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new PriceExceptionRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    row[4] != null && row[4].toString().equals("1") ? "활성" : "비활성",
                    "대기"));
        }
        return results;
    }

    public List<StockLockinRow> loadStockLockins() {
        Query query = entityManager.createNativeQuery("""
                select stock_move_id, move_type, status_code, moved_at
                from stock_move
                order by moved_at desc
                limit 20
                """);
        List<Object[]> rows = query.getResultList();
        List<StockLockinRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new StockLockinRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : ""));
        }
        return results;
    }

    public record PurchaseApprovalRow(
            String requestId,
            String requestDept,
            String itemCode,
            String quantity,
            String status,
            String requestedAt) {
    }

    public record PriceExceptionRow(
            String itemCode,
            String itemName,
            String unitPrice,
            String safetyStock,
            String activeStatus,
            String approvalStatus) {
    }

    public record StockLockinRow(
            String moveId,
            String moveType,
            String status,
            String movedAt) {
    }
}
