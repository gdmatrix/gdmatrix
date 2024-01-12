/* Panel */

class Panel
{
  constructor(containerId, title, iconClassName, insertTop = false)
  {
    this.containerId = containerId;
    this.title = title;
    this.iconClassName = iconClassName || "pi pi-info-circle";
    this.insertTop = insertTop;
    this.createPanel();
  }

  show()
  {
    this.addPanelToContainer();
    this.panelDiv.style.display = "";
    this.container.classList.add("expanded");
    this.panelDiv.scrollIntoView({
      block: "start", inline: "start", behavior : "smooth" });
  }

  hide()
  {
    this.panelDiv.style.display = "none";
    let panels = this.container.getElementsByClassName("panel");
    let firstVisiblePanel = null;
    for (let panel of panels)
    {
      if (panel.style.display !== "none")
      {
        firstVisiblePanel = panel;
        break;
      }
    }
    if (firstVisiblePanel === null)
    {
      this.container.classList.remove("expanded");
    }
    else
    {
      firstVisiblePanel.scrollIntoView({ behavior : "smooth" });
    }
  }

  addPanelToContainer()
  {
    if (this.container) return;

    this.container = this.containerId ?
      document.getElementById(this.containerId) : map.getContainer();      

    const panelDiv = this.panelDiv;

    let firstPanel = this.container.firstElementChild.firstElementChild;
    if (firstPanel && this.insertTop)
    {
      this.container.firstElementChild.insertBefore(panelDiv, firstPanel);
    }
    else
    {
      this.container.firstElementChild.appendChild(panelDiv);
    }      
  }

  createPanel()
  {
    const panelDiv = document.createElement("div");
    this.panelDiv = panelDiv;
    panelDiv.className = "panel";
    panelDiv.style.display = "none";

    const headerDiv = document.createElement("div");
    this.headerDiv = headerDiv;
    headerDiv.className = "header flex-grow-0 flex p-1";
    panelDiv.appendChild(headerDiv);
    this.createHeader(headerDiv);

    const bodyDiv = document.createElement("div");
    this.bodyDiv = bodyDiv;
    bodyDiv.className = "body flex-grow-1 overflow-auto body";
    panelDiv.appendChild(bodyDiv);
  }

  createHeader(headerDiv)
  {
    const titleDiv = document.createElement("div");
    titleDiv.className = "flex-grow-1";
    headerDiv.appendChild(titleDiv);
    this.createTitle(titleDiv);

    const closeButton = document.createElement("button");
    closeButton.className = "panel_button flex-grow-0";
    closeButton.innerHTML = `<span class="pi pi-times-circle"></span>`;
    headerDiv.appendChild(closeButton);

    closeButton.addEventListener("click", (event) =>
    {
      event.preventDefault();
      this.hide();
    });
  }

  createTitle(titleDiv)
  {
    titleDiv.innerHTML = `<span class="${this.iconClassName} mr-1"></span> <span>${this.title}</span>`;
  }
}
  
 export { Panel };

