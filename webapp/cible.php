<!DOCTYPE html>
<html>
<head>
  <title>Page cible du formulaire</title>
  <meta charset="utf-8"/>
</head>
<body>
    <?php
      //Donc bon je dois créer un ficher douka et y mettre mot clé
      if(!isset($_POST["motCles"]) || empty($_POST["motCles"])){
        echo "<p>
        Veuillez introduire un mot clé sur la page <a href=\"test.php\">précédante</a>
        </p>";
      } else {
    ?>
    <p>
      Donc la nous avons un bon jolie mot clé "<? echo $_POST["motCles"]?>" et nous allons l'écrire dans un fichier text qui s'appelle keywords
    </p>
    <?php
        //Écriture des mots clés dans le fichier keywords
        //ouverture du fichier en mode écriture (supprimer son contenu et réecrit dessus)
        $fichier = fopen("../keywords","w");
        //Écrit le le mot clé dans le fichier ouvert
        fputs($fichier,$_POST["motCles"]);
        //Ferme le fichier
        fclose($fichier);

        //Lance l'execution de la topologie storm
        //exec("storm jar ../target/topology_pfe-0.0.1-SNAPSHOT-jar-with-dependencies.jar dz.pfe.storm.OpinionTweetTopology");
      }
    ?>
</body>
</html>
