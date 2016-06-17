var loadingTime = 10000; // 60 secondes
//interval entre chaque update de données
var updareInterval = 4000; // 4 secondes
//Interval de mise à jour des recommandations
var updateRecommandation = 120000; //deux minutes

//Variables utilisées dans la visualisation
var comptes = {};
var tweets = [];
var topTweets = {};
var keywords = window.keywords;
var startTime = "";
var nbTwt = 0;
var tweetData=[];
var tweetPerSec=[];
var months={};
months["Jan"]='01';
months["Feb"]='02';
months["Mar"]='03';
months["Apl"]='04';
months["May"]='05';
months["Jun"]='06';
months["Jul"]='07';
months["Aou"]='08';
months["Seb"]='09';
months["Oct"]='10';
months["Nov"]='11';
months["Dec"]='12';

//Initialisation des comptes des tweets négatifs, positifs, neutres
comptes["Negative"] = 0;
comptes["Neutral"] = 0;
comptes["Positive"] = 0;
topTweets["Negative"]={ score :0, tweet_text :" ", name:"", screenName:"" , favoriteCount:0 , retweetCount:0,ppURL:"https://g.twimg.com/dev/documentation/image/DTC_Services_1h_hero_bg.png", _time:"00:00:00" };
topTweets["Neutral"]={ score :0, tweet_text :" ", name:"", screenName:"",  favoriteCount:0 , retweetCount:0, ppURL:"https://g.twimg.com/dev/documentation/image/DTC_Services_1h_hero_bg.png",_time:"00:00:00"};
topTweets["Positive"]={ score :0, tweet_text :" ", name:"", screenName:"", favoriteCount:0 , retweetCount:0,ppURL:"https://g.twimg.com/dev/documentation/image/DTC_Services_1h_hero_bg.png",_time:"00:00:00"};
positionHist = -1;

//Variables pour la viz de tweets
tweetsNeg = [];
tweetsPos = [];
nbTwtN = 0;
nbTwtP = 0;
var p=0;
var n=0;

var currencyFormat = d3.format("0.2f");

svgNeg = d3.select("#negative").append("svg").attr('height', 660).attr('width', 650);
svgPos = d3.select("#positive").append("svg").attr('height', 660).attr('width', 650);;

var yRectR=20;
var yImgR=35;
var yPseudoIdR=47;
var yTwtR=59;
var yScoreR=100;

var yRectL=20;
var yImgL=35;
var yPseudoIdL=47;
var yTwtL=59;
var yScoreL=100;

//Des trucs dans les airs
//var startTime=vis.moment();
var startTime = Date();
var container = document.getElementById('chronologie');
nbTwtD = 0;

var options = {
  editable: false,
  timeAxis: {scale: 'millisecond', step: 1000},
  maxHeight: "300px",
  height:"300px",
};

var items = new vis.DataSet(tweetData);

var timeline = new vis.Timeline(container, items, options);
timeline.on('select', function (props) {
  d3.select("#chronologie").selectAll("svg").remove();
  if(tweets[props.items]!=null){
    drawTweet(tweetPerSec[props.items]);
  }
});

//Attente du temps de lancement de apache storm avant de lancer l'affichage des viz
window.setTimeout(function(){
  //Cacher l'animation de chargement
  $(function(){
      $(".loading").fadeOut();
  });

  //Enregistrement du temps du début de traitements
  startTime = Date();

  //Dessin des mot-clefs et date de début en haut des visualisation
  afficherMotCle(startTime,keywords,".keywordstime");

  //Premier update
  update();
  //Lancement de la fonction update chaque updateInterval
  window.setInterval(update,updareInterval);
  //Lancement du timer pour la visualisation des recommandations
  window.setInterval(getRecommandation,updateRecommandation)
},loadingTime);

//Fonction de récupération de recommandations
function getRecommandation(){
  $(function(){
    $.get(
      'recommandation.php', //Script serveur qui récupère les informations de redis
      'false', //On n'envoie aucun paramètre à redis.php
      function(data){
        //effacer les recommandations précédentes
        d3.select("#recommandation").selectAll("svg").remove();

        var svg= d3.select("#recommandation").append("svg").attr('height', 500).attr('width', 1200);
      	//rectangle principale
      	svg.append('rect').attr('width', 700)
      		.attr('height', 300)
      		.attr('x', 330)
      		.attr('y', 140)
      		.attr('rx',25)
      		.attr('ry',25)
      		.attr('opacity','0.7')
      		.style('fill','#E8E8E8')
      		.style('stroke-width','5px')
      		.style('stroke','#E8E8E8');

        svg.append('text').text('Recommandations ')
      		.attr('x',280)
      		.attr('y',130)
      		.attr('fill','#B0B0B0 ')
      		.style("font-size","35px");

        //positive recommandations
      	svg.append('rect').attr('width', 300)
      		.attr('height', 150)
      		.attr('x', 390)
      		.attr('y', 180)
      		.attr('rx',25)
      		.attr('ry',25)
      		.attr('opacity',0.8)
      		.style('fill','greenyellow');

        svg.append('text').text('Unigramme ')
      		.attr('x',410)
      		.attr('y',210)
      		.attr('fill','white')
      		.style("font-size","25px");

        svg.append('text').text(data.unigramPos[0][0] + ", "+data.unigramPos[0][1])
      		.attr('x',410)
      		.attr('y',235)
      		.attr('fill','gray')
      		.style("font-size","18px");

        svg.append('text').text('Bigramme ')
      		.attr('x',410)
      		.attr('y',265)
      		.attr('fill','white')
      		.style("font-size","25px");

        svg.append('text').text(data.bigramPos[0][0] + ", "+data.bigramPos[0][1])
      		.attr('x',410)
      		.attr('y',290)
      		.attr('fill','gray')
      		.style("font-size","18px");

      	//negative recommandations
      	svg.append('rect').attr('width', 300)
      		.attr('height', 150)
      		.attr('x', 670)
      		.attr('y', 270)
      		.attr('rx',25)
      		.attr('ry',25)
      		.attr('opacity',0.8)
      		.style('fill','#55ACEE');

        svg.append('text').text('Unigramme ')
      		.attr('x',690)
      		.attr('y',300)
      		.attr('fill','white')
      		.style("font-size","25px");

        svg.append('text').text(data.unigramNeg[0][0] + ", "+data.unigramNeg[0][1])
      		.attr('x',690)
      		.attr('y',325)
      		.attr('fill','gray')
      		.style("font-size","18px");

        svg.append('text').text('Bigramme ')
      		.attr('x',690)
      		.attr('y',355)
      		.attr('fill','white')
      		.style("font-size","25px");
        //data.bigramNeg[0].replace("∕"," ") et data.bigramNeg[1].replace("∕"," ")
        svg.append('text').text(data.bigramNeg[0][0] + ", "+data.bigramNeg[0][1])
      		.attr('x',690)
      		.attr('y',380)
      		.attr('fill','gray')
      		.style("font-size","18px");

      },
      'json'
    );
  });
}

