package dz.pfe.storm;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import backtype.storm.utils.Utils;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TweetSpout extends BaseRichSpout{

  private String consKey;
	private String secret;
	private String accToken;
	private String accTokenSecret;
	private ConfigurationBuilder conf;
	private TwitterStream twitterStream;
	private String[] motCles;
	private FilterQuery requete;
	private LinkedList<Status> tweets;

  private SpoutOutputCollector collector;

  public TweetSpout(String consKey,String secret,String accToken,String accTokenSecret){
    //Authentification
    this.consKey = consKey;
    this.secret = secret;
    this.accToken = accToken;
    this.accTokenSecret = accTokenSecret;
  }

  @Override
  public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector){
    //Initialisation de la file de tweets
		this.tweets = new LinkedList<Status>();

    // Sauvgarde le output pour emettre des tuples
    collector = spoutOutputCollector;

    //Configurationbuilder
		conf = new ConfigurationBuilder().
				setOAuthConsumerKey(this.consKey).
				setOAuthConsumerSecret(this.secret).
				setOAuthAccessToken(this.accToken).
				setOAuthAccessTokenSecret(this.accTokenSecret);

    //TwitterStream factory
    twitterStream = new TwitterStreamFactory(conf.build()).getInstance();

    //Ajouter un listner au flux
		twitterStream.addListener(new StatusListener(){
			//Méthode qui permet de récupérer les tweet (status)
			@Override
			public void onStatus(Status tweet){
        //Check que le tweet n'est pas un retweet
        if (!tweet.isRetweet()){
          tweets.add(tweet);
        }
			}

			@Override
			public void onException(Exception arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTrackLimitationNotice(int arg0) {
				// TODO Auto-generated method stub

			}
		});

    //Lecture des mot clés
    //Ce fichier doit être rempli à partir de l'application web
    String fichierMotCles = "keywords";
    BufferedReader br = null;
    String line = "";
    List<String> keywords = new ArrayList<String>();

    try {
      br = new BufferedReader(new FileReader(fichierMotCles));

      //Parcours des ligne du fichier, chaque ligne représente un mot clé
      while((line = br.readLine()) != null) {
        keywords.add(line);
      }

    } catch (FileNotFoundException e){
      e.printStackTrace();
    } catch (IOException e){
      e.printStackTrace();
    } finally {
      if(br != null){
        try {
          br.close();
        } catch (IOException e){
          e.printStackTrace();
        }
      }
    }

    //Transformation de la liste en tableau
    this.motCles = new String[keywords.size()];
    keywords.toArray(this.motCles);

    //Création de la réquête
		this.requete= new FilterQuery();
		this.requete.track(this.motCles);
		this.requete.language("en");

		//Ajout de la requête au flux
		twitterStream.filter(this.requete);
  }

  @Override
  public void nextTuple(){
	//Tentative de récupération d'un tweet
	Status tweet = this.tweets.poll();

	//Si il n'y a pas de tweet dans la liste
    if(tweet == null) {
      Utils.sleep(50);
      return;
    }

    //Enettre le tweet au prochain bolt
    collector.emit(new Values(this.motCles,tweet));
  }

  @Override
  public void close(){
    this.twitterStream.shutdown();
  }

  @Override
  public Map<String, Object> getComponentConfiguration()
  {
    // Création de la config
    Config config = new Config();

    //Le parallèlisme maximal est de 1
    config.setMaxTaskParallelism(1);

    return config;
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer){
    outputFieldsDeclarer.declare(new Fields("motCles","tweet"));
  }
}
