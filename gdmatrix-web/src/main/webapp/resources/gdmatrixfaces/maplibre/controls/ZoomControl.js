/* ZoomControl */

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class ZoomControl
{
  constructor()
  {
  }

  updateZoom()
  {
    const map = this.map;
    const zoom = map.getZoom();
    this.div.textContent = bundle.get("ZoomControl.zoom", zoom.toFixed(2));
  }
  
  normalizeZoom()
  {    
    const map = this.map;
    const zoom = map.getZoom();
    map.flyTo({ zoom: Math.round(zoom) });
  }

  onAdd(map)
  {
    this.map = map;
    
    map.on("zoom", (e) => this.updateZoom());
    
    const div = document.createElement("div");    
    div.className = "maplibregl-ctrl maplibregl-ctrl-group p-1";
    div.style.cursor = "pointer";
    div.style.fontFamily = "var(--font-family)";
    div.title = bundle.get("ZoomControl.title");
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      this.normalizeZoom();
    });
    this.div = div;
    this.updateZoom();

    return div;
  }
}

export { ZoomControl };
