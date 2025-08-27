/* LegendSetupControl.js */

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class LayerSetupControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : bundle.get("LayerSetupControl.title"),
        "iconClass" : "pi pi-cog"
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="pi pi-cog"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
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
    this.populatePanel();

    return div;
  }

  async populatePanel()
  {
    const panel = this.panel;
    panel.bodyDiv.classList.add("layer_setup");
    panel.bodyDiv.innerHTML = `
      <ul class="tab_selector">
        <li class="sources_sel"><a href="#" class="active"><span class="pi pi-database"></span> Sources</a></li>
        <li class="layers_sel"><a href="#"><span class="fa fa-layer-group"></span> Layers</a></li>
      </ul>
      <div class="sources_tab">
      </div>
      <div class="layers_tab hidden">
      </div>
    `;
    this.sourcesTab = panel.bodyDiv.querySelector(".sources_tab");
    this.layersTab = panel.bodyDiv.querySelector(".layers_tab");
    this.sourcesLink = panel.bodyDiv.querySelector(".sources_sel a");
    this.sourcesLink.addEventListener("click", event =>
    {
      event.preventDefault();
      this.showTab("sources");
    });
    this.layersLink = panel.bodyDiv.querySelector(".layers_sel a");
    this.layersLink.addEventListener("click", event =>
    {
      event.preventDefault();
      this.showTab("layers");
    });

    let url = "https://gis.santfeliu.cat/geoserver/wfs";
    let featureTypes = await OGCServerInspector.getFeatureTypes(url);
    this.sourceList = document.createElement("ul");
    this.sourceList.className = "sources";
    this.sourcesTab.appendChild(this.sourceList);
    for (let featureType of featureTypes)
    {
      if (featureType.keywords.includes("catalogue"))
      {
        let sourceItem = document.createElement("li");
        this.sourceList.appendChild(sourceItem);

        sourceItem.innerHTML = `
        <div class="text">
           <div class="name">${featureType.title}</div>
           <div>${featureType.description}</div>
        </div>
        <div class="buttons">
          <button class="ui-button ui-widget ui-button-icon-only rounded-button ui-button-flat">
            <span class='pi pi-plus-circle'></span>
          </button>
        </div>
        `;
        let sourceButton = sourceItem.querySelector("button");
        sourceButton.addEventListener("click", event => {
          event.preventDefault();
          this.addSourceAndLayer(url, featureType);
        });
      }
    }
    let layerList = document.createElement("ul");
    this.layerList = layerList;
    this.layerList.className = "layers";
    this.layersTab.appendChild(this.layerList);
  }

  async addSourceAndLayer(url, featureType)
  {
    const featureInfo = await FeatureTypeInspector.getInfo(url, featureType.name);
    const geometryType = featureInfo.geometryType;

    const map = this.map;
    let layerItem = document.createElement("li");
    this.layerList.appendChild(layerItem);
    layerItem.innerHTML = `
      <div class="flex flex-row">
        <button class="expand ui-button ui-widget ui-button-icon-only rounded-button ui-button-flat">
          <span class="pi pi-chevron-right"></span>
        </button>
        <div class="flex-grow-1 font-bold flex align-items-center">${featureType.title}</div>
        <button class="up ui-button ui-widget ui-button-icon-only rounded-button ui-button-flat">
          <span class="pi pi-arrow-up"></span>
        </button>
        <button class="down ui-button ui-widget ui-button-icon-only rounded-button ui-button-flat">
          <span class="pi pi-arrow-down"></span>
        </button>
        <button class="close ui-button ui-widget ui-button-icon-only rounded-button ui-button-flat">
          <span class="pi pi-times"></span>
        </button>
      </div>
      <div class="options hidden p-2">
        <label>Type:
          <select class="layer_type">
            <option value="circle">circle</option>
            <option value="line">line</option>
            <option value="fill">fill</option>
          </select>
        </label>
        <ul class="prop_list p-0">
        </ul>
      </div>
    `;

    this.showTab("layers");

    let sourceId = featureType.name.toLowerCase();
    let index = sourceId.indexOf(":");
    if (index !== -1)
    {
      sourceId = sourceId.substring(index + 1);
    }
    console.info("source " + sourceId);

    let source = map.getSource(sourceId);
    if (!source)
    {
      map.addSource(sourceId,
      {
        "type": "geojson",
        "data": "/proxy?url=" + url +"&service=WFS&VERSION=1.0.0&REQUEST=GetFeature&typeName="
          + featureType.name + "&outputFormat=application/json&srsName=EPSG:4326"
      });
      console.info("added source " + sourceId);
    }

    index = 1;
    let layerId = sourceId + "_" + index;
    while (map.getLayer(layerId))
    {
      index++;
      layerId = sourceId + "_" + index;
    }
    
    layerItem.id = "layer_" + layerId;

    let layerType;
    if (geometryType === "Surface")
    {
      layerType = "fill";
    }
    else if (geometryType === "LineString")
    {
      layerType = "line";
    }
    else
    {
      layerType = "circle";
    }

    let optionsPanel = layerItem.querySelector("div.options");
    let propList = layerItem.querySelector(".prop_list");

    this.createLayer(propList, sourceId, layerId, layerType);

    let expandButton = layerItem.querySelector("button.expand");
    expandButton.addEventListener("click", event =>
    {
      event.preventDefault();
      optionsPanel.classList.toggle("hidden");
      let icon = expandButton.querySelector("span");
      if (icon.classList.contains("pi-chevron-right"))
      {
        icon.classList.remove("pi-chevron-right");
        icon.classList.add("pi-chevron-down");
      }
      else
      {
        icon.classList.add("pi-chevron-right");
        icon.classList.remove("pi-chevron-down");
      }
    });

    let upButton = layerItem.querySelector("button.up");
    upButton.addEventListener("click", event =>
    {
      event.preventDefault();
      this.moveLayer(layerItem, layerId, -1);
    });

    let downButton = layerItem.querySelector("button.down");
    downButton.addEventListener("click", event =>
    {
      event.preventDefault();
      this.moveLayer(layerItem, layerId, +1);
    });

    let closeButton = layerItem.querySelector("button.close");
    closeButton.addEventListener("click", event =>
    {
      event.preventDefault();
      map.removeLayer(layerId);
      this.layerList.removeChild(layerItem);
    });

    console.info("added layer " + layerId);
    console.info(map.getStyle().sources);

    let layerTypeSelect = layerItem.querySelector(".layer_type");
    layerTypeSelect.value = layerType;
    layerTypeSelect.addEventListener("change", event =>
    {
      let layerIndex = map.getStyle().layers.findIndex(x => x.id === layerId);
      console.info("layerIndex ", layerIndex);
      map.removeLayer(layerId);

      let nextLayerId = map.getStyle().layers[layerIndex]?.id;
      console.info("nextLayerIndex ", nextLayerId);

      let layerType = layerTypeSelect.value;
      this.createLayer(propList, sourceId, layerId, layerType, nextLayerId);
    });
  }

  showTab(name)
  {
    if (name === "sources")
    {
      this.sourcesLink.classList.add("active");
      this.sourcesTab.classList.remove("hidden");
      this.layersLink.classList.remove("active");
      this.layersTab.classList.add("hidden");
    }
    else
    {
      this.sourcesLink.classList.remove("active");
      this.sourcesTab.classList.add("hidden");
      this.layersLink.classList.add("active");
      this.layersTab.classList.remove("hidden");
    }
  }

  createLayer(propList, sourceId, layerId, layerType, beforeLayerId)
  {
    const map = this.map;

    propList.innerHTML = "";

    if (layerType === "fill")
    {
      map.addLayer({
        "id": layerId,
        "type": layerType,
        "source": sourceId,
        "layout": {},
        "paint": {
          'fill-color': '#00ff00',
          'fill-opacity': 0.8
        }
      }, beforeLayerId);

      propList.innerHTML = `
        <li><label><span>Fill color:</span> <input class="fill_color" type="color" value="#00ff00" /></label></li>
        <li><label><span>Fill opacity:</span> <input class="fill_opacity" type="number" value="0.8" min="0", max="1" step="0.1" style="50px" /></label></li>
      `;
      let colorInput = propList.querySelector(".fill_color");
      colorInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "fill-color", colorInput.value);
      });

      let opacityInput = propList.querySelector(".fill_opacity");
      opacityInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "fill-opacity", parseFloat(opacityInput.value));
      });
    }
    else if (layerType === "line")
    {
      map.addLayer({
        "id": layerId,
        "type": layerType,
        "source": sourceId,
        "layout": {},
        "paint": {
          'line-color': '#0000ff',
          'line-opacity': 0.8
        }
      }, beforeLayerId);

      propList.innerHTML = `
        <li><label><span>Line color:</span> <input class="line_color" type="color" value="#0000ff" /></label></li>
        <li><label><span>Line width:</span> <input class="line_width" type="number" value="1" min="0.1", max="10" step="0.1" style="50px" /></label></li>
        <li><label><span>Line opacity:</span> <input class="line_opacity" type="number" value="0.8" min="0", max="1" step="0.1" style="50px" /></label></li>
      `;
      let colorInput = propList.querySelector(".line_color");
      colorInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "line-color", colorInput.value);
      });

      let widthInput = propList.querySelector(".line_width");
      widthInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "line-width", parseFloat(widthInput.value));
      });

      let opacityInput = propList.querySelector(".line_opacity");
      opacityInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "line-opacity", parseFloat(opacityInput.value));
      });
    }
    else // circle
    {
      map.addLayer({
        "id": layerId,
        "type": "circle",
        "source": sourceId,
        "layout": {},
        "paint": {
          'circle-color': '#ff0000',
          'circle-radius': 4,
          'circle-stroke-color': '#0000ff',
          'circle-stroke-width': 0
        }
      }, beforeLayerId);

      propList.innerHTML = `
        <li><label><span>Circle color:</span> <input class="circle_color" type="color" value="#ff0000" /></label></li>
        <li><label><span>Circle radius:</span> <input class="circle_radius" type="number" value="4" min="0.1", max="30" step="0.1" style="50px" /></label></li>
        <li><label><span>Circle opacity:</span> <input class="circle_opacity" type="number" value="0.8" min="0", max="1" step="0.1" style="50px" /></label></li>
        <li><label><span>Stroke color:</span> <input class="circle_stroke_color" type="color" value="#0000ff" /></label></li>
        <li><label><span>Stroke width:</span> <input class="circle_stroke_width" type="number" value="0" min="0.1", max="10" step="0.1" style="50px" /></label></li>
        <li><label><span>Stroke opacity:</span> <input class="circle_stroke_opacity" type="number" value="0.8" min="0", max="1" step="0.1" style="50px" /></label></li>
      `;
      let colorInput = propList.querySelector(".circle_color");
      colorInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "circle-color", colorInput.value);
      });

      let radiusInput = propList.querySelector(".circle_radius");
      radiusInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "circle-radius", parseFloat(radiusInput.value));
      });

      let opacityInput = propList.querySelector(".circle_opacity");
      opacityInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "circle-opacity", parseFloat(opacityInput.value));
      });

      let strokeColorInput = propList.querySelector(".circle_stroke_color");
      strokeColorInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "circle-stroke-color", strokeColorInput.value);
      });

      let strokeWidthInput = propList.querySelector(".circle_stroke_width");
      strokeWidthInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "circle-stroke-width", parseFloat(strokeWidthInput.value));
      });

      let strokeOpacityInput = propList.querySelector(".circle_stroke_opacity");
      strokeOpacityInput.addEventListener("input", event =>
      {
        map.setPaintProperty(layerId, "circle-stroke-opacity", parseFloat(strokeOpacityInput.value));
      });
    }
  }
  
  moveLayer(layerItem, layerId, move)
  {
    const map = this.map;

    if (move > 0) move = 2;

//    const layers = map.getStyle().layers;
//    let layerIndex = layers.findIndex(x => x.id === layerId);
//    let beforeLayer = layers[layerIndex + move];
//    if (beforeLayer)
//    {
//      console.info("move " + layerId + " " + beforeLayer.id);
//      map.moveLayer(layerId, beforeLayer.id);
//    }
//    else
//    {
//      if (layerIndex < 0 || layerIndex === layers.length)
//      {
//        console.info("move bottom " + layerId);
//        map.moveLayer(layerId);
//      }
//      else
//      {
//        console.info("move top " + layerId + " " + layers[0].id);
//        map.moveLayer(layerId, layers[0].id);
//      }
//    }
    
    const listElem = layerItem.parentElement;
    const layerArray = Array.from(listElem.children);
    const index = layerArray.indexOf(layerItem) + move;

    const beforeLayerElem = layerArray[index];
    if (beforeLayerElem)
    {
      listElem.insertBefore(layerItem, beforeLayerElem);
      map.moveLayer(layerId, beforeLayerElem.id.substring(6));
    }
    else
    {
      if (index < 0 || index === layerArray.length)
      {
        listElem.appendChild(layerItem);
        map.moveLayer(layerId);
      }
      else
      {
        listElem.insertBefore(layerItem, layerArray[0]);
        map.moveLayer(layerId, layerArray[0].id.substring(6));
      }
    }
    console.info(layerItem, layerId, index, move);    
    
    
    
  }
}

export { LayerSetupControl };