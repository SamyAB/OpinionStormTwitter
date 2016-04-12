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
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import emoji4j.*;

public class SpaceSeparationBolt extends BaseRichBolt{
  private OutputCollector collector;
  private Map<String,ArrayList<String>> emojiDictionary;
  private ArrayList<String> nonRepertoriedEmoji;

  public SpaceSeparationBolt(){
    super();

    this.emojiDictionary = new HashMap<String,ArrayList<String>>();
    this.nonRepertoriedEmoji = new ArrayList<String>();
    BufferedReader br = null;

    String txtFile = "Dictionnaires/emojiSentiment.txt";
		String line = "";
		String delim = "	";

		try {

			br = new BufferedReader(new FileReader(txtFile));

			while ((line = br.readLine()) != null) {
			    String[] emoji = line.split(delim);

					ArrayList<String> emojiElem=new ArrayList<String>();
					emojiElem.add(emoji[2]); //unicode
					emojiElem.add(emoji[7]); //pos
					emojiElem.add(emoji[5]);//neg
					emojiElem.add(emoji[8]);//score ????
					this.emojiDictionary.put(emoji[0],emojiElem);	//character, list of unicode, neg, pos
					if(emoji4j.EmojiUtils.isEmoji(emoji[0])==false) ;nonRepertoriedEmoji.add(emoji[0]);
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
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);

      String tweet_text = tweet.getText();
      String[] tweettoken = tweet_text.split(" ");

		  for(int i=0; i< tweettoken.length;i++){
  			String[] token = tweettoken[i].replaceAll(".(?=.)", "$0 ").split(" ");
  			for(int j=0; j<token.length;j++){
  				if(this.emojiDictionary.get(token[j])!=null|| this.nonRepertoriedEmoji.contains(token[j])){
  					token[j]=" "+token[j]+" ";
  				}
  			}
  			tweettoken[i]=String.join("", token);
		  }
		  tweet_text= String.join(" ", tweettoken);

      //Emettre pour le status le tweet sans acronymes et les mots taggés
      this.collector.emit(new Values(motCles,tweet,tweet_text));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","tweet_text"));
  }
}
