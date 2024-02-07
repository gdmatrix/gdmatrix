/* RefreshControl.js */

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class RefreshControl
{
  constructor()
  {
  }

  onAdd(map)
  {
    this.map = map;
    const div = document.createElement("div");
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `<button><span class="pi pi-refresh"/></button>`;
    div.title = bundle.get("RefreshControl.title");
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      this.refreshSources();
    });

    return div;
  }
  
  refreshSources()
  {
    const map = this.map;
    const sources = map.getStyle().sources;
    const seed = "_seed=" + Math.random();
    for (let sourceId in sources)
    {
      let source = sources[sourceId];
      if (source.type === "geojson" && typeof source.data === "string")
      {
        let url = source.data;
        if (url.indexOf("?") === -1)
        {
          url += "?" + seed;
        }
        else
        {
          url += "&" + seed;
        }
        console.info(url);
        map.getSource(sourceId).setData(url);
      }
    }
  }
}

export { RefreshControl };


