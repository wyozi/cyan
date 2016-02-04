# Add default responses to products

# --- !Ups

ALTER TABLE Products ADD COLUMN defaultresp_unreg INT DEFAULT NULL;
ALTER TABLE Products ADD COLUMN defaultresp_reg INT DEFAULT NULL;

ALTER TABLE Products ADD FOREIGN KEY (defaultresp_unreg) REFERENCES Responses(id);
ALTER TABLE Products ADD FOREIGN KEY (defaultresp_reg) REFERENCES Responses(id);

# --- !Downs

ALTER TABLE Products DROP COLUMN defaultresp_unreg;
ALTER TABLE Products DROP COLUMN defaultresp_reg;