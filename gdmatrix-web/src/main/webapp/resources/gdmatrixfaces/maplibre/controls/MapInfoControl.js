/* MapInfoControl.js */

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class MapInfoControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : bundle.get("MapInfoControl.title"),
        "iconClass" : "pi pi-info-circle",
        "infoId" : "map_info"
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);

    const bodyDiv = this.panel.bodyDiv;
    if (this.options.infoId)
    {
      let mapInfoElem = document.getElementById(this.options.infoId);
      if (mapInfoElem)
      {
        mapInfoElem.style.display = "";
        mapInfoElem.removeAttribute("aria-hidden");
        bodyDiv.appendChild(mapInfoElem);
      }
    }

    const attributions = [];
    const sources = map.getStyle().sources;
    for (let sourceId in sources)
    {
      let source = sources[sourceId];
      let attribution = source.attribution;
      if (attribution && attribution.trim().length > 0)
      {
        attribution = attribution.trim();
        if (attributions.indexOf(attribution) === -1)
        {
          attributions.push(attribution);
        }
      }
    }

    if (attributions.length > 0)
    {
      const attribUl = document.createElement("ul");
      attribUl.className = "attributions";
      bodyDiv.appendChild(attribUl);

      // attribution format: attribution text [url]
      for (let attribution of attributions)
      {
        let url = null;
        let index1 = attribution.indexOf("[");
        let index2 = attribution.indexOf("]");
        if (index1 > 0 && index2 > index1)
        {
          url = attribution.substring(index1 + 1, index2).trim();
          attribution = attribution.substring(0, index1).trim();
        }
        
        let attribLi = document.createElement("li");
        if (url)
        {
          attribLi.innerHTML = `<div class="flex"><span class="pi pi-info-circle mt-1 mr-1"></span><a href="${url}" target="_blank" class="no-underline">${attribution}</a></div>`;
        }
        else
        {
          attribLi.innerHTML = `<div class="flex"><span class="pi pi-info-circle mt-1 mr-1"></span><span>${attribution}</span></div>`;
        }
        attribUl.appendChild(attribLi);
      }
    }
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="pi pi-info-circle"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
    div.title = this.options.title;
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


