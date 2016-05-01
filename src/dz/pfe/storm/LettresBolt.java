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
import java.util.HashMap;

public class LettresBolt extends BaseRichBolt{
  private OutputCollector collector;
  //Dictionnaire des pluriels irréguliers
	private HashMap<String,String> pluriel_singulier = null;
	//Dictionnaire des verbes (présent passé pp)
	private HashMap<String,String> verbes = null;
  //Dictionnaire mots en ing
  private ArrayList<String> ingDictionnary = null;
  //Liste sentiWordNet
  private HashMap<String,ArrayList<String>> sentiWords = null;

  public LettresBolt(){
    //Création du dictionnaire pluriel_singulier
    this.pluriel_singulier = new HashMap<String,String>();

    BufferedReader br=null;
    String line="";
    String txtFile = "Dictionnaires/iregular_noun.txt";
		String delim=" ";

		try {
			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(delim);
				this.pluriel_singulier.put(tokens[0],tokens[1]);
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

    //Création du dictionnaire des verbes
		this.verbes = new HashMap<String,String>();

		txtFile = "Dictionnaires/regular_iregular_verbs.txt";
		br = null;
		line = "";

		try {
			br = new BufferedReader(new FileReader(txtFile));

			while ((line = br.readLine()) != null) {
				String[] verb = line.split(" ");
				for(int i=2;i<verb.length;i++){
					this.verbes.put(verb[i].toLowerCase(),verb[1].toLowerCase());
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

    //Dico des mots en ing
    this.ingDictionnary = new ArrayList<String>();
    txtFile = "Dictionnaires/word_ending_with_in.txt";
		br = null;
		line = "";

		try {
			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {
				String ingWord = line;
				this.ingDictionnary.add(ingWord);
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

    //ListSWN
    this.sentiWords = new HashMap<String,ArrayList<String>>();
		txtFile = "Dictionnaires/SWNwordlist.txt";
		br = null;
		line = "";

		try {
			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {
							String[] word= line.split(" ");
							ArrayList<String> tag = new ArrayList<String>();
							for(int i=1;i<word.length;i++){
								tag.add(word[i]);
							}
							this.sentiWords.put(word[0], tag);
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

  //Là aussi à réviser
  private String singularNoun(String noun){
    if(this.pluriel_singulier.get(noun.toLowerCase())!=null){
      noun=this.pluriel_singulier.get(noun.toLowerCase());
    }
    else if( noun.length()>=4 &&(noun.substring(noun.length()-3).toLowerCase().equals("ses")
        || noun.substring(noun.length()-3).toLowerCase().equals("xes")
        || noun.substring(noun.length()-3).toLowerCase().equals("zes"))){

      noun=noun.substring(0,noun.length()-3).toLowerCase();
    }
    else if( noun.length()>=5 && (noun.substring(noun.length()-4).toLowerCase().equals("ches")
        || noun.substring(noun.length()-4).toLowerCase().equals("shes"))){

      noun=noun.substring(0,noun.length()-4).toLowerCase();
    }
    else if(noun.length()>=4 && noun.substring(noun.length()-3).toLowerCase().equals("ies")){
      noun=noun.substring(0,noun.length()-3).toLowerCase() + "y";
    }
    else if(noun.length()>=2 && noun.substring(noun.length()-1).toLowerCase().equals("s")){
      noun=noun.substring(0,noun.length()-1).toLowerCase();
    }
    return noun;
  }

  private String presentVerb(String verb){
    if( this.verbes.get(verb.toLowerCase())!=null){
      //Si le verbe est conjugué le remettre à l'infinitif
      verb=this.verbes.get(verb.toLowerCase());
    }
    return verb;
  }

  //Je suppose que c'est à revoir aussi
  public MotTag compactWord(MotTag mt){
    String mot = mt.getMot();
		String[] Mot = mot.replaceAll(".(?=.)", "$0 ").split(" ");
		ArrayList<String> listMot=new ArrayList<String>();
		String precLetter=Mot[0];
		int nbOcc=1;
		for(int i=1; i<Mot.length;i++){
			if(Mot[i].equals(precLetter)){
				nbOcc++;
			}else{
				if(nbOcc==1){

					if(listMot.isEmpty()){
						listMot.add(precLetter);
					}else{

						int n= listMot.size();
						for(int j=0;j<n;j++){
							listMot.set(j,listMot.get(j) + precLetter);
						}

					}
				}else{
					if(listMot.isEmpty()){
						listMot.add(precLetter);
						listMot.add(precLetter+precLetter);
					}else{
						int n=listMot.size();
						for(int j=0; j<n;j++){
							listMot.add(listMot.get(j)+precLetter);
							listMot.add(listMot.get(j)+precLetter+precLetter);
						}
						for(int j=0;j<n;j++) listMot.remove(0);
					}
				}
				precLetter=Mot[i];
				nbOcc=1;
			}
		}
		if(nbOcc==1){
			if(listMot.isEmpty()){
				listMot.add(precLetter);
			}else{
				int n= listMot.size();
				for(int j=0;j<n;j++){
					listMot.add(listMot.get(j) + precLetter);
				}
				for(int j=0;j<n;j++) listMot.remove(0);
			}
		}else{
			if(listMot.isEmpty()){
				listMot.add(precLetter);
				listMot.add(precLetter+precLetter);
			}else{
				int n=listMot.size();
				for(int j=0; j<n;j++){
					listMot.add(listMot.get(j)+precLetter);
					listMot.add(listMot.get(j)+precLetter+precLetter);
				}
				for(int j=0;j<n;j++) listMot.remove(0);

			}

		}
		String tag="";
		for(int i=0;i<listMot.size();i++){
			mot = listMot.get(i);
			tag=mt.getTag();
			if(tag.equals("N") || tag.equals("A")){
				if(mot.replaceAll("'","").endsWith("in") && !this.ingDictionnary.contains(mot.replaceAll("'",""))){
					mot=mot.replaceAll("'","")+"g";
				}
				if(this.sentiWords.get(this.singularNoun(mot))!=null && this.sentiWords.get(this.singularNoun(mot)).contains(tag.toLowerCase())){
					mt.setMot(mot);
					return mt;
				}

			}else if(tag.equals("V")){
				if(mot.replaceAll("'","").endsWith("in") && !this.ingDictionnary.contains(mot.replaceAll("'",""))){
					mot=mot.replaceAll("'","")+"g";
				}

				if(this.sentiWords.get(this.singularNoun(mot))!=null && this.sentiWords.get(this.singularNoun(mot)).contains(tag.toLowerCase())){
					mt.setMot(mot);
					return mt;
				}
			}else if(tag.equals("R")){
				if(this.sentiWords.get(mot)!=null && this.sentiWords.get(mot).contains(tag.toLowerCase())){
					mt.setMot(mot);
					return mt;
				}
			}
		}
		return mt;
  }

  @Override
  public void prepare(Map map,TopologyContext topologyContext, OutputCollector outputCollector){
    this.collector = outputCollector;
  }

  @Override
  public void execute(Tuple tuple){
    if(tuple!=null){
      //Récupération des informations reçues dans le tuple
      String[] motCles = (String[]) tuple.getValue(0);
      Status tweet = (Status) tuple.getValue(1);
      String tweet_text = (String) tuple.getValue(2);
      ArrayList<MotTag> mots_tags = (ArrayList<MotTag>) tuple.getValue(3);

      //Parcours des mots dans mots_tags
      for(int i=0; i<mots_tags.size(); i++){
        //Remplace le mot tag à la position i par le mot tag envoyé après
        //passage par compactWord
        mots_tags.set(i,compactWord(mots_tags.get(i)));
      }

      this.collector.emit(new Values(motCles,tweet,tweet_text,mots_tags));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","tweet_text","mots_tags"));
  }
}
