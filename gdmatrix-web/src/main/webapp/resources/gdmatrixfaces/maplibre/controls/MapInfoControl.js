/* MapInfoControl.js */

import { Panel } from "../ui/Panel.js";

class MapInfoControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : "Map information",
        "iconClass" : "pi pi-info-circle"
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);

    const bodyDiv = this.panel.bodyDiv;
    for (let i = 0; i < 20; i++)
    {
      let div = document.createElement("div");
      div.innerHTML = `<span>bla bla bla bla bla</span>`;
      bodyDiv.appendChild(div);
    }
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="pi pi-info-circle"/></button>`;
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
    
    this.createPanel(map);

    return div;
  }
}

export { MapInfoControl };


