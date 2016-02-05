# Add 'ip' column to pings

# --- !Ups

ALTER TABLE Pings ADD COLUMN ip VARCHAR(16) DEFAULT '0.0.0.0';

# --- !Downs

ALTER TABLE Pings DROP COLUMN ip;