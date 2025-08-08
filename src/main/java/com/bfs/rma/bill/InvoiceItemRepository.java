package com.bfs.rma.bill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Integer> {

    List<InvoiceItem> findByInvoiceId(Integer invoiceId);
}
