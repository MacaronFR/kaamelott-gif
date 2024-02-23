-- liquibase formatted sql

-- changeset macaron:1708367733826-1
CREATE TABLE EPISODES (id_episode INT AUTO_INCREMENT NOT NULL, number INT NOT NULL, width INT NOT NULL, height INT NOT NULL, fps INT NOT NULL, title VARCHAR(255) NULL, season INT NOT NULL, CONSTRAINT PK_EPISODES PRIMARY KEY (id_episode));

-- changeset macaron:1708367733826-2
CREATE TABLE SCENES (id_scene INT AUTO_INCREMENT NOT NULL, `index` INT NOT NULL, start DOUBLE NOT NULL, end DOUBLE NOT NULL, episode INT NOT NULL, CONSTRAINT PK_SCENES PRIMARY KEY (id_scene));

-- changeset macaron:1708367733826-3
CREATE TABLE SEASONS (id_season INT AUTO_INCREMENT NOT NULL, number INT NOT NULL, series INT NOT NULL, CONSTRAINT PK_SEASONS PRIMARY KEY (id_season));

-- changeset macaron:1708367733826-4
CREATE TABLE SERIES (id_series INT AUTO_INCREMENT NOT NULL, name VARCHAR(255) NULL, CONSTRAINT PK_SERIES PRIMARY KEY (id_series));

-- liquibase formatted sql

-- changeset macaron:1708367880692-1
ALTER TABLE EPISODES ADD duration DOUBLE NOT NULL;

-- liquibase formatted sql

-- changeset macaron:1708462146685-1
CREATE INDEX EPISODES_SEASONS_id_season_fk ON EPISODES(season);

-- changeset macaron:1708462146685-2
CREATE INDEX SCENES_EPISODES_id_episode_fk ON SCENES(episode);

-- changeset macaron:1708462146685-3
CREATE INDEX SEASONS_SERIES_id_series_fk ON SEASONS(series);

-- changeset macaron:1708462146685-4
ALTER TABLE EPISODES ADD CONSTRAINT EPISODES_SEASONS_id_season_fk FOREIGN KEY (season) REFERENCES SEASONS (id_season) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset macaron:1708462146685-5
ALTER TABLE SCENES ADD CONSTRAINT SCENES_EPISODES_id_episode_fk FOREIGN KEY (episode) REFERENCES EPISODES (id_episode) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- changeset macaron:1708462146685-6
ALTER TABLE SEASONS ADD CONSTRAINT SEASONS_SERIES_id_series_fk FOREIGN KEY (series) REFERENCES SERIES (id_series) ON UPDATE RESTRICT ON DELETE RESTRICT;

-- liquibase formatted sql

-- changeset macaron:1708722685277-1
ALTER TABLE EPISODES ADD CONSTRAINT EPISODES_pk UNIQUE (season, number);

-- changeset macaron:1708722685277-2
ALTER TABLE SCENES ADD CONSTRAINT SCENES_pk UNIQUE (episode, `index`);

-- changeset macaron:1708722685277-3
ALTER TABLE SEASONS ADD CONSTRAINT SEASONS_pk UNIQUE (series, number);

-- changeset macaron:1708722685277-4
ALTER TABLE SERIES ADD CONSTRAINT SERIES_pk UNIQUE (name);

-- liquibase formatted sql

-- changeset macaron:1708723698740-1
CREATE TABLE GIFS (id_gif INT AUTO_INCREMENT NOT NULL, user VARCHAR(50) NOT NULL, timecode VARCHAR(20) NOT NULL, text TEXT NOT NULL, date datetime NOT NULL, scene INT DEFAULT NULL NULL, CONSTRAINT PK_GIFS PRIMARY KEY (id_gif));

-- changeset macaron:1708723698740-2
CREATE INDEX GIFS_SCENES_id_scene_fk ON GIFS(scene);

-- changeset macaron:1708723698740-3
ALTER TABLE GIFS ADD CONSTRAINT GIFS_SCENES_id_scene_fk FOREIGN KEY (scene) REFERENCES SCENES (id_scene) ON UPDATE CASCADE ON DELETE CASCADE;

