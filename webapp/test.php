<!DOCTYPE html>
<head>
  <title>Formulaire</title>
  <meta charset = "utf-8"/>
  <link rel="stylesheet" type="text/css" href="webapp.css" />
  <link href='https://fonts.googleapis.com/css?family=Product+Sans' rel='stylesheet' type='text/css'>
</head>
<body>
  <header>
    <a href="test.php"><img src="logo.png" alt="logo"/></a>
    <p id="opinion">
      L'opinion de la twittosphère
    </p>
    <nav>
      <ul>
        <li>
          <a class="active" href="test.php">Acceuil</a>
        </li>
        <li>
          <a href="#">Mémoire</a>
        </li>
        <li>
          <a href="#">Code source du projet</a>
        </li>
        <li id="lastnav">
          <a href="#">À propos</a>
        </li>
      </ul>
    </nav>
  </header>
  <h3>Mot-clefs</h3>
  <p>
    Donnez dans le formulaire suivant les mots-clefs à viser pour la suivie des opinions sur twitter
  </p>
  <section id="recherche">
    <form action="visualization.php" method="post">
      <label for="motCles">Mots-clefs</label>
      <input type="text" name="motCles" />
      <input type="submit" value="Valider" />
    </form>
  </section>
  <section id="info projet">
    <h3>Code source</h3>
    Retrouvez le code source du projet sur <a href="https://github.com/SamyAB/OpinionStormTwitter" >github</a>
  </section>
  <footer>
    Réalisé dans le cadre d'un projet de fin d'études de master.
  </footer>
</body>
</html>
