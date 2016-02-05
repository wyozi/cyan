# Initial

# --- !Ups

CREATE TABLE Responses(
  id SERIAL,

  name VARCHAR(64) NOT NULL,
  response VARCHAR(MAX) NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE Products(
  id SERIAL,

  name VARCHAR(255),
  shortName VARCHAR(16),

  defaultresp_unreg INT DEFAULT NULL REFERENCES Responses(id),
  defaultresp_reg INT DEFAULT NULL REFERENCES Responses(id),

  PRIMARY KEY (id)
);

CREATE TABLE Pings(
  id SERIAL,

  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  userId VARCHAR(64) NOT NULL,
  licenseId VARCHAR(255) NOT NULL,
  product VARCHAR(255) NOT NULL REFERENCES Products(shortName),

  responseId INT DEFAULT NULL REFERENCES Responses(id),

  PRIMARY KEY (id),
);

CREATE TABLE PingResponses(
  id SERIAL,

  userId VARCHAR(64),
  licenseId VARCHAR(255),
  productId INT,

  response INT REFERENCES Responses(id),

  PRIMARY KEY (id),
);
CREATE UNIQUE INDEX ping_userlicprod ON PingResponses (userId, licenseId, productId);

# --- !Downs

DROP TABLE Products;
DROP TABLE Pings;
DROP TABLE Responses;
DROP TABLE PingResponses;