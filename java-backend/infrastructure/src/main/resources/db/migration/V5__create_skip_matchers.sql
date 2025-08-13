CREATE TABLE automatic_skip (
    user_owner VARCHAR(255) NOT NULL,
    skip_matcher VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_owner, skip_matcher)
); 