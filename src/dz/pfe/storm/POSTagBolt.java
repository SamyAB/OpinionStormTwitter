package dz.pfe.storm;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import dz.pfe.storm.ressources.MotTag;
import dz.pfe.storm.ressources.MyRunTagger;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import twitter4j.Status;

import java.util.Map;
import java.util.ArrayList;


public class POSTagBolt extends BaseRichBolt{
  private OutputCollector collector;

  @Override
  public void prepare(Map map,TopologyContext topologyContext, OutputCollector outputCollector){
    this.collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple){
    if(tuple!=null){
      //Récupération des informations reçues dans le tuple
      //Dans ce cas : le status et une chaine de caractère représentant le text du tweet sans acronymes
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);
      String tweet_text = (String) tuple.getValue(2);

      //À ce point il faudrait utiliser le tagger pour tag tweet_text
      //Je ne sais pas quel sera le type de la collection des tweets taggés
      ArrayList<MotTag> mots_tags = null;

      //Tag le tweet
      try{
        mots_tags = MyRunTagger.tagTweet("conll",tweet_text);
      } catch(Exception e) {
        e.printStackTrace();
      }

      //Emettre pour le status le tweet sans acronymes et les mots taggés
      this.collector.emit(new Values(motCles,tweet,tweet_text,mots_tags));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","tweet_text","mots_tags"));
  }
}
