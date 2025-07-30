package com.bfs.rma.payment.dto;


import lombok.Data;

@Data
public class AuthoriseRequestDTO {

    private String bfs_paymentDesc;
    private String txnAmount;
}
