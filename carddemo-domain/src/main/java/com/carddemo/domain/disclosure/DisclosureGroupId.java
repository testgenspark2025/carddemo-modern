package com.carddemo.domain.disclosure;

import java.io.Serializable;
import java.util.Objects;

public class DisclosureGroupId implements Serializable {
    private String groupId;
    private String tranTypeCd;
    private Integer tranCatCd;

    public DisclosureGroupId() {}
    public DisclosureGroupId(String groupId, String tranTypeCd, Integer tranCatCd) {
        this.groupId = groupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisclosureGroupId k)) return false;
        return Objects.equals(groupId, k.groupId)
            && Objects.equals(tranTypeCd, k.tranTypeCd)
            && Objects.equals(tranCatCd, k.tranCatCd);
    }
    @Override public int hashCode() { return Objects.hash(groupId, tranTypeCd, tranCatCd); }
}
