/* Panel.js */

import { PanelManager } from "./PanelManager.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class Panel
{
  constructor(map, options)
  {
    this.title = options.title;
    this.iconClass = options.iconClass || "pi pi-info-circle";
    this.panelManager = PanelManager.getInstance(map);
    this.preferredPosition = options.position || "right";

    this.createPanel();

    this.panelManager.add(this, this.preferredPosition, options.insertTop);
  }

  isVisible()
  {
    return this.panelDiv.style.display !== "none";
  }

  show()
  {
    if (!this.isVisible())
    {
      this.panelDiv.style.display = "";
      this.panelManager.show(this);
      this.onShow();
    }
    else
    {
      this.panelManager.scrollIntoView(this);      
    }
  }

  hide()
  {
    if (this.isVisible())
    {
      this.panelDiv.style.display = "none";
      this.panelManager.hide(this);
      this.onHide();
    }
  }
  
  onShow()
  {    
  }
  
  onHide()
  {    
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
    bodyDiv.className = "body p-1 flex-grow-1 overflow-hidden";
    panelDiv.appendChild(bodyDiv);
  }

  createHeader(headerDiv)
  {
    const titleDiv = document.createElement("div");
    titleDiv.className = "flex-grow-1";
    headerDiv.appendChild(titleDiv);
    this.createTitle(titleDiv);

    const closeButton = document.createElement("button");
    closeButton.className = "panel_button flex-grow-0 cursor-pointer";
    closeButton.title = bundle.get("button.close");
    closeButton.innerHTML = `<span class="pi pi-times"></span>`;
    headerDiv.appendChild(closeButton);

    closeButton.addEventListener("click", (event) =>
    {
      event.preventDefault();
      this.hide();
    });
  }

  createTitle(titleDiv)
  {
    const link = document.createElement("a");
    link.innerHTML = `<span class="${this.iconClass} mr-1"></span> <span>${this.title}</span>`;
    link.href = "#";
    link.addEventListener("click", (event) => { 
      event.preventDefault(); 
      this.panelManager.scrollIntoView(this, true);
    });
    titleDiv.appendChild(link);
  }
}

 export { Panel };