//Fonction de rechargement des vizualisations
function update(){
  $(function(){
    $.get(
      'redis.php', //Script serveur qui récupère les informations de redis
      'false', //On n'envoie aucun paramètre à redis.php
      function(data){
        //Ici se trouve tout ce qu'on doit faire après avoir récupérer les informations de Redis
        for(var i=0; i< data.tweets.length ;i++){

          tweets[nbTwt]={score:data.tweets[i][1] ,ppURL:data.tweets[i][8], pseudo:data.tweets[i][4],userId:data.tweets[i][3],tweet_text:data.tweets[i][2], postedAt:data.tweets[i][7].split(" ")[3]};
          nbTwt = nbTwt+1;

          if(data.tweets[i][0]=="1"){
            comptes["Negative"] = comptes["Negative"]+1;
            if(Math.abs(parseFloat(topTweets["Negative"].score)) <= Math.abs(parseFloat(data.tweets[i][1]))){
              topTweets["Negative"].score=data.tweets[i][1];
              topTweets["Negative"].tweet_text=data.tweets[i][2];
              topTweets["Negative"].name=data.tweets[i][3];
              topTweets["Negative"].screenName=data.tweets[i][4];
              topTweets["Negative"].favoriteCount=data.tweets[i][5];
            	topTweets["Negative"].retweetCount=data.tweets[i][6];
            	topTweets["Negative"].ppURL=data.tweets[i][8];
            	topTweets["Negative"]._time=data.tweets[i][7].split(" ")[3];
            }
          }

          if(data.tweets[i][0]=="3"){
            comptes["Positive"] = comptes["Positive"]+1;
            if(Math.abs(parseFloat(topTweets["Positive"].score)) <= Math.abs(parseFloat(data.tweets[i][1]))){
              topTweets["Positive"].score = data.tweets[i][1];
              topTweets["Positive"].tweet_text=data.tweets[i][2];
              topTweets["Positive"].name=data.tweets[i][3];
              topTweets["Positive"].screenName = data.tweets[i][4];
              topTweets["Positive"].favoriteCount=data.tweets[i][5];
            	topTweets["Positive"].retweetCount=data.tweets[i][6];
            	topTweets["Positive"].ppURL=data.tweets[i][8];
            	topTweets["Positive"]._time=data.tweets[i][7].split(" ")[3];
            }
          }

          if(data.tweets[i][0]=="2"){
            comptes["Neutral"] = comptes["Neutral"]+1;
          }

          var mh=months[data.tweets[i][7].split(" ")[1]];
          var yr=data.tweets[i][7].split(" ")[5];
          var dy=data.tweets[i][7].split(" ")[2];
          var time=data.tweets[i][7].split(" ")[3];
          var hr = time.split(":")[0];
          var mn = time.split(":")[1];
          var sec = time.split(":")[2];

          if(data.tweets[i][0]=="1"){
            tweetsNeg[nbTwtN]={tweet_text: data.tweets[i][2], pseudo: data.tweets[i][4], id: data.tweets[i][3], ppURL:data.tweets[i][8], tweet_color:'#3399CC', score:data.tweets[i][1]};
            nbTwtN+=1;
          } else if(data.tweets[i][0]=="3"){
            tweetsPos[nbTwtP]={tweet_text: data.tweets[i][2], pseudo: data.tweets[i][4], id: data.tweets[i][3], ppURL:data.tweets[i][8], tweet_color:'greenyellow', score:data.tweets[i][1]};
            nbTwtP+=1;
          }

          var found = false;
          var color="gray";
          var twtimg="http://bloximages.newyork1.vip.townnews.com/dailyprogress.com/content/tncms/live/global/resources/images/_site/social/twitter-logo-cutout.png?_dc=1410894165";
          for(var j=0; j< tweetData.length; j++){
            //found = false;
            if(tweetData[j].start==(yr+"-"+mh+"-"+dy+" "+time)){
           	  found=true;
              tweetPerSec[j].nbScore +=1;
              tweetPerSec[j].score=(parseFloat(tweetPerSec[j].score)+ parseFloat(data.tweets[i][1])).toFixed(2);
              tweetPerSec[j].userId=data.tweets[i][3];
              tweetPerSec[j].pseudo=data.tweets[i][4];
              tweetPerSec[j].ppURL=data.tweets[i][8];
              tweetPerSec[j].tweet_text=data.tweets[i][2];
        	    tweetPerSec[j].start= yr+"-"+mh+"-"+dy+" "+time;
        	    sco=parseFloat(parseFloat(tweetPerSec[j].score)/tweetPerSec[j].nbScore);

              if(parseFloat(sco)<parseFloat(0.0)){
                color="steelblue";
                twtimg="http://www.open.ac.uk/community/main/sites/www.open.ac.uk.community.main/files/images/Twitter.png";
                tweetPerSec[j].nbScoreN+=1;
             } else if(parseFloat(sco)>parseFloat(0.0)){
        	      color="green";
        	      twtimg="http://www.twosisterscrafting.com/wp-content/icons/twitter.png";
                tweetPerSec[j].nbScoreP+=1;
             }else{
               twtimg="http://bloximages.newyork1.vip.townnews.com/dailyprogress.com/content/tncms/live/global/resources/images/_site/social/twitter-logo-cutout.png?_dc=1410894165";
             }
             tweetPerSec[j].className=color;
             tweetData[j].className=color;
             tweetData[j].content = '<div>'+parseFloat(sco).toFixed(2)+'</div><img src="'+twtimg+'"style="width:32px; height:32px;">';
            }
          }

          if(found==false){
            tweetPerSec[nbTwtD]={nbScore:1,score:parseFloat(data.tweets[i][1]).toFixed(2), userId:data.tweets[i][3], pseudo:data.tweets[i][4],ppURL:data.tweets[i][8],tweet_text:data.tweets[i][2], year:yr, month:mh, day:dy, hour:hr, minute:mn, second: sec,start: yr+"-"+mh+"-"+dy+" "+time,nbScoreP:0,nbScoreN:0,className:color  };
            if(parseFloat(data.tweets[i][1])<0.0){
              color="steelblue";
              twtimg="http://www.open.ac.uk/community/main/sites/www.open.ac.uk.community.main/files/images/Twitter.png";
              tweetPerSec[nbTwtD].nbScoreN=1;
              tweetPerSec[nbTwtD].className=color;
            } else if(parseFloat(data.tweets[i][1])>0.0){
        	    color="green";
        	    twtimg="http://www.twosisterscrafting.com/wp-content/icons/twitter.png";
              tweetPerSec[nbTwtD].nbScoreP=1;
              tweetPerSec[nbTwtD].className=color;
            } else{
               twtimg="http://bloximages.newyork1.vip.townnews.com/dailyprogress.com/content/tncms/live/global/resources/images/_site/social/twitter-logo-cutout.png?_dc=1410894165";
            }
            tweetData[nbTwtD]={id:nbTwtD, content:'<div>'+parseFloat(data.tweets[i][1]).toFixed(2)+'</div><img src="'+twtimg+'"style="width:32px; height:32px;">', start: yr+"-"+mh+"-"+dy+" "+time, className:color };
            nbTwtD++;
          }

          options = {
        	   editable: false,
             timeAxis: {scale: 'second', step: 5},
             maxHeight: "350px",
             height:"350px",
             start: tweetData[0].start,
             end: tweetData[nbTwtD-1].start
          };

          timeline.setOptions(options);
          timeline.setItems(new vis.DataSet(tweetData));
          timeline.redraw();

          //Paramètrages du dessins des tweets dans la viz de tweets
          //Positif
          if(tweetsPos[0]!=null && p<nbTwtP){
            len=0;
            if((nbTwtP)==1){
              p=0;
              len=1;
            } else if((nbTwtP)==2){
              p=0;
              len=2;
            } else if((nbTwtP)==3){
              p=0;
              len=3;
            } else if((nbTwtP)==4){
              p=0;
              len=4;
            } else{
              len=4;
            }
            svgPos.selectAll("rect").remove();
            svgPos.selectAll("text").remove();
            svgPos.selectAll("image").remove();
            svgPos.selectAll("circle").remove();
            yRectL=20;
            yImgL=35;
            yPseudoIdL=47;
            yTwtL=59;
            yScoreL=100;
            for(var j=0; j<len && (p+j)<nbTwtP;j++){
              drawPositiveTweet(tweetsPos[p+j],svgPos);
            }
            if(p+4 < nbTwtP){
              p+=1;
            }
          }

          //Négatif
          if(tweetsNeg[0]!=null && n<nbTwtN){
            len=0;
            if((nbTwtN)==1){
              n=0;
              len=1;
            } else if((nbTwtN)==2){
              n=0;
              len=2;
            } else if((nbTwtN)==3){
              n=0;
              len=3;
            } else if((nbTwtN)==4){
              n=0;
              len=4;
            } else{
              len=4;
            }
            svgNeg.selectAll("rect").remove();
            svgNeg.selectAll("text").remove();
            svgNeg.selectAll("image").remove();
            svgNeg.selectAll("circle").remove();
            yRectR=20;
            yImgR=35;
            yPseudoIdR=47;
            yTwtR=59;
            yScoreR=100;
            for(var j=0; j<len && (n+j)<nbTwtN;j++){
              drawNegativeTweet(tweetsNeg[n+j],svgNeg);
            }
            if(n+4 < nbTwtN){
              n+=1;
            }
          }

          //Redessiner
          showInformationTweets();
          drawHistogram(comptes);
          drawGraph(tweets);
        }
      },
      'json' //Type de données reçues de redis.php, la on aime bien le json
    );
  });
}

