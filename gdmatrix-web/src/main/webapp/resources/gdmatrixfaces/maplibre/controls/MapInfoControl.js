/* MapInfoControl.js */

import { Panel } from "./Panel.js";

class MapInfoControl
{
  constructor(options)
  {
    this.createPanel(options);
  }

  createPanel(options)
  {
    this.panel = new Panel(options.containerId, 
      "Information", "pi pi-info-circle", options.insertTop);

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

    return div;
  }
}

export { MapInfoControl };


