drop table IF EXISTS users CASCADE;
drop table IF EXISTS items CASCADE;
drop table IF EXISTS bookings CASCADE;
drop table IF EXISTS comments CASCADE;

create TABLE IF NOT EXISTS users
(
    user_id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    email     VARCHAR(512) NOT NULL UNIQUE
);

create TABLE IF NOT EXISTS items
(
    item_id     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    item_name   VARCHAR(255)                      NOT NULL,
    description VARCHAR(512)                      NOT NULL,
    available   BOOLEAN                           NOT NULL,
    owner_id    BIGINT REFERENCES users (user_id) NOT NULL,
    request_id  BIGINT
);

create TABLE IF NOT EXISTS bookings
(
    booking_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE       NOT NULL,
    end_date   TIMESTAMP WITHOUT TIME ZONE       NOT NULL,
    item_id    BIGINT REFERENCES items (item_id) NOT NULL,
    booker_id  BIGINT REFERENCES users (user_id) NOT NULL,
    status     VARCHAR(25)                       NOT NULL
);

create TABLE IF NOT EXISTS comments
(
    comment_id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    comment_text VARCHAR(1024)                     NOT NULL,
    item_id      BIGINT REFERENCES items (item_id) NOT NULL,
    author_id    BIGINT REFERENCES users (user_id) NOT NULL,
    created_date TIMESTAMP WITHOUT TIME ZONE       NOT NULL
);