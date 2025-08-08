package com.bfs.rma.fee;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeRequest {

    private String feeType;
    private String traineeType;
    private BigDecimal amount;
}
