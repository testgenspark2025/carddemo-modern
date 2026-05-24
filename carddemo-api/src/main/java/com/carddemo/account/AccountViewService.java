package com.carddemo.account;

import com.carddemo.domain.account.AccountEntity;
import com.carddemo.domain.account.AccountRepository;
import com.carddemo.domain.card.CardXrefEntity;
import com.carddemo.domain.card.CardXrefRepository;
import com.carddemo.domain.customer.CustomerEntity;
import com.carddemo.domain.customer.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountViewService {

    private final CardXrefRepository xrefs;
    private final AccountRepository accounts;
    private final CustomerRepository customers;

    public AccountViewService(CardXrefRepository xrefs,
                              AccountRepository accounts,
                              CustomerRepository customers) {
        this.xrefs = xrefs;
        this.accounts = accounts;
        this.customers = customers;
    }

    /**
     * Mirrors COACTVWC paragraph 9000-READ-ACCT: three chained lookups -
     *   1) CXACAIX alt-index by acct-id -> CDEMO-CUST-ID
     *   2) ACCTDAT by acct-id          -> ACCOUNT-RECORD
     *   3) CUSTDAT by cust-id          -> CUSTOMER-RECORD
     * Each miss produces a distinct error matching the legacy messages.
     */
    public AccountViewResponse view(long acctId) {
        CardXrefEntity xref = xrefs.findFirstByAcctId(acctId).orElseThrow(() ->
                new AccountNotFoundException(
                        "Did not find this account in account card xref file"));

        AccountEntity acct = accounts.findById(acctId).orElseThrow(() ->
                new AccountNotFoundException(
                        "Did not find this account in account master file"));

        CustomerEntity cust = customers.findById(xref.getCustId()).orElseThrow(() ->
                new AccountNotFoundException(
                        "Did not find associated customer in master file"));

        return AccountViewResponse.from(acct, cust, xref);
    }
}
