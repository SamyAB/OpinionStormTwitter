<!DOCTYPE html>
<html>
<head>
  <title>Twitter's Opinion : Visualization</title>
  <meta charset = "utf-8"/>
  <link rel="stylesheet" type="text/css" href="webapp.css" />
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
        <a href="/webapp/">Accueil</a>
      </li>
    </ul>
  </nav>

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

  <?php include("footer.php"); ?>

  <!--Inclusion du script de jquery depuis le CDN google -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>

  <!-- Scirpts custom, changement d'onglets et mise à jour des viz -->
  <script>
  //Temps de chargement de apache storm
  var loadingTime = 10000; // 10 secondes

  //Attente du temps de lancement de apache storm avant de lancer l'affichage des viz
  window.setTimeout(function(){
    //interval entre chaque update de données
    var updareInterval = 4000; // 2 secondes

    //Cacher l'animation de chargement
    $(function(){
        $(".loading").fadeOut();
    });

    //Lancement de l'update chaque updateInterval
    window.setInterval(update,updareInterval);
  },loadingTime);

  //Fonction de rechargement des vizualisations
  function update(){
    //interval entre chaque update de données
    var updareInterval = 2000; // 2 secondes
    console.log("hoho");
    $(function(){
      $.get(
        'redis.php', //Script serveur qui récupère les informations de redis
        'false', //On n'envoie aucun paramètre à redis.php
        function(data){
          //Ici se trouve tout ce qu'on doit faire après avoir récupérer les informations de Redis
          console.log(data);
        },
        'json' //Type de données reçues de redis.php, la on aime bien le json
      );
    });
  }

  //Fonction de changement d'onglet
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

  //Tuer storm en cas de leave de la page
  $(function(){
    $(window).bind('beforeunload', function(){
      $.get(
        'killall.php', //Script serveur qui tue storm
        'false', //On n'envoie aucun paramètre à killall.php
        function(data){
          //Ne rien faire :o
        },
        'text' //Type de données reçues, on ne sait jamais :o
      );
    });
  });
  </script>
</body>
</html>
