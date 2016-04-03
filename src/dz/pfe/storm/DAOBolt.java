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
      this.connect = DriverManager.getConnection("jdbc:mysql://localhost/twitter_analytics?"+"user=twitter_admin&MotDePasseIci");

    } catch (Exception e) {
      e.printStackTrace();
      this.close();
    }
  }

  @Override
  public void execute(Tuple tuple){
    //En supposant que le status est à la position 0
    Status tweet = (Status) tuple.getValue(0);
    User utilisateur = tweet.getUser();
    //En supposant que le score est à la position 1
    float score = (float) tuple.getValue(1);

    //Insertions dans la base de données
    try{
      //Utilisateur :
      //Création de la déclaration
      this.declaration = this.connect.createStatement();
      //Test de la présence de l'utilisateur dans la base de données
      this.resultSet= this.declaration.executeQuery("SELECT * FROM twitter_analytics.utilisateur WHERE nom_ecran = '"+utilisateur.getScreenName()+"'");
      //Insrtion de l'utilisateur dans la base de données si ce dernier n'existe pas
      if(!this.resultSet.next()){
        //Préparation de la déclaration d'insertion
        this.preparedStatement = connect.prepareStatement("INSERT INTO twitter_analytics.utilisateur VALUES(?,?,?,?,?)");
        //Remplissage de la requête (les indices commencent à partir de 1)
        this.preparedStatement.setString(1,utilisateur.getScreenName());
        this.preparedStatement.setInt(2,utilisateur.getFollowersCount());
        this.preparedStatement.setInt(3,utilisateur.getFriendsCount());
        this.preparedStatement.setInt(4,utilisateur.getStatusesCount());
        this.preparedStatement.setString(5,utilisateur.getName());
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
        this.preparedStatement.setString(5,utilisateur.getScreenName());
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

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer){
    //Pour l'instant le bolt n'émet rien du tout donc c'est vide
  }
}
