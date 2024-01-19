/* Tool.js */

class Tool // extends IControl
{
  constructor(options)
  {
    this.options = options;
  }

  activate()
  {
  }

  deactivate()
  {
  }
  
  reactivate()
  {    
  }
  
  createPanel(map)
  {
  }
  
  activateTool(tool)
  {
    const map = this.map;
    if (tool && !map.activeTool)
    {
      map.activeTool = tool;
      tool.div.classList.add("active");
      tool.activate();
    }
  }
  
  deactivateTool(tool)
  {
    const map = this.map;
    if (tool && map.activeTool === tool)
    {
      map.activeTool = null;
      tool.deactivate();
      tool.div.classList.remove("active");
    }    
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `<button><span class="${this.options.iconClass}"/></button>`;
    div.title = this.options.title;
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      if (map.activeTool === this)
      {
        this.reactivate();
      }
      else
      {
        this.deactivateTool(map.activeTool);
        this.activateTool(this);
      }
    });
    
    this.createPanel(map);
    
    return div;
  }
  
  onRemove()
  {    
  }
}

export { Tool };
