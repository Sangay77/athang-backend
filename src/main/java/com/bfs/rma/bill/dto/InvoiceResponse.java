package com.bfs.rma.bill.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceResponse {
    private Integer id;
    private String invoiceNumber;
    private String recipientName;
    private String recipientEmail;
    private BigDecimal amount;
    private String status;
    private LocalDate dueDate;
    private String description;
    private LocalDateTime createdAt;
    private List<InvoiceItemResponse> items;
}
