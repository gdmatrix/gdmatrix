/* 
 * matrix-client.js
 */
if (typeof contextPath === 'undefined')
{
  contextPath = "";
}

//var wsDirectoryUrl = "http://" + window.location.hostname + contextPath + "/wsdirectory";
var servletUrl = window.location.protocol + "//" + window.location.hostname + contextPath + "/commands";

function getQueryParams()
{
  var queryString = {};
  var query = window.location.search.substring(1);
  var pairs = query.split("&");
  for (var i = 0; i < pairs.length; i++)
  {
    var index = pairs[i].indexOf("=");
    if (index !== -1)
    {
      var name = decodeURIComponent(pairs[i].substring(0, index));
      var value = decodeURIComponent(pairs[i].substring(index + 1));
      if (typeof queryString[name] === "undefined") 
      {
        queryString[name] = value;
      }
      else if (typeof queryString[name] === "string") 
      {
        var arr = [queryString[name], value];
        queryString[name] = arr;
      }
      else 
      {
        queryString[name].push(value);
      }
    }
  }
  return queryString;  
};


function resetClientId()
{
  if (window.localStorage)
  {
    window.localStorage.removeItem("clientId");
    mcCloseButton.click();
  } 
  else alert("Navegador no suportat!");
}

function storeClientIdFromInput(inputElemId)
{
  var inputElem = document.getElementById(inputElemId);
  var clientId = inputElem.value;
  if (clientId !== null && clientId.length === 36)
  {
    if (window.localStorage)
    {
      window.localStorage.setItem("clientId", clientId);
      mcCloseButton.click();
    }
    else alert("Navegador no suportat!");
  }
  else alert("Codi incorrecte!");
}

function registerClientId()
{
  var userAgent = getBrowser(navigator.userAgent);
  var hostname = location.host;
  var status = "";
  var params = getQueryParams();
  var clientId = params['clientid'];
  
  if (window.localStorage && clientId != null)
  {
    window.localStorage.setItem("clientId", clientId);
    status = "OK!";
    document.getElementById("status").style = "color:green;font-weight:bold"; 
    document.getElementById("message").innerHTML = "Aquesta configuraci� �s nom�s v�lida per <b>aquest navegador</b>, per configurar el client en un altre diferent fes servir la seg�ent URL al navegador que desitgis:";
  }
  else
  {
    status = "NO configurat";
    document.getElementById("message").style = "color:red;font-weight:bold";    
    document.getElementById("status").style = "color:red";    
    
    if (clientId != null)
      document.getElementById("message").innerHTML = "Aquest navegador no est� suportat i no s'ha pogut configurar el client. Fes servir la URL seg�ent per configurar-lo en un altre navegador:";    
    else
      document.getElementById("message").innerHTML = "Identificador de client no definit";          
  }
  
  document.getElementById("clientid").innerHTML = clientId;  
  document.getElementById("hostname").innerHTML = hostname;    
  document.getElementById("browser").innerHTML = userAgent;
  document.getElementById("status").innerHTML = status;  
  document.getElementById("url").innerHTML = location.href;  
}

function getBrowser(userAgent)
{
  if (userAgent.indexOf("Firefox") > 0) return "Mozilla Firefox";
  else if (userAgent.indexOf("Edge") > 0) return "Microsoft Edge";
  else if (userAgent.indexOf("Chrome") > 0) return "Google Chrome";
  else if (userAgent.indexOf("Opera") > 0) return "Opera";  
  else if (userAgent.indexOf("Msie") > 0) return "Internet Explorer";  
  else if (userAgent.indexOf("Trident") > 0) return "Internet Explorer";    
  else if (userAgent.indexOf("Safari") > 0) return "Safari";      
}

function waitForCommandTermination(clientId, commandId, callback)
{
  var request = new XMLHttpRequest();
  request.addEventListener('load', function()
  {
    var result = JSON.parse(request.responseText);
    console.info(result);
    if (result.status === 'terminated' && callback)
    {
      callback(result);
    }
    else waitForCommandTermination(clientId, commandId, callback);
  }, false);
  request.addEventListener('error', function()
  {
    alert(request.responseText);
  }, false);
  var timestamp = new Date().getTime();
  request.open("GET", servletUrl + "?commandid=" + commandId + "&timestamp=" + timestamp);
  request.setRequestHeader("CSESSIONID", clientId);
  request.send();
}

