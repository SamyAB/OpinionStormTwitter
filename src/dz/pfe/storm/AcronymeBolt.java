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

  @Override
  public void prepare(Map map, TopologyContext topologyContext,OutputCollector outputCollector){
    this.collector = outputCollector;
    
    //Création d'un bufferedReader
    BufferedReader br = null;
    
    //Chargement du dictionnaire d'acronymes
    this.dico_acronymes = new HashMap<String,String> ();
    String ligne;
    try{
      br = new BufferedReader(new FileReader("/home/samy/Workspaces/topology_pfe/Dictionnaires/acronymes.txt"));

      //Lecture ligne par ligne
      while((ligne=br.readLine()) != null){
        //Découpage de la ligne avec  " – "
        String[] acro = ligne.split(" – ");
        dico_acronymes.put(acro[0],acro[1]);
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
  public void execute(Tuple tuple){
    //Essai de récupération de status envoyé par le spout
    Status tweet = (Status) tuple.getValue(0);

    //Les délimiteurs
    String delimiteurs = "[ .,:!?]+";

    String tweet_text = tweet.getText();
    String[] tokens = tweet_text.split(delimiteurs);

    for(String token : tokens){
      if(this.dico_acronymes.containsKey(token.toUpperCase())){
        tweet_text = tweet_text.replaceAll(token,this.dico_acronymes.get(token.toUpperCase()));
      }
    }

    //emettre le status tweet et le texte sans acronymes
    this.collector.emit(new Values(tweet,tweet_text));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("tweet","tweet_text"));
  }
}
