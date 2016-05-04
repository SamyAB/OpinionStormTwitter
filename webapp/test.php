<!DOCTYPE html>
<head>
  <title>Formulaire</title>
  <meta charset = "utf-8"/>
</head>
<body>
  <header>
    <p>
      Ceci est la page d'acceuil du projet : Suivi de la propagation des opinions en temps réel
    </p>
  </header>
  <nav>
    <ul>
      <li>
        <a href="memoire">Mémoire</a>
      </li>
      <li>
        <a href="code">Code source du projet</a>
      </li>
      <li>
        <a href="about">À propos</a>
      </li>
    </ul>
  </nav>
  <section id="recherche">
    <p>
      Entrez vos mot-clés dans le champs suivant et validez
    </p>
    <form action="cible.php" method="post">
      <input type="text" name="motCles" />
      <input type="submit" value="Valider" />
    </form>
  </section>
  <section id="info projet">
    Retrouvez le code source du projet sur <a href="https://github.com/SamyAB/OpinionStormTwitter" >github</a>
  </section>
  <footer>
    Réalisé dans le cadre d'un projet de fin d'études de master.
  </footer>
</body>
</html>
