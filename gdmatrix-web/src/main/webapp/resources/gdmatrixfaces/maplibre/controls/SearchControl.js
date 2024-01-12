/* Search */

import { Panel } from "./Panel.js";

class SearchControl
{ 
  constructor(containerId, insertTop)
  {
    this.createPanel(containerId, insertTop);
    this.locators = [];
    this.activeLocator = null;
  }
  
  addLocator(locator)
  {
    this.locators.add(locator);
    if (this.activeLocator === null) this.activeLocator = locator;
  }

  createPanel(containerId, insertTop)
  {
    this.panel = new Panel(containerId, "Search", "pi pi-search", insertTop);    
  }

  onAdd(map)
  {
    this.map = map;
    map.searchControl = this;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="pi pi-search"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
    div.title = this.title;
    div.style.width = "29px";
    div.style.height = "29px";
    div.style.fontFamily = "var(--font-family)";
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      this.panel.show();
    });

    return div;
  }
}

export { SearchControl };

