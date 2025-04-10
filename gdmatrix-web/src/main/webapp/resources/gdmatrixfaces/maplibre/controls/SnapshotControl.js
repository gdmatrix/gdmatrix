/* SnapshotControl */

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class SnapshotControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "left",
        "iconClass" : "pi pi-camera",
        "title" : bundle.get("SnapshotControl.title")
      }, ...options};
  }

  sendSnapshot()
  {
    if (typeof saveSnapshot === "function")
    {
      const map = this.map;
      const canvas = map.getCanvas();
      const imageURL = canvas.toDataURL("image/png");
      saveSnapshot([{name: 'snapshot', value: imageURL}]);
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
      this.sendSnapshot();
    });

    return div;
  }
}

export { SnapshotControl };
