package com.bfs.rma.bill;

import com.bfs.rma.auth.model.AppUser;
import com.bfs.rma.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public InvoiceResponse generateInvoice(InvoiceRequest request) {
        AppUser user = getUserById(request.getUserId());
        List<InvoiceItem> items = mapToInvoiceItems(request);
        BigDecimal totalAmount = calculateTotalAmount(items);
        Invoice invoice = buildInvoice(request, user, items, totalAmount);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToInvoiceResponse(savedInvoice);
    }

    private AppUser getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private List<InvoiceItem> mapToInvoiceItems(InvoiceRequest request) {
        return request.getItems().stream()
                .map(item -> {
                    BigDecimal totalPrice = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return InvoiceItem.builder()
                            .description(item.getDescription())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(totalPrice)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalAmount(List<InvoiceItem> items) {
        return items.stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Invoice buildInvoice(InvoiceRequest request, AppUser user, List<InvoiceItem> items, BigDecimal totalAmount) {
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .recipientName(request.getRecipientName())
                .recipientEmail(request.getRecipientEmail())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(InvoiceStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .amount(totalAmount)
                .createdBy(user)
                .build();

        items.forEach(item -> item.setInvoice(invoice));
        invoice.setItems(items);

        return invoice;
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .recipientName(invoice.getRecipientName())
                .recipientEmail(invoice.getRecipientEmail())
                .description(invoice.getDescription())
                .dueDate(invoice.getDueDate())
                .amount(invoice.getAmount())
                .status(invoice.getStatus().name())
                .createdAt(invoice.getCreatedAt())
                .items(invoice.getItems().stream()
                        .map(i -> InvoiceItemResponse.builder()
                                .description(i.getDescription())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .totalPrice(i.getTotalPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    public InvoiceResponse getInvoiceById(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return mapToInvoiceResponse(invoice);
    }

}
