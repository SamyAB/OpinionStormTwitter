package dz.pfe.storm.ressources;

public class MotTag{
  private String mot;
  private String tag;

  public MotTag(String mot,String tag){
    this.mot = mot;
    this.tag = tag;
  }

  public String getMot(){
    return this.mot;
  }

  public String getTag(){
    return this.tag;
  }

  public void setMot(String mot){
    this.mot = mot;
  }

  public void setTag(String tag){
    this.tag = tag;
  }

  public String toString(){
    return this.mot+"/"+this.tag;
  }
}
