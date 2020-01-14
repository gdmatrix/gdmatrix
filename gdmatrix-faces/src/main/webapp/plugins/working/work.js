var workTicks = 0; // number of tics for image cycling
var workSec = 0; // seconds
var workTenths = 0; // tenths
var workCompleted = false; // page unloaded?
var workContextPath = ""; // context path
var imageLoad = 0; // images loaded?
var workSubmitted = false; // page submitted?
var workShowTimer = true; // show timer

function showWork(elem, contextPath)
{
  if (contextPath != null) workContextPath = contextPath;  
  elem.innerHTML = createTimerPanel() + createImages();
  workSubmitted = true;
  workShowTimer = false;
  startWork();
}

function submitWork(contextPath)
{
  if (contextPath != null) workContextPath = contextPath;
  document.body.innerHTML = 
    createWorkBody(document.forms[0].action, document.forms[0].method);
  workSubmitted = false;
  workShowTimer = true;
  startWork();
}

function doWork(url, contextPath)
{
  if (contextPath != null) workContextPath = contextPath;
  document.body.innerHTML = createWorkBody(url, "get");
  workSubmitted = false;
  workShowTimer = true;
  startWork();
}

// internal methods

function createTimerPanel()
{
  var html = "<div id=\"timer\" " +
  "style=\"color:black;font-size:12px;font-family: arial;" +
  "display:inline-block;text-align:center;width:81px;height:81px;" +
  "background-repeat:no-repeat;background-position:center;line-height:81px;\"/>"  
  return html;
}

function createImages()
{
  var html = "<div style=\"visibility:hidden\"><img src=\"/" + workContextPath +
    "plugins/working/work0.png\" onload=\"imageLoaded(0)\">";
  html += "<img src=\"/" + workContextPath +
    "plugins/working/work1.png\" onload=\"imageLoaded(1)\">";
  html += "<img src=\"/" + workContextPath +
    "plugins/working/work2.png\" onload=\"imageLoaded(2)\"></div>";
  return html;
}

function createWorkBody(url, method)
{
  var body = "<div style=\"text-align:center;margin-top:100px\">";
  body += createTimerPanel();
  body += "</div>";
  body += createImages();
  
  body += "<form action=\"" + url + "\" method=\"" + method + "\">";
  if (document.forms[0])
  {
    var form = document.forms[0];
    var numFields = form.elements.length;
    for (i = 0; i < numFields; i++)
    {
      var field = form.elements[i];
      body += "<input type=\"hidden\" name=\"" + field.name +
        "\" value=\"" + field.value + "\" />";
    }
  }
  body += "</form>";
  return body;
}

function startWork()
{
  workCompleted = false;
  try
  {
    window.addEventListener("unload", function workListener()
    {
      workCompleted = true;
    }, false);
  }
  catch (ex)
  {
  }
  setTimeout("updateWork()", 100);
}

function updateWork()
{
  if (workCompleted) return;
  if (imageLoad == 7) // images loaded
  {
    // move wheel
    workTicks++;
    var i = workTicks % 3;
    var elem = document.getElementById("timer");
    elem.style.backgroundImage =
      "url(\"/" + workContextPath + "plugins/working/work" + i + ".png\")";    

    // show timer
    if (workShowTimer)
    {
      workTenths++;
      if (workTenths == 10)
      {
        workSec++;
        workTenths = 0;
      }
      elem.innerHTML = workSec + "." + workTenths;
    }

    if (!workSubmitted)
    {
      workSubmitted = true;
      document.forms[0].submit();
    }
  }
  setTimeout("updateWork()", 100);
}

function imageLoaded(num)
{
  imageLoad = imageLoad | (1 << num);
}