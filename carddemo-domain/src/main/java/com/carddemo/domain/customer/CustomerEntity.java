package com.carddemo.domain.customer;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "first_name", length = 25)  private String firstName;
    @Column(name = "middle_name", length = 25) private String middleName;
    @Column(name = "last_name", length = 25)   private String lastName;
    @Column(name = "addr_line_1", length = 50) private String addrLine1;
    @Column(name = "addr_line_2", length = 50) private String addrLine2;
    @Column(name = "addr_line_3", length = 50) private String addrLine3;
    @Column(name = "addr_state_cd", length = 2)   private String addrStateCd;
    @Column(name = "addr_country_cd", length = 3) private String addrCountryCd;
    @Column(name = "addr_zip", length = 10)       private String addrZip;
    @Column(name = "phone_num_1", length = 15) private String phoneNum1;
    @Column(name = "phone_num_2", length = 15) private String phoneNum2;
    @Column(name = "ssn", length = 9)          private String ssn;
    @Column(name = "govt_issued_id", length = 20) private String govtIssuedId;
    @Column(name = "dob")                      private LocalDate dob;
    @Column(name = "eft_account_id", length = 10) private String eftAccountId;
    @Column(name = "pri_card_holder_ind", length = 1) private String priCardHolderInd;
    @Column(name = "fico_credit_score")        private Short ficoCreditScore;

    public Long getCustId() { return custId; }
    public void setCustId(Long v) { this.custId = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String v) { this.middleName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getAddrLine1() { return addrLine1; }
    public void setAddrLine1(String v) { this.addrLine1 = v; }
    public String getAddrLine2() { return addrLine2; }
    public void setAddrLine2(String v) { this.addrLine2 = v; }
    public String getAddrLine3() { return addrLine3; }
    public void setAddrLine3(String v) { this.addrLine3 = v; }
    public String getAddrStateCd() { return addrStateCd; }
    public void setAddrStateCd(String v) { this.addrStateCd = v; }
    public String getAddrCountryCd() { return addrCountryCd; }
    public void setAddrCountryCd(String v) { this.addrCountryCd = v; }
    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String v) { this.addrZip = v; }
    public String getPhoneNum1() { return phoneNum1; }
    public void setPhoneNum1(String v) { this.phoneNum1 = v; }
    public String getPhoneNum2() { return phoneNum2; }
    public void setPhoneNum2(String v) { this.phoneNum2 = v; }
    public String getSsn() { return ssn; }
    public void setSsn(String v) { this.ssn = v; }
    public String getGovtIssuedId() { return govtIssuedId; }
    public void setGovtIssuedId(String v) { this.govtIssuedId = v; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate v) { this.dob = v; }
    public String getEftAccountId() { return eftAccountId; }
    public void setEftAccountId(String v) { this.eftAccountId = v; }
    public String getPriCardHolderInd() { return priCardHolderInd; }
    public void setPriCardHolderInd(String v) { this.priCardHolderInd = v; }
    public Short getFicoCreditScore() { return ficoCreditScore; }
    public void setFicoCreditScore(Short v) { this.ficoCreditScore = v; }
}
