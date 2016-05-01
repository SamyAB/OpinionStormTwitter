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

import emoji4j.*;

public class ScoreBolt extends BaseRichBolt{
  private OutputCollector collector;
  private HashMap<ArrayList<String>, ArrayList<String>> dico_senti;
  private Map<String,ArrayList<String>> emojiDictionary;
	private ArrayList<String> nonRepertoriedEmoji;

  //List des emiticons
  private ArrayList<String> smiley = new ArrayList<String>(Arrays.asList(":-) :) :D :o) :] :3 :c3 :> =] 8) =) :} :^) :っ) (=^・^=) (=^・・^=) =^_^=".split(" ")));
	private ArrayList<String> laughing = new ArrayList<String>(Arrays.asList(":‑D 8‑D 8D x‑D xD X‑D XD =‑D =D =‑3 =3 B^D ^_^ (゜o゜) (^_^)/ (^O^)／ (^o^)／ (^^)/ (≧∇≦)/ (/◕ヮ◕)/ (^o^)丿 ∩(・ω・)∩ (・ω・) ^ω^ >^_^< <^!^> ^/^ （*^_^*） §^。^§ (^<^) (^.^) (^ム^) (^・^) (^。^) (^_^.) (^_^) (^^) (^J^) (*^。^*) ^_^ (#^.^#) （＾－＾） （●＾o＾●） （＾ｖ＾） （＾ｕ＾） （＾◇＾） (^)o(^) (^O^) (^o^) (^○^) )^o^( (*^▽^*) (✿◠‿◠)".split(" ")));
	private ArrayList<String> frowny = new ArrayList<String>(Arrays.asList(";( >:[ :‑( :( :‑c :c :‑< :っC :< :‑[ :[ :{".split(" ")));
	private ArrayList<String> angry = new ArrayList<String>(Arrays.asList(":-|| :@ >:( <(｀^´)> (╯°□°）╯︵┻━┻".split(" ")));
	private ArrayList<String> sadcrying = new ArrayList<String>(Arrays.asList(":'‑( :'( ('_') (/_;) (T_T) (;_;) (;_; (;_:) (;O;) (:_;) (ToT) (Ｔ▽Ｔ) ;_; ;-; ;n; ;; Q.Q T.T QQ Q_Q".split(" ")));
	private ArrayList<String> happycrying = new ArrayList<String>(Arrays.asList(":'‑) :')".split(" ")));
	private ArrayList<String> disgust = new ArrayList<String>(Arrays.asList("D:< D: D8 D; D= DX v.v D‑':".split(" ")));
	private ArrayList<String> shock = new ArrayList<String>(Arrays.asList(">:O :‑O :O :‑o :o 8‑0 O_O o‑o O_o o_O o_o O-O".split(" ")));
	private ArrayList<String> kiss = new ArrayList<String>(Arrays.asList(":*" ,":-*" ,":^*" ,"( '}{' )"));
	private ArrayList<String> wink = new ArrayList<String>(Arrays.asList(";‑) ;) *-) *) ;‑] ;] ;D ;^) :‑, (^_-) (^_-)-☆".split(" ")));
	private ArrayList<String> cheeky = new ArrayList<String>(Arrays.asList(">:P :‑P :P X‑P x‑p xp XP :‑p :p =p :‑Þ :Þ :þ :‑þ :‑b :b d:".split(" ")));
	private ArrayList<String> annoyed = new ArrayList<String>(Arrays.asList(">:\\ >:/ :‑/ :‑. :/ :\\ =/ =\\ :L =L :S >.< (ー_ー)!!　(-.-)　(-_-) (一一)　(；一_一) (=_=) （￣ー￣）".split(" ")));
	private ArrayList<String> straightface = new ArrayList<String>(Arrays.asList(":| :‑| <:‑|".split(" ")));
	private ArrayList<String> sceptic = new ArrayList<String>(Arrays.asList(":$ :‑X :X :‑# :# :‑& :&".split(" ")));
	private ArrayList<String> saint = new ArrayList<String>(Arrays.asList("O:‑) 0:‑3 0:3 0:‑) 0:) 0;^)".split(" ")));
	private ArrayList<String> evil = new ArrayList<String>(Arrays.asList(">:) >;) >:‑) }:‑) }:) 3:‑) 3:)".split(" ")));
	private ArrayList<String> highfive = new ArrayList<String>(Arrays.asList("o/\\o ^5 >_>^ ^<_<".split(" ")));
	private ArrayList<String> cool = new ArrayList<String>(Arrays.asList("|;‑) |‑O :‑J".split(" ")));
	private ArrayList<String> party = new ArrayList<String>(Arrays.asList("#‑) %‑) %)".split(" ")));
	private ArrayList<String> sick = new ArrayList<String>(Arrays.asList(":‑###.. :###..".split(" ")));
	private ArrayList<String> cheerful = new ArrayList<String>(Arrays.asList("\\o/ *\\0/* \\(~o~)／ \\(^o^)／ \\(-o-)／ ヽ(^。^)ノ ヽ(^o^)丿 (*^0^*)".split(" ")));
	private ArrayList<String> rose = new ArrayList<String>(Arrays.asList("@}‑;‑'‑‑‑ @>‑‑>‑‑".split(" ")));
	private ArrayList<String> heartBreak = new ArrayList<String>(Arrays.asList("</3".split(" ")));
	private ArrayList<String> troubled = new ArrayList<String>(Arrays.asList("(>_<) (>_<)> (^^ゞ (^_^;) (-_-;) (~_~;) (・。・;) (・_・;) (・・;) ^^; ^_^; (#^.^#) (^ ^;) ((+_+)) (+o+) (゜゜) (゜-゜) (゜.゜) (゜_゜) (゜_゜>) (゜レ゜)".split(" ")));
	private ArrayList<String> respect = new ArrayList<String>(Arrays.asList("(_ _)","_(._.)_","_(_^_)_","<(_ _)>","<m(__)m>","m(__)m","m(_ _)m"));
	private ArrayList<String> amazed = new ArrayList<String>(Arrays.asList("(*_*) (*_*; (+_+) (@_@) (@_@。 (＠_＠;) ＼(◎o◎)／！".split(" ")));



