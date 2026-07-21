package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // 1. Dùng cho cái Dashboard: Lấy tất cả hóa đơn của 1 bệnh nhân (bỏ qua hóa đơn đã bị xóa)
    List<Invoice> findByResidentIdAndIsDeletedFalse(Long residentId);

    // 2. Dùng cho Bảng danh sách: Lấy tất cả hóa đơn của 1 bệnh nhân, nhưng CÓ PHÂN TRANG
    Page<Invoice> findByResidentIdAndIsDeletedFalse(Long residentId, Pageable pageable);

    // 3. Dùng cho Bảng danh sách: Lọc theo cả Bệnh nhân + Trạng thái (VD: chỉ lấy DRAFT) + CÓ PHÂN TRANG
    Page<Invoice> findByResidentIdAndStatusAndIsDeletedFalse(Long residentId, String status, Pageable pageable);
}

