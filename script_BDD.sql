#En tant que root
CREATE DATABASE twitter_analytics DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE USER 'twitter_admin'@'localhost' IDENTIFIED BY 'le mot de passe ici';
#IMPORTANT : Ne pas oublier de modifier le mot de passe de twitter_admin
#À moins de vouloir le garder 'le mot de passe ici'… ah et bla 3yate.
GRANT ALL ON twitter_analytics.* TO 'twitter_admin'@'localhost' IDENTIFIED BY 'le mot de passe ici';

#En tant que que twitter_admin
USE twitter_analytics;

CREATE TABLE twitter_analytics.utilisateur (
  id VARCHAR(23) NOT NULL,
  nom_ecran VARCHAR(30) NOT NULL,
  date_entree DATETIME NOT NULL,
  nombre_followers INT NOT NULL,
  nombre_follows INT NOT NULL,
  nombre_tweets INT NOT NULL,
  nom VARCHAR(75) NOT NULL,
  PRIMARY KEY (id)
)
ENGINE=InnoDB;

CREATE TABLE twitter_analytics.status (
  id VARCHAR(23) NOT NULL,
  text_tweet VARCHAR(200) NOT NULL,
  score FLOAT,
  temps_tweet DATETIME NOT NULL,
  utilisateur_tweet VARCHAR(23) NOT NULL,
  nombre_favoris INT,
  nombre_retweet INT,
  ville_tweet VARCHAR(50),
  PRIMARY KEY ( id ),
  CONSTRAINT fk_utilisateur_status
    FOREIGN KEY (utilisateur_tweet)
    REFERENCES utilisateur(id)
)
ENGINE=InnoDB;

CREATE TABLE twitter_analytics.tweet_mot_cle(
  id_tweet VARCHAR(23) NOT NULL,
  mot_cle VARCHAR(200) NOT NULL,
  PRIMARY KEY (id_tweet,mot_cle),
  CONSTRAINT fk_status_tweet_mot_cle
    FOREIGN KEY (id_tweet)
    REFERENCES status(id)
)
ENGINE=InnoDB;
