/* PickupTool.js */

import { Tool } from "./Tool.js";
import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class PickupTool extends Tool
{
  constructor(options)
  {
    super({...{
            "title": bundle.get("PickupTool.title"),
            "iconClass": "fa fa-dolly",
            "position" : "right"
          }, ...options});

    this.sourceId = options.sourceId; // sourceId of the selection layer
    this.layerId = options.layerId; // the id of the selection layer
    this.propertyName = options.propertyName; // the property name to pickup from selection
    this.restServiceUrl = options.restServiceUrl; // the service url to send & read data
    this.referenceText = options.referenceText || "Reference"; // the reference field label 
    this.helpText = options.helpText || ""; // the tool help

    this.codeSelection = new Set();

    this._onMapClick = (event) => this.pickup(event.point);
    this._onMouseMove = (event) => this.onMouseMove(event);
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.on("mousemove", this._onMouseMove);

    map.getCanvas().style.cursor = "crosshair";
    this.panel.show();
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onMapClick);
    map.off("mousemove", this._onMouseMove);

    map.getCanvas().style.cursor = "grab";
    this.panel.hide();
  }

  reactivate()
  {
    this.panel.show();
  }

  pickup(point)
  {
    const map = this.map;
    this.resultDiv.textContent = "";

    // selection for vector/geojson layers
    const tolerance = this.tolerance || 8;
    const bbox = [
      [point.x - tolerance, point.y - tolerance],
      [point.x + tolerance, point.y + tolerance]
    ];

    let features = this.map.queryRenderedFeatures(bbox, { layers: [this.layerId] });
    const boxSelection = new Set();
    for (let feat of features)
    {
      let code = String(feat.properties[this.propertyName]);
      boxSelection.add(code);
    }
    
    // invert/add codeSelection
    const codeSelection = this.codeSelection;
    for (let code of boxSelection)
    {
      if (codeSelection.has(code))
      {
        codeSelection.delete(code);
      }
      else
      {
        codeSelection.add(code);        
      }
    }
    this.updateHighlight();
  }
  
  async loadPickup()
  {
    const map = this.map;
    const codeSelection = this.codeSelection;
    
    let reference = this.referenceInput.value;
    if (!reference) return;

    let response = await fetch(this.restServiceUrl + "?ref=" + reference);
    let codes = await response.json();
    this.resultDiv.textContent = JSON.stringify(codes, null, 2);

    codeSelection.clear();
    for (let code of codes)
    {
      codeSelection.add(code);
    }
    this.updateHighlight(true);
  }
  
  async savePickup()
  {
    let reference = this.referenceInput.value;
    if (!reference) return;
    
    let codes = Array.from(this.codeSelection);

    let response = await fetch(this.restServiceUrl + "?ref=" + reference, {
      method: "POST",
      headers: { "Content-Type": "application/json;charset=UTF-8" },
      body: JSON.stringify(codes)
    });
    this.resultDiv.textContent = JSON.stringify(await response.json(), null, 2);
  }
  
  clearPickup()
  {
    this.codeSelection.clear();
    this.updateHighlight();
  }

  updateHighlight(center = false)
  {
    const map = this.map;
    const codeSelection = this.codeSelection;
    const points = [];
    
    let features = map.querySourceFeatures(this.sourceId);
    for (let feat of features)
    {
      let code = String(feat.properties[this.propertyName]);
      if (codeSelection.has(code))
      {
        if (center)
        {
          var geom = turf.getGeom(feat.geometry);
          points.push(...turf.explode(geom).features.map(f => f.geometry.coordinates));
        }

        map.setFeatureState({ source : this.sourceId, id: feat.id },
         { "highlighted" : true });
      }
      else
      {
        map.setFeatureState({ source : this.sourceId, id: feat.id },
         { "highlighted" : false });
      }
    }
    
    if (center && points.length > 0)
    {
      const bbox = turf.bbox(turf.multiPoint([...points]));
      map.fitBounds([
        [bbox[0], bbox[1]], 
        [bbox[2], bbox[3]]
      ], { maxZoom: 16 });
    }
   
    map.redraw();
  }
  
  onMouseMove(event)
  {
    const map = this.map;
    const point = event.point;

    let features = this.map.querySourceFeatures(this.sourceId);
    for (let feat of features)
    {
      map.setFeatureState({ source : this.sourceId, id: feat.id },
       { "hover" : false });
    }

    // selection for vector/geojson layers
    const tolerance = this.tolerance || 8;
    const bbox = [
      [point.x - tolerance, point.y - tolerance],
      [point.x + tolerance, point.y + tolerance]
    ];
    features = this.map.queryRenderedFeatures(bbox, { layers: [this.layerId] });
    for (let feat of features)
    {
      map.setFeatureState({ source : this.sourceId, id: feat.id },
       { "hover" : true });
    }
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
    this.panel.onHide = () => this.deactivateTool(this);

    const bodyDiv = this.panel.bodyDiv;

    const helpDiv = document.createElement("div");
    helpDiv.className = "p-1";
    helpDiv.textContent = bundle.get(this.helpText);
    bodyDiv.appendChild(helpDiv);
    
    this.referenceLabel = document.createElement("label");
    this.referenceLabel.textContent = bundle.get(this.referenceText) + ":";
    this.referenceLabel.htmlFor = "pickup_ref";
    this.referenceLabel.className = "p-1";
    this.referenceInput = document.createElement("input");
    this.referenceInput.id = "pickup_ref";
    this.referenceInput.className = "w-full mb-1";

    bodyDiv.appendChild(this.referenceLabel);
    bodyDiv.appendChild(this.referenceInput);

    const buttonBar = document.createElement("div");
    buttonBar.className = "button_bar p-1 text-center";
    bodyDiv.appendChild(buttonBar);

    const loadButton = document.createElement("button");
    loadButton.textContent = bundle.get("button.load");
    loadButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.loadPickup();
    });
    buttonBar.appendChild(loadButton);
    
    const saveButton = document.createElement("button");
    saveButton.textContent = bundle.get("button.save");
    saveButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.savePickup();
    });
    buttonBar.appendChild(saveButton);

    const clearButton = document.createElement("button");
    clearButton.textContent = bundle.get("button.clear");
    clearButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.clearPickup();
    });
    buttonBar.appendChild(clearButton);

    this.resultDiv = document.createElement("pre");
    bodyDiv.appendChild(this.resultDiv);
  }
}

export { PickupTool };