package dz.pfe.storm;

import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.topology.OutputFieldsDeclarer;

import twitter4j.Status;
import twitter4j.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

public class DAOBolt extends BaseRichBolt{
  private OutputCollector collector;

  private Connection connect = null;
	private Statement declaration = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

  //Méthode à utiliser en cas d'arrêt d'utilisation de la base de donnée seulement !
  public void close(){
    try{
      if(this.resultSet != null){
        this.resultSet.close();
      }
      if(this.declaration != null){
        this.declaration.close();
      }
      if(this.connect != null){
        this.connect.close();
      }
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public void prepare(Map map,TopologyContext topologyContext, OutputCollector outputCollector){
    this.collector = outputCollector;

    //Initialisation des attributs de la base de données
    try{
      //Chargement du driver JDBC
      Class.forName("com.mysql.jdbc.Driver");

      //Connexion au serveur de base de donnée
      //IMPORTANT : Ne pas oublier de mettre le mot de passe pour pour twitter_admin
      //Ceci dit bla 3yate
      this.connect = DriverManager.getConnection("jdbc:mysql://localhost/twitter_analytics?"+"user=twitter_admin&password=");

    } catch (Exception e) {
      e.printStackTrace();
      this.close();
    }
  }

  @Override
  public void execute(Tuple tuple){
    if(tuple!=null){
      String[] motCles = (String[]) tuple.getValue(0);
      //En supposant que le status est à la position 1
      Status tweet = (Status) tuple.getValue(1);
      User utilisateur = tweet.getUser();
      //En supposant que le score est à la position 2
      float score = (float) tuple.getValue(2);

      //Insertions dans la base de données
      try{
        //Utilisateur :
        //Création de la déclaration
        this.declaration = this.connect.createStatement();
        //Test de la présence de l'utilisateur dans la base de données
        this.resultSet= this.declaration.executeQuery("SELECT * FROM twitter_analytics.utilisateur WHERE id = '"+utilisateur.getId()+"'");
        //Insertion de l'utilisateur dans la base de données si ce dernier existe on mets à jour son tuple et on le réinsert
        if(this.resultSet.next()){
          //L'utilisateur existe dans la base de donnée
          this.preparedStatement = connect.prepareStatement("UPDATE twitter_analytics.utilisateur SET nom_ecran = ? , date_entree = ? , nombre_followers = ? , nombre_follows = ? , nombre_tweets = ? , nom = ? WHERE id = ?");
          this.preparedStatement.setString(1,utilisateur.getScreenName());
          this.preparedStatement.setTimestamp(2, new java.sql.Timestamp(tweet.getCreatedAt().getTime()));
          this.preparedStatement.setInt(3,utilisateur.getFollowersCount());
          this.preparedStatement.setInt(4,utilisateur.getFriendsCount());
          this.preparedStatement.setInt(5,utilisateur.getStatusesCount());
          this.preparedStatement.setString(6,utilisateur.getName());
          this.preparedStatement.setString(7,String.valueOf(utilisateur.getId()));
          //Envoyer la requête à la base de données
          this.preparedStatement.executeUpdate();
        } else {
          //L'utilisateur n'existe pas dans la base de donnée on doit l'
          //Préparation de la déclaration d'insertion
          this.preparedStatement = connect.prepareStatement("INSERT INTO twitter_analytics.utilisateur VALUES(?,?,?,?,?,?,?)");
          //Remplissage de la requête (les indices commencent à partir de 1)
          this.preparedStatement.setString(1,String.valueOf(utilisateur.getId()));
          this.preparedStatement.setString(2,utilisateur.getScreenName());
          this.preparedStatement.setTimestamp(3, new java.sql.Timestamp(tweet.getCreatedAt().getTime()));
          this.preparedStatement.setInt(4,utilisateur.getFollowersCount());
          this.preparedStatement.setInt(5,utilisateur.getFriendsCount());
          this.preparedStatement.setInt(6,utilisateur.getStatusesCount());
          this.preparedStatement.setString(7,utilisateur.getName());
          //Envoyer la requête à la base de données
          this.preparedStatement.executeUpdate();
        }


        //tweet :
        //Test de la présence du tweet dans la base de données
        //Création de la déclaration
        this.declaration = this.connect.createStatement();
        this.resultSet = this.declaration.executeQuery("SELECT * FROM twitter_analytics.status WHERE id = '"+tweet.getId()+"'");
        //Insertion du tweet dans la base de données si ce dernier n'existe pas
        if(!this.resultSet.next()){
          //préparation de la requête
          this.preparedStatement = connect.prepareStatement("INSERT INTO twitter_analytics.status VALUES(?,?,?,?,?,?,?,?)");
          this.preparedStatement.setString(1,String.valueOf(tweet.getId()));
          this.preparedStatement.setString(2,tweet.getText());
          this.preparedStatement.setFloat(3,score);
          this.preparedStatement.setTimestamp(4, new java.sql.Timestamp(tweet.getCreatedAt().getTime()));
          this.preparedStatement.setString(5,String.valueOf(utilisateur.getId()));
          this.preparedStatement.setInt(6,tweet.getFavoriteCount());
          this.preparedStatement.setInt(7,tweet.getRetweetCount());
          if(tweet.getPlace() != null && tweet.getPlace().getFullName() != null){
            this.preparedStatement.setString(8,tweet.getPlace().getName());
          }
          else{
            this.preparedStatement.setNull(8,Types.VARCHAR);
          }
          //Envoyer la requête à la base de données
          this.preparedStatement.executeUpdate();
        }

        //Parcourt de motCles
        for(String m_c : motCles){
          if(tweet.getText().toLowerCase().contains(m_c.toLowerCase())){
            //tweet motCles:
            //Test de la présence du tweet avec ce mot clé dans la base de données
            //Création de la déclaration
            this.declaration = this.connect.createStatement();
            this.resultSet = this.declaration.executeQuery("SELECT * FROM twitter_analytics.tweet_mot_cle WHERE id_tweet = '"+tweet.getId()+"' AND mot_cle = '"+m_c.toLowerCase()+"'");
            //Insertion du tweet dans la base de données si ce dernier n'existe pas
            if(!this.resultSet.next()){
              //préparation de la requête
              this.preparedStatement = connect.prepareStatement("INSERT INTO twitter_analytics.tweet_mot_cle VALUES(?,?)");
              this.preparedStatement.setString(1,String.valueOf(tweet.getId()));
              this.preparedStatement.setString(2,m_c.toLowerCase());

              //Envoyer la requête à la base de données
              this.preparedStatement.executeUpdate();
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    //Pour l'instant le bolt n'émet rien du tout donc c'est vide
  }
}
