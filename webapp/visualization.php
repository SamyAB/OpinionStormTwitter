<!DOCTYPE html>
<html>
<head>
  <title>Visualization</title>
  <meta charset = "utf-8"/>
  <link rel="stylesheet" type="text/css" href="webapp.css" />
  <link href='https://fonts.googleapis.com/css?family=Product+Sans' rel='stylesheet' type='text/css'>
</head>
<body>
  <?php
    if(!isset($_POST["motCles"]) || empty($_POST["motCles"])){
      echo "<p>
      Erreur : Veuillez introduire un mot clé sur la page <a href=\"test.php\">précédante</a>
      </p>";
    } else {
      //Écriture des mots clés dans le fichier keywords
      //ouverture du fichier en mode écriture (supprimer son contenu et réecrit dessus)
      $fichier = fopen("../keywords","w");
      //Écrit le le mot clé dans le fichier ouvert
      fputs($fichier,$_POST["motCles"]);
      //Ferme le fichier
      fclose($fichier);

      //Lance l'execution de la topologie storm
      //exec("storm jar ../target/topology_pfe-0.0.1-SNAPSHOT-jar-with-dependencies.jar dz.pfe.storm.OpinionTweetTopology > ouput_storm");
  ?>
  <header>
    <h3>Mot-clefs : <? echo $_POST["motCles"]; ?></h3>
    <nav>
      <ul>
        <li>
          <a href="#" class="tablinks" onclick="openViz(event, 'visualization1')">Visualization 1</a>
        </li>
        <li>
          <a href="#" class="tablinks" onclick="openViz(event, 'visualization2')">Visualization 2</a>
        </li>
        <li>
          <a href="#" class="tablinks" onclick="openViz(event, 'visualization3')">Visualization 3</a>
        </li>
        <li>
          <a href="#" class="tablinks" onclick="openViz(event, 'visualization4')">Visualization 4</a>
        </li>
      </ul>
    </nav>
  </header>

  <section id="visualization1" class="tabcontent">
    <h3>Visualization 1</h3>
    <p>
      Il y aura la première visualization ici
    </p>
  </section>
  <section id="visualization2" class="tabcontent">
    <h3>Visualization 2</h3>
    <p>
      Il y aura la seconde visualization ici
    </p>
  </section>
  <section id="visualization3" class="tabcontent">
    <h3>Visualization 3</h3>
    <p>
      Il y aura la troisième visualization ici
    </p>
  </section>
  <section id="visualization4" class="tabcontent">
    <h3>Visualization 4</h3>
    <p>
      Il y aura la quatrième visualization ici
    </p>
  </section>

  <?php
    }
  ?>
  <script>
  function openViz(evt, vizNumber){
    var i, tabcontent, tablinks;

    //Cacher les éléments de la class tabcontent
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    //Enelever tout les éléments de la classe tablinks de la classe active
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tabcontent.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    //Afficher l'onglet cliqué
    document.getElementById(vizNumber).style.display = "block";
    evt.currentTarget.className += " active";
  }
  </script>
</body>
</html>
