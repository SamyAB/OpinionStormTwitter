<!DOCTYPE html>
<html>
<head>
  <title>Twitter's Opinion : Acceuil</title>
  <meta charset = "utf-8"/>
  <link rel="stylesheet" type="text/css" href="webapp.css" />
  <link href='https://fonts.googleapis.com/css?family=Product+Sans' rel='stylesheet' type='text/css'>
</head>
<body>

  <?php include("header.php"); ?>

  <nav>
    <ul>
      <li>
        <a class="active" href="index.php">Accueil</a>
      </li>
      <li>
        <a href="memoire.php">Mémoire</a>
      </li>
      <li>
        <a href="code.php">Code source du projet</a>
      </li>
      <li id="lastnav">
        <a href="about.php">À propos</a>
      </li>
    </ul>
  </nav>

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

  <?php include("footer.php"); ?>

</body>
</html>
