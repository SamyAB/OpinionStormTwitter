package dz.pfe.storm;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import twitter4j.Status;

import java.util.Map;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.FileReader;


public class AcronymeBolt extends BaseRichBolt{
  private OutputCollector collector;
  private Map<String,String> dico_acronymes;

  public AcronymeBolt(){
    //Création du bolt en utilisant le constructeur par défaut de la classe mère
    super();

    //Lecture du dictionnaire d'acronymes
    //Création d'un bufferedReader
    BufferedReader br = null;

    //Chargement du dictionnaire d'acronymes
    this.dico_acronymes = new HashMap<String,String> ();
    String ligne;
    try{
      br = new BufferedReader(new FileReader("Dictionnaires/acronymes.txt"));

      //Lecture ligne par ligne
      while((ligne=br.readLine()) != null){
        //Découpage de la ligne avec  " – "
        if(!ligne.equals("")){
          String[] acro = ligne.split(" – ");
          dico_acronymes.put(acro[0],acro[1]);
        }
      }
    } catch(Exception e){
      e.printStackTrace();
    } finally {
      if(br != null){
        try {
          br.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void prepare(Map map, TopologyContext topologyContext,OutputCollector outputCollector){
    //Préaparer l'outputCollector
    this.collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple){
    //Essai de récupération de status envoyé par le SpaceSeparationBolt
    if(tuple!=null){
      //Récupération des objets envoyés par le spout
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);
      String tweet_text = (String) tuple.getValue(2);

      //Les délimiteurs
      String delimiteurs = "[ .,:!?]+";

      //Split le texte du tweet
      String[] tokens = tweet_text.split(delimiteurs);

      //Parcour du texte du tweet à la recherche d'acronyme
      for(String token : tokens){
        if(this.dico_acronymes.containsKey(token.toUpperCase())){
          //Remplace l'acronyme, si trouvé par sa valeur textuelle
          tweet_text = tweet_text.replaceAll(token,this.dico_acronymes.get(token.toUpperCase()));
        }
      }

      //emettre les mot clés, status tweet. et le texte sans acronymes
      this.collector.emit(new Values(motCles,tweet,tweet_text.toLowerCase()));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","tweet_text"));
  }
}
