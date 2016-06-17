package recommandation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;

public class Recommand {

	public ArrayList<String> StopWords;

	public HashMap<String, HashMap<String,Float>> invFile;
	public HashMap<String, HashMap<String,Float>> invFileBigram;

	public HashMap<String,Float> sumDist;
	public HashMap<String,Float> sumDistBigram;
	public ArrayList<String> listMinDist;
	public ArrayList<String> listMinDistBigram;

	private String keywords;
	private Timestamp temps;

	private RedisConnection redis;


	public Recommand(){
		//Lecture de mots clés
		this.readMotCle();

		//Lecture du dictionnaire de mots vides
		this.stopWordDico();

		//Calcul du temps il y a deux minutes pour les reqêtes SQL
		this.tempsMoinsTwoMinutes();

		//Initialisation de Redis
		RedisClient client = new RedisClient("localhost",6379);
	    this.redis = client.connect();
	}

	private void redisReportPositif(){
		String aEnvoyer = null;
		for(String term : this.listMinDist){
			aEnvoyer = aEnvoyer + term + " ";
		}

		redis.lpush("unigramPositif", aEnvoyer);

		aEnvoyer = null;
		for(String term : this.listMinDistBigram){
			aEnvoyer = aEnvoyer + term + " | ";
		}

		redis.lpush("bigramPositif", aEnvoyer);

	}

	private void redisReportNegatif(){
		String aEnvoyer = null;
		for(String term : this.listMinDist){
			aEnvoyer = aEnvoyer + term + " ";
		}

		redis.lpush("unigramNegatif", aEnvoyer);

		aEnvoyer = null;
		for(String term : this.listMinDistBigram){
			aEnvoyer = aEnvoyer + term + " | ";
		}

		redis.lpush("bigramNegatif", aEnvoyer);

	}

	private void tempsMoinsTwoMinutes(){
		//Récupérer le temps actuel
		LocalDateTime now = LocalDateTime.now();

		//En soustraire 2 minutes
		LocalDateTime moinsDeux = now.minusMinutes(2);

		//Convertir en timeStamp et l'affcter à temps
		this.temps = Timestamp.valueOf(moinsDeux);
	}

