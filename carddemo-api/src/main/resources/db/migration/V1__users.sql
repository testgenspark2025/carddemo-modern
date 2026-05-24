-- Modernization of USRSEC VSAM (copybook CSUSR01Y, 80-byte fixed records)
-- Original PICs:
--   SEC-USR-ID    PIC X(08)  -> primary key
--   SEC-USR-FNAME PIC X(20)
--   SEC-USR-LNAME PIC X(20)
--   SEC-USR-PWD   PIC X(08)  -> now BCrypt hash
--   SEC-USR-TYPE  PIC X(01)  -> 'A' admin, 'U' user
CREATE TABLE users (
    user_id       VARCHAR(8)   PRIMARY KEY,
    first_name    VARCHAR(20)  NOT NULL,
    last_name     VARCHAR(20)  NOT NULL,
    password_hash VARCHAR(72)  NOT NULL,
    user_type     VARCHAR(1)   NOT NULL CHECK (user_type IN ('A','U')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
