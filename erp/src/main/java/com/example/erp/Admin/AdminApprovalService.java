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

    @Transactional
    public void updateNewItemApproval(Long requestId, boolean approved) {
        Query itemQuery = entityManager.createNativeQuery("""
                select iri.item_code
                from issue_request_item iri
                where iri.issue_request_id = :id
                """);
        itemQuery.setParameter("id", requestId);
        Object itemCodeObj = itemQuery.getSingleResult();
        if (itemCodeObj != null) {
            updateItemActive(itemCodeObj.toString(), approved);
        }
        updatePurchaseStatus(requestId, approved ? "IR_APPROVED" : "IR_REJECTED");
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
                where ir.status_code = 'IR_WAIT_APPROVAL'
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
                select distinct i.item_code, i.name, i.unit_price, i.safety_stock, i.is_active
                from issue_request ir
                join issue_request_item iri on ir.issue_request_id = iri.issue_request_id
                join item i on i.item_code = iri.item_code
                where ir.status_code = 'IR_WAIT_APPROVAL'
                  and i.unit_price is not null
                  and (i.unit_price * ifnull(iri.requested_qty, 0)) >= 10000000
                order by i.unit_price desc
                """);
        List<Object[]> rows = query.getResultList();
        List<PriceExceptionRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            boolean active = row[4] != null && row[4].toString().equals("1");
            results.add(new PriceExceptionRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    active ? "활성" : "비활성",
                    active ? "승인 완료" : "승인 대기"));
        }
        return results;
    }

    public List<NewItemApprovalRow> loadNewItemApprovals() {
        Query query = entityManager.createNativeQuery("""
                select ir.issue_request_id,
                       iri.item_code,
                       i.name,
                       i.item_type,
                       i.unit_price,
                       ir.status_code,
                       ir.requested_at
                from issue_request ir
                join issue_request_item iri on ir.issue_request_id = iri.issue_request_id
                join item i on i.item_code = iri.item_code
                where ir.status_code = 'IR_REQUESTED'
                  and i.is_active = 0
                order by ir.requested_at desc
                """);
        List<Object[]> rows = query.getResultList();
        List<NewItemApprovalRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new NewItemApprovalRow(
                    row[0] != null ? row[0].toString() : "",
                    row[1] != null ? row[1].toString() : "",
                    row[2] != null ? row[2].toString() : "",
                    row[3] != null ? row[3].toString() : "",
                    row[4] != null ? row[4].toString() : "",
                    row[5] != null ? row[5].toString() : "",
                    row[6] != null ? row[6].toString() : ""));
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

    public record NewItemApprovalRow(
            String requestId,
            String itemCode,
            String itemName,
            String itemType,
            String unitPrice,
            String status,
            String requestedAt) {
    }

    public record StockLockinRow(
            String moveId,
            String moveType,
            String status,
            String movedAt) {
    }
}
