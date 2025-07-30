package com.bfs.rma.payment.repository;

import com.bfs.rma.payment.model.TransactionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionResponseRepository extends JpaRepository<TransactionResponse, Long> {
}

