/* LogoControl */

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class LogoControl
{
  constructor(options)
  {
    this.options = {...{
        "className" : "logo_panel",
        "title" : "Logo",
        "content" : "<div>Logo</div>"
      }, ...options};
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    div.innerHTML = this.options.content || "";
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    if (this.options.className)
    {
      div.classList.add(this.options.className);
    }
    div.title = this.options.title || "";
    div.style.fontFamily = "var(--font-family)";
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    
    console.info("LOGO", div);
    return div;
  }
}

export { LogoControl };
