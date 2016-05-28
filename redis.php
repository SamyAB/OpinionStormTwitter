<?php
  try{
    //Création d'un objet redis et connexion à redis sur loaclhost
    $redis = new Redis();
    $redis->connect('127.0.0.1', 6379);

    //Création d'un array de tweets
    $tweets = array();

    //Récupération des élétements de la liste TweetsList
    //CàD tout les tweets et leurs score placés dans la liste sur redis par ReportBolt
    while($elem = $redis->rpop("TweetsList")){
      //Séparation des éléments de la chaine de caractère envoyée par ReportBolt
      $aMettre = explode(" | ",$elem);

      //Ajout de l'array des informations d'un élémente de la liste de TweetList dans tweets
      array_push($tweets,$aMettre);
    }

    //Création d'un objet à transmettre à viz
    $obj = new stdClass();
    $obj -> tweets = $tweets;

    //Envois d'obj comme réponde HTTP
    echo json_encode($obj);

  } catch (Exception $e){
    //En cas d'erreurs, envois du message d'erreur en réponse HTTP
    echo "message : ".$e->getMessage;
  }
?>
