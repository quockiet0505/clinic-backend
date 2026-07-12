package com.clinic.controller.medical;

import com.clinic.common.enums.PaymentMethod;
import com.clinic.dto.common.ApiResponse;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.InvoiceFilterRequest;
import com.clinic.dto.medical.InvoiceResponse;
import com.clinic.service.medical.InvoiceService;
import com.clinic.util.ResponseUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<PageResponse<InvoiceResponse>> getAll(
            @ModelAttribute InvoiceFilterRequest filter
    ) {
        return ResponseUtil.success(
                "Invoices fetched successfully",
                invoiceService.getAllInvoices(filter)
        ).getBody();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<List<InvoiceResponse>> getMyInvoices() {
        return ResponseUtil.success(
                "My invoices fetched successfully",
                invoiceService.getMyInvoices()
        ).getBody();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'PATIENT')")
    public ApiResponse<InvoiceResponse> getInvoiceDetails(
            @PathVariable Integer id
    ) {
        return ResponseUtil.success(
                "Invoice details fetched successfully",
                invoiceService.getInvoiceDetails(id)
        ).getBody();
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<InvoiceResponse> payInvoice(
            @PathVariable Integer id,
            @RequestParam PaymentMethod paymentMethod
    ) {
        return ResponseUtil.success(
                "Invoice payment completed successfully",
                invoiceService.payInvoice(id, paymentMethod)
        ).getBody();
    }

    @PutMapping("/{id}/request-transfer")
    @PreAuthorize("hasRole('PATIENT')")
    public ApiResponse<InvoiceResponse> requestTransferPayment(
            @PathVariable Integer id
    ) {
        return ResponseUtil.success(
                "Transfer payment request submitted",
                invoiceService.requestTransferPayment(id)
        ).getBody();
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<InvoiceResponse> confirmVerifyPayment(
            @PathVariable Integer id,
            @RequestParam boolean approve
    ) {
        return ResponseUtil.success(
                approve ? "Transfer payment verified successfully" : "Transfer payment verification rejected",
                invoiceService.confirmVerifyPayment(id, approve)
        ).getBody();
    }
}
