package com.bfs.rma.bill;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.generateInvoice(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Integer id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }
}
