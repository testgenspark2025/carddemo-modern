-- CVTRA02Y DIS-GROUP-RECORD (RECLN 50) -> disclosure_groups
CREATE TABLE disclosure_groups (
    group_id        VARCHAR(10) NOT NULL,
    tran_type_cd    VARCHAR(2)  NOT NULL,
    tran_cat_cd     INT         NOT NULL,
    interest_rate   NUMERIC(7,2) NOT NULL,
    PRIMARY KEY (group_id, tran_type_cd, tran_cat_cd)
);

-- CVTRA01Y TRAN-CAT-BAL-RECORD (RECLN 50) -> transaction_category_balance
CREATE TABLE transaction_category_balance (
    acct_id        BIGINT        NOT NULL,
    tran_type_cd   VARCHAR(2)    NOT NULL,
    tran_cat_cd    INT           NOT NULL,
    balance        NUMERIC(12,2) NOT NULL,
    PRIMARY KEY (acct_id, tran_type_cd, tran_cat_cd),
    FOREIGN KEY (acct_id) REFERENCES accounts(acct_id)
);
CREATE INDEX idx_tcatbal_acct ON transaction_category_balance(acct_id);

-- CVTRA05Y TRAN-RECORD (RECLN 350) -> transactions
-- Output of CBACT04C interest-calc + everyday tx posting.
CREATE TABLE transactions (
    tran_id          VARCHAR(16)   PRIMARY KEY,
    tran_type_cd     VARCHAR(2)    NOT NULL,
    tran_cat_cd      INT           NOT NULL,
    tran_source      VARCHAR(10),
    tran_desc        VARCHAR(100),
    tran_amount      NUMERIC(12,2) NOT NULL,
    merchant_id      BIGINT,
    merchant_name    VARCHAR(50),
    merchant_city    VARCHAR(50),
    merchant_zip     VARCHAR(10),
    card_num         VARCHAR(16),
    acct_id          BIGINT        NOT NULL REFERENCES accounts(acct_id),
    tran_date        DATE          NOT NULL,
    orig_ts          TIMESTAMPTZ,
    proc_ts          TIMESTAMPTZ
);
CREATE INDEX idx_tx_acct_date ON transactions(acct_id, tran_date);
