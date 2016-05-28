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
      $aMettre = explode(" | ",$elem);
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
