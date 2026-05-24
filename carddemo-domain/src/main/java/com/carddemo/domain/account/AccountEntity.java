package com.carddemo.domain.account;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "active_status", length = 1, nullable = false) private String activeStatus;
    @Column(name = "current_balance", nullable = false)   private BigDecimal currentBalance;
    @Column(name = "credit_limit", nullable = false)      private BigDecimal creditLimit;
    @Column(name = "cash_credit_limit", nullable = false) private BigDecimal cashCreditLimit;
    @Column(name = "open_date")        private LocalDate openDate;
    @Column(name = "expiration_date")  private LocalDate expirationDate;
    @Column(name = "reissue_date")     private LocalDate reissueDate;
    @Column(name = "curr_cyc_credit", nullable = false)   private BigDecimal currCycCredit;
    @Column(name = "curr_cyc_debit", nullable = false)    private BigDecimal currCycDebit;
    @Column(name = "addr_zip", length = 10)  private String addrZip;
    @Column(name = "group_id", length = 10)  private String groupId;

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String v) { this.activeStatus = v; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal v) { this.currentBalance = v; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal v) { this.creditLimit = v; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal v) { this.cashCreditLimit = v; }
    public LocalDate getOpenDate() { return openDate; }
    public void setOpenDate(LocalDate v) { this.openDate = v; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate v) { this.expirationDate = v; }
    public LocalDate getReissueDate() { return reissueDate; }
    public void setReissueDate(LocalDate v) { this.reissueDate = v; }
    public BigDecimal getCurrCycCredit() { return currCycCredit; }
    public void setCurrCycCredit(BigDecimal v) { this.currCycCredit = v; }
    public BigDecimal getCurrCycDebit() { return currCycDebit; }
    public void setCurrCycDebit(BigDecimal v) { this.currCycDebit = v; }
    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String v) { this.addrZip = v; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String v) { this.groupId = v; }
}
