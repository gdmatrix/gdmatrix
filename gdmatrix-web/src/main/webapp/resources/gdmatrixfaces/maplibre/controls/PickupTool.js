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
    this.sourceIdsToUpdate = options.sourceIdsToUpdate || [];

    this.codeSelection = new Set();
    this.drawingPolygon = false;

    this._onMapClick = (event) => {
      if (this.drawingPolygon)
      {
        this.addPolygonVertex(event.lngLat);      
      }
      else
      {
        this.pickup(event.point);
      }
    };
    this._onMouseMove = (event) => this.onMouseMove(event);

    this.points = {
      "type": "Feature",
      "geometry": { "type": "MultiPoint", "coordinates": [] }
    };

    this.linestring = {
      "type": "Feature",
      "geometry": { "type": "LineString", "coordinates": [] }
    };  

    this.polygon = {
      "type": "Feature",
      "geometry": { "type": "Polygon", "coordinates": [[]] }
    };  
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.on("mousemove", this._onMouseMove);

    map.addSource("pickup_polygon", {
      type: 'geojson',
      data: this.polygon
    });

    map.addSource("pickup_linestring", {
      type: 'geojson',
      data: this.linestring
    });

    map.addSource("pickup_points", {
      type: 'geojson',
      data: this.points
    });

    map.addLayer({
      "id": "pickup_polygon",
      "type": 'fill',
      "source": "pickup_polygon",
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.1
      }
    });

    map.addLayer({
      "id": "pickup_linestring",
      "type": 'line',
      "source": "pickup_linestring",
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 2
      }
    });

    map.addLayer({
      "id": "pickup_points",
      "type": 'circle',
      "source": "pickup_points",
      "layout": {},
      "paint":
      {
        "circle-color": "#ffffff",
        "circle-radius": 2,
        "circle-stroke-color" : "#000000",
        "circle-stroke-width" : 2
      }
    });

    map.getCanvas().style.cursor = "crosshair";
    this.panel.show();
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onMapClick);
    map.off("mousemove", this._onMouseMove);

    map.removeLayer("pickup_polygon");
    map.removeLayer("pickup_linestring");
    map.removeLayer("pickup_points");
    
    map.removeSource("pickup_linestring");  
    map.removeSource("pickup_polygon");  
    map.removeSource("pickup_points");  

    map.getCanvas().style.cursor = "grab";
    this.panel.hide();
  }

  reactivate()
  {
    const map = this.map;    
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

    let features = map.queryRenderedFeatures(bbox, { layers: [this.layerId] });

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
  
  addPolygonVertex(lngLat)
  {
    const map = this.map;

    let geomPoint = [lngLat.lng, lngLat.lat];
    let coordinates = this.polygon.geometry.coordinates[0];
    if (coordinates.length === 0)
    {
      coordinates.push(geomPoint);
      coordinates.push(geomPoint);
    }
    else
    {
      let p = coordinates.pop();
      coordinates.push(geomPoint);
      coordinates.push(p);
    }
    this.linestring.geometry.coordinates = coordinates;
    this.points.geometry.coordinates = coordinates;
    map.getSource("pickup_polygon").setData(this.polygon);
    map.getSource("pickup_linestring").setData(this.linestring);    
    map.getSource("pickup_points").setData(this.points);

    if (this.polygon.geometry.coordinates[0].length === 4)
    {
      this.polygonBar.style.display = "";
    }
  }
  
  selectByPolygon(add = true)
  {
    const geomSel = turf.getGeom(this.polygon.geometry);

    const map = this.map;
    const codeSelection = this.codeSelection;

    let features = map.queryRenderedFeatures({ layers: [this.layerId] });
    for (let feat of features)
    {
      let geom = turf.getGeom(feat.geometry);
      if (turf.intersect(geomSel, geom))
      {
        let code = String(feat.properties[this.propertyName]);
        if (code !== undefined)
        {
          if (add)
          {
            codeSelection.add(code);
            map.setFeatureState({ source : this.sourceId, id: feat.id },
             { "highlighted" : true });
          }
          else
          {
            codeSelection.delete(code);
            map.setFeatureState({ source : this.sourceId, id: feat.id },
             { "highlighted" : false });            
          }           
        }
      }
    }
    this.clearPolygon();
  }
    
  async loadPickup()
  {
    const map = this.map;
    const codeSelection = this.codeSelection;
    
    let reference = this.referenceInput.value;
    if (!reference)
    {
      this.resultDiv.innerHTML = "";    
      return;
    }

    this.resultDiv.innerHTML =  `<span class="pi pi-spin pi-spinner p-2" />`;

    let response = await fetch(this.restServiceUrl + "?ref=" + reference);
    let codes = await response.json();
    this.resultDiv.innerHTML = JSON.stringify(codes, null, 2);

    codeSelection.clear();
    for (let code of codes)
    {
      codeSelection.add(code);
    }
    if (codeSelection.size > 0)
    {
      // fly to general zoom to load all geometries from source
      map.flyTo({ center: map.getStyle().center, zoom: map.getStyle().zoom, maxDuration: 2000 });    
      setTimeout(() => this.updateHighlight(true), 3000);
    }
    else
    {
      this.updateHighlight();
    }
  }
  
  async savePickup()
  {
    let reference = this.referenceInput.value;
    if (!reference) return;

    this.resultDiv.innerHTML =  `<span class="pi pi-spin pi-spinner p-2" />`;
    
    let codes = Array.from(this.codeSelection);
    
    if (!this.restServiceUrl)
    {
      this.resultDiv.innerHTML = JSON.stringify(codes, null, 2);
      return;
    }

    let response = await fetch(this.restServiceUrl + "?ref=" + reference, {
      method: "POST",
      headers: { "Content-Type": "application/json;charset=UTF-8" },
      body: JSON.stringify(codes)
    });
    this.resultDiv.innerHTML = await response.text();
    
    if (this.sourceIdsToUpdate.length > 0)
    {
      const map = this.map;
      const sources = map.getStyle().sources;
      const seed = "_seed=" + Math.random();
  
      for (let sourceId of this.sourceIdsToUpdate)
      {
        let source = sources[sourceId];
        if (source.type === "geojson")
        {
          let url = getSourceUrl(sourceId, map.getStyle());
          if (!url) continue;
          
          if (url.indexOf("?") === -1)
          {
            url += "?" + seed;
          }
          else
          {
            url += "&" + seed;
          }
          console.info("Refresh " + sourceId);
          map.getSource(sourceId).setData(url);
        }
      }
    }
  }
  
  clearPolygon()
  {
    this.drawingPolygon = false;
    this.polygonBar.style.display = "none";
    this.polygon.geometry.coordinates = [[]];
    this.linestring.geometry.coordinates = [];
    this.points.geometry.coordinates = [];
    const map = this.map;
    map.getSource("pickup_polygon").setData(this.polygon);  
    map.getSource("pickup_linestring").setData(this.linestring);  
    map.getSource("pickup_points").setData(this.points);      
    this.polygonButton.style.display = "";
    this.resultDiv.innerHTML = "";
  }
  
  clearPickup()
  {
    const map = this.map;
    
    this.codeSelection.clear();
    this.updateHighlight();
    this.resultDiv.innerHTML = "";
    
    this.clearPolygon();
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

    const polygonButton = document.createElement("button");
    this.polygonButton = polygonButton;
    polygonButton.textContent = bundle.get("button.selectByPolygon");
    polygonButton.addEventListener("click", (e) => {
      e.preventDefault();
      polygonButton.style.display = "none";
      this.drawingPolygon = true;
      this.resultDiv.innerHTML = `<div class="text-center">${bundle.get("PickupTool.drawPolygon")}</div>`;
    });
    buttonBar.appendChild(polygonButton);        
    
    const polygonBar = document.createElement("div");
    polygonBar.className = "button_bar p-1 text-center";
    bodyDiv.appendChild(polygonBar);
    this.polygonBar = polygonBar;
    
    const selectButton = document.createElement("button");
    selectButton.textContent = bundle.get("button.select");
    selectButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.selectByPolygon(true);
    });
    polygonBar.appendChild(selectButton);
    
    const deselectButton = document.createElement("button");
    deselectButton.textContent = bundle.get("button.unselect");
    deselectButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.selectByPolygon(false);
    });
    polygonBar.appendChild(deselectButton);

    const cancelButton = document.createElement("button");
    cancelButton.textContent = bundle.get("button.cancel");
    cancelButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.clearPolygon();
    });
    polygonBar.appendChild(cancelButton);
    polygonBar.style.display = "none";

    this.resultDiv = document.createElement("pre");
    bodyDiv.appendChild(this.resultDiv);
  }
}

export { PickupTool };