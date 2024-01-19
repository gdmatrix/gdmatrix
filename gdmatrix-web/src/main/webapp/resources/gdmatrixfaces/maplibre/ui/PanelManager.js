/* PanelManager.js */

class PanelManager
{
  static SMALL_SCREEN_SIZE = 700;
  static HANDLER_SIZE = 14;
    
  constructor(map)
  {
    this.map = map;
    const panelContainers = {};
    const resizeHandlers = {};
    const mapContainerElem = map.getContainer();
    const controlsElem = mapContainerElem
      .getElementsByClassName("maplibregl-control-container")[0];

    this.panelContainers = panelContainers;
    this.resizeHandlers = resizeHandlers;
    this.controlsElem = controlsElem;

    panelContainers.top = new PanelContainer(this, "top");
    panelContainers.bottom = new PanelContainer(this, "bottom");
    panelContainers.left = new PanelContainer(this, "left");
    panelContainers.right = new PanelContainer(this, "right");

    resizeHandlers.top = new ResizeHandler(this, "top");
    resizeHandlers.bottom = new ResizeHandler(this, "bottom");
    resizeHandlers.left = new ResizeHandler(this, "left");
    resizeHandlers.right = new ResizeHandler(this, "right");

    window.addEventListener("resize", () => this.organizePanels());
  }

  static getInstance(map)
  {
    let panelManager = map.panelManager;
    if (panelManager === undefined)
    {
      panelManager = new PanelManager(map);
      map.panelManager = panelManager;
    }
    return panelManager;
  }

  organizePanels()
  {
    const map = this.map;
    const mapContainerElem = map.getContainer();
    let width = mapContainerElem.clientWidth;

    const panelContainers = this.panelContainers;
    if (width < PanelManager.SMALL_SCREEN_SIZE)
    {
      let panels = [...panelContainers.left.panels];
      for (let panel of panels)
      {
        panelContainers.left.remove(panel);
        panelContainers.top.add(panel, false);
      }
      panelContainers.left.size = 0;
      if (panelContainers.top.getFirstVisiblePanel() !== null)
      {
        panelContainers.top.size = panelContainers.top.preferredSize;
      }

      panels = [...panelContainers.right.panels];
      for (let panel of panels)
      {
        panelContainers.right.remove(panel);
        panelContainers.bottom.add(panel, false);
      }
      panelContainers.right.size = 0;
      if (panelContainers.bottom.getFirstVisiblePanel() !== null)
      {
        panelContainers.bottom.size = panelContainers.bottom.preferredSize;
      }
    }
    else
    {
      let panels = [...panelContainers.top.panels];
      for (let panel of panels)
      {
        if (panel.preferredPosition === "left")
        {
          panelContainers.top.remove(panel);
          panelContainers.left.add(panel, false);
        }
      }
      if (panelContainers.left.getFirstVisiblePanel() !== null)
      {
        panelContainers.left.size = panelContainers.left.preferredSize;
      }
      if (panelContainers.top.getFirstVisiblePanel() === null)
      {
        panelContainers.top.size = 0;
      }

      panels = [...panelContainers.bottom.panels];
      for (let panel of panels)
      {
        if (panel.preferredPosition === "right")
        {
          panelContainers.bottom.remove(panel);
          panelContainers.right.add(panel, false);
        }
      }
      if (panelContainers.right.getFirstVisiblePanel() !== null)
      {
        panelContainers.right.size = panelContainers.right.preferredSize;
      }
      if (panelContainers.bottom.getFirstVisiblePanel() === null)
      {
        panelContainers.bottom.size = 0;
      }
    }
    this.doLayout();
  }

