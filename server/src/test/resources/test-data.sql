BEGIN TRANSACTION;
--ROLLBACK;

DROP TABLE IF EXISTS transfers, tenmo_user, account;

DROP SEQUENCE IF EXISTS seq_user_id, seq_account_id, seq_transfer_id;

-- Sequence to start user_id values at 1001 instead of 1
CREATE SEQUENCE seq_user_id
  INCREMENT BY 1
  START WITH 1001
  NO MAXVALUE;

CREATE TABLE tenmo_user (
	user_id int NOT NULL DEFAULT nextval('seq_user_id'),
	username varchar(50) NOT NULL,
	password_hash varchar(200) NOT NULL,
	CONSTRAINT PK_tenmo_user PRIMARY KEY (user_id),
	CONSTRAINT UQ_username UNIQUE (username)
);

-- Sequence to start account_id values at 2001 instead of 1
-- Note: Use similar sequences with unique starting values for additional tables
CREATE SEQUENCE seq_account_id
  INCREMENT BY 1
  START WITH 2001
  NO MAXVALUE;

CREATE TABLE account (
	account_id int NOT NULL DEFAULT nextval('seq_account_id'),
	user_id int NOT NULL,
	balance numeric(13, 2) NOT NULL,
	CONSTRAINT PK_account PRIMARY KEY (account_id),
	CONSTRAINT FK_account_tenmo_user FOREIGN KEY (user_id) REFERENCES tenmo_user (user_id)
);

CREATE SEQUENCE seq_transfer_id
  INCREMENT BY 1
  START WITH 3001
  NO MAXVALUE;

CREATE TABLE transfers (
    transfer_id int NOT NULL DEFAULT nextval('seq_transfer_id'),
    transfer_amount numeric(13,2) NOT NULL,
    account_from int NOT NULL,
    account_to int NOT NULL,
    status int NOT NULL,
    CONSTRAINT PK_transfers PRIMARY KEY (transfer_id),
    CONSTRAINT FK_transfers_account_from FOREIGN KEY (account_from) REFERENCES account (account_id),
    CONSTRAINT FK_transfers_account_to FOREIGN KEY (account_to) REFERENCES account (account_id)
);

--INSERT INTO tenmo_user (username, password_hash)
--VALUES ('bob', '$2a$10$G/MIQ7pUYupiVi72DxqHquxl73zfd7ZLNBoB2G6zUb.W16imI2.W2'),
--       ('user', '$2a$10$Ud8gSvRS4G1MijNgxXWzcexeXlVs4kWDOkjE7JFIkNLKEuE57JAEy');

INSERT INTO tenmo_user (user_id, username, password_hash)
VALUES (1003, 'TEST_USER1', 'pw1');

INSERT INTO tenmo_user (user_id, username, password_hash)
VALUES (1004, 'TEST_USER2', 'pw2');

INSERT INTO account (account_id, user_id, balance)
VALUES (2003, 1003, 1000);

INSERT INTO account (account_id, user_id, balance)
VALUES (2004, 1004, 1000);

INSERT INTO transfers (transfer_id, transfer_amount, account_from, account_to, status)
VALUES (3003, 20, 2004, 2003, 1);

INSERT INTO transfers (transfer_id, transfer_amount, account_from, account_to, status)
VALUES (3004, 50, 2003, 2004, 1);

INSERT INTO transfers (transfer_id, transfer_amount, account_from, account_to, status)
VALUES (3005, 100, 2003, 2004, 2);

INSERT INTO transfers (transfer_id, transfer_amount, account_from, account_to, status)
VALUES (3006, 12, 2004, 2003, 2);

COMMIT;