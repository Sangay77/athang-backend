package com.bfs.rma.bill;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class InvoiceItemResponse {
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
