package com.bfs.rma.bill.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceRequest {
    private Integer userId;
    private String recipientName;
    private String recipientEmail;
    private String description;
    private LocalDate dueDate;
    private List<InvoiceItemRequest> items;
}
