# Add product configuration

# --- !Ups
CREATE TABLE "product_config" (
  "product_id" INT REFERENCES "products" ("id") ON DELETE CASCADE,
  "key"     VARCHAR(16) NOT NULL,
  "value"   TEXT        NOT NULL,

  PRIMARY KEY ("product_id", "key")
);

# --- !Downs

DROP TABLE "product_config";