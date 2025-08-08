package com.bfs.rma.bill;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceItemRequest {
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
}

