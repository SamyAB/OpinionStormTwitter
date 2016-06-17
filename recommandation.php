<?php
  //Execution de la classe de recommandation.
  exec("java -jar reco.jar 2>&1 recommandation_output");

  try{
    //Création d'un objet redis et connexion à redis sur loaclhost
    $redis = new Redis();
    $redis->connect('127.0.0.1', 6379);

    //Création d'un array de termes
    $unigramPos = array();
    $bigramPos = array();
    $unigramNeg = array();
    $bigramNeg = array();

    //Récupération des chaines de caractère représentant les termes puis de les ajouter aux arrays
    $termsUnigramPos = $redis->rpop("unigram");
    $aMettre = explode(" ",$termsUnigramPos);
    array_push($unigramPos,$aMettre);

    $termsBigramPos = $redis->rpop("bigram");
    $aMettre = explode(" | ",$termsBigramPos);
    array_push($bigramPos,$aMettre);

    $termsUnigramNeg = $redis->rpop("unigramNegatif");
    $aMettre = explode(" ",$termsUnigramNeg);
    array_push($unigramNeg,$aMettre);

    $termsBigramNeg = $redis->rpop("bigramNegatif");
    $aMettre = explode(" | ",$termsBigramNeg);
    array_push($bigramNeg,$aMettre);

    //Création d'un objet à transmettre à viz
    $obj = new stdClass();
    $obj -> unigramPos = $unigramPos;
    $obj -> bigramPos = $bigramPos;
    $obj -> unigramNeg = $unigramNeg;
    $obj -> bigramNeg = $bigramNeg;

    //Envois d'obj comme réponde HTTP
    echo json_encode($obj);

  } catch (Exception $e){
    //En cas d'erreurs, envois du message d'erreur en réponse HTTP
    echo "message : ".$e->getMessage;
  }
?>
