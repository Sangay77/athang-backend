package com.bfs.rma.bill;
import com.bfs.rma.auth.model.AppUser;
import com.bfs.rma.payment.model.TransactionMaster;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String invoiceNumber;
    private String recipientName;
    private String recipientEmail;
    private BigDecimal amount;
    private String description;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private LocalDate dueDate;
    private LocalDateTime createdAt;

    @ManyToOne
    private AppUser createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id")
    private TransactionMaster transaction;
}
