package com.clinic.service.medical;

import com.clinic.common.enums.InvoiceItemType;
import com.clinic.common.enums.InvoiceStatus;
import com.clinic.common.enums.PaymentMethod;
import com.clinic.dto.common.PageResponse;
import com.clinic.dto.medical.InvoiceFilterRequest;
import com.clinic.dto.medical.InvoiceResponse;
import com.clinic.dto.finance.SepayWebhookPayload;
import com.clinic.entity.auth.Account;
import com.clinic.entity.medical.Invoice;
import com.clinic.entity.medical.InvoiceItem;
import com.clinic.entity.medical.MedicalRecord;
import com.clinic.entity.medical.ServiceOrder;
import com.clinic.entity.patient.Patient;
import com.clinic.mapper.medical.InvoiceMapper;
import com.clinic.repository.auth.AccountRepository;
import com.clinic.repository.medical.InvoiceItemRepository;
import com.clinic.repository.medical.InvoiceRepository;
import com.clinic.repository.medical.MedicalRecordRepository;
import com.clinic.repository.medical.ServiceOrderRepository;
import com.clinic.repository.patient.PatientRepository;
import com.clinic.specification.medical.InvoiceSpecification;
import com.clinic.util.FilterUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final PatientRepository patientRepository;
    private final AccountRepository accountRepository;
    private final InvoiceMapper invoiceMapper;
    private final com.clinic.service.crm.NotificationService notificationService;

    @Transactional
    public Invoice generateInvoice(Integer recordId) {
        Optional<Invoice> existingOpt = invoiceRepository.findByMedicalRecord_RecordId(recordId);
        if (existingOpt.isPresent()) {
            return existingOpt.get();
        }

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        Invoice invoice = new Invoice();
        invoice.setMedicalRecord(record);
        invoice.setPatient(record.getPatient());
        invoice.setStatus(InvoiceStatus.UNPAID);

        List<InvoiceItem> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 1. Phí khám bệnh (Consultation Fee)
        BigDecimal consultationFee = record.getConsultationFinalFee();
        if (consultationFee == null) {
            consultationFee = new BigDecimal("300000"); // Giá mặc định nếu chưa cấu hình
        }

        InvoiceItem consultationItem = new InvoiceItem();
        consultationItem.setInvoice(invoice);
        consultationItem.setItemType(InvoiceItemType.CONSULTATION);
        consultationItem.setReferenceId(record.getRecordId());
        consultationItem.setDescription("Phí khám bệnh - Bác sĩ " + record.getMainDoctor().getFullName());
        consultationItem.setPriceAtTime(consultationFee);
        items.add(consultationItem);
        totalPrice = totalPrice.add(consultationFee);

        // 2. Phí dịch vụ chỉ định (Service Fees)
        List<ServiceOrder> serviceOrders = serviceOrderRepository.findByMedicalRecordId(recordId);
        if (serviceOrders != null) {
            for (ServiceOrder order : serviceOrders) {
                // Bỏ qua các chỉ định dịch vụ bị hủy hoặc đã hoàn thành thanh toán
                if (order.getStatus() == com.clinic.common.enums.ServiceOrderStatus.CANCELLED) {
                    continue;
                }
                BigDecimal serviceFee = order.getServiceFinalFee();
                if (serviceFee == null) {
                    serviceFee = order.getServiceOriginalFee() != null ? order.getServiceOriginalFee() : BigDecimal.ZERO;
                }

                InvoiceItem serviceItem = new InvoiceItem();
                serviceItem.setInvoice(invoice);
                serviceItem.setItemType(InvoiceItemType.SERVICE);
                serviceItem.setReferenceId(order.getOrderId());
                serviceItem.setDescription("Dịch vụ chỉ định: " + order.getService().getServiceName());
                serviceItem.setPriceAtTime(serviceFee);
                items.add(serviceItem);
                totalPrice = totalPrice.add(serviceFee);
            }
        }

        invoice.setTotalPrice(totalPrice);
        invoice.setItems(items);

        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getAllInvoices(InvoiceFilterRequest filter) {
        Specification<Invoice> spec = InvoiceSpecification.filterBy(filter);
        Pageable pageable = FilterUtils.buildPageable(filter);
        Page<Invoice> page = invoiceRepository.findAll(spec, pageable);
        return FilterUtils.buildPageResponse(page.map(invoiceMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceDetails(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse payInvoice(Integer invoiceId, PaymentMethod method) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(method);

        Invoice saved = invoiceRepository.save(invoice);

        // Notify Patient
        if (saved.getPatient() != null && saved.getPatient().getAccount() != null) {
            notificationService.createAndSendNotification(
                    saved.getPatient().getAccount().getAccountId(),
                    "Thanh toán thành công hóa đơn số #INV-" + saved.getInvoiceId() + " số tiền " + saved.getTotalPrice() + "đ. Cảm ơn quý khách!",
                    "SYSTEM"
            );
        }

        return invoiceMapper.toResponse(saved);
    }

    @Transactional
    public void processSepayWebhook(SepayWebhookPayload payload) {
        if (!"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("Webhook is not an incoming transfer. Ignored.");
            return;
        }

        String content = payload.getContent();
        if (content == null || content.isEmpty()) {
            log.warn("Webhook content is empty. Ignored.");
            return;
        }

        // Extract BILL{id}
        Pattern pattern = Pattern.compile("BILL(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (!matcher.find()) {
            log.warn("Webhook content does not contain a valid BILL reference. Content: {}", content);
            return;
        }

        try {
            Integer invoiceId = Integer.parseInt(matcher.group(1));
            Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);

            if (invoice == null) {
                log.warn("Invoice not found for ID: {}", invoiceId);
                return;
            }

            if (invoice.getStatus() == InvoiceStatus.PAID) {
                log.info("Invoice {} is already paid. Ignored.", invoiceId);
                return;
            }

            if (invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.REFUNDED) {
                log.warn("Invoice {} is cancelled or refunded. Cannot pay via transfer.", invoiceId);
                return;
            }

            BigDecimal transferAmount = BigDecimal.valueOf(payload.getTransferAmount());
            if (transferAmount.compareTo(invoice.getTotalPrice()) < 0) {
                log.warn("Transfer amount {} is less than invoice total {}. Cannot mark as PAID.", 
                    transferAmount, invoice.getTotalPrice());
                // Depending on business logic, we could create a partial payment, but for now we skip.
                return;
            }

            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaymentMethod(PaymentMethod.TRANSFER);
            Invoice saved = invoiceRepository.save(invoice);
            log.info("Invoice {} successfully marked as PAID via Sepay Webhook.", invoiceId);

            // Notify Patient
            if (saved.getPatient() != null && saved.getPatient().getAccount() != null) {
                notificationService.createAndSendNotification(
                        saved.getPatient().getAccount().getAccountId(),
                        "Thanh toán chuyển khoản thành công hóa đơn số #INV-" + saved.getInvoiceId() + " số tiền " + saved.getTotalPrice() + "đ. Cảm ơn quý khách!",
                        "SYSTEM"
                );
            }

        } catch (NumberFormatException e) {
            log.error("Failed to parse invoice ID from content: {}", content);
        }
    }

    @Transactional
    public InvoiceResponse requestTransferPayment(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PENDING_VERIFY);
        invoice.setPaymentMethod(PaymentMethod.TRANSFER);

        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse confirmVerifyPayment(Integer invoiceId, boolean approve) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.PENDING_VERIFY) {
            throw new RuntimeException("Invoice is not in PENDING_VERIFY status");
        }

        if (approve) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.UNPAID);
            invoice.setPaymentMethod(null);
        }

        Invoice saved = invoiceRepository.save(invoice);

        // Notify Patient
        if (saved.getPatient() != null && saved.getPatient().getAccount() != null) {
            if (approve) {
                notificationService.createAndSendNotification(
                        saved.getPatient().getAccount().getAccountId(),
                        "Xác nhận thanh toán thành công hóa đơn số #INV-" + saved.getInvoiceId() + " số tiền " + saved.getTotalPrice() + "đ. Cảm ơn quý khách!",
                        "SYSTEM"
                );
            } else {
                notificationService.createAndSendNotification(
                        saved.getPatient().getAccount().getAccountId(),
                        "Yêu cầu xác nhận thanh toán hóa đơn số #INV-" + saved.getInvoiceId() + " của bạn đã bị từ chối.",
                        "SYSTEM"
                );
            }
        }

        return invoiceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getMyInvoices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Patient patient = patientRepository.findByAccount_AccountId(account.getAccountId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return invoiceRepository.findByPatient_PatientIdOrderByCreatedAtDesc(patient.getPatientId())
                .stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }
}
