/* CustomPanelControl */

import { Panel } from "../ui/Panel.js";
import { toUtm } from "../utm-latlng.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class CustomPanelControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "left",
        "iconClass" : "pi pi-print",
        "title" : "Title",
        "onShowPanel" : (control) => {}
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);

    if (typeof this.options.onShowPanel === "function")
    {
      this.panel.onShow = () => this.options.onShowPanel(this);
    }
  }
  
  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="${this.options.iconClass}"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
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

export { CustomPanelControl };
