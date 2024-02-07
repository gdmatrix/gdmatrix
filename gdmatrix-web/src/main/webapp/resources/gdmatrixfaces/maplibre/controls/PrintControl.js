/* PrintControl */

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class PrintControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "left",
        "iconClass" : "pi pi-print",
        "title" : bundle.get("PrintControl.title")
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);

    const bodyDiv = this.panel.bodyDiv;
    bodyDiv.innerHTML = "Print options";
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

// https://www.santfeliu.cat/reports/generic_a3_apaisat.pdf?layer_visibility=00110111111111111111111111111111110110000000000&map_name=planejament&bbox=413162.7845892%2C4579318.8196704%2C427781.13992658%2C4586941.0533658
// https://www.santfeliu.cat/reports/fitxa_planejament.pdf?layer_visibility=00110111111111111111111111111111110110000000000&map_name=planejament&bbox=413162.7845892%2C4579318.8196704%2C427781.13992658%2C4586941.0533658

export { PrintControl };
