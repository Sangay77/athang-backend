package com.bfs.rma.payment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Table(name = "transaction_responses")
@Data
public class TransactionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionMaster transaction;

    @Column(name = "msg_type", length = 5)
    private String msgType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "bfs_response_code", length = 50)
    private String bfs_responseCode;
    @Column(name = "bfs_response_type", length = 50)
    private String bfs_response_type;

    @Lob
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
