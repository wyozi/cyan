# Initial

# --- !Ups

CREATE TABLE Responses (
  id       SERIAL UNIQUE,

  name     VARCHAR(64) NOT NULL,
  response TEXT        NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE Products (
  id         SERIAL UNIQUE,
  short_name VARCHAR(16) UNIQUE,

  name       VARCHAR(255),

  PRIMARY KEY (id)
);

CREATE TABLE Pings (
  id          SERIAL UNIQUE,

  product     VARCHAR(255) NOT NULL REFERENCES Products (short_name),
  license     VARCHAR(255) NOT NULL,
  user_name   VARCHAR(64)  NOT NULL,
  date        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  response_id INT       DEFAULT NULL REFERENCES Responses (id),

  PRIMARY KEY (id)
);

CREATE TABLE PingResponses (
  id          SERIAL UNIQUE,

  product_id  INT,
  license     VARCHAR(255),
  user_name   VARCHAR(64),

  response_id INT REFERENCES Responses (id),

  PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ping_userlicprod ON PingResponses (product_id, license, user_name);

# --- !Downs

DROP TABLE Products;
DROP TABLE Pings;
DROP TABLE Responses;
DROP TABLE PingResponses;