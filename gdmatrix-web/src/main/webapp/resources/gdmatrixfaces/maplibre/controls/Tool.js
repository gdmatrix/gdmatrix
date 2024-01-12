/* Tool */

class Tool // extends IControl
{
  constructor(iconClass, title)
  {
    this.iconClass = iconClass;
    this.title = title;
  }

  activate()
  {
  }

  deactivate()
  {
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `<button><span class="${this.iconClass}"/></button>`;
    div.title = this.title;
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      if (map.activeTool === this)
      {
        this.deactivate();
        this.div.classList.remove("active");
        map.activeTool = null;
      }
      else
      {
        if (map.activeTool)
        {
          map.activeTool.deactivate();
          map.activeTool.div.classList.remove("active");
        }
        map.activeTool = this;
        this.div.classList.add("active");
        this.activate();
      }
    });
    return div;
  }
  
  onRemove()
  {    
  }
}

export { Tool };
