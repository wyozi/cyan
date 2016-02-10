# Initial

# --- !Ups
CREATE TABLE "responses" (
  "id"       SERIAL UNIQUE,

  "name"     VARCHAR(64) NOT NULL,
  "response" TEXT        NOT NULL,

  PRIMARY KEY ("id")
);

CREATE TABLE "products" (
  "id"         SERIAL UNIQUE,
  "short_name" VARCHAR(16) UNIQUE,

  "name"       VARCHAR(255),

  PRIMARY KEY ("id")
);

CREATE TABLE "pings" (
  "id"          SERIAL UNIQUE,

  "product"     VARCHAR(255) NOT NULL REFERENCES "products" ("short_name"),
  "license"     VARCHAR(255) NOT NULL,
  "user_name"   VARCHAR(64)  NOT NULL,
  "date"        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

  "response_id" INT         DEFAULT NULL REFERENCES "responses" ("id"),
  "ip"          VARCHAR(16) DEFAULT '0.0.0.0',

  PRIMARY KEY ("id")
);

CREATE TABLE "pingresponses" (
  "id"          SERIAL UNIQUE,

  "product_id"  INT,
  "license"     VARCHAR(255),
  "user_name"   VARCHAR(64),

  "response_id" INT REFERENCES "responses" ("id"),

  PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX ping_userlicprod ON "pingresponses" ("product_id", "license", "user_name");

CREATE TABLE "pingextras" (
  "ping_id" INT REFERENCES "pings" ("id") ON DELETE CASCADE,
  "key"     VARCHAR(16) NOT NULL,
  "value"   TEXT        NOT NULL,

  PRIMARY KEY ("ping_id", "key")
);
CREATE INDEX pingextra_id ON "pingextras" ("ping_id");

# --- !Downs

DROP TABLE "products";
DROP TABLE "pings";
DROP TABLE "responses";
DROP TABLE "pingresponses";
DROP TABLE "pingextras";