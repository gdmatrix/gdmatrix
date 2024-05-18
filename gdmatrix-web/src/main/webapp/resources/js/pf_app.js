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

onResize();
window.addEventListener("resize", onResize);

document.body.addEventListener("pointerdown",
  (event) => _pointerSource = event.srcElement);
