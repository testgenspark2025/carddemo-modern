package com.carddemo.domain.transaction;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2, nullable = false) private String tranTypeCd;
    @Column(name = "tran_cat_cd", nullable = false)               private Integer tranCatCd;
    @Column(name = "tran_source", length = 10)                    private String tranSource;
    @Column(name = "tran_desc", length = 100)                     private String tranDesc;
    @Column(name = "tran_amount", nullable = false)               private BigDecimal tranAmount;
    @Column(name = "merchant_id")     private Long   merchantId;
    @Column(name = "merchant_name")   private String merchantName;
    @Column(name = "merchant_city")   private String merchantCity;
    @Column(name = "merchant_zip")    private String merchantZip;
    @Column(name = "card_num", length = 16) private String cardNum;
    @Column(name = "acct_id", nullable = false) private Long acctId;
    @Column(name = "tran_date", nullable = false) private LocalDate tranDate;
    @Column(name = "orig_ts") private OffsetDateTime origTs;
    @Column(name = "proc_ts") private OffsetDateTime procTs;

    public String getTranId() { return tranId; }
    public void setTranId(String v) { this.tranId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public String getTranSource() { return tranSource; }
    public void setTranSource(String v) { this.tranSource = v; }
    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String v) { this.tranDesc = v; }
    public BigDecimal getTranAmount() { return tranAmount; }
    public void setTranAmount(BigDecimal v) { this.tranAmount = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public LocalDate getTranDate() { return tranDate; }
    public void setTranDate(LocalDate v) { this.tranDate = v; }
    public OffsetDateTime getOrigTs() { return origTs; }
    public void setOrigTs(OffsetDateTime v) { this.origTs = v; }
    public OffsetDateTime getProcTs() { return procTs; }
    public void setProcTs(OffsetDateTime v) { this.procTs = v; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long v) { this.merchantId = v; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String v) { this.merchantName = v; }
    public String getMerchantCity() { return merchantCity; }
    public void setMerchantCity(String v) { this.merchantCity = v; }
    public String getMerchantZip() { return merchantZip; }
    public void setMerchantZip(String v) { this.merchantZip = v; }
    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { this.cardNum = v; }
}
