package dz.pfe.storm;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import twitter4j.Status;

import java.util.Map;

public class ScoreBolt.java extends BaseRichBolt{
  private OutputCollector collector;

  @Override
  public void prepare(Map map, TopologyContext topologyContext,OutputCollector outputCollector){
    this.collector = outputCollector;
    //Ouverture des dictionnaires de scores
  }

  @Override
  public void execute(Tuple tuple){
    //Récupération des informations reçues dans le tuple
    //Pour le moment on ne suppose que le status tweet
    Status tweet = (Status) tuple.getValue(0);

    //Initialisation du score à 0
    float score = 0.0;

    //Utilisation des dictionnaires pour donner un score au tweets reçus
    //Pour le moment on suppose que le score est 0

    //Emettre le status et son score
    this.collector.emit(tweet,score);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("tweet","score"))
  }

}
