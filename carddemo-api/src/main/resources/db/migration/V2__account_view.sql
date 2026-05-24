-- CVCUS01Y CUSTOMER-RECORD (RECLN 500) -> customers
CREATE TABLE customers (
    cust_id              BIGINT       PRIMARY KEY,
    first_name           VARCHAR(25)  NOT NULL,
    middle_name          VARCHAR(25),
    last_name            VARCHAR(25)  NOT NULL,
    addr_line_1          VARCHAR(50),
    addr_line_2          VARCHAR(50),
    addr_line_3          VARCHAR(50),
    addr_state_cd        VARCHAR(2),
    addr_country_cd      VARCHAR(3),
    addr_zip             VARCHAR(10),
    phone_num_1          VARCHAR(15),
    phone_num_2          VARCHAR(15),
    ssn                  VARCHAR(9),
    govt_issued_id       VARCHAR(20),
    dob                  DATE,
    eft_account_id       VARCHAR(10),
    pri_card_holder_ind  VARCHAR(1),
    fico_credit_score    SMALLINT
);

-- CVACT01Y ACCOUNT-RECORD (RECLN 300) -> accounts
CREATE TABLE accounts (
    acct_id              BIGINT          PRIMARY KEY,
    active_status        VARCHAR(1)      NOT NULL,
    current_balance      NUMERIC(12,2)   NOT NULL,
    credit_limit         NUMERIC(12,2)   NOT NULL,
    cash_credit_limit    NUMERIC(12,2)   NOT NULL,
    open_date            DATE,
    expiration_date      DATE,
    reissue_date         DATE,
    curr_cyc_credit      NUMERIC(12,2)   NOT NULL DEFAULT 0,
    curr_cyc_debit       NUMERIC(12,2)   NOT NULL DEFAULT 0,
    addr_zip             VARCHAR(10),
    group_id             VARCHAR(10)
);

-- CVACT03Y CARD-XREF (RECLN 50/36) -> card_xref
CREATE TABLE card_xref (
    card_num   VARCHAR(16)  PRIMARY KEY,
    cust_id    BIGINT       NOT NULL REFERENCES customers(cust_id),
    acct_id    BIGINT       NOT NULL REFERENCES accounts(acct_id)
);
CREATE INDEX idx_xref_acct ON card_xref(acct_id);   -- replaces CXACAIX alternate index
CREATE INDEX idx_xref_cust ON card_xref(cust_id);
