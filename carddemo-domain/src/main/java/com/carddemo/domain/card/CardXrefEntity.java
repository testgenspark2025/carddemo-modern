package com.carddemo.domain.card;

import jakarta.persistence.*;

@Entity
@Table(name = "card_xref")
public class CardXrefEntity {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "cust_id", nullable = false) private Long custId;
    @Column(name = "acct_id", nullable = false) private Long acctId;

    public String getCardNum() { return cardNum; }
    public void setCardNum(String v) { this.cardNum = v; }
    public Long getCustId() { return custId; }
    public void setCustId(Long v) { this.custId = v; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
}
