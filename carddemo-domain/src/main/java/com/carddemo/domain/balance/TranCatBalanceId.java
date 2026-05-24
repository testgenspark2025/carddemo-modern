package com.carddemo.domain.balance;

import java.io.Serializable;
import java.util.Objects;

public class TranCatBalanceId implements Serializable {
    private Long acctId;
    private String tranTypeCd;
    private Integer tranCatCd;

    public TranCatBalanceId() {}
    public TranCatBalanceId(Long acctId, String tranTypeCd, Integer tranCatCd) {
        this.acctId = acctId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long v) { this.acctId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranCatBalanceId k)) return false;
        return Objects.equals(acctId, k.acctId)
            && Objects.equals(tranTypeCd, k.tranTypeCd)
            && Objects.equals(tranCatCd, k.tranCatCd);
    }
    @Override public int hashCode() { return Objects.hash(acctId, tranTypeCd, tranCatCd); }
}
