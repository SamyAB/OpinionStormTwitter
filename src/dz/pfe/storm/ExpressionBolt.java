package dz.pfe.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import dz.pfe.storm.ressources.MotTag;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import twitter4j.Status;


public class ExpressionBolt extends BaseRichBolt{
  private OutputCollector collector;
  private HashMap<String,ArrayList<String>> expDict;

  public ExpressionBolt(){
    super();

    //Création du dictionnaire d'expression
    this.expDict = new HashMap<String,ArrayList<String>>();
    String swnFile = "Dictionnaires/Expression-_SWN.txt";
		BufferedReader br = null;
		String line = "";
		String[] exp = null;

		try {
			br = new BufferedReader(new FileReader(swnFile));
			while ((line = br.readLine()) != null) {
				if(this.expDict.get(line)==null)
					exp= line.split(" ");
					ArrayList<String> tags= new ArrayList<String>();
					for(int i=1;i<exp.length;i++) tags.add(exp[i]);
					this.expDict.put(exp[0],tags);
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
  public void prepare(Map map, TopologyContext topologyContext,OutputCollector outputCollector){
    this.collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple){
    if(tuple!=null){
      //Récupération des valeurs dans le Tuple
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);
      String tweet_text = (String) tuple.getValue(2);
      ArrayList<MotTag> mots_tags = (ArrayList<MotTag>) tuple.getValue(3);

      //Remplacement des expressions dans mots_tags
      //Parcours des expressions
      for(HashMap.Entry<String, ArrayList<String>> entry : this.expDict.entrySet()){
        String exp_ = entry.getKey();
        String exp= exp_.replaceAll("_"," ").replaceAll("-"," ");
        String tag = entry.getValue().get(0);
        if(tweet_text.contains(exp)){
          int tailleExp = exp.split(" ").length;
          int i=0;
          //Parcours des mots tags
          while(i<mots_tags.size()-tailleExp+1){
            ArrayList<String> expMot = new ArrayList<String>();
            for(int j=i; j<i+tailleExp;j++){
              expMot.add(mots_tags.get(j).getMot().replaceAll("_"," ").replaceAll("-"," "));
            }
            if(exp.equals(String.join(" ", expMot))){
              MotTag mt = new MotTag(exp_,tag);
              mots_tags.set(i,mt);
              for(int j=i+1; j<i+tailleExp && j<mots_tags.size();j++){
                mots_tags.remove(j);
              }
            }
            i++;
          }
        }
      }

      this.collector.emit(new Values(motCles,tweet,tweet_text,mots_tags));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","tweet_text","mots_tags"));
  }

}
