/* pf_template.js */

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
  const style = getComputedStyle(document.body);
  let mode = style.getPropertyValue("--menu-mode");
  return mode.trim();
}

function getMenuOptions()
{
  return menuSetup[getMenuMode()];
}

function closeSubMenus(subMenu)
{
  if (!subMenu)
  {
    const mainMenuClass = menuSetup.mainMenuClass;
    subMenu = document.querySelector("." + mainMenuClass);
  }
  var items = [...subMenu.querySelectorAll(".drop")];
  for (var item of items)
  {
    item.classList.remove("drop");
  }
}

function dropSubMenu(elem)
{
  if (!getMenuOptions().multiDrop)
  {
    closeSubMenus();
  }

  const mainMenuClass = menuSetup.mainMenuClass;
  let mainMenu = document.querySelector("." + mainMenuClass);
  let ancestorElem = elem.parentElement;
  let levels = 0;
  while (ancestorElem !== mainMenu)
  {
    if (ancestorElem.nodeName.toUpperCase() === "LI")
    {
      ancestorElem.classList.add("drop");
      levels++;
    }
    ancestorElem = ancestorElem.parentElement;
  }
  if (levels === 1)
  {
    resetSubMenuMargins();
  }
}

function toggleSubMenu(elem)
{
  const mainMenuClass = menuSetup.mainMenuClass;
  let mainMenu = document.querySelector("." + mainMenuClass);
  let ancestorElem = elem.parentElement;
  while (ancestorElem !== mainMenu)
  {
    if (ancestorElem.nodeName.toUpperCase() === "LI")
    {
      if (ancestorElem.classList.contains("drop"))
      {
        ancestorElem.classList.remove("drop");
        updateMenuLayout();
      }
      else
      {
        dropSubMenu(elem);
        let subMenu = ancestorElem.querySelector("ul");
        if (subMenu && subMenu.pageIndex !== undefined && subMenu.pageIndex > 1)
        {
          showSubMenuPage(subMenu, 1);
        }
        else
        {
          updateMenuLayout();
        }
      }
      return;
    }
    ancestorElem = ancestorElem.parentElement;
  }
}

function resetSubMenuMargins()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  const mainMenu = document.querySelector("." + mainMenuClass);
  let subMenus = mainMenu.querySelectorAll(":scope ul > li > ul");
  for (var subMenu of subMenus)
  {
    subMenu.style.marginLeft = "0";
    subMenu.style.marginTop = "0";
  }
}

function updateMenuLayout()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  const mainMenu = document.querySelector("." + mainMenuClass);
  const items = mainMenu.querySelectorAll(":scope > ul > li");

  for (let i = 0; i < items.length; i++)
  {
    let item = items[i];
    updateItemHeight(item);
  }

  if (getMenuMode() === "vertical") return;

  let subMenus = mainMenu.querySelectorAll(":scope ul > li.drop > ul");
  if (subMenus.length === 0) return;

  var topSubMenu = subMenus[0];

  var xdif, ydif;
  for (let level = 0; level < subMenus.length; level++)
  {
    let subMenu = subMenus[level];
    let rect = subMenu.getBoundingClientRect();
    xdif = window.innerWidth - (rect.x + rect.width);
    ydif = window.innerHeight - (rect.y + rect.height);

    if (xdif < 0)
    {
      var marginLeft = parseInt(topSubMenu.style.marginLeft);
      marginLeft += xdif;
      topSubMenu.style.marginLeft = marginLeft + "px";
    }

    if (ydif < 0)
    {
      var marginTop = parseInt(subMenu.style.marginTop);
      marginTop += ydif;
      subMenu.style.marginTop = marginTop + "px";
      rect = subMenu.getBoundingClientRect();
      if (rect.y < 0)
      {
        marginTop -= rect.y;
        marginTop += 30;
        subMenu.style.marginTop = marginTop + "px";
        subMenu.style.height = (window.innerHeight - 30) + "px";
      }
    }
    else
    {
      subMenu.style.overflowY = "";
      subMenu.style.height = "auto";
    }
  }
  if (xdif >= 0)
  {
    var marginLeft = parseInt(topSubMenu.style.marginLeft);
    if (marginLeft + xdif <= 0)
    {
      topSubMenu.style.marginLeft = (marginLeft + xdif) + "px";
    }
    else
    {
      topSubMenu.style.marginLeft = "0px";
    }
  }
}

