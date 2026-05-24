package com.carddemo.batch.interest;

import com.carddemo.domain.account.AccountEntity;
import com.carddemo.domain.account.AccountRepository;
import com.carddemo.domain.balance.TranCatBalanceEntity;
import com.carddemo.domain.balance.TranCatBalanceRepository;
import com.carddemo.domain.disclosure.DisclosureGroupEntity;
import com.carddemo.domain.disclosure.DisclosureGroupId;
import com.carddemo.domain.disclosure.DisclosureGroupRepository;
import com.carddemo.domain.transaction.TransactionEntity;
import com.carddemo.domain.transaction.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Modern reimplementation of CBACT04C.CBL.
 *
 * COBOL outer loop (paraphrased):
 *   for each TCATBAL row in sequential order:
 *       if account changes:
 *           if not first time: 1050-UPDATE-ACCOUNT      (REWRITE account)
 *           else: WS-FIRST-TIME := 'N'
 *           WS-TOTAL-INT := 0
 *           1100-GET-ACCT-DATA  (random read by acct-id)
 *           1110-GET-XREF-DATA  (random read by alt-idx acct-id)
 *       1200-GET-INTEREST-RATE  (random read DISCGRP by (group, type, cat))
 *       if rate != 0:
 *           1300-COMPUTE-INTEREST: monthly = (cat_balance * rate) / 1200
 *           WS-TOTAL-INT += monthly
 *           1300-B-WRITE-TX: append a Transaction row (type=01, cat=05, source=System)
 *           1400-COMPUTE-FEES  (no-op in CBACT04C - reserved)
 *   1050-UPDATE-ACCOUNT for the very last account
 */
@Component
public class InterestCalcTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(InterestCalcTasklet.class);
    private static final BigDecimal MONTHS_PER_YEAR_X_100 = new BigDecimal("1200");

    private final TranCatBalanceRepository tcatbals;
    private final DisclosureGroupRepository discgrps;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public InterestCalcTasklet(TranCatBalanceRepository tcatbals,
                               DisclosureGroupRepository discgrps,
                               AccountRepository accounts,
                               TransactionRepository transactions) {
        this.tcatbals = tcatbals;
        this.discgrps = discgrps;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext ctx) {
        LocalDate parmDate = LocalDate.parse(
                (String) ctx.getStepContext().getJobParameters().get("date"));

        var rows = tcatbals.findAllByOrderByAcctIdAscTranTypeCdAscTranCatCdAsc();

        Long currentAcctId = null;
        AccountEntity currentAccount = null;
        BigDecimal totalInterest = BigDecimal.ZERO.setScale(2);
        int tranSuffix = 0;
        int rowsProcessed = 0;
        int txWritten = 0;

        for (TranCatBalanceEntity row : rows) {
            rowsProcessed++;

            if (!Objects.equals(currentAcctId, row.getAcctId())) {
                if (currentAccount != null) {
                    finalizeAccount(currentAccount, totalInterest);
                }
                currentAcctId = row.getAcctId();
                currentAccount = accounts.findById(currentAcctId).orElse(null);
                totalInterest = BigDecimal.ZERO.setScale(2);
                if (currentAccount == null) {
                    log.warn("acct {} referenced by TCATBAL row but missing in accounts", currentAcctId);
                    continue;
                }
            }
            if (currentAccount == null || currentAccount.getGroupId() == null) {
                continue;  // mirrors COBOL fall-through when group-id is blank
            }

            BigDecimal rate = lookupRate(currentAccount.getGroupId(),
                                         row.getTranTypeCd(), row.getTranCatCd());
            if (rate.signum() != 0) {
                BigDecimal monthly = row.getBalance()
                        .multiply(rate)
                        .divide(MONTHS_PER_YEAR_X_100, 2, RoundingMode.HALF_UP);
                totalInterest = totalInterest.add(monthly);

                if (monthly.signum() != 0) {
                    tranSuffix++;
                    TransactionEntity t = new TransactionEntity();
                    t.setTranId(buildTranId(parmDate, tranSuffix));
                    t.setTranTypeCd("01");
                    t.setTranCatCd(5);
                    t.setTranSource("System");
                    t.setTranDesc("Monthly interest on acct " + currentAcctId
                                  + " cat " + row.getTranCatCd());
                    t.setTranAmount(monthly);
                    t.setAcctId(currentAcctId);
                    t.setTranDate(parmDate);
                    transactions.save(t);
                    txWritten++;
                }
            }
        }
        if (currentAccount != null) {
            finalizeAccount(currentAccount, totalInterest);
        }

        for (int i = 0; i < rowsProcessed; i++) contribution.incrementReadCount();
        contribution.incrementWriteCount(txWritten);
        log.info("Interest calc complete: processed {} TCATBAL rows, wrote {} transactions",
                 rowsProcessed, txWritten);
        return RepeatStatus.FINISHED;
    }

    private BigDecimal lookupRate(String groupId, String typeCd, Integer catCd) {
        DisclosureGroupEntity g = discgrps.findById(
                new DisclosureGroupId(groupId, typeCd, catCd)).orElse(null);
        if (g != null) return g.getInterestRate();
        // CBACT04C fallback: try 'DEFAULT' group when specific row not found.
        DisclosureGroupEntity d = discgrps.findById(
                new DisclosureGroupId("DEFAULT", typeCd, catCd)).orElse(null);
        return d != null ? d.getInterestRate() : BigDecimal.ZERO;
    }

    private void finalizeAccount(AccountEntity acct, BigDecimal totalInterest) {
        acct.setCurrentBalance(acct.getCurrentBalance().add(totalInterest));
        acct.setCurrCycCredit(BigDecimal.ZERO.setScale(2));
        acct.setCurrCycDebit(BigDecimal.ZERO.setScale(2));
        accounts.save(acct);
    }

    /** Mirrors CBACT04C 1300-B-WRITE-TX: STRING PARM-DATE + 6-digit suffix into TRAN-ID (16). */
    private static String buildTranId(LocalDate parmDate, int suffix) {
        return parmDate.toString() + String.format("%06d", suffix);
    }
}
