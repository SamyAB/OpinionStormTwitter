<?php
  //Basiquement ce script tue les instences java lancé par "deamon"
  //Utilisé pour tué Apache Storm, qui utilise java …
  exec("killall java &");
  echo "success";
?>
