#Suivi des opinions sur les réseaux sociaux en temps-réel
## Suivi des opinions en temps-réel sur twitter

Dans le cadre du projet de fin d'étude de master
Le but de ce projet et de réaliser une application permettant de suivre les opinions par rapport à un mot clé en anglais, sur twitter en utilisant différents outils tels que Apache Storm, twitter4j, D3 … ect

## Parties du projet
Le projet est divisé en deux partie distinctes

* La topologie Apache Storm
* L'application web de visualisation

La topologie Apache Storm est utilisable séparément, et récupère les résultats dans une base de données.


###Langue
Français

###Language de programmation
* Java (apache storm, lettuce(redis), mysql)
* HTML5/CSS3
* JavaScript (D3, jQuery)
* PHP (phpredis(redis))
* SQL

##Utilisation
Il est primordial de posséder une distribution fonctionnelle de Apache Storm (testé sous la version 0.9.6).
Pour profiter de la visualisation de données avec D3, déployez l'application web sur un serveur. (Pas encore implémenté)