  doLayout()
  {
    const panelContainers = this.panelContainers;
    const leftContainer = panelContainers.left;
    const rightContainer = panelContainers.right;
    const topContainer = panelContainers.top;
    const bottomContainer = panelContainers.bottom;

    const leftSize = leftContainer.size + "px";
    const rightSize = rightContainer.size + "px";
    const topSize = topContainer.size + "px";
    const bottomSize = bottomContainer.size + "px";

    const topStyle = topContainer.containerDiv.style;
    topStyle.top = 0;
    topStyle.bottom = "auto";
    topStyle.left = leftSize;
    topStyle.right = rightSize;
    topStyle.height = topSize;

    const bottomStyle = bottomContainer.containerDiv.style;
    bottomStyle.top = "auto";
    bottomStyle.bottom = 0;
    bottomStyle.left = leftSize;
    bottomStyle.right = rightSize;
    bottomStyle.height = bottomSize;

    const leftStyle = leftContainer.containerDiv.style;
    leftStyle.left = 0;
    leftStyle.right = "auto";
    leftStyle.top = 0;
    leftStyle.bottom = 0;
    leftStyle.width = leftSize;

    const rightStyle = rightContainer.containerDiv.style;
    rightStyle.left = "auto";
    rightStyle.right = 0;
    rightStyle.top = 0;
    rightStyle.bottom = 0;
    rightStyle.width = rightSize;

    const HANDLER_SIZE = PanelManager.HANDLER_SIZE;
    const handlerSize = HANDLER_SIZE + "px";

    const topHandlerStyle = this.resizeHandlers.top.handlerDiv.style;
    topHandlerStyle.left = leftSize;
    topHandlerStyle.right = rightSize;
    topHandlerStyle.top = topContainer.size - (HANDLER_SIZE / 2) + "px";
    topHandlerStyle.bottom = "auto";
    topHandlerStyle.height = handlerSize;
    topHandlerStyle.display = topContainer.size === 0 ? "none" : ""; 

    const bottomHandlerStyle = this.resizeHandlers.bottom.handlerDiv.style;
    bottomHandlerStyle.left = leftSize;
    bottomHandlerStyle.right = rightSize;
    bottomHandlerStyle.top = "auto";
    bottomHandlerStyle.bottom = bottomContainer.size - (HANDLER_SIZE / 2) + "px";
    bottomHandlerStyle.height = handlerSize;
    bottomHandlerStyle.display = bottomContainer.size === 0 ? "none" : ""; 

    const leftHandlerStyle = this.resizeHandlers.left.handlerDiv.style;
    leftHandlerStyle.left = leftContainer.size - (HANDLER_SIZE / 2) + "px";
    leftHandlerStyle.right = "auto";
    leftHandlerStyle.top = 0;
    leftHandlerStyle.bottom = 0;
    leftHandlerStyle.width = handlerSize;
    leftHandlerStyle.display = leftContainer.size === 0 ? "none" : ""; 

    const rightHandlerStyle = this.resizeHandlers.right.handlerDiv.style;
    rightHandlerStyle.left = "auto";
    rightHandlerStyle.right = rightContainer.size - (HANDLER_SIZE / 2) + "px";
    rightHandlerStyle.top = 0;
    rightHandlerStyle.bottom = 0;
    rightHandlerStyle.width = handlerSize;
    rightHandlerStyle.display = rightContainer.size === 0 ? "none" : ""; 

    const controlsElem = this.controlsElem;

    let elems = controlsElem
      .getElementsByClassName("maplibregl-ctrl-top-left");
    for (let elem of elems)
    {
      elem.style.left = leftSize;
      elem.style.top = topSize;
    }

    elems = controlsElem
            .getElementsByClassName("maplibregl-ctrl-top-right");
    for (let elem of elems)
    {
      elem.style.right = rightSize;
      elem.style.top = topSize;
    }

    elems = controlsElem
            .getElementsByClassName("maplibregl-ctrl-bottom-left");
    for (let elem of elems)
    {
      elem.style.left = leftSize;
      elem.style.bottom = bottomSize;
    }

    elems = controlsElem
            .getElementsByClassName("maplibregl-ctrl-bottom-right");
    for (let elem of elems)
    {
      elem.style.right = rightSize;
      elem.style.bottom = bottomSize;
    }
  }

  setContainerSize(position, size)
  {
    this.panelContainers[position].size = size;
    this.doLayout();
  }

  resizeContainer(position, targetSize, delay = 2)
  {
    let panelContainer = this.panelContainers[position];
    let size = panelContainer.size;

    if (targetSize === 0) // collapse
    {
      if (size > 0)
      {
        size -= 10;
        if (size < 0) size = 0;
        this.setContainerSize(position, size);
        if (panelContainer.animationTimerId)
        {
          clearTimeout(panelContainer.animationTimerId);
        }
        panelContainer.animationTimerId = setTimeout(() =>
          this.resizeContainer(position, targetSize, delay), delay);
      }
    }
    else // expand
    {
      if (size < targetSize)
      {
        size += 10;
        if (size > targetSize) size = targetSize;
        this.setContainerSize(position, size);
        if (panelContainer.animationTimerId)
        {
          clearTimeout(panelContainer.animationTimerId);
        }
        panelContainer.animationTimerId = setTimeout(() =>
          this.resizeContainer(position, targetSize, delay), delay);
      }
    }
  }

  add(panel, position, insertTop)
  {
    let panelContainer = this.panelContainers[position];
    panelContainer.add(panel, insertTop);
  }

