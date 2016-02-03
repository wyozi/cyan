# Initial

# --- !Ups

CREATE TABLE Products(
  id SERIAL,

  name VARCHAR(255),
  shortName VARCHAR(16),

  PRIMARY KEY (id)
);

CREATE TABLE Pings(
  id SERIAL,

  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  userId VARCHAR(64) NOT NULL,
  licenseId VARCHAR(255) NOT NULL,
  product VARCHAR(255) NOT NULL,

  PRIMARY KEY (id),
  FOREIGN KEY (product) REFERENCES Products(shortName)
);

CREATE TABLE Responses(
  id SERIAL,

  name VARCHAR(64) NOT NULL,
  response VARCHAR(MAX) NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE PingResponses(
  id SERIAL,

  userId VARCHAR(64),
  licenseId VARCHAR(255),
  productId INT,

  response INT,

  PRIMARY KEY (id),
  FOREIGN KEY (response) REFERENCES Responses(id)
);
CREATE UNIQUE INDEX ping_userlicprod ON PingResponses (userId, licenseId, productId);

# --- !Downs

DROP TABLE Products;
DROP TABLE Pings;
DROP TABLE Responses;
DROP TABLE PingResponses;