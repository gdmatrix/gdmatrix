var pageLoaded = false;

function onLoad()
{
  pageLoaded = true;
  console.info("page loaded");
  resetForm();
}

function onUnload()
{
  resetForm();
}
// register event listeners
if (window.addEventListener)
{
  window.addEventListener("load", onLoad, false);
  window.addEventListener("unload", onUnload, false);
}
else // IE8
{
  window.attachEvent("onload", onLoad);  
  window.attachEvent("onunload", onUnload);  
}

function onSubmit()
{
  if (pageLoaded)
  {
    // submit
    return true;
  }
  else
  {
    console.info("submit aborted!");
    hideOverlay();
    return false;
  }
}

function resetForm()
{
  console.info("reset form");
  var elems = document.forms[0].elements['mainform:_idcl'];
  if (elems && elems.length > 1) // t:commandLink can duplicate element if submit is aborted
  {
    console.info("remove duplicate");    
    elems[1].parentNode.removeChild(elems[1]);
    elems[0].value = "";
  }
}

function setMid(mid)
{
  var action = document.forms[0].action;
  document.forms[0].action = action + "?smid=" + mid;
  return true;
}

function capture()
{
  var action = document.forms[0].action;
  var index = action.indexOf('?');
  var newAction;
  if (index != -1)
  {
    newAction = action.substring(0, index) +
      window.location.search + "&capture=true";
  }
  else
  {
    newAction = action + window.location.search + "&capture=true";
  }
  document.forms[0].action = newAction;
  document.forms[0].target = "_blank";
  document.forms[0].submit();
  document.forms[0].action = action;
  document.forms[0].target = "_self";
}

function goMid(mid)
{
  var action = document.forms[0].action;
  var index = action.indexOf('?');
  if (index != -1) // no params
  {
    action = action.substring(0, index) + "?xmid=" + mid;
  }
  else
  {
    action = action + "&xmid=" + mid;
  }
  document.forms[0].action = action;
  document.forms[0].submit();
  return false;
}

function goTopic(topic)
{
  var action = document.forms[0].action;
  var index = action.indexOf('?');
  if (index != -1)
  {
    action = action.substring(0, index) + "?topic=" + topic;
  }
  else
  {
    action = action + "?topic=" + topic;
  }
  document.forms[0].action = action;
  document.forms[0].submit();
  return false;
}

function showObject(command)
{
  document.getElementById('hiddenjumpcommand').value = command;
  document.getElementById('jumptoobjectaction').click(); 
  return true;
}

function executeAction(action)
{
  document.getElementById('hiddenaction').value = action;
  document.getElementById('doactionexecution').click(); 
  return true;
}

function changeTarget(target)
{
  target = target || '_blank';
  setTimeout(function()
  {
    document.forms[0].target='_self';  
  }, 0);
  document.forms[0].target=target;  
}

function confirmAction(msg)
{
  if (confirm(msg))
  {
    document.forms[0].submit();
  }
}

//toggles layer visibility on and off
function show(id)
{
  var element = document.getElementById(id);
  if (element != null)
  {
    var iframe = document.getElementById('opaqueLayer');
    if (iframe != null)
    {
      iframe.style.position = 'absolute';
      iframe.style.width = element.offsetWidth + "px";
      iframe.style.height = element.offsetHeight + "px";
      iframe.style.left = element.offsetLeft;
      iframe.style.top = element.offsetTop;
      iframe.style.display = '';
    }
    element.style.visibility = "visible";
  }
}

function hide(id)
{
  var element = document.getElementById(id);
  if (element != null)
  {
    var iframe = document.getElementById('opaqueLayer');
    if (iframe != null)
    {
      iframe.style.display = 'none';
    }
    element.style.visibility = "hidden";
  }
}

function push(id)
{
  var element = document.getElementById(id);
  if (element != null)
  {
    if (element.style.visibility == "hidden") 
    {
      show(id);   
    }
    else
    {
      hide(id);    
    }
    buttonPushed = true;
  }
}

var buttonPushed = false;

function pushOutside(id)
{
  if (!buttonPushed)
  {
    hide(id);
  }
  buttonPushed = false;
}

function checkMaxLength(field, limit)
{
  if (field.value.length >= limit) field.value = field.value.substring(0, limit - 1);
}

/* create an opaque layer with an iframe to fix IE 5-6 bug */
function opaqueLayer()
{
  var version = navigator.appVersion;
  if (version.indexOf('MSIE 6.0') > 0 ||
      version.indexOf('MSIE 5.5') > 0 ||
      version.indexOf('MSIE 5.0') > 0)
  {
    document.write('<iframe width="0" height="0" src="/topframe/blank.htm" scrolling="no" style="display:none;z-index:9" frameborder="0" id="opaqueLayer"></iframe>');
  }
}

function initKeyCapture()
{
  if (document.addEventListener)
  {
    document.addEventListener("keypress",keypress,false);
  }
  else if (document.attachEvent)
  {
    document.attachEvent("onkeypress", keypress);
  }
  else
  {
    document.onkeypress= keypress;
  }
}

