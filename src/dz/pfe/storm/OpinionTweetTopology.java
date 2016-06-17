package dz.pfe.storm;

import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.LocalCluster;

public class OpinionTweetTopology{
  public static void main(String[] args) throws Exception{
    //Instensiation de la topologie
    TopologyBuilder topologie = new TopologyBuilder();

    //Intensiation d'un spout
    //Ne pas oublier de remplacer par les clés twitter
    TweetSpout tweetSpout = new TweetSpout("","","","");

    //Création du schéma de la topologie
    //Cette topologie est appelée à changer avec l'insertion d'au moins un autre bolt
    //TweetSpout avec un parallèlisme de 1
    topologie.setSpout("tweetSpout",tweetSpout,1);

    /*
    //Attacher le SpaceSeparationBolt au spout via shuffle avec parallelism 10
    topologie.setBolt("SpaceSepatarionBolt",new SpaceSeparationBolt(),10).shuffleGrouping("tweetSpout");
    //Attacher le bolt de correction au bolt SpaceSepatarionBolt avec un parallelism de 10
    topologie.setBolt("LanguageCorrectionBolt",new LanguageCorrectionBolt(),10).shuffleGrouping("SpaceSepatarionBolt");
    //Attacher l'AcronymeBolt au LanguageCorrectionBolt via shuffle avec parallèlisme de 10
    topologie.setBolt("acronymeBolt",new AcronymeBolt(),10).shuffleGrouping("LanguageCorrectionBolt");
    //Attacher le POStagBolt aux AcronymeBolt via shuffle avec un parallelism de 15
    topologie.setBolt("POStagBolt",new POSTagBolt(),15).shuffleGrouping("acronymeBolt");
    //Attacher le PreExpressionBolt aux POSTagBolt via shuffle un parallelism de 15
    //topologie.setBolt("LettresBolt",new LettresBolt(),15).shuffleGrouping("POStagBolt");
    //Attacher le PreExpressionBolt aux POSTagBolt via shuffle un parallelism de 15
    topologie.setBolt("PreExpressionBolt",new ExpressionBolt(),15).shuffleGrouping("POStagBolt");
    //Attacher le DesambiguisationBolt aux PreExpressionBolt via shuffleGrouping parallelism de 15
    topologie.setBolt("DesambiguisationBolt",new DesambiguisationBolt(),15).shuffleGrouping("POStagBolt");
    //Attacher le PostExpressionBolt au DesambiguisationBolt via shuffleGrouping parallelism de 15
    topologie.setBolt("PostExpressionBolt",new ExpressionBolt(),15).shuffleGrouping("DesambiguisationBolt");
    //Attacher le ScoreBolt aux PostExpressionBolt via shuffle avec un parallelism de 15
    */

    topologie.setBolt("scoreBolt",new ScoreBolt(),15).shuffleGrouping("tweetSpout");
    //Attacher un DAOBolt aux scoreBolt via global avec un parallelism de 1
    topologie.setBolt("DAOBolt",new DAOBolt(),1).globalGrouping("scoreBolt");
    //Attacher un ReportBolt aux scoreBolt via global avec un parallelisme de 1
    topologie.setBolt("ReportBolt",new ReportBolt(),1).globalGrouping("scoreBolt");

    //Création de la configuration de la topologie
    //Instensiation
    Config configuration = new Config();
    //Activer le mode de debug
    configuration.setDebug(true);

    //Configuration pour cluster si presence d'arguements en entrée
    if(args != null && args.length > 0){
      //Configuration du nombre de workers par noeud de la topologie
      configuration.setNumWorkers(3);

      //Lancement de la topologie sur le cluster
      StormSubmitter.submitTopology(args[0],configuration,topologie.createTopology());

    } else {
      //Executer la topologie sur un cluster simulé localement

      //Configuration du nombre de threads
      configuration.setMaxTaskParallelism(3);

      //Instensiation d'un cluster local
      LocalCluster cluster = new LocalCluster();

      //lacement de la topologie sur le cluster local
      cluster.submitTopology("opinionTweetTopology",configuration,topologie.createTopology());

      // Pour le moment on va laisser la topologie tourner pendant 300 secondes
      Utils.sleep(300000);

      //Arrêter la topologie
      cluster.killTopology("opinionTweetTopology");

      //Arrête du cluster local
      cluster.shutdown();
    }
  }
}
