# Convert 'product' column into a numeric id reference instead of the short name

# --- !Ups

-- Add numeric product id field
ALTER TABLE pings
  ADD COLUMN product_id INTEGER,
  ADD CONSTRAINT pings_product_id_fkey
  FOREIGN KEY (product_id)
  REFERENCES products(id);

-- Update product_id field using the existing product field
UPDATE pings
  SET product_id = prods.id
  FROM products prods
  WHERE pings.product = prods.short_name;

ALTER TABLE pings
  ALTER COLUMN product_id
  SET NOT NULL;

ALTER TABLE pings
  DROP COLUMN product;

# --- !Downs

ALTER TABLE pings
  ADD COLUMN product VARCHAR(255),
  ADD CONSTRAINT pings_product_fkey
  FOREIGN KEY (product)
  REFERENCES products(short_name);

-- Update product field using the existing product_id field
UPDATE pings
  SET product = prods.short_name
  FROM products prods
  WHERE pings.product_id = prods.id;

ALTER TABLE pings
  ALTER COLUMN product
  SET NOT NULL;

ALTER TABLE pings
  DROP COLUMN product_id;