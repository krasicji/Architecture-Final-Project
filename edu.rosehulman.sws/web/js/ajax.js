function loadOpponentList(){
var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
	xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
	xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
xmlhttp.onreadystatechange=function()
  {
  if (xmlhttp.readyState==4 && xmlhttp.status==200)
    {
	  document.getElementById("myDiv").style.visibility="visible";
	  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
		
    }
  }
xmlhttp.open("GET","/PlayoffTracker/OpponentList/opponentList.html",true);
xmlhttp.send();
}

function createSeriesSchedule(){
	var xmlhttp;
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
	    {
		  document.getElementById("myDiv").style.visibility="visible";
		  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
			
	    }
	  }
	xmlhttp.open("GET","createSchedule.html",true);
	xmlhttp.send();
}

function postSeriesSchedule(){
	var xmlhttp;
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	  
	var selectedRound = document.getElementById("selectedRound");
	var fileName = selectedRound.options[selectedRound.selectedIndex].value;
	
	var params = "opponent=" + document.getElementById("GameData").elements["opponent"].value + "\n";
	for (var i = 1; i <= 7; i++) {
		var date = "date" + i;
		var location = "location" + i;		
		params += "date="+document.getElementById("GameData").elements[date].value + "\n";
		params += "location="+document.getElementById("GameData").elements[location].value + "\n";
	}
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
	    {
		  document.getElementById("myDiv").style.visibility="visible";
		  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
	    }
	  }
	xmlhttp.open("POST","/PlayoffTracker/CreateSeries/" + fileName + ".html",true);
	xmlhttp.send(params);
}

function loadRound(round){
	var xmlhttp;
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
		{
		  document.getElementById("myDiv").style.visibility="visible";
		  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
			
		}
	  }
	xmlhttp.open("GET","/PlayoffTracker/RetrieveSeries/" + round + ".html",true);
	xmlhttp.send();
}

function loadRoundForEdit(round){
	var xmlhttp;
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
		{
		  document.getElementById("myDiv").style.visibility="visible";
		  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
		
		  document.getElementById("remove").style.display="";
		  document.getElementById("submitButton").style.display="";
		  document.getElementById("submitButton").onclick=function() {
			editRound(round);
		  } 
		  for (var i = 1; i <= 7; i++) {
			if(document.getElementById("wL" + i) != null) {
				document.getElementById("wL" + i).style.display="";
				document.getElementById("score" + i).style.display="";
				document.getElementById("deleteButton" + i).style.display="";
				document.getElementById("deleteButton" + i).onclick=function() {
					deleteGame(round, this.attributes["value"].value);
				}
			}
		  }
		}
	  }
	xmlhttp.open("GET","/PlayoffTracker/RetrieveSeries/" + round + ".html",true);
	xmlhttp.send();
}

function editRound(round){
	var xmlhttp;
	var params = "";
	for (var i = 1; i <= 7; i++) {
		if (document.getElementById("wL"+i) != null){
			var selectedResult = document.getElementById("wL"+i);
			var result = selectedResult.options[selectedResult.selectedIndex].value;
			params += "wL=" + result + "\n";
			params += "score="+document.getElementById("score"+i).value + "\n";
		}
	}
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
		{
		  document.getElementById("myDiv").style.visibility="visible";
		  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
			
		}
	  }
	xmlhttp.open("PUT","/PlayoffTracker/UpdateSeries/" + round + ".html",true);
	xmlhttp.send(params);
}

function deleteGame(round, game){
	var xmlhttp;
	var params = "id=\"" + game + "\"";
	if (window.XMLHttpRequest)
	  {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp=new XMLHttpRequest();
	  }
	else
	  {// code for IE6, IE5
		xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
	  }
	xmlhttp.onreadystatechange=function()
	  {
	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
		{
		  document.getElementById("myDiv").style.visibility="visible";
		  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
		
		  document.getElementById("remove").style.display="";
		  document.getElementById("submitButton").style.display="";
		  document.getElementById("submitButton").onclick=function() {
			editRound(round);
		  } 
		  for (var i = 1; i <= 7; i++) {
			if(document.getElementById("wL" + i) != null) {
				document.getElementById("wL" + i).style.display="";
				document.getElementById("score" + i).style.display="";
				document.getElementById("deleteButton" + i).style.display="";
				document.getElementById("deleteButton" + i).onclick=function() {
					deleteGame(round, this.attributes["value"].value);
				}
			}
		  }
		}
	  }
	xmlhttp.open("DELETE","/PlayoffTracker/DeleteGames/" + round + ".html",true);
	xmlhttp.send(params);
}