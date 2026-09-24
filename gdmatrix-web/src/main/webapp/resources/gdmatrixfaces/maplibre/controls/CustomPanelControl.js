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
    const floatingPanel = this.options.floatingPanel;
    if (floatingPanel)
    {
      this.floatingPanel = document.createElement("div");
      this.div.appendChild(this.floatingPanel);
      this.div.style.display = "flex";
      this.div.style.flexDirection = "column";
      this.floatingPanel.style.display = "none";
      this.floatingPanel.style.padding = "4px";
      this.floatingPanel.style.padding = "4px";
      this.floatingPanel.style.overflow = "auto";
    }
    else
    {
      this.panel = new Panel(map, this.options);
      if (typeof this.options.onShowPanel === "function")
      {
        this.panel.onShow = () => this.options.onShowPanel(this);
      }
    }
  }
  
  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button style="flex-shrink:0"><span class="${this.options.iconClass}"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.title = this.options.title;
    div.style.width = "29px";
    div.style.height = "29px";
    div.style.transition = "width 0.3s ease, height 0.3s ease";
    div.style.fontFamily = "var(--font-family)";
    div.style.overflow = "hidden";
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    const button = div.querySelector("button");
    button.addEventListener("click", (e) =>
    {
      e.preventDefault();
      const floatingPanel = this.floatingPanel;
      if (floatingPanel)
      {
        if (floatingPanel.style.display === "none")
        {
          div.style.width = this.options.floatingPanelWidth || "30vw";
          div.style.height = this.options.floatingPanelHeight || "20vh";
          floatingPanel.style.display = "block";
          this.options.onShowPanel?.(this);
        }
        else
        {
          div.style.width = "29px";
          div.style.height = "29px";
          floatingPanel.style.display = "none";          
        }
      }
      else
      {
        this.panel.show();
      }
    });

    this.createPanel(map);

    return div;
  }
}

export { CustomPanelControl };
