package com.bfs.rma.bill.repository;


import com.bfs.rma.bill.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
}