//Fonction d'affichage des mots-clefs et du temps de début du traitement
function afficherMotCle(startTime,keywords,selected){
  //Reçoit le temps de début, les mot-clefs, et les section ou afficher la viz

  //Afficher les headers des viz dans les tabcontenthead = d3.select('.keywordstime');
  head = d3.selectAll(selected);

  //Créer un svg qui continendra les mot-clefs
  container = head.append('svg').attr('height', 110).attr('width', 1000);

  //Créer un rectangle 180x50 dans container
  container.append('rect').attr('width', 150).attr('height', 50)
    .attr('x', 25).attr('y', 25) // À la position (25,25) du container
    .attr('rx',25).attr('ry',25) // Au bords arrondis
    .attr('opacity',0.8) // Avec un opacité de 80 %
    .style('fill','#2b7bb9'); // Et d'une couleur couleur bleue foncée

  //Ajout de texte fixe dans le container
  container.append('text').text('Mots-clés : ') // Dont le text est "Mot-clefs : "
    .attr('x',35).attr('y',60) // aux positions (35,60)
    .attr('fill','#F8F8F8') // De couleur gris très clair
    .style("font-size","27px") // De taille de police de 27PX
    .style('font-family','Product Sans'); // De police Product Sans

  //Ajout de texte dynamique dans le conrainer
  container.append('text').text(keywords) // Contenant les mot-clefs entrées
    .attr('x',185).attr('y',60) // Aux positions (220,60) Même hauteur que le texte "Mot-clefs : "
    .attr('fill','#808080 ') //En gris foncé
    .style("font-size","25px") // De taille 25PX
    .style('font-family','Product Sans'); // Et toujours en Product Sans

  //Ajout de text en dessous des Mot-clefs
  container.append('text').text(startTime) // texte contenant les temps de début du traitement
    .attr('x',35).attr('y',100) // À la position (35,100) du container SVG
    .attr('fill','#808080 ') // En gris foncé
    .style('font-family','Product Sans') // De police Product Sans
    .style("font-size","18px"); // De taille de police 18px
}

