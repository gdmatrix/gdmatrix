/* PanelManager.js */

class PanelManager
{
  static SMALL_SCREEN_SIZE = 700;
  static HANDLER_SIZE = 14;
  static MIN_PANEL_SIZE = 32;
  static CONTROL_AREA_WIDTH = 160;
  static CONTROL_AREA_MARGIN = 6;

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

    panelContainers.top = new PanelContainer(this, "top", 200);
    panelContainers.bottom = new PanelContainer(this, "bottom", 200);
    panelContainers.left = new PanelContainer(this, "left", 280);
    panelContainers.right = new PanelContainer(this, "right", 280);

    resizeHandlers.top = new ResizeHandler(this, "top");
    resizeHandlers.bottom = new ResizeHandler(this, "bottom");
    resizeHandlers.left = new ResizeHandler(this, "left");
    resizeHandlers.right = new ResizeHandler(this, "right");
    
    this.leftMaxHeight = 0;
    this.rightMaxHeight = 0;
    
    this._organizePanels = () => {
      if (this.controlsElem.isConnected)
      {
        this.organizePanels();
      }
      else
      {
        window.removeEventListener("resize", this._organizePanels);        
      }
    };

    window.addEventListener("resize", this._organizePanels);
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
    const bounds = map.getContainer().getBoundingClientRect();
    this.bounds = bounds;

    const panelContainers = this.panelContainers;
    if (bounds.width < PanelManager.SMALL_SCREEN_SIZE)
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
    
