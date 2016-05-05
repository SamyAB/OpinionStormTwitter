<!DOCTYPE html>
<head>
  <title>Formulaire</title>
  <meta charset = "utf-8"/>
  <link rel="stylesheet" type="text/css" href="webapp.css" />
  <link href='https://fonts.googleapis.com/css?family=Product+Sans' rel='stylesheet' type='text/css'>
</head>
<body>
  <header>
    <div id="banner">
      <h3 id="opinion">
        L'opinion de la twittosphère anglophone
      </h3>
      <a href="test.php"><img src="logo.png" alt="logo"/></a>
    </div>
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
  <section id="recherche">
    <h3>Mot-clefs</h3>
    <p>
      Donnez dans le formulaire suivant les mot-clefs à viser pour la suivie des opinions sur twitter
    </p>
    <form action="visualization.php" method="post">
      <input type="text" name="motCles" placeholder="Mot-clefs" />
      <input type="submit" value="Valider" />
    </form>
  </section>
  <footer>
    Réalisé dans le cadre d'un projet de fin d'études de master.
  </footer>
</body>
</html>