//Fonction de dessin de l'histograme
function drawHistogram(hash) {
  //Effacer ce qu'il y avait avant.
  d3.select(".d3-tip-histo").remove();
  d3.select("#histograme").selectAll("svg").remove();

  var binsize = 1;
  var minbin = 1;
  var maxbin = 6;
  var numbins = (maxbin - minbin) / binsize;

  // whitespace on either side of the bars in units of MPG
  var binmargin = .001;
  var margin = {top: 30, right: 30, bottom: 50, left: 60};
  var width = 550 - margin.left - margin.right;
  var height = 350 - margin.top - margin.bottom;

  // Set the limits of the x axis
  var xmin = minbin - 1;
  var xmax = maxbin + 1;

  histdata = new Array(numbins);
  for (var i = 0; i < numbins; i++) {
	   histdata[i] = { numfill: 0, meta: "" };
  }

  var i=0;
  for(var opinion in hash){
    i=i+1;
  	var bin =i;
  	if ((bin.toString() != "NaN") && (bin < histdata.length)) {
  		histdata[bin].numfill = hash[opinion];//d.pTwt;
  		user_name = " @" + topTweets[opinion].name;
  		fav ="❤ " +topTweets[opinion].favoriteCount;
  		rt="⭯  "  + topTweets[opinion].retweetCount;
  		histdata[bin].meta = "<tr><td>" +hash[opinion]+ " "+ opinion + " tweets"+ "</td></tr>";
  	}
  }

  var x = d3.scale.linear()
    .domain([0, (xmax - xmin)])
    .range([0, width]);

  // Scale for the placement of the bars
  var x2 = d3.scale.ordinal()
    .domain(["Negative","Neutral","Positive",""])
    .rangePoints([50, width]);

  var y = d3.scale.linear()
    .domain([0, d3.max(histdata, function(d) {
		  return d.numfill;
    }) + 5])
    .range([height, 0]);

  var xAxis = d3.svg.axis()
    .scale(x2)
	  .orient("bottom");

  var yAxis = d3.svg.axis()
    .scale(y)
    .ticks(10)
    .orient("left");

  var tip = d3.tip()
    .attr('class', 'd3-tip-histo')
    .attr('width',300)
    .attr('height',120)
    .direction('e')
    .offset([0, 20])
    .html(function(d){
      return '<table id="tiptable">' + d.meta + "</table>";
    });

  var svg = d3.select("#histograme").append("svg")
    .attr("width",1300)
    .attr("height",500)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  svg.call(tip);

  var bar = svg.selectAll(".bar")
    .data(histdata)
    .enter().append("g")
    .attr("class", "bar")
    .attr("transform", function(d, i) {
      if(i==1){
        return "translate(" +
	      x2("Negative") + "," + y(d.numfill) + ")";
      }else if(i==2){
        return "translate(" +
        x2("Neutral") + "," + y(d.numfill) + ")";
      }else if(i==3){
        return "translate(" +
        x2("Positive") + "," + y(d.numfill) + ")";
      }
    })
    .on('mouseover', tip.show)
    .on('mouseout', tip.hide);

  // add rectangles of correct size at correct location
  bar.append("rect")
    .attr("x", x(binmargin))
    .attr("width", x(binsize - 2 * binmargin))
    .attr("height", function(d) { return height - y(d.numfill); });

  // add the x axis and x-label
  svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + height + ")")
    .style({ 'stroke': 'none', 'fill': 'black', 'stroke-width': '1.5px'})
    .call(xAxis);

  svg.append("text")
    .attr("class", "xlabel")
    .attr("text-anchor", "middle")
    .attr("x", width / 2)
    .attr("y", height + margin.bottom)
    .text("# Polarité");

  // add the y axis and y-label
  svg.append("g")
    .attr("class", "y axis")
    .attr("transform", "translate(0,0)")
    .style({ 'stroke': 'none', 'fill': 'black', 'stroke-width': '1.5px'})
    .call(yAxis);

  svg.append("text")
    .attr("class", "ylabel")
    .attr("y", 0 - margin.left) // x and y switched due to rotation
    .attr("x", 0 - (height / 2))
    .attr("dy", "1em")
    .attr("transform", "rotate(-90)")
    .style("text-anchor", "middle")
    .text("# nombre de tweets");

  drawTweets(hash,svg);
}

