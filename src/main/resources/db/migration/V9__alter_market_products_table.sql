ALTER TABLE market_gmail_account_products ADD datestamp BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE market_gmail_account_products ADD cost_history VARCHAR(2048) DEFAULT '' NOT NULL;
ALTER TABLE market_gmail_account_products ADD quantity_history VARCHAR(2048) DEFAULT '' NOT NULL;
ALTER TABLE market_gmail_account_products ADD datestamp_history VARCHAR(5096) DEFAULT '' NOT NULL;