function keypress(e)
{  
  if (document.attachEvent)
  {
    keyPressed = e.keyCode;
  }
  else
  {
    keyPressed = e.keyCode;
  }
  var targetId = null;
  if (e.target) targetId = e.target.id;
  else if (e.srcElement) targetId = e.srcElement.id;  
  if (keyPressed == 13 && (targetId == null || (targetId != null && targetId.indexOf('checkedTextField') != 0)))
  {
    var elem = document.getElementById("mainform:default_button");
    if (elem != null && (e.currentTarget.activeElement.tagName === 'INPUT' || e.currentTarget.activeElement.tagName === 'SELECT'))
    {   
      if (elem.getAttribute('onclick') != null && 
        elem.getAttribute('onclick').indexOf && 
        elem.getAttribute('onclick').indexOf('showOverlay()') >= 0)
      {
        showOverlay();
      }
      var action = document.forms[0].action + "&mainform:default_button=activated";
      document.forms[0].action = action;
      document.forms[0].submit();  
      if (window.event)
      {
        event.cancelBubble = true;
        event.returnValue = false;  
      }
      return false;
    }
  } 
  return true;
}

function inputTextKeyCheck(e, buttonName)
{
  var keyID = (window.event) ? event.keyCode : e.keyCode;
  if (keyID == 13)
  {    
    var button = document.getElementById(buttonName);
    if (button != null)
    {  
      if (button.getAttribute('onclick') != null && 
        button.getAttribute('onclick').indexOf && 
        button.getAttribute('onclick').indexOf('showOverlay()') >= 0)
      {
        showOverlay();
      }
      button.click();
      if (window.event)
      {        
        event.cancelBubble = true;
        event.returnValue = false;
      }
    }
  }
}

function showOverlay()
{
  var overlay = document.getElementById('_overlay_');
  if (overlay != null)
  {
    overlay.style.visibility = "visible";
  }
}

function hideOverlay()
{
  var overlay = document.getElementById('_overlay_');
  if (overlay != null)
  {
    overlay.style.visibility = "hidden";
  }
}

function getCookieValue(cookieName)
{
  var cookieValue = "";
  var nameEQ = cookieName + "=";
  var ca = document.cookie.split(';');
  for (var i = 0; i < ca.length && cookieValue == ""; i++) 
  {
    var c = ca[i];
    while (c.charAt(0) == ' ') c = c.substring(1, c.length);
    if (c.indexOf(nameEQ) == 0) cookieValue = c.substring(nameEQ.length, c.length);
  }    
  cookieValue = cookieValue.replace(/"/g, '');
  return cookieValue;
}

function setCookieValue(cookieName, cookieValue)
{
  document.cookie = cookieName + '="' + cookieValue + '";path=/';
}

function setCookieValue(cookieName, cookieValue, expireMillis)
{
  var expireDate = new Date();  
  expireDate.setTime(expireDate.getTime() + expireMillis);
  var expireDateStr = expireDate.toUTCString();    
  document.cookie = cookieName + '="' + cookieValue + '";expires="' + expireDateStr + '";path=/';
}

function putDefaultLoginValues()
{
  var userBox = document.getElementById('mainform:userBox');
  if (userBox && userBox.value != 'Usuari')
  {
    userBox.value = 'Usuari';
    var passwordBox = document.getElementById('mainform:passwordBox');
    if (passwordBox)
    {
      passwordBox.value = 'Paraula';
    }    
  }
}

function resetDefaultLoginValues()
{
  var userBox = document.getElementById('mainform:userBox');
  if (userBox && userBox.value == 'Usuari')
  {
    userBox.value = '';
    var passwordBox = document.getElementById('mainform:passwordBox');
    if (passwordBox)
    {
      passwordBox.value = '';
    }  
  }  
}

function showLanguageSelector()
{
  var languageSelector = document.getElementById('mainform:languageSelector');
  if (languageSelector)
  {
    languageSelector.style.display = 'block';
  }  
}

function hideLanguageSelector()
{
  var languageSelector = document.getElementById('mainform:languageSelector');
  if (languageSelector)
  {
    languageSelector.style.display = 'none';
  }    
}

function toggleVisibility(id)
{
  var element = document.getElementById(id);
  if (element != null && element.style.display != 'none')
    element.style.display = 'none';
  else if (element != null && element.style.display == 'none')
    element.style.display = 'block';
}

function makeLanguageSelectorVisible()
{  
  var languageSelector = document.getElementById('languageSelectorDescription');
  if (languageSelector) languageSelector.style.visibility = 'visible';
}

function makeFoldWidgetIconsVisible(widgetIds)
{  
  var formId = document.forms[0].id;
  for (i = 0; i < widgetIds.length; i++)
  {
    var foldButton = document.getElementById(formId + ':' + widgetIds[i] + '_fold');
    if (foldButton) 
    {  
      //foldButton.style.visibility = 'visible';
      if (foldButton.style.display == 'none')
      {
        foldButton.setAttribute("aria-hidden", "true");
      }
      else if (foldButton.style.display == 'block')
      {
        foldButton.setAttribute("aria-hidden", "false");
      }
    }  
    var unfoldButton = document.getElementById(formId + ':' + widgetIds[i] + '_unfold');
    if (unfoldButton) 
    {  
      //unfoldButton.style.visibility = 'visible';
      if (unfoldButton.style.display == 'none')
      {
        unfoldButton.setAttribute("aria-hidden", "true");
      }
      else if (unfoldButton.style.display == 'block')
      {
        unfoldButton.setAttribute("aria-hidden", "false");
      }      
    }  
//    var closeButton = document.getElementById(formId + ':' + widgetIds[i] + '_close');
//    if (closeButton) closeButton.style.visibility = 'visible';    
  }
}

function checkSkipBottomLink() 
{
  if (window.pageYOffset > 0)
  {          
    document.getElementById("templateSkipBottom").className = "skipDiv show";
  }
  else
  {
    document.getElementById("templateSkipBottom").className = "skipDiv hide";  
  }        
}

function skipToTop()
{
  document.body.scrollTop = 0;
  document.documentElement.scrollTop = 0;        
}      

initKeyCapture();