//Fonction de dessins des top tweets avec la visualization d'histogramme
function drawTweets(hash,twt){
  //twt= d3.select('#histograme').append('svg').attr('height', 500).attr('width', 600);
  var nekes = 130;
  if(hash["Positive"]>=1){
    twt.append('text').text("Top Tweet Positif")
      .attr('x',700-nekes)
      .attr('y',0)
      .attr('fill','#808080')
      .style("font-size","25px");

    twt.append('rect').attr('width', 500)
      .attr('height', 150)
      .attr('x', 750-nekes)
      .attr('y', 15)
		  .attr('rx',10)
		  .attr('ry',10)
      .attr('fill','white')
      .style('stroke-width','3.5px')
      .style('stroke','#2b7bb9');

    tweet_pseudo=twt.append('text').text(topTweets["Positive"].screenName)
		  .attr('x',835-nekes)
		  .attr('y',45)
		  .attr('fill','#808080')
		  .style("font-size","18px")
		  .style("font-weight", "bold")
		  .style('font-family','arial');

    twt.append('text').text(" " + "@"+topTweets["Positive"].name)
		  .attr('x',835 - nekes)
		  .attr('y',45 + 20)
		  .attr('fill','black')
		  .style('font-family','arial');

    var text="";
    var y=60;
    words=topTweets["Positive"].tweet_text.split(" ");

    for(var i=0; i<words.length;i++){
      if((text.length + words[i].length +1)<53){
         text+=" "; text+=words[i];
      }else{
         twt.append('text').text(text)
          .attr('x', 835 -nekes)
          .attr('y', y + 30)
          .attr('fill', '#808080')
          .style('font-family','Product Sans');
        y+=18;
        text=words[i];
      }
    }

    twt.append('text').text(text)
    .attr('x', 835 -nekes)
    .attr('y', y + 30)
    .attr('fill', '#808080')
    .style('font-family','Product Sans');

    //Temps du top tweet
    twt.append('text').text(topTweets["Positive"]._time)
		  .attr('x',1180-nekes)
		  .attr('y',155)
		  .attr('fill','#2b7bb9 ')
		  .style("font-size","15px");

    //Score du top tweet
    twt.append('text').text("+"+parseFloat(topTweets["Positive"].score).toFixed(2))
      .attr('x', 775-nekes)
      .attr('y', 105)
      .attr('fill', "#2b7bb9")
		  .style('font-family','arial')
		  .style("font-size","13px")
		  .style('font-weight','bold');

      imgs= twt.selectAll("image").data([0]);
      imgs.enter()
        .append("svg:image")
        .attr("xlink:href", topTweets["Positive"].ppURL)
        .attr("x", 765-nekes)
        .attr("y", 30)
        .attr("width", "60")
        .attr("height", "60");
  }

  if(hash["Negative"]>=1){
    twt.append('text').text("Top Tweet Négatif")
		  .attr('x',700-nekes)
		  .attr('y',198)
		  .attr('fill','#808080 ')
		  .style("font-size","25px");

    twt.append('rect').attr('width', 500)
      .attr('height', 150)
      .attr('x', 750-nekes)
      .attr('y', 213)
		  .attr('rx',10)
		  .attr('ry',10)
      .attr('fill','white')
      .style('stroke-width','3.5px')
      .style('stroke','#55ACEE');

    tweet_pseudo = twt.append('text').text(topTweets["Negative"].screenName)
		  .attr('x',835-nekes)
		  .attr('y',243)
		  .attr('fill','#808080')
		  .style("font-size","18px")
		  .style("font-weight", "bold")
		  .style('font-family','arial');

    twt.append('text').text(" " + "@"+topTweets["Negative"].name)
		  .attr('x',835-nekes)
		  .attr('y',243 + 20)
		  .attr('fill','black')
		  .style('font-family','Product Sans');

    text="";
    y=258;
    words=topTweets["Negative"].tweet_text.split(" ");

    for(var i=0; i<words.length;i++){
      if((text.length + words[i].length +1)<53){
         text+=" "; text+=words[i];
      }else{
         twt.append('text').text(text)
          .attr('x', 835-nekes)
          .attr('y', y + 30)
          .attr('fill', '#808080')
          .style('font-family','Product Sans');
        y+=18;
        text=words[i];
      }
    }

    twt.append('text').text(text)
        .attr('x', 835-nekes)
        .attr('y', y + 30)
        .attr('fill', '#808080')
        .style('font-family','Product Sans');

    //Temps du top tweet négatif
    twt.append('text').text(topTweets["Negative"]._time)
		  .attr('x',1180-nekes)
		  .attr('y',353)
		  .attr('fill','#55ACEE ')
		  .style("font-size","15px");

    //Score du top tweet négatif
    twt.append('text').text(parseFloat(topTweets["Negative"].score).toFixed(2))
      .attr('x', 775-nekes)
      .attr('y', 303)
      .attr('fill', "#55ACEE")
		  .style('font-family','arial')
		  .style("font-size","13px")
		  .style('font-weight','bold');

    //Photo de profil de l'utilisateur du top négatif
    twt.append("image")
      .attr("xlink:href", topTweets["Negative"].ppURL)
      .attr("x", 765-nekes)
      .attr("y", 228)
      .attr("width", "60")
      .attr("height", "60");
  }
}

