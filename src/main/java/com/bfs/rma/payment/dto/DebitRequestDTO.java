package com.bfs.rma.payment.dto;

import lombok.Data;

@Data
public class DebitRequestDTO {

    private String bfs_remitterOtp;
    private String bfs_bfsTxnId;

}
