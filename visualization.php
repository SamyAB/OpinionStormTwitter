<!DOCTYPE html>
<html>
<head>
  <title>Twitter's Opinion : Visualisation</title>
  <meta charset = "utf-8"/>
  <link rel="stylesheet" type="text/css" href="webapp.css" />
  <link href='https://fonts.googleapis.com/css?family=Product+Sans' rel='stylesheet' type='text/css'>
  <style>
    .d3-tip {
      line-height: 1;
      padding: 12px;
      background: rgba(0, 0, 0, 0.8);
      color: white;
      border-radius: 2px;
    }
    .bar rect {
      fill: #5ea9dd;
      shape-rendering: crispEdges;
    }
    .bar rect:hover{
      fill: #808080

    }
    .axis path, .axis line {
      fill: none;
      stroke:black;
      shape-rendering: crispEdges;
    }
    .container{
      float:left;
    }
  </style>
</head>
<body>

  <?php include("header.php"); ?>

  <?php
    if(!isset($_POST["motCles"]) || empty($_POST["motCles"])){
      echo "<p>
      Erreur : Veuillez introduire un mot clé sur la page <a href=\"index.php\">précédante</a>
      </p>";
    } else {
      //Écriture des mots clés dans le fichier keywords
      //ouverture du fichier en mode écriture (supprimer son contenu et réecrit dessus)
      $fichier = fopen("keywords","w");
      //Écrit le le mot clé dans le fichier ouvert
      fputs($fichier,$_POST["motCles"]);
      //Ferme le fichier
      fclose($fichier);

      //Lance l'execution de la topologie storm
      exec("storm jar target/topology_pfe-0.0.1-SNAPSHOT-jar-with-dependencies.jar dz.pfe.storm.OpinionTweetTopology > output_storm 2>&1 &");
  ?>
  <!--Chargement de apache storm-->
  <div class="loading" id="divChargement"> </div>
  <img src="loading.gif" alt="loading" class="loading" id="imgChargement"/>

  <nav>
    <ul>
      <li>
        <a href="#" class="tablinks active" onclick="openViz(event, 'visualization1')">Visualization 1</a>
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
      <li id="lastnav">
        <a href="index.php">Accueil</a>
      </li>
    </ul>
  </nav>

  <section id="visualization1" class="tabcontent">

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

  <?php include("footer.php"); ?>

  <!--Inclusion du script de jquery depuis le CDN google d3.js d3plus.js d3.tip.js -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
  <script src="http://d3js.org/d3.v3.min.js" charset="utf-8"></script>
  <script src="http://labratrevenge.com/d3-tip/javascripts/d3.tip.v0.6.3.js"></script>
  <script src="http://www.d3plus.org/js/d3plus.js"></script>

  <!-- Scirpts custom, changement d'onglets et mise à jour des viz -->
  <script src="visualization.js"></script>

</body>
</html>
