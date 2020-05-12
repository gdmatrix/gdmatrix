var clockUrl;
var clockIds = new Array();
var clockFormats = new Array();
var clockClient = false;
var clockTics = 0;
var clockMillis = 0;
var clockShowMillis = 0;

function setClockUrl(url)
{
  clockUrl = url;
  requestServerTime();
}

function addClock(id, format)
{
  clockIds.push(id);
  clockFormats.push(format);
}

function requestServerTime()
{
  clockClient = false;
  if (window.XMLHttpRequest)
  {
    // Mozilla, Safari,...
    clockClient = new XMLHttpRequest();
  }
  else if (window.ActiveXObject)
  { // IE
    try
    {
      clockClient = new ActiveXObject("Msxml2.XMLHTTP");
    }
    catch (e)
    {
      try
      {
        clockClient = new ActiveXObject("Microsoft.XMLHTTP");
      }
      catch (e) {}
    }
  }
  if (clockClient)
  {
    clockClient.onreadystatechange = readServerTime;
    clockClient.open('GET', clockUrl, true);
    clockClient.send(null);
  }
}

function readServerTime()
{
  if (clockClient.readyState == 4)
  {
    if (clockClient.status == 200)
    {
      clockMillis = parseInt(clockClient.responseText);
      showTime();
    }
  }
}

function showTime()
{
  now = new Date(parseInt(clockMillis));
  hours = now.getHours(); if (hours < 10) hours = "0" + hours;
  minutes = now.getMinutes();
  if (minutes < 10) minutes = "0" + minutes;
  seconds = now.getSeconds();
  if (seconds < 10) seconds = "0" + seconds;

  var date = now.getDate() + "/" + (now.getMonth() + 1) + "/" +
    now.getFullYear();
  var time = hours + ":" + minutes + ":" + seconds;

  for (i = 0; i < clockIds.length; i++) // update all instances
  {
    clockElem = document.getElementById(clockIds[i]);
    if (clockElem)
    {
      var format = clockFormats[i];
      if (format == 'date') clockElem.innerHTML = date;
      else if (format == 'time') clockElem.innerHTML = time;
      else clockElem.innerHTML = date + " " + time;
    }
  }

  // 1 clockTic = 1 second = 1000 milliseconds
  if (clockTics == 180) // requestServerTime after 180 seconds
  {
    clockTics = 0;
    requestServerTime();
  }
  else
  {
    clockTics++;
    setTimeout('incrementTime()', 1000);
    clockShowMillis = (new Date()).getTime();
  }
}

function incrementTime()
{
  nowMillis = (new Date()).getTime();
  incr =  nowMillis - clockShowMillis;
  if (incr < 900 || incr > 1100) incr = 1000;
  clockMillis += incr;
  showTime();
}
