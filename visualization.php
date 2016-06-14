<!DOCTYPE html>
<html>
<head>
  <title>Twitter's Opinion : Visualisation</title>
  <meta charset = "utf-8"/>
  <!-- le CSS général (de l'application) -->
  <link rel="stylesheet" type="text/css" href="webapp.css" />
  <link href="libjs/vis.css" rel="stylesheet" type="text/css" />
  <!-- le CSS des visualisations ->
  <link rel="stylesheet" type="text/css" href="visualization.css" />-->
  <style>
    .d3-tip-histo {
      line-height: 1;
      padding: 12px;
      background: rgba(0, 0, 0, 0.8);
      color: white;
      border-radius: 2px;
    }

    .d3-tip-graph {
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

    .line {
      stroke: Steelblue;
      fill:none;
      stroke-width: 3;
    }

    .axis text {
      font-size: 10px;
      font-family: sans-serif;
    }

    .text-label {
      font-size: 10px;
      font-family: sans-serif;
    }

    .datapoint:hover{
     fill: steelblue;
    }

    .xlabel, .ylabel {
     font-size:20px;
    }

    .vis-item.green {
      background-color: greenyellow;
      border-color: green;
    }

    .vis-item.blue {
      background-color: steelblue;
      border-color: blue;
    }

    .vis-item.gray {
      background-color: gray;
      border-color: gray;
    }

    /* Celle-là pour la viz des tweets */
    .containerLeft{
      float: left;
    }

  </style>

  <link href='https://fonts.googleapis.com/css?family=Product+Sans' rel='stylesheet' type='text/css'>
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
        <a href="#" class="tablinks active" onclick="openViz(event, 'visualization1')">Histograme</a>
      </li>
      <li>
        <a href="#" class="tablinks" onclick="openViz(event, 'visualization2')">Graphe</a>
      </li>
      <li>
        <a href="#" class="tablinks" onclick="openViz(event, 'visualization3')">Chronologie</a>
      </li>
      <li>
        <a href="#" class="tablinks" onclick="openViz(event, 'visualization4')">Tweets</a>
      </li>
      <li id="lastnav">
        <a href="index.php">Accueil</a>
      </li>
    </ul>
  </nav>

  <section id="visualization1" class="tabcontent">
    <header class="keywordstime"></header>
    <article id="histograme"></article>
  </section>
  <section id="visualization2" class="tabcontent">
    <header class="keywordstime"></header>
    <article id="graphe"></article>
  </section>
  <section id="visualization3" class="tabcontent">
    <header class="keywordstime"></header>
    <article id="chronologie"></article>
  </section>
  <section id="visualization4" class="tabcontent">
    <header class="keywordstime"></header>
    <article class="containerLeft" id="positive"></article>
    <article class="containerLeft" id="negative"></article>
    <article class="containerLeft" id="informationTweets"></article>
  </section>

  <?php
    }
  ?>

  <?php include("footer.php"); ?>

  <!--Inclusion du script de jquery depuis le CDN google d3.js d3plus.js d3.tip.js -->
  <script src="libjs/jquery.min.js"></script>
  <script src="libjs/d3.v3.min.js" charset="utf-8"></script>
  <script src="libjs/d3.tip.v0.6.3.js"></script>
  <script src="libjs/d3plus.js"></script>
  <script src="libjs/vis.js"></script>

  <!--Script de sauvegarde des mot-clefs -->
  <script>
    window.keywords = '<?=$_POST["motCles"] ?>';
  </script>

  <!-- Scirpts custom, changement d'onglets et mise à jour des viz -->
  <script src="visualization.js"></script>

</body>
</html>
