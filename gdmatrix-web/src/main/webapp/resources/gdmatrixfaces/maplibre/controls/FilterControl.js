/** FilterControl.js **/

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class FilterControl
{
  /*
    options:
      sourceUrls: { sourceId: (startDate, endDate) => url, ... } url function for each geojson source (optional).
      paintProperties : array of [layerId, property name], the Paint properties to apply the variable values
      layoutProperties : array of [layerId, property name], the Layout properties to apply the variable values
      toolbar : array of { label, title, icon, params: {key:value,...} }
      toolbarTitle : text
      toolbarFontSize: text
   */
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : bundle.get("FilterControl.title"),
        "iconClass" : "pi pi-filter",
        "populateForm" : null
      }, ...options};
    this._activeToolbarParams = {};
  }

  createFilterPanel(map)
  {
    const filterPanel = new Panel(map, this.options);
    this.filterPanel = filterPanel;
    
    const bodyDiv = filterPanel.bodyDiv;
    
    bodyDiv.innerHTML =
      `<div class="form flex flex-column p-1">
       </div>
       <div class="button_bar text-right">
        <button class="accept">${bundle.get("button.accept")}</button>
      </div>`;
    this.formDiv = bodyDiv.querySelector(".form");
    
    const populateForm = this.options.populateForm || null;
    
    if (typeof populateForm === "function")
    {
      populateForm(this.formDiv);
    }
    else
    {
      this.formDiv.innerHTML = `
        <textarea id="json_filter" rows=6 style="font-family:monospace"
          class="outline-none border-radius mt-3 mb-2">{}</textarea>
      `;        
    }
    const acceptButton = bodyDiv.querySelector("button.accept");
    acceptButton.addEventListener("click", event => 
    {
      event.preventDefault();
      this.updateSourcesAndLayers();
    });
  }
  
  updateSourcesAndLayers()
  {
    const map = this.map;
    const options = this.options;
    let params = {};
    
    if (options.toolbar)
    {
      params = this._activeToolbarParams;
    }
    else
    {
      const jsonFilter = document.getElementById("json_filter")?.value;
      if (jsonFilter)
      {
        params = JSON.parse(jsonFilter);
      }
      else
      {
        this.paramsFromForm(params);
      }
    }
    console.info(params);
    this.setGlobalParameters(params);

    let sources = map.getStyle().sources;
    for (let sourceId in sources)
    {
      let source = sources[sourceId];
      if (source.type === "geojson")
      {
        const sourceUrls = this.options.sourceUrls || {};
        let sourceUrl = sourceUrls[sourceId];
        if (typeof sourceUrl === "function")
        {
          source = map.getSource(sourceId);
          let url = sourceUrl(params);
          source.setData(url);
          source.updateData();
        }
        else if (source.filter)
        {
          source = map.getSource(sourceId);
          let filter = source.workerOptions.filter;
          if (this.updateExpression(filter, params))
          {
            source.updateData();
          }
        }
      }
    }

    // apply filter
    let layers = map.getStyle().layers;
    for (let layer of layers)
    {
      let layerId = layer.id;
      let filter = map.getFilter(layerId);
      if (this.updateExpression(filter, params))
      {
        map.setFilter(layerId, filter);
      }
    }

    // apply variables to paint properties
    const paintProperties = this.options.paintProperties || [];
    for (let layerIdProperty of paintProperties)
    {
      let layerId = layerIdProperty[0];
      let property = layerIdProperty[1];
      console.info("getPaintProperty", layerId, property);
      let expr = map.getPaintProperty(layerId, property);
      if (this.updateExpression(expr, params))
      {
        map.setPaintProperty(layerId, property, expr);
      }
    }

    // apply variables to layout properties
    const layoutProperties = this.options.layoutProperties || [];
    for (let layerIdProperty of layoutProperties)
    {
      let layerId = layerIdProperty[0];
      let property = layerIdProperty[1];
      console.info("layout property", property);
      let expr = map.getLayoutProperty(layerId, property);
      if (this.updateExpression(expr, params))
      {
        map.setLayoutProperty(layerId, property, expr);
      }
    }
  }
  
  paramsFromForm(params)
  {
    const inputs = this.formDiv.querySelectorAll("input");
    for (const input of inputs)
    {
      let value = input.value;
      params[input.id] = value;
    }

    const selects = this.formDiv.querySelectorAll("select");
    for (const select of selects)
    {
      let value = select.value;
      params[select.id] = value;
    }
  }
  
  setGlobalParameters(params)
  {
    const map = this.map;
  
    for (let param in params)
    {
      let value = params[param];
      map.setGlobalStateProperty(param, value);
      console.info(`set global ${param} = ${value}`);
    }
  }
  
  updateExpression(expression, params)
  {
    if (expression instanceof Array)
    {
      if (expression.indexOf("let") !== 0) return false;
      
      let count = 0;
      for (let param in params)
      {
        let index = expression.indexOf(param);
        if (index !== -1) 
        {
          let value = params[param];
          expression[index + 1] = value;
          console.info(`set ${param} = ${value}`);
          count++;
        }
      }
      return count > 0;
    }
    return true;
  }
  
  onLoad()
  {    
  }
  
  onAdd(map)
  {
    this.map = map;
    const options = this.options;
    const div = document.createElement("div");
    this.div = div;
    if (options.toolbar)
    {
      const buttons = options.toolbar;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group";
      div.title = options.title;
      div.style.width = "29px";
      div.style.fontFamily = "var(--font-family)";
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      
      if (options.toolbarTitle)
      {
        const titleElem = document.createElement("div");
        titleElem.textContent = options.toolbarTitle;
        titleElem.classList.add("text-center");
        titleElem.style.minHeight = "24px";
        titleElem.style.borderBottom = "1px solid var(--surface-300)";
        
        div.appendChild(titleElem);
        if (options.toolbarFontSize)
        {
          titleElem.style.fontSize = options.toolbarFontSize;
        }
      }
      
      let activeButtonElem = null;
      for (let button of buttons)
      {
        const buttonElem = document.createElement("button");
        if (button.icon)
        {
          buttonElem.innerHTML = `<i class="${button.icon}" title="${button.title || ""}" />`;          
        }
        else if (button.label)
        {
          buttonElem.textContent = button.label;
        }
        buttonElem.title = button.title || "";
        buttonElem.style.fontFamily = "var(--font-family)";
        if (options.toolbarFontSize)
        {
          buttonElem.style.fontSize = options.toolbarFontSize;
        }
        if (button.active)
        {
          activeButtonElem = buttonElem;          
        }

        buttonElem.addEventListener("click", (e) =>
        {
          e.preventDefault();
          let selectedButtonElem = div.querySelector(".active");
          if (selectedButtonElem)
          {
            selectedButtonElem.classList.remove("active");
          }
          buttonElem.classList.add("active");
          this._activeToolbarParams = button.params;
          this.updateSourcesAndLayers();
        });
        div.appendChild(buttonElem);
      }
      if (activeButtonElem)
      {
        activeButtonElem.click();
      }      
    }
    else
    {
      // add button to show side panel
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
        this.filterPanel.show();
      });

      this.createFilterPanel(map);
    }
    
    map.on("load", () => this.onLoad());

    return div;
  }
}

export { FilterControl };
