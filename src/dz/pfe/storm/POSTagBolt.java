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
import java.io.IOException;
import java.util.ArrayList;


public class POSTagBolt extends BaseRichBolt{
  private OutputCollector collector;

  @Override
  public void prepare(Map map,TopologyContext topologyContext, OutputCollector outputCollector){
    this.collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple){
    //Récupération des informations reçues dans le tuple
    //Dans ce cas : le status et une chaine de caractère représentant le text du tweet sans acronymes
    Status tweet = (Status) tuple.getValue(0);
    String tweet_text = (String) tuple.getValue(1);

    //À ce point il faudrait utiliser le tagger pour tag tweet_text
    //Je ne sais pas quel sera le type de la collection des tweets taggés
    ArrayList<MotTag> mots_tags = null;
    try {
		mots_tags = MyRunTagger.tagTweet("conll",tweet_text);
	} catch (ClassNotFoundException | IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    //Emettre pour le moment le status et les mots taggés
    this.collector.emit(new Values(tweet,tweet_text,mots_tags));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("tweet","tweet_text","mots_tags"));
  }
}