    this.setContainerSize("left", panelContainers.left.size, false);
    this.setContainerSize("right", panelContainers.right.size, false);
    this.setContainerSize("top", panelContainers.top.size, false);
    this.setContainerSize("bottom", panelContainers.bottom.size, false);
    this.doLayout();
  }
  
  getSize(visible = true)
  {
    const map = this.map;
    const mapContainer = map.getContainer();
    let width = mapContainer.offsetWidth;
    let height = mapContainer.offsetHeight;

    if (visible)
    {
      const panelContainers = this.panelContainers;
      const left = panelContainers.left.size;
      const right = panelContainers.right.size;
      const top = panelContainers.top.size;
      const bottom = panelContainers.bottom.size;
      width = width - left - right;
      height = height - top - bottom;
    }
      
    return  {
      width: width,
      height: height
    };
  }
  
  getPadding(offset = 0)
  {
    const panelContainers = this.panelContainers;
    const left = panelContainers.left.size;
    const right = panelContainers.right.size;
    const top = panelContainers.top.size;
    const bottom = panelContainers.bottom.size;

    return {
      top: top + offset, 
      bottom: bottom + offset, 
      left: left + offset, 
      right: right + offset
    };
  }

  doLayout()
  {
    const bounds = this.bounds;
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
    topStyle.display = topContainer.size === 0 ? "none" : "";

    const bottomStyle = bottomContainer.containerDiv.style;
    bottomStyle.top = "auto";
    bottomStyle.bottom = 0;
    bottomStyle.left = leftSize;
    bottomStyle.right = rightSize;
    bottomStyle.height = bottomSize;
    bottomStyle.display = bottomContainer.size === 0 ? "none" : "";

    const leftStyle = leftContainer.containerDiv.style;
    leftStyle.left = 0;
    leftStyle.right = "auto";
    leftStyle.top = 0;
    leftStyle.bottom = 0;
    leftStyle.width = leftSize;
    leftStyle.display = leftContainer.size === 0 ? "none" : "";

    const rightStyle = rightContainer.containerDiv.style;
    rightStyle.left = "auto";
    rightStyle.right = 0;
    rightStyle.top = 0;
    rightStyle.bottom = 0;
    rightStyle.width = rightSize;
    rightStyle.display = rightContainer.size === 0 ? "none" : "";

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

    let topLeftElem, bottomLeftElem, topRightElem, bottomRightElem;
    let topLeftHeight, bottomLeftHeight, topRightHeight, bottomRightHeight; 
    
    let elems = controlsElem
      .getElementsByClassName("maplibregl-ctrl-top-left");
    if (elems.length > 0)
    {
      topLeftElem = elems[0];
      topLeftElem.style.left = leftSize;
      topLeftElem.style.top = topSize;
      topLeftHeight = topLeftElem.offsetHeight;
    }

    elems = controlsElem
            .getElementsByClassName("maplibregl-ctrl-top-right");
    if (elems.length > 0)
    {
      topRightElem = elems[0];
      topRightElem.style.right = rightSize;
      topRightElem.style.top = topSize;
      topRightHeight = topRightElem.offsetHeight;
    }

    elems = controlsElem
            .getElementsByClassName("maplibregl-ctrl-bottom-left");
    if (elems.length > 0)
    {
      bottomLeftElem = elems[0];
      bottomLeftElem.style.left = leftSize;
      bottomLeftElem.style.bottom = bottomSize;
      bottomLeftHeight = bottomLeftElem.offsetHeight;  
    }

    elems = controlsElem
            .getElementsByClassName("maplibregl-ctrl-bottom-right");
    if (elems.length > 0)
    {
      bottomRightElem = elems[0];
      bottomRightElem.style.right = rightSize;
      bottomRightElem.style.bottom = bottomSize;
      bottomRightHeight = bottomRightElem.offsetHeight;
    }

    let leftHeight = topLeftHeight + bottomLeftHeight;
    if (leftHeight > this.leftMaxHeight) this.leftMaxHeight = leftHeight;
    
    let rightHeight = topRightHeight + bottomRightHeight;    
    if (rightHeight > this.rightMaxHeight) this.rightMaxHeight = rightHeight;

    let visibleSize = this.getSize();

    let leftOrientation = visibleSize.height > 
      this.leftMaxHeight + PanelManager.CONTROL_AREA_MARGIN ?
      "vertical" : "horizontal";
    this.updateControlsLayout("left", topLeftElem, leftOrientation);
    this.updateControlsLayout("left", bottomLeftElem, leftOrientation);    
    
    let rightOrientation = visibleSize.height > 
      this.rightMaxHeight + PanelManager.CONTROL_AREA_MARGIN ?
      "vertical" : "horizontal";

    this.updateControlsLayout("right", topRightElem, rightOrientation);
    this.updateControlsLayout("right", bottomRightElem, rightOrientation);

    // TODO: detect which container actually changes
    leftContainer.notifyOnResize();
    rightContainer.notifyOnResize();
    topContainer.notifyOnResize();
    bottomContainer.notifyOnResize();  
  }
  
  updateControlsLayout(side, elem, orientation)
  {
    elem.style.display = "flex";
    if (orientation === "vertical")
    {
      elem.style.flexDirection = "column";
      elem.style.alignItems = side === "left" ? "start" : "end";
      elem.style.width = "auto";
      elem.style.justifyContent = "";
    }
    else
    {
      elem.style.flexDirection = side === "left" ? "row" : "row-reverse";      
      elem.style.alignItems = "start";
      elem.style.width = PanelManager.CONTROL_AREA_WIDTH + "px";
      elem.style.flexWrap = "wrap";
      elem.style.justifyContent = side === "left" ? "start" : "end";
    }
  }

  setContainerSize(position, size, doLayout = true, saveAsPreferred = false)
  {
    const panelContainers = this.panelContainers;
    const panelContainer = panelContainers[position];
    const bounds = this.bounds;

    let maxSize = 1000;
    const gap = PanelManager.HANDLER_SIZE / 2;

    if (position === "left")
    {
      maxSize = bounds.width - panelContainers.right.size - gap;
    }
    else if (position === "right")
    {
      maxSize = bounds.width - panelContainers.left.size - gap;
    }
    else if (position === "top")
    {
      maxSize = bounds.height - panelContainers.bottom.size - gap;
    }
    else if (position === "bottom")
    {
      maxSize = bounds.height - panelContainers.top.size - gap;
    }
    if (maxSize < 0) maxSize = 0;

    let completed = true;
    
    if (size < 0)
    {
      size = 0;
      completed = false;
    }

    if (size > maxSize)
    {
      size = maxSize;
      completed = false;
    }

    panelContainer.size = size;
    if (saveAsPreferred) panelContainer.preferredSize = size;
    if (doLayout) this.doLayout();
    
    return completed;
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
        if (this.setContainerSize(position, size))
        {
          if (panelContainer.animationTimerId)
          {
            clearTimeout(panelContainer.animationTimerId);
          }
          panelContainer.animationTimerId = setTimeout(() =>
            this.resizeContainer(position, targetSize, delay), delay);
        }
      }
    }
    else // expand
    {
      if (size < targetSize)
      {
        size += 10;
        if (size > targetSize) size = targetSize;
        if (this.setContainerSize(position, size))
        {
          if (panelContainer.animationTimerId)
          {
            clearTimeout(panelContainer.animationTimerId);
          }
          panelContainer.animationTimerId = setTimeout(() =>
            this.resizeContainer(position, targetSize, delay), delay);
        }
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

  scrollIntoView(panel, smooth = false)
  {
    panel.panelDiv.scrollIntoView({
      block: "start",
      inline: "start",
      behavior : smooth ? "smooth" : "instant"
    });
  }
}

class PanelContainer
{
  constructor(panelManager, position, size = 240)
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
    this.preferredSize = size;
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
  
  notifyOnResize()
  {
    for (let panel of this.panels)
    {
      panel.onResize();
    }
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
    handlerDiv.innerHTML = "<div></div>";

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

  onMove(event)
  {
    const position = this.position;
    const bounds = this.panelManager.bounds;
    let size;

    if (position === "left")
    {
      size = event.clientX - bounds.left;
    }
    else if (position === "right")
    {
      size = bounds.left + bounds.width - event.clientX;
    }
    else if (position === "top")
    {
      size = event.clientY - bounds.top;
    }
    else if (position === "bottom")
    {
      size = bounds.top + bounds.height - event.clientY;
    }
    
    if (size < PanelManager.MIN_PANEL_SIZE)
    {
      size = PanelManager.MIN_PANEL_SIZE;
    }

    this.panelManager.setContainerSize(position, size, true, true);
  }
}

export { PanelManager };