//Fonction de dessins de graphe
function drawGraph(tweets){
  //Effacer ce qu'il y avait avant.
  d3.select(".d3-tip-graph").remove();
  d3.select("#graphe").selectAll("svg").remove();

  var dom=[];
  var data=[]

  var i=0;
  var found;
  for (k = 0; k < Object.size(tweets) ; k++){
    found='0';

    for(j in dom){
      if(dom[j]==tweets[k].postedAt){
        found='1';
        data[j].score = parseFloat(data[j].score)+parseFloat(tweets[k].score);
        data[j].nbTweet+=1;
        data[j].ppURL=tweets[k].ppURL;
        data[j].pseudo=tweets[k].pseudo;
        data[j].userId=tweets[k].userId;
        data[j].tweet=tweets[k].tweet_text;
      }
    }

    if(found=='0'){
      dom[i]=tweets[k].postedAt;
      data[i]={score:tweets[k].score,
          postedAt:tweets[k].postedAt,
          ppURL:tweets[k].ppURL,
          pseudo:tweets[k].pseudo,
          userId:tweets[k].userId,
          tweet:tweets[k].tweet_text,
          nbTweet:1
        };
      i=i+1;
    }

  }

  for(var j =0;j<Object.size(data);j++){
    data[j].score=parseFloat(data[j].score)/data[j].nbTweet;
  }

  var height = 310;
  var width = 1000;
  var margin = {top: 20, right:20, bottom: 50, left: 20};

  // formatters for axis and labels
  var currencyFormat = d3.format("0.2f");

  var svg = d3.select("#graphe")
    .append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom + 30)
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  svg.append("g").attr("class", "y axis");

  svg.append("g").attr("class", "x axis");

  var tip = d3.tip()
    .attr('class', 'd3-tip-graph')
    .offset([120, 40])
    .html(function(d) {
      return '<div>at '+d.postedAt+'<br> Moyenne des scores : '+ parseFloat(d.score/d.nbTweet).toFixed(2) + '</div><br><img src="'+d.ppURL+'"style="width:35px; height:35px;"> '+
        d.pseudo.bold() + " @"+d.userId.fontcolor("gray")+ '<br>'+
        d.tweet;
  	});

  svg.call(tip);
  var xScale = d3.scale.ordinal()
    .rangeRoundBands([margin.left, width], .1);

  var yScale = d3.scale.linear()
    .range([height, 0]);

  var xAxis = d3.svg.axis()
    .scale(xScale)
    .orient("bottom");

  var yAxis = d3.svg.axis()
    .scale(yScale)
    .orient("left");

  // extract the x labels for the axis and scale domain
  var xLabels =dom;

  xScale.domain(xLabels);
  yScale.domain([-3,3/*-1*Math.round(d3.max(data, function(d)
  				 { return parseFloat(Math.abs(d.score));
  				 })),
  			       Math.round(d3.max(data, function(d)
  				 { return parseFloat(Math.abs(d.score));
  				 }))
  				*/]);

  var line = d3.svg.line()
    .x(function(d) { return xScale(d.postedAt); })
    .y(function(d) { return yScale(d.score); })
    .interpolate("monotone");

  svg.append("path")
    .datum(data)
    .attr("class","line")
    .attr("d", line);

  svg.select(".x.axis")
    .attr("transform", "translate(0," + (height) + ")")
    .call(xAxis.tickValues(xLabels.filter(function(d, i) {
      if (i % 12 == 0) return d;
    })))
    .selectAll("text")
    .style("text-anchor","end")
    .attr("transform", function(d) {
      return "rotate(-45)";
    });

  svg.select(".y.axis")
    .attr("transform", "translate(" + (margin.left) + ",0)")
    .call(yAxis.tickFormat(currencyFormat));

  // chart title
  svg.append("text")
    .attr("x", (width + (margin.left + margin.right) )/ 2)
    .attr("y", 0 + margin.top)
    .attr("text-anchor", "middle")
    .style("font-size", "16px")
    .style("font-family", "sans-serif")
    .text("Scores");

  // x axis label
  svg.append("text")
    .attr("x", (width + (margin.left + margin.right) )/ 2)
    .attr("y", height + 10 + margin.bottom)
    .attr("class", "text-label")
    .attr("text-anchor", "middle")
    .text("Temps (HH:MM:SS)");

  svg.append("svg:line")
    .attr("x1", 0)
    .attr("x2", width)
    .attr("y1", yScale(0))
    .attr("y2", yScale(0))
    .style("stroke", "#55acee");

  circle = svg.selectAll("circle")
    .data(data)
    .enter().append("circle")
    .attr('class', 'datapoint')
    .attr('cx', function(d) { return xScale(d.postedAt); })
    .attr('cy', function(d) { return yScale(d.score); })
    .attr('r', 7)
    .attr('fill', 'white');

  circle.attr('stroke', 'steelblue')
    .attr('stroke-width', '3');

  circle.on('mouseover', tip.show)
    .on('mouseout', tip.hide);

  showInformation();
}

//Information du graphe
function showInformation(){
  body = d3.select('#graphe');
  svg = body.append('svg').attr('height', 500).attr('width', 500);

  //Pour le nombre de tweets positifs
  svg.append('rect').attr('width', 365)
    .attr('height', 50)
    .attr('x', 45)
    .attr('y', 10)
    .attr('rx',25)
    .attr('ry',25)
    .style('fill','#2b7bb9');

  svg.append('text').text('Nombre de tweet(s) positif(s) :')//+' tweets')
    .attr('x',65)
    .attr('y',45)
    .attr('fill','white')
    .style("font-size","25px")
    .style('font-family','Product Sans');

  svg.append('text').text(comptes['Positive'])
    .attr('x',420)
    .attr('y',45)
    .attr('fill','#808080')
    .style("font-size","25px")
    .style('font-family','Product Sans');

  //Pour le nombre de tweets négatifs
  svg.append('rect').attr('width', 365)
    .attr('height', 50)
    .attr('x', 45)
    .attr('y', 70)
    .attr('rx',25)
    .attr('ry',25)
    .style('fill','#5ea9dd');

  svg.append('text').text('Nombre de tweet(s) négatif(s) :')//+' tweets')
    .attr('x',65)
    .attr('y',105)
    .attr('fill','white')
    .style("font-size","25px")
    .style('font-family','Product Sans');

  svg.append('text').text(comptes['Negative'])
    .attr('x',420)
    .attr('y',105)
    .attr('fill','#808080')
    .style("font-size","25px")
    .style('font-family','Product Sans');
}