function updateItemHeight(item)
{
  let height = 0;

  let list = item.querySelector("ul");
  if (list)
  {
    if (item.classList.contains("drop"))
    {
      let subItems = item.querySelectorAll(":scope > ul > li");
      for (let subItem of subItems)
      {
        let menuitem = subItem.querySelector(".menuitem");
        let itemHeight = updateItemHeight(subItem);
        height += (menuitem.offsetHeight + itemHeight);
      }
    }
    list.style.maxHeight = height + "px";
    list.style.height = "auto";
  }
  return height;
}

function onMenuClick(event)
{
  doMenuLayout();
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
    dropSubMenu(event.target);
  }
}

function doMenuLayout()
{
  const mode = getMenuMode();
  if (mode === "horizontal")
  {
    closeSubMenus();
    resetSubMenuMargins();
    updateMenuLayout();
    showDialogPanel("main_menu_panel");
  }
  else // vertical
  {
    hideDialogPanel("main_menu_panel");
  }
}

function scrollSubMenuUp(subMenu)
{
  const maxItems = menuSetup.maxItemsPerSubMenu;

  let pageIndex = subMenu.pageIndex;
  if (pageIndex > 1)
  {
    pageIndex -= maxItems;
    if (pageIndex < 1) pageIndex = 1;
    showSubMenuPage(subMenu, pageIndex);
  }
}

function scrollSubMenuDown(subMenu)
{
  const maxItems = menuSetup.maxItemsPerSubMenu;
  const lastIndex = subMenu.children.length - 1;

  let pageIndex = subMenu.pageIndex;
  if (pageIndex < lastIndex)
  {
    pageIndex += maxItems;
    if (pageIndex > lastIndex) pageIndex = lastIndex;
    showSubMenuPage(subMenu, pageIndex);
  }
}

function showSubMenuPage(subMenu, pageIndex)
{
  const maxItems = menuSetup.maxItemsPerSubMenu;
  subMenu.pageIndex = pageIndex;

  for (let i = 1; i < subMenu.children.length - 1; i++)
  {
    let item = subMenu.children[i];
    if (i >= pageIndex && i < pageIndex + maxItems)
    {
      item.classList.remove("invisible");
    }
    else
    {
      item.classList.add("invisible");
    }
  }
  // paginator buttons
  if (pageIndex === 1)
  {
    subMenu.children[0].classList.add("disabled");
  }
  else
  {
    subMenu.children[0].classList.remove("disabled");
  }
  if (pageIndex + maxItems > subMenu.children.length - 2)
  {
    subMenu.children[subMenu.children.length - 1].classList.add("disabled");
  }
  else
  {
    subMenu.children[subMenu.children.length - 1].classList.remove("disabled");
  }

  // close subMenu
  closeSubMenus(subMenu);
  updateMenuLayout();
}

