/* pf_app.js */

function showContent(mid)
{
  // remove duplicates in detachedWidgets
  PrimeFaces.detachedWidgets = PrimeFaces.detachedWidgets.filter(
    (value, index) => PrimeFaces.detachedWidgets.indexOf(value) === index);

  // highlight toolbar
  var elems = document.getElementsByClassName("ui-button current");
  if (elems.length > 0)
  {
    elems[0].classList.remove("current");
  }

  elems = document.getElementsByClassName("btn-" + mid);
  if (elems.length > 0)
  {
    var elem = elems[0];
    elem.classList.add("current");
    elem.scrollIntoView();
  }

  // highlight vertical menu
  var verticalMenu = document.getElementById("mainform:vertical_menu");
  if (verticalMenu)
  {
    var selected = verticalMenu.querySelector(".current");
    if (selected) selected.classList.remove("current");
    selected = document.getElementById("mainform:vertical_menu_" + mid + ":cl");
    if (selected) selected.classList.add("current");
  }          
}

function setBannerZIndex(value)
{
  var elems = document.getElementsByClassName("bannerFixed");
  if (elems.length > 0)
  {
    elems[0].style.zIndex = value;
  }
}
            
function enableUserPassButton()
{
  var widget = PrimeFaces.widgets['usernameInput'];
  if (widget)
  {
    var value = widget.jq.val();
    widget = PrimeFaces.widgets['userPassButton'];
    if (widget)
    {
      if (value?.length > 0) 
      {
        widget.enable();
      } 
      else 
      {
        widget.disable();
      }        
    }
  }
}
      
function showObject(typeId, objectId)
{
  _showObject([
    { "name" : "typeId", "value" : typeId }, 
    { "name" : "objectId", "value": objectId }]);
}

function updateFontSize(size)
{
  document.querySelector(":root").style.setProperty("--font-size", size + 'px');            
}

function onResize()
{
  var elems = document.getElementsByClassName("webtoolbar");
  if (elems.length > 0)
  {
    var toolbar = elems[0];
    var frame = document.getElementById("frame");
    frame.style.top = toolbar.clientHeight + "px";
  }
}

/* menu logic */

function getMenuMode()
{
  const responsiveWidth = menuSetup?.responsiveWidth || 1050;
  return (window.innerWidth > responsiveWidth) ? "horizontal" : "vertical";  
}

function getMenuOptions()
{  
  return menuSetup[getMenuMode()];
}

function hideMenus()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  var items = [...document.querySelectorAll("." + mainMenuClass + " .drop")];
  for (var item of items)
  {
    item.classList.remove("drop");
  }
}

function dropMenu(elem)
{
  if (!getMenuOptions().multiDrop)
  {
    hideMenus();
  }

  const mainMenuClass = menuSetup.mainMenuClass;
  let menuElement = document.querySelector("." + mainMenuClass);
  let ancestorElem = elem.parentElement;
  while (ancestorElem !== menuElement)
  {
    if (ancestorElem.nodeName.toUpperCase() === "LI")
    {
      ancestorElem.classList.add("drop");
    }
    ancestorElem = ancestorElem.parentElement;
  }
}

function showMenuPanel()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  const mainMenuElement = document.querySelector("." + mainMenuClass);
  mainMenuElement.classList.add("menu_visible");
}

function hideMenuPanel()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  const mainMenuElement = document.querySelector("." + mainMenuClass);
  mainMenuElement.classList.remove("menu_visible");
}

function toggleMenuPanel(event)
{
  event.preventDefault();
  event.cancelBubble = true;
  const mainMenuClass = menuSetup.mainMenuClass;
  const mainMenuElement = document.querySelector("." + mainMenuClass);
  mainMenuElement.classList.toggle("menu_visible");
}

function toggleMenu(elem)
{
  const mainMenuClass = menuSetup.mainMenuClass;  
  let menuElement = document.querySelector("." + mainMenuClass);
  let ancestorElem = elem.parentElement;
  while (ancestorElem !== menuElement)
  {
    if (ancestorElem.nodeName.toUpperCase() === "LI")
    {
      if (ancestorElem.classList.contains("drop"))
      {
        ancestorElem.classList.remove("drop");
      }
      else
      {
        dropMenu(elem);
      }            
      return;
    }
    ancestorElem = ancestorElem.parentElement;
  }
}

function onMenuClick(event)
{
  doLayout();
}

function onMenuOver(event)
{
  if (getMenuOptions().hoverDrop)
  {
    let ancestorElem = event.target.parentElement;
    while (ancestorElem)
    {
      if (ancestorElem.nodeName.toUpperCase() === "LI")
      {
        if (ancestorElem.classList.contains("drop")) return;
        break;
      }
      ancestorElem = ancestorElem.parentElement;
    }
    dropMenu(event.target);
  }
}

function doLayout()
{
  const mode = getMenuMode();
  if (mode === "horizontal")
  {
    hideMenus();
    showMenuPanel();
  }
  else // vertical
  {
    hideMenuPanel();
  }
}

function toggleSideMenu(event)
{
  event.cancelBubble = true;
  var element = document.querySelector(".side_menu_panel");
  element.classList.toggle("menu_visible");
  return false;
}

function hideSideMenu()
{
  var element = document.querySelector(".side_menu_panel");
  element.classList.remove("menu_visible");  
}

function initMenu()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  var links = document.querySelectorAll("." + mainMenuClass + " .menuitem .entry");
  for (let link of links)
  {
    link.addEventListener("click", (event) => onMenuClick(event));
    link.addEventListener("mousemove", (event) => onMenuOver(event));
  }  

  var carets = document.querySelectorAll("." + mainMenuClass + " .menuitem .caret");
  for (let caret of carets)
  {
    caret.addEventListener("click", (event) => { 
      event.preventDefault();
      toggleMenu(event.target);
    }); 
  }
  
  window.addEventListener("resize", () =>
  {
    doLayout();
  });
  
  doLayout();

  document.body.addEventListener("click", (event) => {
    let elem = event.target;
    
    let menuElement = document.querySelector("." + mainMenuClass);
    let ancestorElem = elem.parentElement;
    while (ancestorElem)
    {
      if (ancestorElem === menuElement) return;
      ancestorElem = ancestorElem.parentElement;
    }
    if (getMenuMode() === "horizontal")
    {
      hideMenus();     
    }
    else
    {
      hideMenuPanel();
      hideSideMenu();
    }
  });
}

onResize();
window.addEventListener("resize", onResize);

document.body.addEventListener("pointerdown",
  (event) => _pointerSource = event.srcElement);
