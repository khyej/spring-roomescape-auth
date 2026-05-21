-- schema.sql
DROP TABLE IF EXISTS "user";
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS reservation_time;
DROP TABLE IF EXISTS theme;
DROP TABLE IF EXISTS store;

CREATE TABLE IF NOT EXISTS store
(
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "user"
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER',
    store_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE IF NOT EXISTS reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    store_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    UNIQUE (start_at, store_id)
);

CREATE TABLE IF NOT EXISTS theme
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    thumbnail   VARCHAR(255) NOT NULL,
    store_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE IF NOT EXISTS reservation
(
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(255) NOT NULL,
    theme_id  BIGINT       NOT NULL,
    date      DATE         NOT NULL,
    time_id   BIGINT       NOT NULL,
    store_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    FOREIGN KEY (store_id) REFERENCES store (id),
    UNIQUE (theme_id, date, time_id)
);

CREATE INDEX idx_reservation_date ON reservation (date);
CREATE INDEX idx_reservation_user_name ON reservation (user_name);
CREATE INDEX idx_reservation_store_id ON reservation (store_id);
