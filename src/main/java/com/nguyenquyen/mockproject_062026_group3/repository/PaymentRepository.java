package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 1. Dùng khi xem chi tiết 1 Hóa đơn: Kéo tất cả các lần trả góp/thanh toán của hóa đơn đó lên
    List<Payment> findByInvoiceId(Long invoiceId);

    // 2. Dùng cho Bảng lịch sử thanh toán: Lấy tất cả thanh toán của 1 bệnh nhân (Thông qua mối quan hệ từ bảng Invoice) + CÓ PHÂN TRANG
    // Dấu gạch dưới (_) mang ý nghĩa: "Vào bảng Invoice, tìm cột ResidentId"
    Page<Payment> findByInvoice_ResidentId(Long residentId, Pageable pageable);
}

