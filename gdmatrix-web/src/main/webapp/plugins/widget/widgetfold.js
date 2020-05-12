function doFoldWidget(widgetId, cookieName)
{  
  //Is folded or unfolded?
  var newValue = "";  
  var contentParent = 
    document.getElementById('content_' + widgetId).children[0].children[0];
  var footerParent = 
    document.getElementById('footer_' + widgetId).children[0].children[0];
  if (contentParent)
  {
    if (contentParent.className == "widget_content_3 empty")
    {
      newValue = "u";
    }
    else
    {
      newValue = "f";
    }    
  }
  else if (footerParent)
  {
    if (footerParent.className == "widget_footer_3 empty")
    {
      newValue = "u";
    }
    else 
    {
      newValue = "f";
    }
  }
  //First, we find the cookie value    
  var cookieValue = getCookieValue(cookieName);
  
  cookieValue = cookieValue.replace(/,/g, "%2C");
  cookieValue = cookieValue.replace(/=/g, "%3D");
  
  //Now, we find and update the widget entry
  var i = cookieValue.indexOf(widgetId + "%3D");
  if (i < 0) //not found
  {      
    if (cookieValue != "") cookieValue += "%2C";      
    cookieValue += (widgetId + "%3D" + newValue);      
  }
  else
  {
    cookieValue = cookieValue.substring(0, i) + widgetId + "%3D" + newValue + 
      cookieValue.substring(i + widgetId.length + 4);      
  }
  
  setCookieValue(cookieName, cookieValue, 500 * 24 * 60 * 60 * 1000);

  if (newValue == "f") //fold
  {
    if (footerParent)
    {      
      footerParent.className = "widget_footer_3 empty";
      if (footerParent.children != null && footerParent.children.length > 0)
      {
        for (var i = 0; i < footerParent.children.length; i++)
        {
          footerParent.children[i].style.display = "none";
        }        
      }      
    }
    if (contentParent)
    {
      contentParent.className = "widget_content_3 empty";
      if (contentParent.children != null && contentParent.children.length > 0)
      {
        for (var i = 0; i < contentParent.children.length; i++)
        {        
          contentParent.children[i].style.display = "none";
        }
      }
    }
    switchFoldLink(widgetId, true);
    switchHeaderColor(widgetId, true);
  }
  else //unfold
  {
    if (footerParent)
    {      
      footerParent.className = "widget_footer_3";
      if (footerParent.children != null && footerParent.children.length > 0)
      {
        for (var i = 0; i < footerParent.children.length; i++)
        {        
          footerParent.children[i].style.display = "block";
        }
      }
    }
    if (contentParent)
    {
      contentParent.className = "widget_content_3";
      if (contentParent.children != null && contentParent.children.length > 0)
      {
        for (var i = 0; i < contentParent.children.length; i++)
        {                
          contentParent.children[i].style.display = "block";
        }
      }            
    }
    switchFoldLink(widgetId, false);
    switchHeaderColor(widgetId, false);
  }    
}

function switchFoldLink(widgetId, fold)
{  
  var foldLink = 
    document.getElementById("mainform:" + widgetId + "_fold");
  if (!foldLink)
    foldLink = document.getElementById(widgetId + "_fold");
  var unfoldLink = 
    document.getElementById("mainform:" + widgetId + "_unfold");
  if (!unfoldLink)
    unfoldLink = document.getElementById(widgetId + "_unfold");        
  if (fold)
  {
    foldLink.style.display = "none";
    foldLink.setAttribute("aria-hidden", "true");    
    unfoldLink.style.display = "block";    
    unfoldLink.setAttribute("aria-hidden", "false");
  }
  else
  {
    foldLink.style.display = "block";
    foldLink.setAttribute("aria-hidden", "false");    
    unfoldLink.style.display = "none";    
    unfoldLink.setAttribute("aria-hidden", "true");    
  }
}

function switchHeaderColor(widgetId, fold)
{  
  var headerElement = 
    document.getElementById("mainform:" + widgetId + "_header");
  if (headerElement)
  {
    if (fold)
    {
      headerElement.className = 'widget_header_panel';
    }
    else
    {
      headerElement.className = 'widget_header_panel unfolded';
    }
  }
}

