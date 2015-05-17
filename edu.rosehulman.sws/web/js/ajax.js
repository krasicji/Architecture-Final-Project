function loadXMLDoc()
{
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
	    var new_window = window.open("r1.html");
		
    }
  }
xmlhttp.open("GET","/PlayoffTracker/RetrieveSeries/r1.html",true);
xmlhttp.send();
}


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