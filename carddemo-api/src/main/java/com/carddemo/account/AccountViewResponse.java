package com.carddemo.account;

import com.carddemo.domain.account.AccountEntity;
import com.carddemo.domain.card.CardXrefEntity;
import com.carddemo.domain.customer.CustomerEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountViewResponse(
        Long acctId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        LocalDate openDate,
        LocalDate expirationDate,
        LocalDate reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId,
        CustomerView customer,
        String cardNum) {

    public record CustomerView(
            Long custId,
            String firstName,
            String middleName,
            String lastName,
            String addrLine1,
            String addrLine2,
            String city,
            String stateCd,
            String countryCd,
            String zip,
            String phone1,
            String phone2,
            String ssn,
            String govtIssuedId,
            LocalDate dob,
            String eftAccountId,
            String priCardHolderInd,
            Short ficoCreditScore) {
    }

    public static AccountViewResponse from(AccountEntity a, CustomerEntity c, CardXrefEntity x) {
        return new AccountViewResponse(
                a.getAcctId(),
                a.getActiveStatus(),
                a.getCurrentBalance(),
                a.getCreditLimit(),
                a.getCashCreditLimit(),
                a.getOpenDate(),
                a.getExpirationDate(),
                a.getReissueDate(),
                a.getCurrCycCredit(),
                a.getCurrCycDebit(),
                trim(a.getGroupId()),
                new CustomerView(
                        c.getCustId(),
                        trim(c.getFirstName()),
                        trim(c.getMiddleName()),
                        trim(c.getLastName()),
                        trim(c.getAddrLine1()),
                        trim(c.getAddrLine2()),
                        trim(c.getAddrLine3()),
                        trim(c.getAddrStateCd()),
                        trim(c.getAddrCountryCd()),
                        trim(c.getAddrZip()),
                        trim(c.getPhoneNum1()),
                        trim(c.getPhoneNum2()),
                        formatSsn(c.getSsn()),
                        trim(c.getGovtIssuedId()),
                        c.getDob(),
                        trim(c.getEftAccountId()),
                        trim(c.getPriCardHolderInd()),
                        c.getFicoCreditScore()),
                trim(x.getCardNum()));
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }

    // Mirrors the STRING formatting in COACTVWC paragraph 1200: xxx-xx-xxxx
    private static String formatSsn(String raw) {
        if (raw == null || raw.length() != 9) return raw;
        return raw.substring(0,3) + "-" + raw.substring(3,5) + "-" + raw.substring(5,9);
    }
}
