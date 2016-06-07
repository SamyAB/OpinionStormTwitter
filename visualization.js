mots-clés//Temps de chargement de apache storm
var loadingTime = 60000; // 60 secondes
//interval entre chaque update de données
var updareInterval = 4000; // 4 secondes

//Variables utilisées dans la visualisation
var comptes = {};
var widthHist = 1200;
var heightHist = 700;
var tweets = [];
var topTweets = {};
var keywords = window.keywords;
var startTime = "";

//Initialisation des comptes des tweets négatifs, positifs, neutres
comptes["Negative"] = 0;
comptes["Neutral"] = 0;
comptes["Positive"] = 0;
topTweets["Negative"]={ score :0, tweet_text :" ", name:"", screenName:"" , favoriteCount:0 , retweetCount:0,ppURL:"https://g.twimg.com/dev/documentation/image/DTC_Services_1h_hero_bg.png", _time:"00:00:00" };
topTweets["Neutral"]={ score :0, tweet_text :" ", name:"", screenName:"",  favoriteCount:0 , retweetCount:0, ppURL:"https://g.twimg.com/dev/documentation/image/DTC_Services_1h_hero_bg.png",_time:"00:00:00"};
topTweets["Positive"]={ score :0, tweet_text :" ", name:"", screenName:"", favoriteCount:0 , retweetCount:0,ppURL:"https://g.twimg.com/dev/documentation/image/DTC_Services_1h_hero_bg.png",_time:"00:00:00"};
positionHist = -1;

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

  //Lancement de la fonction update chaque updateInterval
  window.setInterval(update,updareInterval);
},loadingTime);

