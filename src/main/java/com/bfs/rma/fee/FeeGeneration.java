package com.bfs.rma.fee;

import com.bfs.rma.auth.model.AppUser;
import com.bfs.rma.payment.model.TransactionMaster;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "fee-details")
public class FeeGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String feeType;
    private BigDecimal amount;
    private String traineeType;
    @Enumerated(EnumType.STRING)
    private FeeStatus feeStatus;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    @ManyToOne
    @JoinColumn(name = "user")
    private AppUser user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private TransactionMaster transaction;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
