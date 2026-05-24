package com.carddemo.account;

import com.carddemo.domain.account.AccountEntity;
import com.carddemo.domain.account.AccountRepository;
import com.carddemo.domain.balance.TranCatBalanceEntity;
import com.carddemo.domain.balance.TranCatBalanceRepository;
import com.carddemo.domain.card.CardXrefEntity;
import com.carddemo.domain.card.CardXrefRepository;
import com.carddemo.domain.customer.CustomerEntity;
import com.carddemo.domain.customer.CustomerRepository;
import com.carddemo.domain.disclosure.DisclosureGroupEntity;
import com.carddemo.domain.disclosure.DisclosureGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Loads CardDemo ASCII fixed-width sample files into PostgreSQL.
 *
 * IMPORTANT - signed numerics ("zoned decimal with sign overpunch"):
 *   PIC S9(10)V99 fields render the last digit with the sign encoded in its high nibble.
 *   ASCII overpunch table for positive: '{'=0,'A'-'I' = 1-9
 *                          for negative: '}'=0,'J'-'R' = 1-9
 *   The implicit V99 means the last two digits are the cents (no literal decimal point).
 *   Example:  "00000001940{"  -> digits 000000019400 -> 19400 cents -> +194.00
 */
@Component
public class CardDemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CardDemoDataLoader.class);

    private final ResourceLoader resourceLoader;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final CardXrefRepository xrefs;
    private final DisclosureGroupRepository discgrps;
    private final TranCatBalanceRepository tcatbals;

    @Value("${carddemo.data.acct:file:/home/factory-user/repos/carddemo/app/data/ASCII/acctdata.txt}")
    private String acctPath;
    @Value("${carddemo.data.cust:file:/home/factory-user/repos/carddemo/app/data/ASCII/custdata.txt}")
    private String custPath;
    @Value("${carddemo.data.xref:file:/home/factory-user/repos/carddemo/app/data/ASCII/cardxref.txt}")
    private String xrefPath;
    @Value("${carddemo.data.discgrp:file:/home/factory-user/repos/carddemo/app/data/ASCII/discgrp.txt}")
    private String discgrpPath;
    @Value("${carddemo.data.tcatbal:file:/home/factory-user/repos/carddemo/app/data/ASCII/tcatbal.txt}")
    private String tcatbalPath;

    public CardDemoDataLoader(ResourceLoader rl,
                              CustomerRepository customers,
                              AccountRepository accounts,
                              CardXrefRepository xrefs,
                              DisclosureGroupRepository discgrps,
                              TranCatBalanceRepository tcatbals) {
        this.resourceLoader = rl;
        this.customers = customers;
        this.accounts = accounts;
        this.xrefs = xrefs;
        this.discgrps = discgrps;
        this.tcatbals = tcatbals;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        if (accounts.count() > 0) {
            log.info("CardDemo sample data already loaded - skipping");
            return;
        }
        int c = loadCustomers();
        int a = loadAccounts();
        int x = loadXref();
        int d = loadDiscGroups();
        int t = loadTcatBal();
        int s = seedSampleBalances();
        log.info("Loaded {} customers, {} accounts, {} card cross-refs, {} discgrps, {} tcatbal "
                 + "({} seeded with non-zero balances for batch demo)", c, a, x, d, t, s);
    }

    /**
     * The CardDemo sample fixture ships TCATBAL with all-zero balances. To make
     * the interest-calc batch produce visible output we deterministically seed a few
     * rows. Real production data would already carry running balances.
     */
    private int seedSampleBalances() {
        var all = tcatbals.findAll();
        int updated = 0;
        BigDecimal[] demoAmounts = {
                new BigDecimal("1000.00"),
                new BigDecimal("2500.50"),
                new BigDecimal("750.25")};
        for (int i = 0; i < Math.min(3, all.size()); i++) {
            all.get(i).setBalance(demoAmounts[i]);
            tcatbals.save(all.get(i));
            updated++;
        }
        return updated;
    }

    // ---------- customers (CVCUS01Y, RECLN 500) ----------
    private int loadCustomers() throws IOException {
        int count = 0;
        for (String line : readAll(custPath)) {
            if (line.length() < 332) continue;   // ignore short/blank
            CustomerEntity c = new CustomerEntity();
            c.setCustId(Long.parseLong(line.substring(0, 9)));
            c.setFirstName(stripField(line, 9, 34));
            c.setMiddleName(stripField(line, 34, 59));
            c.setLastName(stripField(line, 59, 84));
            c.setAddrLine1(stripField(line, 84, 134));
            c.setAddrLine2(stripField(line, 134, 184));
            c.setAddrLine3(stripField(line, 184, 234));
            c.setAddrStateCd(stripField(line, 234, 236));
            c.setAddrCountryCd(stripField(line, 236, 239));
            c.setAddrZip(stripField(line, 239, 249));
            c.setPhoneNum1(stripField(line, 249, 264));
            c.setPhoneNum2(stripField(line, 264, 279));
            c.setSsn(stripField(line, 279, 288));
            c.setGovtIssuedId(stripField(line, 288, 308));
            c.setDob(parseDate(stripField(line, 308, 318)));
            c.setEftAccountId(stripField(line, 318, 328));
            c.setPriCardHolderInd(stripField(line, 328, 329));
            c.setFicoCreditScore(Short.parseShort(stripField(line, 329, 332)));
            customers.save(c);
            count++;
        }
        return count;
    }

    // ---------- accounts (CVACT01Y, RECLN 300) ----------
    private int loadAccounts() throws IOException {
        int count = 0;
        for (String line : readAll(acctPath)) {
            if (line.length() < 122) continue;
            AccountEntity a = new AccountEntity();
            a.setAcctId(Long.parseLong(line.substring(0, 11)));
            a.setActiveStatus(line.substring(11, 12));
            a.setCurrentBalance(decodeS9V99(line.substring(12, 24)));
            a.setCreditLimit(decodeS9V99(line.substring(24, 36)));
            a.setCashCreditLimit(decodeS9V99(line.substring(36, 48)));
            a.setOpenDate(parseDate(line.substring(48, 58)));
            a.setExpirationDate(parseDate(line.substring(58, 68)));
            a.setReissueDate(parseDate(line.substring(68, 78)));
            a.setCurrCycCredit(decodeS9V99(line.substring(78, 90)));
            a.setCurrCycDebit(decodeS9V99(line.substring(90, 102)));
            // The sample acctdata.txt fixture stores the value that CBACT04C uses as
            // ACCT-GROUP-ID at the copybook's ACCT-ADDR-ZIP offset (and leaves ACCT-GROUP-ID
            // blank). We map the populated column into group_id so the batch can join
            // disclosure_groups; addr_zip falls back to the customer's zip if needed.
            a.setGroupId(stripField(line, 102, 112));
            a.setAddrZip(stripField(line, 112, 122));
            accounts.save(a);
            count++;
        }
        return count;
    }

    // ---------- disclosure_groups (CVTRA02Y, RECLN 50) ----------
    // Layout: GROUP-ID X(10) | TYPE-CD X(2) | CAT-CD 9(4) | INT-RATE S9(4)V99 (6) | FILLER
    private int loadDiscGroups() throws IOException {
        int count = 0;
        for (String line : readAll(discgrpPath)) {
            if (line.length() < 22) continue;
            DisclosureGroupEntity g = new DisclosureGroupEntity();
            g.setGroupId(line.substring(0, 10));
            g.setTranTypeCd(line.substring(10, 12));
            g.setTranCatCd(Integer.parseInt(line.substring(12, 16)));
            g.setInterestRate(decodeS9V99(line.substring(16, 22), 4));
            discgrps.save(g);
            count++;
        }
        return count;
    }

    // ---------- transaction_category_balance (CVTRA01Y, RECLN 50) ----------
    // Layout: ACCT-ID 9(11) | TYPE-CD X(2) | CAT-CD 9(4) | BAL S9(9)V99 (11) | FILLER
    private int loadTcatBal() throws IOException {
        int count = 0;
        for (String line : readAll(tcatbalPath)) {
            if (line.length() < 28) continue;
            TranCatBalanceEntity b = new TranCatBalanceEntity();
            b.setAcctId(Long.parseLong(line.substring(0, 11)));
            b.setTranTypeCd(line.substring(11, 13));
            b.setTranCatCd(Integer.parseInt(line.substring(13, 17)));
            b.setBalance(decodeS9V99(line.substring(17, 28), 9));
            tcatbals.save(b);
            count++;
        }
        return count;
    }

    // ---------- card_xref (CVACT03Y, RECLN 50 - file is 36 bytes wide) ----------
    private int loadXref() throws IOException {
        int count = 0;
        for (String line : readAll(xrefPath)) {
            if (line.length() < 36) continue;
            CardXrefEntity x = new CardXrefEntity();
            x.setCardNum(line.substring(0, 16));
            x.setCustId(Long.parseLong(line.substring(16, 25)));
            x.setAcctId(Long.parseLong(line.substring(25, 36)));
            xrefs.save(x);
            count++;
        }
        return count;
    }

    // ---------- helpers ----------
    private java.util.List<String> readAll(String path) throws IOException {
        Resource r = resourceLoader.getResource(path);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(r.getInputStream(), StandardCharsets.US_ASCII))) {
            return br.lines().toList();
        }
    }

    private static String stripField(String line, int from, int to) {
        if (from >= line.length()) return null;
        String s = line.substring(from, Math.min(to, line.length())).trim();
        return s.isEmpty() ? null : s;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank() || s.startsWith("0000")) return null;
        try { return LocalDate.parse(s); } catch (DateTimeParseException e) { return null; }
    }

    /** Decode 12-char zoned-decimal S9(10)V99 into BigDecimal. */
    static BigDecimal decodeS9V99(String raw) {
        return decodeS9V99(raw, 10);
    }

    /**
     * Decode zoned-decimal S9(integerDigits)V99 into BigDecimal.
     * Expected length = integerDigits + 2 (cents).
     * The last byte holds the last digit + sign per the overpunch table.
     */
    static BigDecimal decodeS9V99(String raw, int integerDigits) {
        int total = integerDigits + 2;
        if (raw == null || raw.length() != total) {
            throw new IllegalArgumentException(
                "expected " + total + " chars, got " + (raw == null ? "null" : raw.length())
                + ": '" + raw + "'");
        }
        char last = raw.charAt(total - 1);
        int signedDigit;
        int sign;
        if (last >= '0' && last <= '9') { signedDigit = last - '0'; sign = +1; }
        else if (last == '{') { signedDigit = 0; sign = +1; }
        else if (last >= 'A' && last <= 'I') { signedDigit = last - 'A' + 1; sign = +1; }
        else if (last == '}') { signedDigit = 0; sign = -1; }
        else if (last >= 'J' && last <= 'R') { signedDigit = last - 'J' + 1; sign = -1; }
        else throw new IllegalArgumentException("unknown overpunch char: '" + last + "'");
        String digits = raw.substring(0, total - 1) + signedDigit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(2);
        return sign < 0 ? value.negate() : value;
    }
}
