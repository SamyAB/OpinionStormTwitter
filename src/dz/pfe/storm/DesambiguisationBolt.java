package dz.pfe.storm;

import backtype.storm.topology.base.BaseRichBolt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import dz.pfe.storm.ressources.MotTag;
import dz.pfe.storm.ressources.MyRunTagger;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;

import twitter4j.Status;

public class DesambiguisationBolt extends BaseRichBolt{
	private OutputCollector collector;
	//Dictionnaire des pluriels irréguliers
	private HashMap<String,String> pluriel_singulier = null;
	//Dictionnaire des verbes (présent passé pp)
	private HashMap<String,String> verbes = null;

	public DesambiguisationBolt(){
		super();
		//Création du dictionnaires des plurieles irréguliers
		pluriel_singulier = new HashMap<String,String>();

		String txtFile = "/home/samy/Workspaces/topology_pfe/Dictionnaires/iregular_noun.txt";
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {
				String[] noun = line.split(" ");
				for(int i=1;i<noun.length;i++){
					this.pluriel_singulier.put(noun[i].toLowerCase(),noun[0].toLowerCase());
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

		//Création du dictionnaire des verbes
		this.verbes = new HashMap<String,String>();

		txtFile = "/home/samy/Workspaces/topology_pfe/Dictionnaires/regular_iregular_verbs.txt";
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
	}

	@Override
	public void prepare(Map map,TopologyContext topologyContext, OutputCollector outputCollector){
		this.collector = outputCollector;
	}

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
			verb=this.verbes.get(verb.toLowerCase());
		}
		return verb;
	}

	@Override
	public void execute(Tuple tuple){
		if(tuple!=null){
			//Récupération des informations reçues X bolt (on verra quel bolt)
			String[] motCles = (String[]) tuple.getValue(0);
			Status tweet = (Status) tuple.getValue(1);
			String tweet_text = (String) tuple.getValue(2);
			ArrayList<MotTag> mots_tags = (ArrayList<MotTag>) tuple.getValue(3);

			//Désambiguisation des mots dans la ArrayList mots_tags
			for(int i=0;i < mots_tags.size();i++){
				MotTag mt = mots_tags.get(i);
				if(mt.getTag().equals("V")){
					mt.setMot(presentVerb(mt.getMot()));
					mots_tags.set(i,mt);
				} else if(mt.getTag().equals("N") || mt.getTag().equals("A")){
					mt.setMot(singularNoun(mt.getMot()));
					mots_tags.set(i,mt);
				}
			}

			//Emettre le tweet la chaine sans abbréviation et les mots taggés désanbiguisés
			this.collector.emit(new Values(motCles,tweet,tweet_text,mots_tags));
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer){
		//mots_tags dans l'expression suivante peut changer :3
		declarer.declare(new Fields("motCles","tweet","tweet_text","mots_tags"));
	}
}