//Dessin des tweets pour la chronologie
function drawTweet(tweet){
  body = d3.select('#chronologie');
  svg = body.append('svg').attr('height', 500).attr('width', 1200);

  rect = svg.append('rect').attr('width', 500)
    .attr('height', 140)
    .attr('x', 350)
    .attr('y', 30)
		.attr('rx',10)
		.attr('ry',10)
		.attr('opacity',0.4)
    .style('fill','gray');

  imgs = svg.selectAll("image").data([0]);

  imgs.enter()
    .append("svg:image")
    .attr("xlink:href", tweet.ppURL)
    .attr("x", "360")
    .attr("y", "45")
    .attr("width", "60")
    .attr("height", "60");

  tweet_pseudo=svg.append('text').text(tweet.pseudo)
    .attr('x',430)
    .attr('y',57)
    .attr('fill','black')
    .style("font-size","18px")
    .style("font-weight", "bold")
    .style('font-family','arial');

  tweet_id=svg.append('text').text(" " + "@"+tweet.userId)
    .attr('x',430 + tweet_pseudo.node().getBoundingClientRect().width+ 2)
	   .attr('y',57)
     .attr('fill','gray')
     .style('font-family','arial');

  var text="";
  var y=79;
  words=tweet.tweet_text.split(" ");

  for(var i=0; i<words.length;i++){
    if((text.length + words[i].length +1)<45){
	     text+=" "; text+=words[i];
    }else{
	     svg.append('text').text(text)
        .attr('x', 430)
        .attr('y', y)
        .attr('fill', 'black')
        .style('font-family','arial');
      y+=20;
      text=words[i];
    }
  }

  svg.append('text').text(text)
    .attr('x', 430)
    .attr('y', y)
    .attr('fill', 'black')
    .style('font-family','arial');

  svg.append('text').text(tweet.start)
		.attr('x',690)
		.attr('y',160)
		.attr('fill','white')
		.style('font-family','arial');

  svg.append('text').text("Sur "+tweet.nbScore+" tweet(s)")
    .attr('x',893)
    .attr('y',50)
    .attr('fill','gray')
    .style("font-size","18px")
    .style('font-family','arial');

  svg.append('rect').attr('width', 165)
    .attr('height', 50)
    .attr('x', 890)
    .attr('y', 60)
    .attr('rx',25)
    .attr('ry',25)
    .attr('opacity',0.8)
    .style('fill','#2b7bb9');

  svg.append('text').text(tweet.nbScoreP+ ' tweet(s) positif(s)')
    .attr('x',899)
    .attr('y',90)
    .attr('fill','white')
    .style('font-weight','bold')
    .style('font-family','arial');

  svg.append('rect').attr('width', 165)
    .attr('height', 50)
    .attr('x', 890)
    .attr('y', 115)
    .attr('rx',25)
    .attr('ry',25)
    .attr('opacity',0.6)
    .style('fill','#5ea9dd');

  svg.append('text').text(tweet.nbScoreN+ ' tweet(s) négatif(s)')
    .attr('x',899)
    .attr('y',145)
    .attr('fill','white')
    .style('font-weight','bold')
    .style('font-family','arial');

  svg.append('circle')
    .attr('cx',605)
    .attr('cy',290)
    .attr('r',80)
    .attr('opacity',0.3)
    .style('fill','gray');

  svg.append('text').text('Score')
    .attr('x',560)
    .attr('y',280)
    .attr('fill','white')
    .style("font-size","35px")
    .style('font-family','arial');

  var signe="±";
  if(tweet.score<0){signe="-";} else if(tweet.score>0){signe="+";}
  score=svg.append('text').text(signe+Math.abs(parseFloat(tweet.score)/tweet.nbScore).toFixed(2))
    .attr('x', 570)
    .attr('y', 315)
    .attr('fill', 'white')
    .style('font-family','arial')
    .style("font-size","25px");

  comment="Neutre !";
  if(tweet.className=="green"){comment="Positif ! ";}
  else if(tweet.className=="steelblue"){comment="Négatif !";}
  svg.append('text').text(comment)
    .attr('x',730)
    .attr('y',300)
    .attr('fill',tweet.className)
    .style("font-size","35px")
    .style('font-family','arial');
}

//Dessin des informations sur le nombre de tweets sur la visualisation "tweets"
function showInformationTweets(){
  /*Effecerce qu'il y avait*/
  d3.select("#informationTweets").select("svg").remove();

  svg = d3.select('#informationTweets').append('svg').attr('height', 300).attr('width', 1300);

  //Pour le nombre de tweets positifs
  svg.append('rect').attr('width', 365)
    .attr('height', 50)
    .attr('x', 95)
    .attr('y', 10)
    .attr('rx',25)
    .attr('ry',25)
    .style('fill','#2b7bb9');

  svg.append('text').text('Nombre de tweet(s) positif(s) :')
    .attr('x',115)
    .attr('y',45)
    .attr('fill','white')
    .style("font-size","25px")
    .style('font-family','Product Sans');

  svg.append('text').text(comptes['Positive'])
    .attr('x',470)
    .attr('y',45)
    .attr('fill','#808080')
    .style("font-size","25px")
    .style('font-family','Product Sans');

  //Pour le nombre de tweets négatifs
  svg.append('rect').attr('width', 365)
    .attr('height', 50)
    .attr('x', 745)
    .attr('y', 10)
    .attr('rx',25)
    .attr('ry',25)
    .style('fill','#5ea9dd');

  svg.append('text').text('Nombre de tweet(s) négatif(s) :')//+' tweets')
    .attr('x',765)
    .attr('y',45)
    .attr('fill','white')
    .style("font-size","25px")
    .style('font-family','Product Sans');

  svg.append('text').text(comptes['Negative'])
    .attr('x',1120)
    .attr('y',45)
    .attr('fill','#808080')
    .style("font-size","25px")
    .style('font-family','Product Sans');
}

