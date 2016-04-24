package dz.pfe.storm;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import dz.pfe.storm.ressources.MotTag;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import twitter4j.Status;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;



public class LanguageCorrectionBolt extends BaseRichBolt{
  private OutputCollector collector;
  private HashMap<String,String> slangDictionary=null;

  public LanguageCorrectionBolt(){
    this.slangDictionary= new HashMap<String,String>();

    BufferedReader br=null;
    String line="";
    String txtFile = "Dictionnaires/word_correction.txt";
		String delim=":";

		try {
			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {
				String[] slangWord = line.split(delim);
				this.slangDictionary.put(slangWord[0],slangWord[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
  }

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

      String delimiteurs = "[ .,:!?\n]+";

      String[] tweetTokens= tweet_text.split(delimiteurs);
  		for(int i=0;i<tweetTokens.length;i++){
  			if(this.slangDictionary.get(tweetTokens[i])!=null){
  				tweet_text = tweet_text.replaceAll(tweetTokens[i],this.slangDictionary.get(tweetTokens[i]));
  			}
  		}



      //Emettre pour le status le tweet sans acronymes et les mots taggés
      this.collector.emit(new Values(motCles,tweet,tweet_text));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","tweet_text"));

  }
}
