-- liquibase formatted sql

-- changeset macaron:1708164682481-1
CREATE TABLE EPISODES (id_episode INT AUTO_INCREMENT NOT NULL, number INT NOT NULL, width INT NOT NULL, height INT NOT NULL, fps INT NOT NULL, title VARCHAR(255) NULL, season INT NOT NULL, CONSTRAINT PK_EPISODES PRIMARY KEY (id_episode));

-- changeset macaron:1708164682481-2
CREATE TABLE SCENES (id_scene INT AUTO_INCREMENT NOT NULL, `index` INT NOT NULL, start DOUBLE NOT NULL, end DOUBLE NOT NULL, episode INT NOT NULL, CONSTRAINT PK_SCENES PRIMARY KEY (id_scene));

-- changeset macaron:1708164682481-3
CREATE TABLE SEASONS (id_season INT AUTO_INCREMENT NOT NULL, number INT NOT NULL, series INT NOT NULL, CONSTRAINT PK_SEASONS PRIMARY KEY (id_season));

-- changeset macaron:1708164682481-4
CREATE TABLE SERIES (id_series INT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NULL, CONSTRAINT PK_SERIES PRIMARY KEY (id_series));

