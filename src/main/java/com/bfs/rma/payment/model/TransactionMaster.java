package com.bfs.rma.payment.model;

import com.bfs.rma.bill.Invoice;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.stereotype.Component;


@Entity
@Component
@Data
public class TransactionMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String accountNumber;
    private String bfs_benfBankCode;
    private String bfs_benfId;
    private String bfs_benfTxnTime;
    private String bfs_orderNo;
    @Transient
    private String bfs_txnCurrency;
    private String bfs_txnAmount;
    @Transient
    private String bfs_paymentDesc;
    @Transient
    private String bfs_checkSum;
    @Transient
    private String bfs_remitterEmail;
    @Transient
    private String bfs_version;
    private String bfs_remitterBankId;
    @Column(name = "BFS_TXN_ID")
    private String bfs_bfsTxnId;
    private String bfs_remitterOtp;

    @OneToOne(mappedBy = "transaction")
    private Invoice invoice;

    @Override
    public String toString() {
        return "TransactionMaster{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", bfs_benfBankCode='" + bfs_benfBankCode + '\'' +
                ", bfs_benfId='" + bfs_benfId + '\'' +
                ", bfs_benfTxnTime='" + bfs_benfTxnTime + '\'' +
                ", bfs_orderNo='" + bfs_orderNo + '\'' +
                ", bfs_txnCurrency='" + bfs_txnCurrency + '\'' +
                ", bfs_txnAmount='" + bfs_txnAmount + '\'' +
                ", bfs_paymentDesc='" + bfs_paymentDesc + '\'' +
                ", bfs_checkSum='" + bfs_checkSum + '\'' +
                ", bfs_remitterEmail='" + bfs_remitterEmail + '\'' +
                ", bfs_version='" + bfs_version + '\'' +
                '}';
    }
}