function initMenu()
{
  const mainMenuClass = menuSetup.mainMenuClass;
  const mainMenu = document.querySelector("." + mainMenuClass);

  var links = mainMenu.querySelectorAll(":scope .menuitem .entry");
  for (let link of links)
  {
    link.addEventListener("click", (event) => 
    {
      if (!link.classList.contains("ui-link"))
      {
        event.preventDefault();
        event.cancelBubble = true;
        onMenuClick(event);
      }
    });
    link.addEventListener("mousemove", (event) => onMenuOver(event));
  }

  var carets = mainMenu.querySelectorAll(":scope .menuitem .caret");
  for (let caret of carets)
  {
    caret.addEventListener("click", (event) => {
      event.preventDefault();
      event.cancelBubble = true;
      toggleSubMenu(event.target);
    });
  }

  const maxItems = menuSetup.maxItemsPerSubMenu;
  var subMenus = mainMenu.querySelectorAll(":scope ul ul");
  for (let subMenu of subMenus)
  {
    if (subMenu.children.length > maxItems)
    {
      let upItem = document.createElement("li");
      upItem.className = "scroll";
      let upAnchor = document.createElement("a");
      upAnchor.title = "Anterior";
      upAnchor.className = "menuitem";
      upAnchor.href = "#";
      upAnchor.addEventListener("click", (event) => 
      {
        event.preventDefault();
        event.cancelBubble = true; 
        scrollSubMenuUp(subMenu); 
      });
      upAnchor.innerHTML = '<i class="fa fa-caret-up" />';
      upItem.appendChild(upAnchor);
      subMenu.insertBefore(upItem, subMenu.children[0]);

      let downItem = document.createElement("li");
      downItem.className = "scroll";
      let downAnchor = document.createElement("a");
      downAnchor.title = "Següent";
      downAnchor.className = "menuitem";
      downAnchor.href = "#";
      downAnchor.addEventListener("click", (event) => 
      {
        event.preventDefault();
        event.cancelBubble = true; 
        scrollSubMenuDown(subMenu);
      });
      downAnchor.innerHTML = '<i class="fa fa-caret-down" />';
      downItem.appendChild(downAnchor);
      subMenu.appendChild(downItem);

      showSubMenuPage(subMenu, 1);
    }
  }

  doMenuLayout();

  // register once
  if (document._menuInitialized) return;
  
  document._menuInitialized = true;

  window.addEventListener("resize", () =>
  {
    doMenuLayout();
  });

  document.body.addEventListener("click", (event) => 
  {
    var elem = event.target;
    var isDialog = false;
    while (elem !== document.body)
    {
      if (elem.classList.contains("dialog_panel"))
      {
        isDialog = true;
        break;
      }
      elem = elem.parentElement;
    }
    if (!isDialog)
    {
      doMenuLayout();
      hideDialogPanels();
    }
  });
}

/* dialog panel */

function hideDialogPanels()
{
  let panels = [...document.querySelectorAll(".dialog_panel")];
  for (let panel of panels)
  {
    panel.classList.remove("show");
  }  
  setBannerZIndex("");
}

function showDialogPanel(panelClass, event)
{
  setBannerZIndex(0);
  if (event)
  {
    event.preventDefault();
    event.cancelBubble = true;
  }  
  const panel = typeof panelClass === "string" ? 
    document.querySelector("." + panelClass) : panelClass;
  if (panel) panel.classList.add("show");
  return false;
}

function hideDialogPanel(panelClass, event)
{
  setBannerZIndex('');
  if (event)
  {
    event.preventDefault();
    event.cancelBubble = true;
  }
  const panel = typeof panelClass === "string" ? 
    document.querySelector("." + panelClass) : panelClass;
  if (panel) panel.classList.remove("show");
  return false;
}

function toggleDialogPanel(panelClass, event)
{
  const panel = typeof panelClass === "string" ? 
    document.querySelector("." + panelClass) : panelClass;
  if (panel)
  {
    if (panel.classList.contains("show"))
    {
      hideDialogPanel(panel, event);
    }
    else
    {
      showDialogPanel(panel, event);    
    }
  }
  return false;
}

function scrollUp()
{
  var cnt = document.body.querySelector(".content_footer");
  if (cnt)
  {
    cnt.scrollTo({
     top: 0,
     left: 0,
     behavior: "smooth"
    });
  }
}

function loadNodeCss(baseUrl, urls)
{
  if (urls instanceof Array)
  {
    const head = document.getElementsByTagName("head")[0];
    
    var links = [...head.querySelectorAll("link")];
    for (var link of links)
    {
      if (link.title === "nodecss")
      {
        head.removeChild(link);
      }
    }
    
    for (var url of urls)
    {
      link = document.createElement("link");
      link.type = "text/css";
      link.rel = "stylesheet";
      link.href = baseUrl + url;
      link.title = "nodecss";
      head.appendChild(link);
    }
  }
}

onResize();
window.addEventListener("resize", onResize);

document.body.addEventListener("pointerdown",
  (event) => _pointerSource = event.srcElement);
