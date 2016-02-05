# Add response id column to Pings

# --- !Ups

ALTER TABLE Pings ADD COLUMN responseId INT DEFAULT NULL;

ALTER TABLE Pings ADD FOREIGN KEY (responseId) REFERENCES Responses(id);

# --- !Downs

ALTER TABLE Pings DROP COLUMN responseId;