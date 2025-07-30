package com.bfs.rma.bill.model;
import com.bfs.rma.auth.model.AppUser;
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
    private String status;

    private LocalDate dueDate;
    private LocalDateTime createdAt;

    @ManyToOne
    private AppUser createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items;
}
