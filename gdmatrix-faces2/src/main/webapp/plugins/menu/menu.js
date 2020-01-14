menus = new Array();

function initMenu(elem)
{
  var menuItems = new Array();
  menus[elem] = menuItems;

  var vi = 0;
  var menuElem = document.getElementById(elem);
  for (i = 0; i < menuElem.childNodes.length; i++)
  {
    child = menuElem.childNodes[i];
    if (child.nodeName == "LI")
    {
      var className = child.className;
      menuItems[vi] = "<li class='" + className + "'>" +
        child.innerHTML + "</li>";
      vi++;
    }
  }
}

function showMenu(elem, start, size)
{
  menuItems = menus[elem];
  var menuElem = document.getElementById(elem);
  var text = "";

  if (start < 0) start = 0;
  else if (start > 0) text = "<li class=\"more\"><a href='javascript:showMenu(" + '"' + elem + '",' +
   (start - size) + "," + size + ")'>...</a></li>";

  var end = start + size;
  if (end > menuItems.length) end = menuItems.length;

  for (i = start; i < end; i++)
  {
    text += menuItems[i];
  }

  if (end < menuItems.length)
  {
    text += "<li class=\"more\"><a href='javascript:showMenu(" + '"' + elem + '",' +
    (start + size) + "," + size + ")'>...</a></li>";
  }
  menuElem.innerHTML = text;
}


