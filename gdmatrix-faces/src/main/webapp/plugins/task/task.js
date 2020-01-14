/* task.js */

TaskMonitor = function(taskId, terminateHiddenId, progressElemId, wait)
{
  this.taskId = taskId;
  this.terminateHidden = document.getElementById(terminateHiddenId);
  this.progressElem = document.getElementById(progressElemId);
  this.wait = wait ? wait : 5000;
  this.attempts = 0;
  this.terminateHidden.value = "false";
};

TaskMonitor.prototype = 
{
  monitor : function()
  {
    var scope = this;

    // show undefined progress bar
    scope.progressElem.style.textAlign = "center";
    scope.progressElem.style.fontWeight = "bold";
    scope.progressElem.style.margin = "5px 0 5px 0";
    scope.progressElem.style.overflow = "hidden";
    scope.progressElem.style.border = "1px solid gray";
    scope.progressElem.style.height = "100%";
    scope.progressElem.style.height = "24px";
    scope.progressElem.style.lineHeight = "24px";
    scope.progressElem.style.backgroundRepeat = "no-repeat";
    scope.progressElem.style.backgroundImage = 
      "url(" + baseUrl + "/plugins/task/progressbar_undefined.gif)";
    scope.progressElem.style.backgroundSize = "100% 24px";
    
    var httpClient = new XMLHttpRequest();
    httpClient.onreadystatechange = function()
    {
      if (httpClient.readyState === 4 && httpClient.status === 200) 
      {
        console.info(httpClient.responseText);
        var json = JSON.parse(httpClient.responseText);
        if (json.state === undefined || json.state === "TERMINATED")
        {
          scope.terminateHidden.value = "true";
          document.forms[0].submit();
        }
        else if (json.state && json.state !== "TERMINATED")
        {
          if (json.progress === -1) // undefined
          {
            scope.progressElem.innerHTML = json.message || json.state;
          }
          else
          {
            scope.progressElem.style.backgroundImage = 
              "url(" + baseUrl + "/plugins/task/progressbar.png)";
            var perc = json.progress + "%";
            scope.progressElem.style.backgroundSize = perc + " 24px";
            scope.progressElem.innerHTML = perc;
          }
          setTimeout(function(){scope.monitor();}, 100);
        }
      }
    };
    var url = baseUrl + "/tasks/" + scope.taskId;
    if (this.attempts > 0) // wait for nexts attempts
    {
      url += "?wait=" + this.wait;      
    }
    this.attempts++;
    httpClient.open('GET', url, true);
    httpClient.send();
  }
};