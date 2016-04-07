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

import java.util.Map;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ScoreBolt extends BaseRichBolt{
  private OutputCollector collector;
  private HashMap<ArrayList<String>, ArrayList<String>> dico_senti;

  public ScoreBolt(){
    super();

    this.dico_senti = new HashMap<ArrayList<String>, ArrayList<String>>();

    //Lecture du dictionnaire SentiWordNet
    String swnFile = "/home/samy/Workspaces/topology_pfe/Dictionnaires/SentiWordNet_3.0.0_20130122.txt";
		BufferedReader br = null;
		String line = "";
		String swnSplitBy = "	";

		try {
			br = new BufferedReader(new FileReader(swnFile));
			while ((line = br.readLine()) != null) {
				String[] swn = line.split(swnSplitBy);
				String[] synsets = swn[4].split(" ");

				for(int i=0; i<synsets.length; i++)
				{
					ArrayList<String> key = new ArrayList<String>();
					key.add(swn[0]);
					key.add(swn[1]);
					String word = synsets[i].split("#")[0];
					key.add(word);

					ArrayList<String> value = new ArrayList<String>();
					value.add(swn[2]);
					value.add(swn[3]);
					this.dico_senti.put(key, value);
				}
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

  //Fait la moyenne des scores présent pour ce mot et ce tag sans senti
  public float[] searchWordScore(String word, String tag){
		float scoreP=0;
		float scoreN=0;
		float nbOcc=0;
		float[] score = new float[2];
		for (Map.Entry<ArrayList<String>, ArrayList<String>> entry : this.dico_senti.entrySet()) {
			ArrayList<String > key=entry.getKey();
			if(key.get(0).equals(tag) && key.get(2).equals(word)){
				scoreP+=(float) Float.parseFloat(entry.getValue().get(0));
				scoreN+=(float) Float.parseFloat(entry.getValue().get(1));
				nbOcc++;
			}
		}
		if(nbOcc !=0){
			score[0]=(float)(scoreP/(float)(nbOcc));
			score[1]=(float)(scoreN/(float)(nbOcc));
		}
    else {
      score[0] = (float)-1;
      score[1] = (float)-1;
    }
		return score;
	}


  @Override
  public void prepare(Map map, TopologyContext topologyContext,OutputCollector outputCollector){
    this.collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple){
    if(tuple!=null){
      System.out.println("\n\n\n\n je suis dans execute \n\n");
      //Récupération des informations reçues dans le tuple
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);
      String tweet_text = (String) tuple.getValue(2);
      ArrayList<MotTag> mots_tags = (ArrayList<MotTag>) tuple.getValue(3);

      ArrayList<String> negations = new ArrayList(Arrays.asList("without against nor no useless never not cannot".split(" ")));
	  String negationForm = "n't";

      //Initialisation de score des mots
      float[] scoreWords = new float[2]; //Pos, Neg
		  scoreWords[0]=(float)0;
		  scoreWords[1]=(float)0;

      int i = 0;
      float negationScore = (float) 0.125;
      boolean estNegation = false;
      String negationPrec = "";

      //Parcourt des mots-tags
      while(i < mots_tags.size()){
        MotTag mt = mots_tags.get(i);
        if(negations.contains(mt.getMot()) || mt.getMot().contains(negationForm)){
          if(estNegation){
            if(!mt.getMot().equals("useless") && !negationPrec.equals("useless")){
              estNegation = false;
              scoreWords[1] += negationScore;
            }
          } else {
            estNegation = true ;
            negationPrec = mt.getMot();
          }
        } else if(mt.getTag().equals("A") || mt.getTag().equals("V") || mt.getTag().equals("R") || mt.getTag().equals("EXP")){
          //Si ce n'est pas une négation
          float[] score = this.searchWordScore(mt.getMot(),(mt.getTag().replace("EXP","R")).toLowerCase());
          if(score[0] == (float)-1 && estNegation){
            scoreWords[1] += negationScore ;
          } else {
            if(score[0]!= (float) -1 && estNegation){
              scoreWords[0] += score[1];
              scoreWords[1] += score[0];
            } else if(score[0] != (float) -1 && !estNegation){
              scoreWords[0] += score[0];
              scoreWords[1] += score[1];
            }
          }
          estNegation = false;
        }
        i++;
      }
      if(estNegation){
        //Si on a une négation en fin de tweet
        scoreWords[1] += negationScore;
      }

      //Calcule du score du tweet
      float scoreTweet = scoreWords[0] - scoreWords[1];
      System.out.println("\n\n\n"+tweet_text+" "+scoreWords[0]+" "+scoreWords[1]+"\n\n");

      //Emettre le status et son score
      this.collector.emit(new Values(motCles,tweet,scoreTweet));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","score"));
  }

}
