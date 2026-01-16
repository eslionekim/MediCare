package com.example.erp.Admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AdminInventoryService {

    @PersistenceContext
    private EntityManager entityManager;

    public InventoryMonitoringData loadInventoryMonitoring(
            String itemType,
            String warehouseCode,
            String status,
            LocalDate startDate,
            LocalDate endDate) {
        String type = normalize(itemType);
        String warehouse = normalize(warehouseCode);
        String statusFilter = normalize(status);
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay().minusNanos(1) : null;

        List<InventoryRow> rows = queryInventoryRows(type, warehouse, statusFilter, start, end);
        InventoryRow selected = rows.isEmpty() ? null : rows.get(0);
        List<LotRow> lots = selected == null ? List.of() : queryLots(selected.itemCode());
        List<MoveRow> moves = selected == null ? List.of() : queryMoves(selected.itemCode(), start, end);
        List<LockinRow> lockinRows = queryLockinRows();
        List<Option> warehouses = queryWarehouseOptions();
        return new InventoryMonitoringData(rows, lots, moves, lockinRows, warehouses, selected);
    }

    private List<InventoryRow> queryInventoryRows(
            String itemType,
            String warehouseCode,
            String status,
            LocalDateTime start,
            LocalDateTime end) {
        StringBuilder sql = new StringBuilder("""
                select i.item_code,
                       i.name,
                       i.item_type,
                       coalesce(sum(s.quantity),0) as qty,
                       i.safety_stock,
                       coalesce(w.name,'') as warehouse_name,
                       (
                         select max(sm.moved_at)
                         from stock_move sm
                         join stock_move_item smi on sm.stock_move_id = smi.stock_move_id
                         where smi.item_code = i.item_code
                       ) as last_move
                from item i
                left join stock s on s.item_code = i.item_code
                left join warehouse w on s.warehouse_code = w.warehouse_code
                where 1=1
                """);
        if (itemType != null) {
            sql.append(" and i.item_type = :itemType");
        }
        if (warehouseCode != null) {
            sql.append(" and s.warehouse_code = :warehouseCode");
        }
        sql.append(" group by i.item_code, i.name, i.item_type, i.safety_stock, w.name");
        sql.append(" order by i.name");
        Query query = entityManager.createNativeQuery(sql.toString());
        if (itemType != null) {
            query.setParameter("itemType", itemType);
        }
        if (warehouseCode != null) {
            query.setParameter("warehouseCode", warehouseCode);
        }
        List<Object[]> rows = query.getResultList();
        List<InventoryRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String itemCode = row[0] != null ? row[0].toString() : "";
            String name = row[1] != null ? row[1].toString() : "";
            String type = row[2] != null ? row[2].toString() : "";
            BigDecimal qty = row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString());
            BigDecimal safety = row[4] == null ? BigDecimal.ZERO : new BigDecimal(row[4].toString());
            String warehouseName = row[5] != null ? row[5].toString() : "";
            String lastMove = row[6] != null ? row[6].toString() : "";
            String statusLabel = resolveStatus(qty, safety);
            if (status != null) {
                boolean isLow = "LOW".equals(status);
                boolean isNormal = "NORMAL".equals(status);
                if (isLow && !"부족".equals(statusLabel)) {
                    continue;
                }
                if (isNormal && !"정상".equals(statusLabel)) {
                    continue;
                }
            }
            results.add(new InventoryRow(itemCode, name, type, qty, safety, warehouseName, lastMove, statusLabel));
        }
        return results;
    }

    private List<LotRow> queryLots(String itemCode) {
        Query query = entityManager.createNativeQuery("""
                select lot_code, expiry_date, quantity
                from stock
                where item_code = :itemCode
                order by expiry_date
                """);
        query.setParameter("itemCode", itemCode);
        List<Object[]> rows = query.getResultList();
        List<LotRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String lot = row[0] != null ? row[0].toString() : "";
            String expiry = row[1] != null ? row[1].toString() : "";
            String qty = row[2] != null ? row[2].toString() : "0";
            results.add(new LotRow(lot, expiry, qty));
        }
        return results;
    }

    private List<MoveRow> queryMoves(String itemCode, LocalDateTime start, LocalDateTime end) {
        StringBuilder sql = new StringBuilder("""
                select sm.moved_at, smi.quantity, sm.move_type, sm.status_code
                from stock_move_item smi
                join stock_move sm on smi.stock_move_id = sm.stock_move_id
                where smi.item_code = :itemCode
                """);
        if (start != null && end != null) {
            sql.append(" and sm.moved_at between :start and :end");
        }
        sql.append(" order by sm.moved_at desc limit 10");
        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("itemCode", itemCode);
        if (start != null && end != null) {
            query.setParameter("start", start);
            query.setParameter("end", end);
        }
        List<Object[]> rows = query.getResultList();
        List<MoveRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String movedAt = row[0] != null ? row[0].toString() : "";
            String qty = row[1] != null ? row[1].toString() : "0";
            String moveType = row[2] != null ? row[2].toString() : "";
            String status = row[3] != null ? row[3].toString() : "";
            results.add(new MoveRow(movedAt, qty, moveType, status));
        }
        return results;
    }

    private List<LockinRow> queryLockinRows() {
        Query query = entityManager.createNativeQuery("""
                select sm.stock_move_id, sm.move_type, sm.status_code, sm.moved_at
                from stock_move sm
                where sm.status_code in ('SM_DRAFT','SM_DONE','SM_CANCELED')
                order by sm.moved_at desc
                limit 10
                """);
        List<Object[]> rows = query.getResultList();
        List<LockinRow> results = new ArrayList<>();
        for (Object[] row : rows) {
            String id = row[0] != null ? row[0].toString() : "";
            String moveType = row[1] != null ? row[1].toString() : "";
            String status = row[2] != null ? row[2].toString() : "";
            String movedAt = row[3] != null ? row[3].toString() : "";
            results.add(new LockinRow(id, moveType, status, movedAt));
        }
        return results;
    }

    private List<Option> queryWarehouseOptions() {
        Query query = entityManager.createNativeQuery("""
                select warehouse_code, name
                from warehouse
                where is_active = 1
                order by name
                """);
        List<Object[]> rows = query.getResultList();
        List<Option> results = new ArrayList<>();
        for (Object[] row : rows) {
            results.add(new Option(row[0].toString(), row[1].toString()));
        }
        return results;
    }

    private String resolveStatus(BigDecimal qty, BigDecimal safety) {
        if (safety == null || safety.compareTo(BigDecimal.ZERO) <= 0) {
            return "정상";
        }
        if (qty.compareTo(safety) <= 0) {
            return "부족";
        }
        return "정상";
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    public record InventoryMonitoringData(
            List<InventoryRow> inventoryRows,
            List<LotRow> lotRows,
            List<MoveRow> moveRows,
            List<LockinRow> lockinRows,
            List<Option> warehouseOptions,
            InventoryRow selectedRow) {
    }

    public record InventoryRow(
            String itemCode,
            String itemName,
            String itemType,
            BigDecimal quantity,
            BigDecimal safetyStock,
            String warehouseName,
            String lastMove,
            String status) {
    }

    public record LotRow(String lotCode, String expiryDate, String quantity) {
    }

    public record MoveRow(String movedAt, String quantity, String requestDept, String status) {
    }

    public record LockinRow(String moveId, String moveType, String status, String movedAt) {
    }

    public record Option(String code, String name) {
    }
}
