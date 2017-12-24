# Add 'color' field to responses

# --- !Ups
CREATE TYPE notification_color
AS ENUM ('primary', 'secondary', 'success', 'danger', 'warning', 'info', 'light', 'dark');

ALTER TABLE responses
ADD COLUMN color notification_color
NOT NULL
DEFAULT 'light';

# --- !Downs

ALTER TABLE responses
DROP COLUMN color;

DROP TYPE notification_color;