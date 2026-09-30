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

function showObject(typeId, objectId, parameters)
{
  const leap = [
    { "name" : "objectId", "value" : objectId }
  ];
  if (typeId !== null)
  {
    leap.push({ "name" : "typeId", "value" : typeId });
  }
  if (parameters)
  {
    leap.push({ "name" : "parameters", "value" : JSON.stringify(parameters) });
  }
  console.info("showObject", leap);
  _showObject(leap);
  return false;
}

function viewObject(objectId, parameters)
{
  showObject(null, objectId, parameters);
  return false;
}

function showPage(pageType, parameters)
{
  const page = [
    { name: "page_type", value: pageType }
  ];
  if (parameters)
  {
    for (let parameter in parameters)
    {
      page.push({ name: parameter, value: parameters[parameter] });
    }
  }
  console.info("showPage", page);
  _showPage(page);
  return false;
}

function controlNavigationHistory(pageState)
{
  console.info("controlNavigationHistory", pageState);
  if (window._popstate)
  {
    window._popstate = false;
  }
  else
  {
    if (pageState.url !== document.location.pathname + document.location.search)
    {
      window.history.pushState(pageState, null, pageState.url);
    }
  }
  document.title = `${pageState.title}`;
}

function setupPopupListener()
{
  console.info("setupPopupListener");
  if (window.popStateListener)
  {
    window.removeEventListener("popstate", window.popStateListener);
  }

  window.popStateListener = event =>
  {
    window._popstate = true;
    var prevPageState = event.state;
    if (prevPageState && prevPageState.baseTypeId)
    {
      showObject(prevPageState.baseTypeId, prevPageState.objectId);
    }
    else
    {
      let params = new URLSearchParams(document.location.search);
      let mid = params.get("xmid");
      if (mid)
      {
        const fparams = Object.fromEntries(params);
        delete fparams["xmid"];
        delete fparams["smid"];
        fparams["mid"] = mid;
        console.info("fparams", fparams);
        showPage("Node", fparams);
      }
      else // pathnames: /web/<mid>
      {
        let pathname = document.location.pathname;
        if (pathname.startsWith("/web/"))
        {
          mid = pathname.substring(5);
          const fparams = Object.fromEntries(params);
          fparams["mid"] = mid;
          console.info("fparams", fparams);
          showPage("Node", fparams);
        }
      }
    }
  };

  window.addEventListener("popstate", window.popStateListener);
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

function updateFontSize(size)
{
  document.querySelector(":root").style.setProperty("--font-size", size + 'px');
  if (isMenuFitEnabled()) refitMenu(); // the bar may no longer fit
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

/* compact mode by fit (menuSetup.compactWhenNoFit) */

function isMenuFitEnabled()
{
  return typeof menuSetup !== "undefined" && menuSetup.compactWhenNoFit === true;
}

// Sets the class "menu-compact" on <html> (COMPACT MODE rules of template.css)
// when the horizontal bar does not fit in the header: more rows than allowed
// (2 for the mega menu, 1 otherwise). It does not depend on the window width,
// so it also covers longer labels and bigger font sizes. The horizontal layout
// is measured in the same frame (forced layout, nothing is painted meanwhile).
function updateMenuFit()
{
  if (!isMenuFitEnabled()) return;

  const root = document.documentElement;
  const mainMenu = document.querySelector("." + menuSetup.mainMenuClass);
  const bar = mainMenu?.querySelector(":scope > ul");
  if (!bar) return;

  const mega = menuSetup.megaMenu === true;
  const hadMega = mainMenu.classList.contains("mega");
  const wasCompact = root.classList.contains("menu-compact");
  root.classList.remove("menu-compact");
  mainMenu.classList.toggle("mega", mega);

  const tops = new Set();
  for (let li of bar.children)
  {
    if (li.offsetHeight > 0) tops.add(Math.round(li.getBoundingClientRect().top));
  }
  const fits = tops.size <= (mega ? 2 : 1);

  mainMenu.classList.toggle("mega", hadMega);
  // the horizontal bar is shown with the "show" class (doMenuLayout): remove it
  // before entering compact mode, or the drawer is painted open and then slides out
  if (!fits && !wasCompact) hideDialogPanel("main_menu_panel");
  root.classList.toggle("menu-compact", !fits);
}

// refit and lay out the menu again (resize, fonts loaded, font size changed)
function refitMenu()
{
  updateMenuFit();
  doMenuLayout();
}

/* mega menu */

const MEGA_HOVER_DELAY = 60;

function isMegaMenu()
{
  return menuSetup.megaMenu === true && getMenuMode() === "horizontal";
}

function clearMenuInlineSizes(mainMenu)
{
  // remove the sizes/margins set inline by the cascade/accordion logic
  for (let list of mainMenu.querySelectorAll(":scope ul"))
  {
    list.style.maxHeight = "";
    list.style.height = "";
    list.style.marginLeft = "";
    list.style.marginTop = "";
    list.style.overflowY = "";
  }
}

function updateMenuAria(mainMenu)
{
  const mega = isMegaMenu();
  for (let entry of mainMenu.querySelectorAll(".entry.toggle"))
  {
    const li = entry.closest("li");
    const topLevel = li.parentElement.parentElement === mainMenu;
    if (mega&& !topLevel)
    {
      // in the sheet, levels 2 and 3 pin/navigate instead of expanding
      entry.removeAttribute("aria-expanded");
    }
    else
    {
      entry.setAttribute("aria-expanded", li.classList.contains("drop"));
    }
  }
}

function activateMegaColumn(panel, li)
{
  for (let col of panel.querySelectorAll(":scope > li"))
  {
    col.classList.toggle("mega_active", col === li);
  }
}

function pinMegaColumn(panel, li)
{
  for (let col of panel.querySelectorAll(":scope > li"))
  {
    col.classList.toggle("mega_pinned", col === li);
  }
  activateMegaColumn(panel, li);
}

function getMegaRest(panel)
{
  return panel.querySelector(":scope > li.mega_pinned") || panel._megaDefault;
}

// called each time a level 1 panel is opened
function initMegaPanel(panel)
{
  if (!panel) return;
  const cols = [...panel.querySelectorAll(":scope > li")];
  panel.style.setProperty("--l2-count", cols.length);
  panel.scrollTop = 0;
  
  // lock the panel height to --mega-rows level 2 rows. It is published on the
  // header so that the sheet card (.app_header::after) and the panel share it
  const owner = panel.closest(".app_header") || panel;
  owner.style.removeProperty("--mega-panel-height");
  
  // level 3: raise the rows if the columns do not fit in the available width
  const style = getComputedStyle(panel);
  const rem = parseFloat(getComputedStyle(document.documentElement).fontSize);
  const colWidth = parseFloat(style.getPropertyValue("--mega-column-width"));
  const l2Width = parseFloat(style.getPropertyValue("--mega-l2-width"));
  const baseRows = parseInt(style.getPropertyValue("--mega-rows")) || 8;
  const maxCols = Math.max(1,
    Math.floor((panel.clientWidth - l2Width - 2 * rem) / colWidth));
  for (let col of cols)
  {
    const list = col.querySelector(":scope > ul");
    if (!list) continue;
    const listRows = Math.max(baseRows, Math.ceil(list.children.length / maxCols));
    list.style.setProperty("--mega-rows", listRows);
  }
  
  const visible = parseInt(
    getComputedStyle(panel).getPropertyValue("--mega-rows")) || 8;
  const rows = cols.map(col => col.querySelector(":scope > .menuitem"));

  // height of the level 2 column: --mega-rows rows, or all of them if fewer
  let height = panel.scrollHeight;
  if (rows.length > visible)
  {
    const top = panel.getBoundingClientRect().top;
    height = rows[visible].getBoundingClientRect().top - top;
  }

  // the panel must never be lower than the tallest level 3
  for (let col of cols)
  {
    const list = col.querySelector(":scope > ul");
    if (!list) continue;
    col.classList.add("mega_active");   // display it to measure it
    list.style.minHeight = "0";
    list.style.maxHeight = "none";
    height = Math.max(height, list.scrollHeight);
    list.style.minHeight = "";
    list.style.maxHeight = "";
    col.classList.remove("mega_active");
  }
  owner.style.setProperty("--mega-panel-height", height + "px");
    
  // every opening starts clean: nothing pinned, first level 2 with children shown
  for (let col of cols) col.classList.remove("mega_pinned", "mega_active");
  panel._megaDefault = cols.find(col => col.querySelector(":scope > ul"));
  if (panel._megaDefault) activateMegaColumn(panel, panel._megaDefault);
}

function onMegaColumnOver(event)
{
  if (!event.currentTarget.classList.contains("mega")) return;

  const panel = event.target.closest(".main_menu.mega > ul > li > ul");
  const li = event.target.closest(".main_menu.mega > ul > li > ul > li");
  if (!panel || !li) return;

  clearTimeout(panel._megaTimer);

  // level 2 without children: it has no level 3, so show none
  if (!li.querySelector(":scope > ul"))
  {
    activateMegaColumn(panel, null);
    return;
  }

  if (li.classList.contains("mega_active")) return;
  panel._megaTimer = setTimeout(() => activateMegaColumn(panel, li),
    MEGA_HOVER_DELAY);
}

function onMegaColumnOut(event)
{
  if (!event.currentTarget.classList.contains("mega")) return;

  const panel = event.target.closest(".main_menu.mega > ul > li > ul");
  if (!panel || panel.contains(event.relatedTarget)) return;

  clearTimeout(panel._megaTimer);
  const rest = getMegaRest(panel);
  if (rest) activateMegaColumn(panel, rest);
}

// --------------------------------------------------

function onMegaMenuClick(event)
{
  const mainMenu = event.currentTarget;
  if (!mainMenu.classList.contains("mega")) return;

  const entry = event.target.closest(".entry");
  if (!entry || !mainMenu.contains(entry)) return;

  const item = entry.closest("li");
  if (!item) return;

  // level 2 with children: pin its level 3 instead of navigating
  const parentPanel = item.parentElement;
  if (parentPanel?.parentElement?.parentElement?.parentElement === mainMenu
      && item.querySelector(":scope > ul"))
  {
    event.preventDefault();
    event.stopPropagation();
    pinMegaColumn(parentPanel, item);
    return;
  }

  // level 1 items that own a panel
  if (item.parentElement.parentElement !== mainMenu) return;
  if (!item.querySelector(":scope > ul")) return;

  // capture phase: stop the click before it reaches the PrimeFaces onclick
  event.preventDefault();
  event.stopPropagation();
  toggleSubMenu(entry);
}

function onMegaMenuWheel(event)
{
  const mainMenu = event.currentTarget;
  if (!mainMenu.classList.contains("mega")) return;

  const panel = event.target.closest(".main_menu.mega > ul > li > ul");
  if (!panel) return;
   
  // Support horizontal actions
  if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) return;
  // let the panel scroll vertically if it really needs it
  if (panel.scrollHeight > panel.clientHeight) return;
  if (panel.scrollWidth <= panel.clientWidth) return;

  const delta = event.deltaMode === 1 ? event.deltaY * 16 : event.deltaY;
  panel.scrollLeft += delta;
  event.preventDefault();
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
        const childDrops = [...ancestorElem.querySelectorAll(":scope li.drop")];
        for (let child of childDrops)
        {
          child.classList.remove("drop");
        }
        updateMenuLayout();
      }
      else
      {
        // mega_opening (template.css): the content unfolds with the card when
        // the sheet opens (no tab was open) and fades when switching tabs. Set
        // before showing the panel and kept while it is open: removing it at the
        // end of the animation would restart the fade (a flash)
        if (isMegaMenu())
        {
          mainMenu.classList.toggle("mega_opening",
            !mainMenu.querySelector(":scope > ul > li.drop"));
        }

        dropSubMenu(elem);
        if (isMegaMenu())
        {
          initMegaPanel(ancestorElem.querySelector(":scope > ul"));
        }

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
  
  updateMenuAria(mainMenu);
  
  if (isMegaMenu())
  {
    clearMenuInlineSizes(mainMenu);
    return;
  }
  
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
  if (menuSetup.onClick)
  {
    menuSetup.onClick(event);
  }
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

  const mainMenu = document.querySelector("." + menuSetup.mainMenuClass);
  if (mainMenu)
  {
    mainMenu.classList.toggle("mega",
      menuSetup.megaMenu === true && mode === "horizontal");
  }

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
    if (mainMenu) updateMenuAria(mainMenu);
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
  
  if (!mainMenu) return;
  
  if (menuSetup.megaMenu === true && !mainMenu._megaMenuInitialized)
  {
    mainMenu._megaMenuInitialized = true;
    // capture = true, runs before the onclick of the level 1 commandLinks
    mainMenu.addEventListener("click", onMegaMenuClick, true);
    mainMenu.addEventListener("wheel", onMegaMenuWheel, { passive: false });
    mainMenu.addEventListener("mouseover", onMegaColumnOver);
    mainMenu.addEventListener("mouseout", onMegaColumnOut);
    mainMenu.addEventListener("keydown", onMegaKeyDown);
    mainMenu.addEventListener("focusin", onMegaFocusIn);
    mainMenu.addEventListener("focusout", onMegaFocusOut);
  }
  
  var links = mainMenu.querySelectorAll(":scope .menuitem .entry");
  for (let link of links)
  {
    link.addEventListener("click", (event) =>
    {
      if (link.classList.contains("toggle"))
      {
        event.preventDefault();
        event.cancelBubble = true;
        toggleSubMenu(link);
        return;
      }
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
    if (caret.closest(".entry")) continue;
    caret.addEventListener("click", (event) => {
      event.preventDefault();
      event.cancelBubble = true;
      toggleSubMenu(event.target);
    });
  }

  const maxItems = menuSetup.maxItemsPerSubMenu;
  if (menuSetup.megaMenu !== true)
  {
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
  }

  updateMenuFit();
  doMenuLayout();

  // register once
  if (document._menuInitialized) return;

  document._menuInitialized = true;

  // the web fonts change the width of the tabs
  if (isMenuFitEnabled() && document.fonts)
  {
    document.fonts.ready.then(refitMenu);
  }
  
  document.addEventListener("keydown", (event) =>
  {
    if (event.key !== "Escape" || !isMegaMenu()) return;
    const mainMenu = document.querySelector("." + menuSetup.mainMenuClass);
    const open = mainMenu?.querySelector(":scope > ul > li.drop");
    if (!open) return;
    closeSubMenus();
    updateMenuLayout();
    open.querySelector(":scope > .menuitem > .entry")?.focus();
  });

  let resizeFrame = 0;
  window.addEventListener("resize", () =>
  {
    // once per frame: updateMenuFit() forces a layout
    cancelAnimationFrame(resizeFrame);
    resizeFrame = requestAnimationFrame(refitMenu);
  });

  document.body.addEventListener("click", (event) =>
  {
    var elem = event.target;
    var isDialog = false;
    while (elem && elem !== document.body)
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
  
  if (isDialogFocusEnabled())
  {
    document.addEventListener("keydown", (event) =>
    {
      if (event.key !== "Escape" || event.defaultPrevented) return;
      const panel = document.activeElement?.closest(".right_panel.show");
      if (panel) hideDialogPanel(panel, event);
    });
  }
}

/* keyboard navigation (mega menu) */

// depth of an entry: 1 = tab, 2 = column title, 3 = link
function getMegaLevel(entry)
{
  const mainMenu = document.querySelector("." + menuSetup.mainMenuClass);
  let level = 0;
  for (let el = entry.parentElement; el && el !== mainMenu; el = el.parentElement)
  {
    if (el.nodeName.toUpperCase() === "LI")
    {
      level++;
    }
  }
  return level;
}

function getMegaEntries(list)
{
  return [...list.querySelectorAll(":scope > li > .menuitem > .entry")]
    .filter(e => e.offsetParent !== null);
}

function openMegaTab(entry)
{
  const li = entry.closest("li");
  if (li.querySelector(":scope > ul")  && !li.classList.contains("drop"))
  {
    toggleSubMenu(entry);
  }
}

function onMegaKeyDown(event)
{
  const mainMenu = event.currentTarget;
  if (!mainMenu.classList.contains("mega")) return;
  if (event.altKey || event.ctrlKey || event.metaKey) return;

  const entry = event.target.closest(".entry");
  if (!entry || !mainMenu.contains(entry)) return;
  
  const key = event.key;
  
  // Space
  if (key === " ")
  {
    event.preventDefault();
    entry.click();
    return;
  }
  
  const level = getMegaLevel(entry);
  const li = entry.closest("li");
  const list = li.parentElement;
  const items = getMegaEntries(list);
  const i = items.indexOf(entry);
  const last = items.length - 1;
  let target = null;
  
  if (level === 1)
  {
    const anyOpen = !!mainMenu.querySelector(":scope > ul > li.drop");
    
    if (key === "ArrowRight" || key === "ArrowLeft")
    {
      const step = key === "ArrowRight" ? 1 : -1;
      target = items[(i + step + items.length) % items.length];
      target.focus();
      event.preventDefault();
      if (anyOpen)
      {
        if (target.closest("li").querySelector(":scope > ul")) 
        {
          openMegaTab(target);
        }
        else
        {
          closeSubMenus();
          updateMenuLayout();
        }
      }
      return;
    }
    else if (key === "ArrowDown")
    {
      const panel = li.querySelector(":scope > ul");
      if (panel)
      {
        openMegaTab(entry);
        target = getMegaEntries(panel)[0];
      }
    }
  }
  else if (level === 2)
  {
    const tab = list.parentElement.querySelector(":scope > .menuitem > .entry");
    if (key === "ArrowDown")
    {
      target = items[Math.min(i + 1, last)];
    }
    else if (key === "ArrowUp")
    {
      target = i > 0 ? items[i - 1] : tab;
    }
    else if (key === "ArrowLeft")
    {
      target = tab;
    }
    else if (key === "ArrowRight")
    {
      const sub = li.querySelector(":scope > ul");
      if (sub)
      {
        activateMegaColumn(list, li);
        target = getMegaEntries(sub)[0];
      }
    }
  }
  else if (level === 3)
  {
    const owner = list.parentElement.querySelector(":scope > .menuitem > .entry");
    const rows = parseInt(
      getComputedStyle(list).getPropertyValue("--mega-rows")) || 8;
    
    // Up/Down cyclic level 3
    if (key === "ArrowDown") 
    {
      target = items[(i + 1) % items.length];
    }
    else if (key === "ArrowUp")
    {
      target = items[(i - 1 + items.length) % items.length];
    }
    else if (key === "ArrowRight")
    {
      if(Math.floor(i / rows) < Math.floor(last / rows))
      {
        target = items[Math.min(i + rows, last)];
      }
    }
    // Previous column, in the 1s one, back to level 2
    else if (key === "ArrowLeft")
    {
      target = i - rows >= 0 ? items[i - rows] : owner;
    }
  }
    
  // any level: first/last item of its list
  if (key === "Home") target = items[0];
  else if (key === "End") target = items[last];

  if (target)
  {
    event.preventDefault();
    target.focus();
  }
}

// a level 2 that gets the focus shows its level 3
function onMegaFocusIn(event)
{
  const mainMenu = event.currentTarget;
  if (!mainMenu.classList.contains("mega")) return;
  
  const entry = event.target.closest(".entry");
  if (!entry || getMegaLevel(entry) !== 2) return;
  
  const li = entry.closest("li");
  activateMegaColumn(li.parentElement, li.querySelector(":scope > ul") ? li : null);
}

function onMegaFocusOut(event)
{
  const mainMenu = event.currentTarget;
  if (!mainMenu.classList.contains("mega")) return;
  if (!event.relatedTarget || mainMenu.contains(event.relatedTarget)) return;
  closeSubMenus();
  updateMenuLayout();
}

/* dialog panel */

function hideDialogPanels()
{
  let panels = [...document.querySelectorAll(".dialog_panel")];
  for (let panel of panels)
  {
    releaseDialogPanel(panel);
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
  
  if (panel)
  {
    const wasShown = panel.classList.contains("show");
    panel.classList.add("show");
    if (event && !wasShown && isDialogFocusEnabled())
    {
      panel._returnFocus = event.currentTarget || document.activeElement;
      focusDialogPanel(panel);
    }
  }
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
  if (panel)
  {
    releaseDialogPanel(panel);
    panel.classList.remove("show");
  }
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


/* Nuevas funciones para respetar el foco en los dialogs (revisar pf_app)*/
function focusDialogPanel(panel)
{
  if (!panel) return;
  if (!panel.hasAttribute("tabindex")) panel.setAttribute("tabindex", "-1");
  const field = panel.querySelector(
    "input:not([type=hidden]):not([type=range]):not([disabled]), textarea:not([disabled])");
  (field || panel).focus({ preventScroll: true });
}

function releaseDialogPanel(panel)
{
  const target = panel._returnFocus;
  panel._returnFocus = null;
  if (target && panel.contains(document.activeElement))
  {
    target.focus({preventScroll: true});
  }
}

function isDialogFocusEnabled()
{
  return typeof menuSetup !== "undefined" && menuSetup.focusDialogs === true;
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