  show(panel)
  {
    const preferredSize = this.panelContainers[panel.position].preferredSize;
    this.resizeContainer(panel.position, preferredSize);
    this.scrollIntoView(panel);
  }

  hide(panel)
  {
    let panelContainer = this.panelContainers[panel.position];
    let firstVisiblePanel = panelContainer.getFirstVisiblePanel();
    if (firstVisiblePanel === null)
    {
      this.resizeContainer(panel.position, 0);
    }
    else
    {
      this.scrollIntoView(firstVisiblePanel);
    }
  }

  scrollIntoView(panel)
  {
    panel.panelDiv.scrollIntoView({
      block: "start", inline: "start", behavior : "smooth" });
  }
}

class PanelContainer
{
  constructor(panelManager, position)
  {
    this.panelManager = panelManager;
    const controlsElem = panelManager.controlsElem;

    const containerDiv = document.createElement("div");
    containerDiv.className = "maplibre_panel_container";
    containerDiv.innerHTML = `<div class="scroller"></div>`;
    controlsElem.appendChild(containerDiv);

    this.containerDiv = containerDiv;
    this.position = position;
    this.panels = [];
    this.size = 0;
    this.preferredSize = position === "left" || position === "right" ? 260 : 200;
    this.animationTimerId = null;
  }

  getFirstVisiblePanel()
  {
    let firstVisiblePanel = null;
    for (let panel of this.panels)
    {
      if (panel.isVisible())
      {
        firstVisiblePanel = panel;
        break;
      }
    }
    return firstVisiblePanel;
  }

  add(panel, insertTop)
  {
    const containerDiv = this.containerDiv;
    const panelDiv = panel.panelDiv;

    let firstPanelDiv = containerDiv.firstElementChild.firstElementChild;
    if (firstPanelDiv && insertTop)
    {
      containerDiv.firstElementChild.insertBefore(panelDiv, firstPanelDiv);
      this.panels.splice(0, 0, panel);
    }
    else
    {
      containerDiv.firstElementChild.appendChild(panelDiv);
      this.panels.push(panel);
    }
    panel.position = this.position;
  }

  remove(panel)
  {
    const containerDiv = this.containerDiv;
    const panelDiv = panel.panelDiv;
    if (panelDiv.parentElement === containerDiv)
    {
      containerDiv.removeChild(panelDiv);
    }

    let index = this.panels.indexOf(panel);
    if (index !== -1)
    {
      this.panels.splice(index, 1);
    }
    panel.position = null;
  }
}

class ResizeHandler
{
  constructor(panelManager, position)
  {
    this.panelManager = panelManager;
    const controlsElem = panelManager.controlsElem;
    this.position = position;

    const handlerDiv = document.createElement("div");
    handlerDiv.className = "maplibre_handler " + position;
    controlsElem.appendChild(handlerDiv);
    this.handlerDiv = handlerDiv;

    this._onMove = (event) => this.onMove(event);
    this._removeListeners = (event) => this.removeListeners(event);

    handlerDiv.addEventListener("pointerdown",
      (event) => this.addListeners(event));
  }

  addListeners(event)
  {
    event.preventDefault();
    const handlerDiv = this.handlerDiv;
    handlerDiv.addEventListener("pointermove", this._onMove, false);
    handlerDiv.addEventListener("pointerup", this._removeListeners, false);    
    handlerDiv.setPointerCapture(event.pointerId);
  }

  removeListeners(event)
  {
    event.preventDefault();
    const handlerDiv = this.handlerDiv;
    handlerDiv.removeEventListener("pointermove", this._onMove, false);
    handlerDiv.removeEventListener("pointerup", this._removeListeners, false);
    handlerDiv.releasePointerCapture(event.pointerId);
  }

  getCurrentSize(event)
  {    
    const rect = this.panelManager.map.getContainer().getBoundingClientRect();
    let curSize = 0;
    if (this.position === "left")
    {
      curSize = event.clientX - rect.left;
    }
    else if (this.position === "right")
    {
      curSize = rect.left + rect.width - event.clientX;
    }
    else if (this.position === "top")
    {
      curSize = event.clientY - rect.top;
    }
    else if (this.position === "bottom")
    {
      curSize = rect.top + rect.height - event.clientY;
    }
    return curSize;
  }

  onMove(event)
  {
    const size = this.getCurrentSize(event);
    const panelContainer = this.panelManager.panelContainers[this.position];    
    panelContainer.size = size;
    panelContainer.preferredSize = size;
    this.panelManager.doLayout();
  }
}

function init(map)
{
  if (map.panelManager)
  {
    map.panelManager.organizePanels();
  }
}

export { PanelManager, init };