  public ScoreBolt(){
    super();

    //Instenciation des Dictionnaires
    this.dico_senti = new HashMap<ArrayList<String>, ArrayList<String>>();
    this.emojiDictionary = new HashMap<String,ArrayList<String>>();
    this.nonRepertoriedEmoji = new ArrayList<String>();

    //Lecture du dictionnaire SentiWordNet
    String swnFile = "Dictionnaires/SentiWordNet_3.0.0_20130122.txt";
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

    //Chargement du dictionnaire emoji
    String txtFile = "Dictionnaires/emojiSentiment.txt";
		br = null;
		line = "";
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

  //Recupere le score d'un emoji si il existe dans le dico ou la liste d'emoji non repértorié
  public float[] getEmojiScoreIfExiste(String emo){
	  float[] score = new float[2];
	  score[0]=(float)0;
	  score[1]=(float)0;

		int matcher = EmojiUtils.countEmojis(emo.replaceAll("[a-zA-Z0-9=,;:()/+-<>゜・.# \"\'ゞ_^~]",""));
    if(matcher>0) {
      if(this.emojiDictionary.get(emo) != null){
         ArrayList<String> emojiElem=this.emojiDictionary.get(emo);
         score[0]=(float)(Float.parseFloat(emojiElem.get(1)));
         score[1] = (float)(Float.parseFloat(emojiElem.get(2)));
      }
    }
    return score;
	}

  //Recpere le score d'un emoticon si il existe dans les listes d'emoticons
  public float[] getEmoticonScoreIfExiste(String emo){
		float sc[] = new float[2];
		sc[0]=(float)0;
		sc[1]=(float)0;
		if(!emo.equals("") && EmojiUtils.getEmoji(emo)!=null && EmojiUtils.getEmoji(emo).getEmoji() != null && this.emojiDictionary.get(EmojiUtils.getEmoji(emo).getEmoji())!=null){
			String s = EmojiUtils.getEmoji(emo).getEmoji();

				ArrayList<String> score =this.emojiDictionary.get(s);
				sc[0]=(float)Float.parseFloat(score.get(1));
				sc[1]=(float)(Float.parseFloat(score.get(2)));
				return sc;

		}else{
		if(this.smiley.contains(emo.toLowerCase())){ sc[0]=(float)0.614125; sc[1]=(float)0.08749; 	}
		else if(this.laughing.contains(emo.toLowerCase())){ sc[0]=(float)0.583; sc[1]=(float)0.12375; 	}
		else if(this.frowny.contains(emo.toLowerCase())){ sc[0]=(float)0.205; sc[1]=(float)0.5506667; 	}
		else if(this.angry.contains(emo.toLowerCase())){ sc[0]=(float)0.3125; sc[1]=(float)0.548; 	}
		else if(this.sadcrying.contains(emo.toLowerCase())) { sc[0]=(float)0.391; sc[1]=(float)0.384; 	}
		else if(this.happycrying.contains(emo.toLowerCase())) { sc[0]=(float)0.391; sc[1]=(float)0.384; 	}
		else if(this.disgust.contains(emo.toLowerCase())){ sc[0]=(float)0.3125; sc[1]=(float)0.548; 	}
		else if(this.shock.contains(emo.toLowerCase())){ sc[0]=(float)0.452; sc[1]=(float)0.183; 	}
		else if(this.kiss.contains(emo.toLowerCase())){ sc[0]=(float)0.722; sc[1]=(float)0.111; 	}
		else if(this.wink.contains(emo.toLowerCase())) { sc[0]=(float)0.863; sc[1]=(float)0.1; 	}
		else if(this.cheeky.contains(emo.toLowerCase())){ sc[0]=(float)0.686; sc[1]=(float)0.085; 	}
		else if(this.annoyed.contains(emo.toLowerCase())) { sc[0]=(float)0.2066; sc[1]=(float)0.599; 	}
		else if(this.straightface.contains(emo.toLowerCase())) { sc[0]=(float)0.165; sc[1]=(float)0.553; 	}
		else if(this.sceptic.contains(emo.toLowerCase())){ sc[0]=(float)0.217; sc[1]=(float)0.591; 	}
		else if(this.saint.contains(emo.toLowerCase())) { sc[0]=(float)0.659; sc[1]=(float)0.072; 	}
		else if(this.evil.contains(emo.toLowerCase())){ sc[0]=(float)0.659; sc[1]=(float)0.072; 	}
		else if(this.highfive.contains(emo.toLowerCase())){ sc[0]=(float)0.498; sc[1]=(float)0.081; 	}
		else if(this.cool.contains(emo.toLowerCase())){ sc[0]=(float)0.614125; sc[1]=(float)0.08749; 	}
		else if(this.party.contains(emo.toLowerCase())) { sc[0]=(float)0.6165; sc[1]=(float)0.1515; 	}
		else if(this.sick.contains(emo.toLowerCase())) { sc[0]=(float)0.305; sc[1]=(float)0.474; 	}
		else if(this.cheerful.contains(emo.toLowerCase())){ sc[0]=(float)0.583; sc[1]=(float)0.12375; 	}
		else if(this.rose.contains(emo.toLowerCase())){ sc[0]=(float)0.623; sc[1]=(float)0.023; 	}
		else if(this.heartBreak.contains(emo.toLowerCase())) { sc[0]=(float)0.293; sc[1]=(float)0.414; 	}
		else if(this.troubled.contains(emo.toLowerCase())) { sc[0]=(float)0.217; sc[1]=(float)0.591; 	}
		else if(this.respect.contains(emo.toLowerCase())) { sc[0]=(float)0.452; sc[1]=(float)0.183; 	}
		else if(this.amazed.contains(emo.toLowerCase())){ sc[0]=(float)0.452; sc[1]=(float)0.183; 	}
		return sc;
		}

	}

  //Calcule general du score des emoticons et emojis du tweet
  public float[] evaluateTweetEmoList(ArrayList<MotTag> taggedTwt, int twtlenght){

		float[] scores = new float[2];
		scores[0]=0;
		scores[1]=0;
		int j=0;

		int i=0; float coef=0;
		while(i < taggedTwt.size()){
			int count =0;
			j=i;
			if(taggedTwt.get(i).getTag().equals(",") || taggedTwt.get(i).getTag().equals("!") || taggedTwt.get(i).getTag().equals("E") || taggedTwt.get(i).getTag().equals("G") || taggedTwt.get(i).getTag().equals("^") || taggedTwt.get(i).getTag().equals("~")){

				while( j< taggedTwt.size() && taggedTwt.get(i).getMot().equals(taggedTwt.get(j).getMot())){
					count++; j++;
				}
				if(count==1) coef=(float)1.0;
				else if(count==2) coef=(float)1.25;
				else if(count>=3) coef=(float)1.5;
				float[] semoti= new float[2];
				float[] semoji= new float[2];
				//ajout de la produit avec le coef
				if(!taggedTwt.get(i).getMot().replaceAll("[a-zA-Z0-9=,;.*-+!:()/ \\ @ \"\'ゞ_^]", "").equals("") && (nonRepertoriedEmoji.contains(taggedTwt.get(i).getMot().replaceAll("[a-zA-Z0-9=,;.*-+!:()/ \\ @ \"\'ゞ_^]", ""))|| EmojiUtils.isEmoji(taggedTwt.get(i).getMot().replaceAll("[a-zA-Z0-9=,;.*-+!:()/ \\ @ \"\'ゞ_^]", "")))){
					semoji= this.getEmojiScoreIfExiste(taggedTwt.get(i).getMot());

				  scores[0]+=(float)(semoji[0]*coef);
					scores[1]+=(float)(semoji[1]*coef);

				}else{
					semoti= this.getEmoticonScoreIfExiste(taggedTwt.get(i).getMot());
					scores[0]+=(float)(semoti[0]*coef);
					scores[1]+=(float)(semoti[1]*coef);
				}

				i+=count;

			}else i++;
		}
		return scores;
	}


  public float[] evaluateTweetWord(ArrayList<MotTag> mots_tags){

    //Initialisation de score des mots
    float[] scoreWords = new float[2]; //Pos, Neg
    scoreWords[0]=(float)0;
    scoreWords[1]=(float)0;

    ArrayList<String> negations = new ArrayList(Arrays.asList("without against nor no useless never not cannot".split(" ")));
	  String negationForm = "n't";

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
      } else if(mt.getTag().equals("N") || mt.getTag().equals("A") || mt.getTag().equals("V") || mt.getTag().equals("R") || mt.getTag().equals("EXP")){
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

    return scoreWords;
  }

  @Override
  public void prepare(Map map, TopologyContext topologyContext,OutputCollector outputCollector){
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

      //Initialisation de score des mots
      float[] scoreWords = new float[2]; //Pos, Neg
		  scoreWords[0]=(float)0;
		  scoreWords[1]=(float)0;

      //initialisation du score des emojis/emoticons
      float[] scoreEmo = new float[2];
      scoreEmo[0]=(float)0;
      scoreEmo[1]=(float)0;

      //Evaluation des score pour les mots et des scores pour les émojis
      scoreWords= this.evaluateTweetWord(mots_tags);
      scoreEmo = this.evaluateTweetEmoList(mots_tags, tweet_text.length());

      //Calcule du score du tweet
      //sommer et faire la difference entre emoji et word pour l'instant
      float scoreTweet = (scoreWords[0]+scoreEmo[0]) - (scoreWords[1]+scoreEmo[1]);
      System.out.println("\n\n\n"+tweet_text+"\nWordScore= "+scoreWords[0]+"     "+scoreWords[1]);
      System.out.println("EmoScore= "+ scoreEmo[0] + "     "+scoreEmo[1]);
      System.out.println("TweetScore= "+ scoreTweet +"\n\n\n");

      //Emettre le status et son score
      this.collector.emit(new Values(motCles,tweet,scoreTweet));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    declarer.declare(new Fields("motCles","tweet","score"));
  }
}