	//Méthode de lecture de mots-clés
	private void readMotCle(){
		String txtFile = "keywords";
		BufferedReader br = null;
		String line = "";


		try {
			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {
				this.keywords = line;
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

	//Récuperation des tweets à partir de la base de données MySQL

	public ArrayList<String> getPositiveTweets() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{

		ArrayList<String> tweets = new ArrayList<String>();

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost/twitter_analytics?"+"user=twitter_admin&password=azerty1234");

		Statement st = con.createStatement();
		String sql = ("SELECT text_tweet FROM twitter_analytics.status, twitter_analytics.tweet_mot_cle WHERE id_tweet = id AND score > 0 AND mot_cle = '" + this.keywords + "' AND temps_tweet > '"+ this.temps + "';");

		int i = 0;
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()) {

			String str = rs.getString("text_tweet");
			tweets.add(str);
			i++;

		}
		System.out.println(i);
		con.close();

		return tweets;
	}

	public ArrayList<String> getNegativeTweets() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{

		ArrayList<String> tweets = new ArrayList<String>();

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost/twitter_analytics?"+"user=twitter_admin&password=azerty1234");

		Statement st = con.createStatement();
		String sql = ("SELECT text_tweet FROM twitter_analytics.status, twitter_analytics.tweet_mot_cle WHERE id_tweet = id AND score < 0 AND mot_cle = '" + this.keywords + "' AND temps_tweet > '"+ this.temps + "';");
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()) {
			String str = rs.getString("text_tweet");
			tweets.add(str);
		}

		con.close();

		return tweets;
	}

	//Liste de mots vides anglais
	public void stopWordDico(){
		StopWords= new ArrayList<String>();

		String txtFile = "Dictionnaires/stop-word-list.txt";
		BufferedReader br = null;
		String line = "";


		try {

			br = new BufferedReader(new FileReader(txtFile));
			while ((line = br.readLine()) != null) {

				String stopWord = line;

				StopWords.add(stopWord);

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

	//Indexation des unigrammes et bigrammes dans les fichiers inverses avec leurs fréquences
	public void fichierInverse(ArrayList<String> tweets){

		invFile = new HashMap<String,HashMap<String,Float>>();
		String[] tweetToken;

		for(int i=0;i<tweets.size();i++){
			tweetToken= tweets.get(i).toLowerCase().replaceAll("[#,.;:!?]", "").split(" ");
			HashMap<String,Float> docList;
			for(int j=0; j<tweetToken.length;j++){
				if(!StopWords.contains(tweetToken[j])){
					if(invFile.containsKey(tweetToken[j])){
						docList = invFile.get(tweetToken[j]);

						if(docList.containsKey(tweets.get(i))){
							docList.replace(tweets.get(i), (float)(docList.get(tweets.get(i))+1));
							invFile.replace(tweetToken[j], invFile.get(tweetToken[j]), docList);
						}else{
							docList.put(tweets.get(i), (float)1);
							invFile.replace(tweetToken[j],docList);
						}
					}else{
						docList = new HashMap<String,Float>();
						docList.put(tweets.get(i),(float)1);
						invFile.put(tweetToken[j], docList);
					}
				}
			}
		}
	}


	public void fichierInverseBigram(ArrayList<String> tweets){

		invFileBigram = new HashMap<String,HashMap<String,Float>>();
		String[] tweetToken;

		for(int i=0;i<tweets.size();i++){
			tweetToken= tweets.get(i).toLowerCase().replaceAll("[#,.;:!?]","").split(" ");
			ArrayList<String> tweetBigrams = new ArrayList<String>();

			for(int j=0; j< tweetToken.length -1;j++){
				if(!StopWords.contains(tweetToken[j])){
					tweetBigrams.add(tweetToken[j]+"/"+tweetToken[j+1]);
				}
			}
			HashMap<String,Float> docList=null;

			for(int j=0; j<tweetBigrams.size();j++){

				if(invFileBigram.containsKey(tweetBigrams.get(j))){
					docList = invFileBigram.get(tweetBigrams.get(j));

					if(docList.containsKey(tweets.get(i))){
						docList.replace(tweets.get(i), (float)(docList.get(tweets.get(i))+1));
						invFileBigram.replace(tweetBigrams.get(j), invFileBigram.get(tweetBigrams.get(j)), docList);
					}else{
						docList.put(tweets.get(i), (float)1);
						invFileBigram.replace(tweetBigrams.get(j),docList);
					}
				}else{
					docList = new HashMap<String,Float>();
					docList.put(tweets.get(i),(float)1);
					invFileBigram.put(tweetBigrams.get(j), docList);

				}

			}
		}
	}

	//Calcul des poids TF*IDF
	public HashMap<String,HashMap<String,Float>> calculateTfIdf( int tailltwt){
		float tfidf=0;
		for (Map.Entry<String, HashMap<String,Float>> entry : invFile.entrySet()){

			String term = entry.getKey();
			HashMap<String,Float> docList = entry.getValue();
			float tf=0;
			int fd=0;
			for(Map.Entry<String, Float> doc : docList.entrySet()){
				tf =(float)doc.getValue();
				fd= docList.size();

				tfidf= (float)(tf*(Math.log((float)(tailltwt/(float)fd))/Math.log(10)));
				docList.replace(doc.getKey(), tfidf);
			}
			invFile.replace(term, docList);
		}
		return invFile;
	}


	public HashMap<String,HashMap<String,Float>> calculateTfIdfBigram( int tailltwt){
		float tfidf=0;
		for (Map.Entry<String, HashMap<String,Float>> entry : invFileBigram.entrySet()){

			String term = entry.getKey();
			HashMap<String,Float> docList = entry.getValue();
			float tf=0;
			int fd=0;
			for(Map.Entry<String, Float> doc : docList.entrySet()){
				tf =(float)doc.getValue();
				fd= docList.size();
				tfidf= (float)(tf*Math.log10((float)(tailltwt/(float)fd)));
				docList.replace(doc.getKey(), tfidf);
			}
			invFileBigram.replace(term, docList);
		}
		return invFileBigram;
	}

	//Calculs des distances cosinus
	public HashMap<String,HashMap<String,Float>> cosDist(){

		HashMap<String,Float> sommeDj = new HashMap<String,Float>();

		for(Map.Entry<String, HashMap<String,Float>> entry : invFile.entrySet()){
			HashMap<String,Float> docList = entry.getValue();
			for(Map.Entry<String, Float> doc : docList.entrySet()){
				String tweet = doc.getKey();
				if(!sommeDj.containsKey(tweet)){
					sommeDj.put(tweet, doc.getValue()*doc.getValue());
				}else{
					sommeDj.replace(tweet, sommeDj.get(tweet) + (doc.getValue()*doc.getValue()));
				}
			}
		}

		for(Map.Entry<String, HashMap<String,Float>> entry : invFile.entrySet()){
			String term = entry.getKey();
			HashMap<String,Float> docList = entry.getValue();

			for(Map.Entry<String, Float> doc : docList.entrySet()){
				String tweet = doc.getKey();
				float cosd= (doc.getValue())/(float)Math.sqrt(sommeDj.get(tweet));
				docList.replace(tweet, cosd);
			}
			invFile.replace(term, docList);
		}

		return invFile;
	}

	public HashMap<String,HashMap<String,Float>> cosDistBigram(){
		HashMap<String,Float> sommeDj = new HashMap<String,Float>();

		for(Map.Entry<String, HashMap<String,Float>> entry : invFileBigram.entrySet()){
			HashMap<String,Float> docList = entry.getValue();
			for(Map.Entry<String, Float> doc : docList.entrySet()){
				String tweet = doc.getKey();
				if(!sommeDj.containsKey(tweet)){
					sommeDj.put(tweet, doc.getValue()*doc.getValue());
				}else{
					sommeDj.replace(tweet, sommeDj.get(tweet) + (doc.getValue()*doc.getValue()));
				}
			}
		}

		for(Map.Entry<String, HashMap<String,Float>> entry : invFileBigram.entrySet()){
			String term = entry.getKey();
			HashMap<String,Float> docList = entry.getValue();

			for(Map.Entry<String, Float> doc : docList.entrySet()){
				String tweet = doc.getKey();
				float cosd=(float)(doc.getValue())/(float)Math.sqrt(sommeDj.get(tweet));
				docList.replace(tweet, cosd);
			}
			invFileBigram.replace(term, docList);
		}

		return invFileBigram;
	}

	//Moyenne des distances de chaque unigrammes et bigramme par rapport aux tweets.
	public void moyenneCosDist(){

		sumDist = new HashMap<String,Float>();
		float somme=0;
		String term="";
		for (Map.Entry<String, HashMap<String,Float>> entry : invFile.entrySet()) {
			somme=0;
			term = entry.getKey();
			HashMap<String,Float> dist= entry.getValue();
			for (Map.Entry<String, Float> entry1 : dist.entrySet()) {
				somme+=(float)entry1.getValue();
			}

			sumDist.put(term, (float)(somme/(float)dist.size()));

		}

	}

	public void moyenneCosDistBigram(){

		sumDistBigram = new HashMap<String,Float>();
		float somme=0;
		String term="";
		for (Map.Entry<String, HashMap<String,Float>> entry : invFileBigram.entrySet()) {
			somme=0;
			term = entry.getKey();
			HashMap<String,Float> dist= entry.getValue();
			for (Map.Entry<String, Float> entry1 : dist.entrySet()) {
				somme+=(float)entry1.getValue();
			}

			sumDistBigram.put(term, (float)(somme/(float)dist.size()));

		}
	}

	//Recuperation des 2 unigrammes et bigrammes dont la moyenne des distance est la plus petite
	public void MinDist(){

		listMinDist= new ArrayList<String>();
		int i=0;

		while(!sumDist.isEmpty() && i<=2){
			float min=1;
			String term ="";
			for (Map.Entry<String,Float> entry : sumDist.entrySet()) {
				if(min>entry.getValue()){
					min=entry.getValue();
					term=entry.getKey();
				}
			}
			System.out.println(term);
			sumDist.remove(term);
			listMinDist.add(term);
			i++;
		}
	}

	public void MinDistBigram(){

		listMinDistBigram= new ArrayList<String>();
		int i=0;

		while(!sumDistBigram.isEmpty() && i<=2){
			float min=1;
			String term=null;
			for (Map.Entry<String,Float> entry : sumDistBigram.entrySet()) {
				if(min>entry.getValue()){
					min=entry.getValue();
					term=entry.getKey();
				}
			}
			System.out.println(term.replace("/", " "));
			sumDistBigram.remove(term);
			listMinDistBigram.add(term);
			i++;
		}
	}

	//Affichage des fichiers inverses (dreq + poids)
	public void showFichierInv(){

		for (Map.Entry<String, HashMap<String,Float>> entry : invFile.entrySet()) {

			String key=entry.getKey();
			System.out.println(key +":\n");
			HashMap<String,Float> docs= entry.getValue();

			for (Map.Entry<String,Float> entry1 : docs.entrySet()) {
				String tweet= entry1.getKey();
				Float freq= entry1.getValue();
				System.out.println("\""+tweet+"\" "+ freq + "\n");
			}
			System.out.println("*************************************\n");
		}
		for (Map.Entry<String,Float> entry : sumDist.entrySet()) {
			System.out.println(entry.getKey()+" "+ entry.getValue()+"\n");
		}

	}


	public void showFichierInvBigram(){

		for (Map.Entry<String, HashMap<String,Float>> entry : invFileBigram.entrySet()) {

			String key=entry.getKey();
			System.out.println(key.toString() +":\n");
			HashMap<String,Float> docs= entry.getValue();

			for (Map.Entry<String,Float> entry1 : docs.entrySet()) {
				String tweet= entry1.getKey();
				Float freq= entry1.getValue();
				System.out.println("\""+tweet+"\" "+ freq + "\n");
			}
			System.out.println("*************************************\n");
		}
		for (Map.Entry<String,Float> entry : sumDistBigram.entrySet()) {
			System.out.println(entry.getKey()+" "+ entry.getValue()+"\n");
		}

	}

	//Exécution des recommandations unigramme et bigramme
	public void unigramRecommandation(ArrayList<String> tweets){
		this.fichierInverse(tweets);
		this.calculateTfIdf(tweets.size());
		this.cosDist();
		this.moyenneCosDist();
		this.MinDist();

	}

	public void bigramRecommandation(ArrayList<String> tweets){
		this.fichierInverseBigram(tweets);
		this.calculateTfIdfBigram(tweets.size());
		this.cosDistBigram();
		this.moyenneCosDistBigram();
		this.MinDistBigram();

	}


	public static void main(String[] args) {
		//Créer un objet de ce type
		Recommand rec = new Recommand();
		ArrayList<String> tweetsPositifs = null;
		ArrayList<String> tweetsNegatifs = null;


		//Reste du traitement
		try {
			//Récupération textes des tweets d'il y a 2 minutes
			//Les positif
			tweetsNegatifs =  rec.getNegativeTweets();
			rec.unigramRecommandation(tweetsNegatifs);
			rec.bigramRecommandation(tweetsNegatifs);
			rec.redisReportNegatif();


			//Les positif
			tweetsPositifs = rec.getPositiveTweets();
			rec.unigramRecommandation(tweetsPositifs);
			rec.bigramRecommandation(tweetsNegatifs);
			rec.redisReportPositif();


		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}
}
