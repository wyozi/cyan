# create a table for extra ping data

# --- !Ups

CREATE TABLE "PingExtras" (
  ping_id INT REFERENCES "Pings"(id) ON DELETE CASCADE,
  key     VARCHAR(16) NOT NULL,
  value   TEXT NOT NULL,

  PRIMARY KEY(ping_id, key)
);
CREATE INDEX pingextra_id ON "PingExtras"(ping_id);

# --- !Downs

DROP TABLE "PingExtras";