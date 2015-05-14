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
	  document.getElementById("myDiv").innerHTML=xmlhttp.responseText;
		
    }
  }
xmlhttp.open("GET","/PlayoffTracker/OpponentList/opponentList.html",true);
xmlhttp.send();
}
