function switchWidget(widgetId, widgetsCookieName) 
{  
  var cookieValue = getCookieValue(widgetsCookieName);
  
  //Now, we find and update the widget entry
  if (isWidgetInCookie(cookieValue, widgetId)) //unselect
  {    
    removeWidget(widgetId);     
  }
  else //select
  {
    var newCookieValue = getNewCookieValue(cookieValue, widgetId);
    setCookieValue(widgetsCookieName, newCookieValue, 500 * 24 * 60 * 60 * 1000);    
    document.getElementById("mainform:refreshWidgetContainer").click();    
  }
  return false;
}

function isWidgetInCookie(cookieValue, widgetId)
{
  var columns = cookieValue.split("%7C"); //split by |
  for (var i = 0; i < columns.length; i++)
  {
    var widgets = columns[i].split("%2C"); //split by ,
    for (var j = 0; j < widgets.length; j++)
    {
      if (widgets[j] == widgetId) return true;
    }
  }
  return false;
}

//Put the new widget in the emptiest column
function getNewCookieValue(currentCookieValue, widgetId)
{
  //Now we found the emptiest column
  var minSize = 9999;
  var minColumn = -1;
  var columns = currentCookieValue.split("%7C"); //split by |
  for (var i = 0; i < columns.length; i++)
  {
    var auxSize = 0;
    var widgets = columns[i].split("%2C"); //split by ,
    for (var j = 0; j < widgets.length; j++)
    {
      var widgetIdTrim = widgets[j].replace(/^\s+|\s+$/g, ''); //trim
      if (widgetIdTrim.length > 0) auxSize++;
    }        
    if (auxSize < minSize)
    {
      minSize = auxSize;
      minColumn = i;
    }
  }
  //Now we build and return the new cookieValue
  var newCookieValue = "";
  for (var i = 0; i < columns.length; i++)
  {
    if (i > 0) newCookieValue += "%7C"; //separator |
    var widgets = columns[i].split("%2C"); //split by ,
    if (i == minColumn) widgets.unshift(widgetId);
    for (var j = 0; j < widgets.length; j++)
    {
      if (j > 0) newCookieValue += "%2C"; //separator ,
      newCookieValue += widgets[j];
    }    
  }
  return newCookieValue;
}

function unselectWidget(widgetId) 
{
  var linkElement = document.getElementById('widget_selector_' + widgetId);
  if (linkElement) 
  {
    //linkElement.className = "widgetLink";
    var idx = linkElement.parentNode.className.indexOf("selected");
    if (idx != -1)
    {
      linkElement.parentNode.className = 
        linkElement.parentNode.className.substring(0, idx) + 
        linkElement.parentNode.className.substring(idx + 8);      
    }    
  }  
  
}

function widgetSelectorMove(newIndex)
{
  if (newIndex > widgetSelectorSize) newIndex = 1;
  var stop = false;
  var i = 1;
  while (!stop)
  {
    var item = document.getElementById('widgetSelectorItem' + i);
    if (item)
    {
      var selected = (item.className.indexOf("selected") != -1);
      if (i >= newIndex && i < newIndex + widgetSelectorWindow)
      {
        item.className = 'item visible';
      }
      else
      {        
        item.className = 'item hidden';
      }
      if (selected) item.className = item.className + " selected";
      i++;
    }
    else
    {
      stop = true;
    }
  }
  
  leftImg = document.getElementById('widgetSelectorLeftImg');
  rightImg = document.getElementById('widgetSelectorRightImg');
  
  if (newIndex + widgetSelectorWindow <= widgetSelectorSize)
  {
    rightImg.src = "/templates/widgetportal2/images/widget_selector_right_on.png";
  }
  else
  {
    rightImg.src = "/templates/widgetportal2/images/widget_selector_right_off.png";
  }
  
  if (newIndex - widgetSelectorWindow >= 1)
  {
    leftImg.src = "/templates/widgetportal2/images/widget_selector_left_on.png";
  }
  else
  {
    leftImg.src = "/templates/widgetportal2/images/widget_selector_left_off.png";    
  }

  widgetSelectorIndex = newIndex;
  
  //Update widget selector cookie
  setCookieValue(widgetSelectorIndexCookieName, widgetSelectorIndex, 30 * 60 * 1000);
}

function widgetSelectorLeft()
{
  if (widgetSelectorIndex > 1)
  {
    var newIndex = widgetSelectorIndex - widgetSelectorWindow;  
    widgetSelectorMove(newIndex);  
  }  
}

function widgetSelectorRight()
{
  if (widgetSelectorIndex <= (widgetSelectorSize - widgetSelectorWindow))
  {
    var newIndex = widgetSelectorIndex + widgetSelectorWindow;  
    widgetSelectorMove(newIndex);        
  }
}

function widgetSelectorMakeElementsVisible()
{
  document.getElementById('widgetSelectorLeftLink').style.visibility = 'visible';
  document.getElementById('widgetSelectorRightLink').style.visibility = 'visible';
  for (i = 1; i <= widgetSelectorSize; i++)
  {
    document.getElementById('widgetSelectorItem' + i).style.visibility = 'visible';
  }  
}
