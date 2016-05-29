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
import java.util.ArrayList;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;


public class ReportBolt extends BaseRichBolt{
  private OutputCollector collector;
  private RedisConnection redis;

  @Override
  public void prepare(Map map,TopologyContext topologyContext, OutputCollector outputCollector){
    this.collector = outputCollector;

    //Ouvrire une connexion redis et s'y connecter
    RedisClient client = new RedisClient("localhost",6379);
    this.redis = client.connect();
  }

  @Override
  public void execute(Tuple tuple){
    if(tuple!=null){
      //Récupération des informations reçues dans le tuple
      //Dans ce cas : Les mots clés, le status et le score de ce status
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);
      float score = (float) tuple.getValue(2);

      int positionHist=0;
      if(score>0) positionHist = 3;
      else if(score < 0) positionHist = 1;
      else positionHist = 2;

      //Création d'une chaine de caractères contenant l'ID du tweet et son score
      String aEnvoyer = positionHist + " | " + score + " | " + tweet.getText() + " | " +
      tweet.getUser().getName() + " | " + tweet.getUser().getScreenName() + " | " +
      tweet.getFavoriteCount() + " | " + tweet.getRetweetCount() + " | " + tweet.getCreatedAt() + " | " +
      tweet.getUser().getProfileImageURL() + " | " + tweet.getId() + " | " + motCles[0];

      //Enregistrement du tweet dans redis dans la liste TweetsList
      redis.lpush("TweetsList",aEnvoyer);
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    //Il y a rien à envoyer
  }
}
