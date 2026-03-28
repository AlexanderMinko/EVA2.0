CREATE TABLE IF NOT EXISTS words (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(255),
    transcript VARCHAR(255),
    frequency INTEGER,
    date_created TIMESTAMP NOT NULL,
    last_modified TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS meaning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word_id BIGINT,
    target VARCHAR(255),
    transcript VARCHAR(255),
    topic VARCHAR(255),
    description VARCHAR(1024),
    part_of_speech VARCHAR(50),
    proficiency_level VARCHAR(10),
    meaning_source VARCHAR(50),
    learning_status VARCHAR(20),
    date_created TIMESTAMP NOT NULL,
    last_modified TIMESTAMP NOT NULL,
    CONSTRAINT fk_meaning_word FOREIGN KEY (word_id) REFERENCES words(id)
);

CREATE TABLE IF NOT EXISTS example (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meaning_id BIGINT,
    text VARCHAR(1024),
    CONSTRAINT fk_example_meaning FOREIGN KEY (meaning_id) REFERENCES meaning(id)
);
