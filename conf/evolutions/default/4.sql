# Convert 'product' column into a numeric id reference instead of the short name

# --- !Ups

-- Add numeric product id field
ALTER TABLE pings
  ADD COLUMN product_id INTEGER,
  ADD CONSTRAINT pings_product_id_fkey
  FOREIGN KEY (product_id)
  REFERENCES products(id);

-- Set values to the right product
UPDATE pings
SET product_id = prods.id
FROM products prods
WHERE pings.product = prods.short_name;

ALTER TABLE pings
  ALTER COLUMN product_id
  SET NOT NULL;

ALTER TABLE pings
  ALTER COLUMN product
  DROP NOT NULL;

# --- !Downs

ALTER TABLE pings
DROP COLUMN product_id;

ALTER TABLE pings
ALTER COLUMN product
SET NOT NULL;