//Colonne des tweets négatifs dans la visualisation des tweets
function drawNegativeTweet(tweet,svg){
  rect = svg.append('rect').attr('width', 500)
    .attr('height', 140)
    .attr('x', 40)
    .attr('y', yRectR)
    .attr('rx',10)
    .attr('ry',10)
    .attr('fill','white')
    .style('stroke','#5ea9dd')
    .style('stroke-width','3.5px');

  yRectR+=155;

  svg.append("image")
    .attr("xlink:href", tweet.ppURL)
    .attr("x", "50")
    .attr("y", yImgR)
    .attr("width", "60")
    .attr("height", "60");

  yImgR+=155;

  tweet_pseudo=svg.append('text').text(tweet.pseudo)
    .attr('x',120)
    .attr('y',yPseudoIdR)
    .attr('fill','#808080')
    .style("font-size","18px")
    .style("font-weight", "bold")
    .style('font-family','arial');

  tweet_id=svg.append('text').text(" " + "@"+tweet.id)
    .attr('x',120)
    .attr('y',yPseudoIdR + 20)
    .attr('fill','black')
    .attr("font-weight", "bold")
    .style('font-family','arial');

  yPseudoIdR+=155;


    var text="";
    var y=yTwtR;
    words=tweet.tweet_text.split(" ");

    for(var i=0; i<words.length;i++){
      if((text.length + words[i].length +1)<53){
  	     text+=" "; text+=words[i];
      }else{
  	     svg.append('text').text(text)
          .attr('x', 120)
          .attr('y', y + 30)
          .attr('fill', '#808080')
          .style('font-family','Product Sans');
        y+=20;
        text=words[i];
      }
    }

    svg.append('text').text(text)
      .attr('x', 120)
      .attr('y', y + 30)
      .attr('fill', '#808080')
      .style('font-family','Product Sans');

  yTwtR+=155;

  svg.append('circle')
    .attr('cx',573)
    .attr('cy',yScoreR-5)
    .attr('r',27)
    .style('fill','none')
    .style('stroke-width','2.5px')
    .style('stroke','#5ea9dd');

  var signe="-";

  score=svg.append('text').text(signe+currencyFormat(Math.abs(tweet.score)))
    .attr('x', 550)
    .attr('y', yScoreR)
    .attr('fill', '#5ea9dd')
    .style('font-family','arial')
    .style("font-size","18px")
    .style('font-weight','bold');

  yScoreR+=155;
}

//Colonne des tweets négatifs dans la visualisation des tweets
function drawPositiveTweet(tweet,svg) {
  rect = svg.append('rect').attr('width', 500)
    .attr('height', 140)
    .attr('x', 40)
    .attr('y', yRectL)
    .attr('rx',10)
    .attr('ry',10)
    .attr('fill','white')
    .style('stroke-width','3.5px')
    .style('stroke','#2b7bb9');

  yRectL+=155;

  svg.append("image")
    .attr("xlink:href", tweet.ppURL)
    .attr("x", "50")
    .attr("y", yImgL)
    .attr("width", "60")
    .attr("height", "60");

  yImgL+=155;

  //Pseudo
  tweet_pseudo=svg.append('text').text(tweet.pseudo)
    .attr('x',120)
    .attr('y',yPseudoIdL)
    .attr('fill','#808080')
    .style("font-size","18px")
    .style("font-weight", "bold")
    .style('font-family','arial');

  //Nom @user
  tweet_id=svg.append('text').text(" " + "@"+tweet.id)
    .attr('x',120)
    .attr('y',yPseudoIdL + 20)
    .attr('fill','black')
    .attr("font-weight", "bold")
    .style('font-family','arial');

  yPseudoIdL+=155;

  var text="";
  var y=yTwtL;
  words=tweet.tweet_text.split(" ");

  for(var i=0; i<words.length;i++){
    if((text.length + words[i].length +1)<53){
       text+=" "; text+=words[i];
    }else{
       svg.append('text').text(text)
        .attr('x', 120)
        .attr('y', y + 30)
        .attr('fill', '#808080')
        .style('font-family','Product Sans');
      y+=20;
      text=words[i];
    }
  }

  svg.append('text').text(text)
    .attr('x', 120)
    .attr('y', y + 30)
    .attr('fill', '#808080')
    .style('font-family','Product Sans');

  yTwtL+=155;

  svg.append('circle')
    .attr('cx',573)
    .attr('cy',yScoreL-5)
    .attr('r',27)
    .style('fill','none')
    .style('stroke-width','2.5px')
    .style('stroke','#2b7bb9');

  var signe="+";

  score=svg.append('text').text(signe+currencyFormat(Math.abs(tweet.score)))
    .attr('x', 550)
    .attr('y', yScoreL)
    .attr('fill','#2b7bb9')
    .style('font-family','arial')
    .style("font-size","18px")
    .style('font-weight','bold');

  yScoreL+=155;
}

//Fonction de changement d'onglet
function openViz(evt, vizNumber){
  var i, tabcontent, tablinks;

  //Cacher les éléments de la class tabcontent
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
      tabcontent[i].style.display = "none";
  }

  //Enelever tout les éléments de la classe tablinks de la classe active
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tabcontent.length; i++) {
      tablinks[i].className = tablinks[i].className.replace(" active", "");
  }

  //Afficher l'onglet cliqué
  document.getElementById(vizNumber).style.display = "block";
  evt.currentTarget.className += " active";
}

//Tuer storm en cas de leave de la page
$(function(){
  $(window).bind('beforeunload', function(){
    $.get(
      'killall.php', //Script serveur qui tue storm
      'false', //On n'envoie aucun paramètre à killall.php
      function(data){
        //Ne rien faire :o
      },
      'text' //Type de données reçues, on ne sait jamais :o
    );
  });
});

//Size
Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};