function executeCommand(properties, callback)
{
  var clientId = window.localStorage.clientId;
  if (clientId == null)
  {
    showHelpInfo(properties, "not-configured");
  }
  else
  {
    properties.clientId = clientId;
    sendCommand(properties, callback, clientId, true);
  }
}

function sendCommand(properties, callback, clientId, async)
{
  var request = new XMLHttpRequest();
  var success = false;
  request.addEventListener('load', function()
  {
    var result = JSON.parse(request.responseText);
    console.info(result);
    var commandId = result.commandId;
    if (commandId)
    {
      success = true;      
      waitForCommandTermination(clientId, commandId, callback);
    }
    else 
    {
      if (async)
      {
        //launch app by protocol
        launchClient(servletUrl);
        
        //overlay 
        showOverlay();
        loopCount = 10;
        loopTime = 3000;
        setIntervalNTimes(function(loopCount)
        {
          success = sendCommand(properties, callback, clientId, false);
          if (success)
            hideOverlay();
          if (loopCount === 1 && !success)
          {
            showHelpInfo(properties, "not-started");
            hideOverlay();
          }
          return success;
        }, loopTime, loopCount);
      }
    }
  }, false);
  request.open("PUT", servletUrl, async);
  request.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
  request.setRequestHeader("CSESSIONID", clientId);
  request.send(JSON.stringify(properties));  
  return success;
}

function launchClient(url, show)
{
  if (show === undefined)
    show = false;
  var ifr = document.createElement('iframe');
  ifr.style.display = 'none';
  ifr.onload = function() { ifr.parentNode.removeChild(ifr); };
  if (url.indexOf("/localhost") > 0)
    url = 'matrix://' + url.replace("https", "http");
  else
    url = 'matrix://' + url;
  if (show)
  {
    url += "?show";
  }
  ifr.src = url;
  document.body.appendChild(ifr);
}

function setIntervalNTimes(callback, delay, repetitions) 
{
  var success = false;
  var intervalID = window.setInterval(function () 
  {
    success = callback(repetitions);
    repetitions--;
    if (success || repetitions === 0) 
    {
      window.clearInterval(intervalID);
    }
  }, delay);
}

function showHelpInfo(properties, cause)
{
  defaultButton = document.getElementById("mainform:default_button");
  if (defaultButton)
  {
    defaultButton.id = "mainform:default_button_mc";
  }  
  mcModalPanel = document.createElement("div");
  mcModalPanel.className = "modal";
  document.body.appendChild(mcModalPanel);

  mcModalContent = document.createElement("div");
  mcModalContent.className = "modal-content";
  mcModalPanel.appendChild(mcModalContent);
  
  mcCloseButton = document.createElement("button");
  mcCloseButton.className = "close";
  mcCloseButton.alt = "tanca/cierra/close";
  mcCloseButton.title = "tanca/cierra/close";
  mcCloseButton.innerHTML = "";
  mcCloseButton.onclick = function()
  {
    if (defaultButton)
    {
      setTimeout(function() 
      {
        defaultButton.id = "mainform:default_button";
      }, 1000);
    }
    document.body.removeChild(mcModalPanel);
  };
  mcModalContent.appendChild(mcCloseButton);

  mcModalText = document.createElement("div");
  mcModalText.className = "modal-text"; 
  mcModalContent.appendChild(mcModalText);

  var helpUrl = properties.helpUrl;
  if (helpUrl === undefined || helpUrl === null)
    helpUrl = "/plugins/client/" + cause + ".html";
  else
  {
    if (helpUrl.indexOf("?") < 0)
      helpUrl = helpUrl + "?cause=" + cause;
    else
      helpUrl = helpUrl + "&cause=" + cause;
  }
  var helpReq = new XMLHttpRequest();
  helpReq.addEventListener('load', function(data, status) 
  {
    mcModalPanel.style.display = 'block';
    mcModalText.innerHTML = this.responseText;
  });
  helpReq.open("GET", helpUrl);
  helpReq.send();
}

function echo(message, callback)
{
  var properties = 
  {
    "command" : "org.santfeliu.matrix.client.cmd.EchoCommand",
    "message" : message
  };
  executeCommand(properties, callback);
}
