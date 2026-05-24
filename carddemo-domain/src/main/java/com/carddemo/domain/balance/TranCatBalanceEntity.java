package com.carddemo.domain.balance;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transaction_category_balance")
@IdClass(TranCatBalanceId.class)
public class TranCatBalanceEntity {

    @Id @Column(name = "acct_id") private Long acctId;
    @Id @Column(name = "tran_type_cd", length = 2) private String tranTypeCd;
    @Id @Column(name = "tran_cat_cd") private Integer tranCatCd;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal v) { this.balance = v; }
}
