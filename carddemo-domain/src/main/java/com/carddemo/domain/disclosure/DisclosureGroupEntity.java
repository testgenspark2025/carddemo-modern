package com.carddemo.domain.disclosure;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroupEntity {

    @Id @Column(name = "group_id", length = 10) private String groupId;
    @Id @Column(name = "tran_type_cd", length = 2) private String tranTypeCd;
    @Id @Column(name = "tran_cat_cd") private Integer tranCatCd;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    public String getGroupId() { return groupId; }
    public void setGroupId(String v) { this.groupId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal v) { this.interestRate = v; }
}