//Fonction de rechargement des vizualisations
function update(){
  $(function(){
    $.get(
      'redis.php', //Script serveur qui récupère les informations de redis
      'false', //On n'envoie aucun paramètre à redis.php
      function(data){
        //Ici se trouve tout ce qu'on doit faire après avoir récupérer les informations de Redis
        for(var i=0; i< data.tweets.length ;i++){
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
              topTweets["Positive"].score=data.tweets[i][1];
              topTweets["Positive"].tweet_text=data.tweets[i][2];
              topTweets["Positive"].name=data.tweets[i][3];
              topTweets["Positive"].screenName=data.tweets[i][4];
              topTweets["Positive"].favoriteCount=data.tweets[i][5];
            	topTweets["Positive"].retweetCount=data.tweets[i][6];
            	topTweets["Positive"].ppURL=data.tweets[i][8];
            	topTweets["Positive"]._time=data.tweets[i][7].split(" ")[3];
            }
          }

          if(data.tweets[i][0]=="2"){
            comptes["Neutral"] = comptes["Neutral"]+1;
          }

          var mh=data.tweets[i][7].split(" ")[1];
          var yr=data.tweets[i][7].split(" ")[5];
          var dy=data.tweets[i][7].split(" ")[2];
          var time=data.tweets[i][7].split(" ")[3];
          var hr = time.split(":")[0];
          var mn = time.split(":")[1];
          var sec = time.split(":")[2];

          drawHistogram(comptes);
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
  d3.select(".d3-tip").remove();
  d3.select("#histograme").selectAll("svg").remove();

  var binsize = 1;
  var minbin = 1;
  var maxbin = 6;
  var numbins = (maxbin - minbin) / binsize;

  // whitespace on either side of the bars in units of MPG
  var binmargin = .001;
  var margin = {top: 30, right: 30, bottom: 50, left: 60};
  var width = 650 - margin.left - margin.right;
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
    .attr('class', 'd3-tip')
    .attr('width',300)
    .attr('height',120)
    .direction('e')
    .offset([0, 20])
    .html(function(d){
      return '<table id="tiptable">' + d.meta + "</table>";
    });

  var svg = d3.select("#histograme").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
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
    .text("# Polarity");

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
    .text("# number of tweets");

  drawTweets(hash);
}

//Fonction de dessins des top tweets avec la visualization d'histogramme
function drawTweets(hash){
  twt= d3.select('#histograme').append('svg').attr('height', 500).attr('width', 800);
  if(hash["Positive"]>=1){
    twt.append('text').text("Top Tweet Positif")
      .attr('x',50)
      .attr('y',22)
      .attr('fill','#808080 ')
      .style("font-size","25px");

    twt.append('rect').attr('width', 500)
      .attr('height', 150)
      .attr('x', 200)
      .attr('y', 25)
		  .attr('rx',25)
		  .attr('ry',25)
		  .attr('opacity',0.5)
      .style('fill','#55ACEE');

    imgs= twt.selectAll("image").data([0]);
    imgs.enter()
      .append("svg:image")
      .attr("xlink:href", topTweets["Positive"].ppURL)
      .attr("x", 215)
      .attr("y", 40)
      .attr("width", "60")
      .attr("height", "60");

    tweet_pseudo=twt.append('text').text(topTweets["Positive"].screenName)
		  .attr('x',279)
		  .attr('y',55)
		  .attr('fill','black')
		  .style("font-size","18px")
		  .style("font-weight", "bold")
		  .style('font-family','arial');

    twt.append('text').text(" " + "@"+topTweets["Positive"].name)
		  .attr('x',275+tweet_pseudo.node().getBoundingClientRect().width+ 2)
		  .attr('y',55)
		  .attr('fill','gray')
		  .style('font-family','arial');

    tweet_text = twt.append('text').text(topTweets["Positive"].tweet_text)
      .attr('x', 279)
      .attr('y', 65)
      .attr('fill', '#383838')
		  .style('font-family','arial');

    d3plus.textwrap()
      .container(tweet_text)
      .width(380)
      .draw();

    twt.append('text').text(topTweets["Positive"]._time)
		  .attr('x',600)
		  .attr('y',165)
		  .attr('fill','#F0F0F0 ')
		  .style("font-size","15px");

    twt.append('text').text("+"+parseFloat(topTweets["Positive"].score).toFixed(2))
      .attr('x', 225)
      .attr('y', 115)
      .attr('fill', "white")
		  .style('font-family','arial')
		  .style("font-size","13px")
		  .style('font-weight','bold');
  }

  if(hash["Negative"]>=1){
    twt.append('text').text("Top Tweet Négatif")
		  .attr('x',50)
		  .attr('y',198)
		  .attr('fill','#808080 ')
		  .style("font-size","25px");

    twt.append('rect').attr('width', 500)
      .attr('height', 150)
      .attr('x', 200)
      .attr('y', 205)
		  .attr('rx',25)
		  .attr('ry',25)
		  .attr('opacity',0.5)
      .style('fill','#55ACEE');

    imgs.enter()
      .append("svg:image")
      .attr("xlink:href", topTweets["Negative"].ppURL)
      .attr("x", 215)
      .attr("y", 220)
      .attr("width", "60")
      .attr("height", "60");

    tweet_pseudo = twt.append('text').text(topTweets["Negative"].screenName)
		  .attr('x',279)
		  .attr('y',235)
		  .attr('fill','black')
		  .style("font-size","18px")
		  .style("font-weight", "bold")
		  .style('font-family','Product Sans');

    twt.append('text').text(" " + "@"+topTweets["Negative"].name)
		  .attr('x',275+tweet_pseudo.node().getBoundingClientRect().width+ 2)
		  .attr('y',235)
		  .attr('fill','gray')
		  .style('font-family','Product Sans');

    tweet_text = twt.append('text').text(topTweets["Negative"].tweet_text)
      .attr('x', 279)
      .attr('y', 245)
      .attr('fill', '#383838')
		  .style('font-family','Product Sans');

    d3plus.textwrap()
      .container(tweet_text)
      .width(380)
      .draw();

    twt.append('text').text(topTweets["Negative"]._time)
		  .attr('x',600)
		  .attr('y',345)
		  .attr('fill','#F0F0F0 ')
		  .style("font-size","15px");

    twt.append('text').text(parseFloat(topTweets["Negative"].score).toFixed(2))
      .attr('x', 225)
      .attr('y', 295)
      .attr('fill', "white")
		  .style('font-family','Product Sans')
		  .style("font-size","13px")
		  .style('font-weight','bold');
  }
}

//Fonction de dessins de graphe
function drawGraph(hash){
  //Effacer ce qu'il y avait avant.
  d3.select(".d3-tip").remove();
  d3.select("#graphe").selectAll("svg").remove();


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
