#En tant que root
CREATE DATABASE twitter_analytics DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE USER 'twitter_admin'@'localhost' IDENTIFIED BY 'le mot de passe ici';
#IMPORTANT : Ne pas oublier de modifier le mot de passe de twitter_admin
#À moins de vouloir le garder 'le mot de passe ici'… ah et bla 3yate.
GRANT ALL ON twitter_analytics.* TO 'twitter_admin'@'localhost' IDENTIFIED BY 'le mot de passe ici';

#En tant que que twitter_admin
USE twitter_analytics;

CREATE TABLE twitter_analytics.utilisateur (
  nom_ecran VARCHAR(30) NOT NULL,
  nombre_followers INT NOT NULL,
  nombre_follows INT NOT NULL,
  nombre_tweets INT NOT NULL,
  nom VARCHAR(75) NOT NULL,
  PRIMARY KEY (nom_ecran)
)
ENGINE=InnoDB;

CREATE TABLE twitter_analytics.status (
  id VARCHAR(23) NOT NULL,
  text_tweet VARCHAR(200) NOT NULL,
  score FLOAT,
  temps_tweet DATETIME NOT NULL,
  utilisateur_tweet VARCHAR(30) NOT NULL,
  nombre_favoris INT,
  nombre_retweet INT,
  ville_tweet VARCHAR(50),
  PRIMARY KEY ( id ),
  CONSTRAINT fk_utilisateur_status
    FOREIGN KEY (utilisateur_tweet)
    REFERENCES utilisateur(nom_ecran)
)
ENGINE=InnoDB;
