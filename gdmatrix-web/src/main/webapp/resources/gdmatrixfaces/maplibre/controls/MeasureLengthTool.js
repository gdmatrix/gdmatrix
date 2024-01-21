/* MeasureLengthTool.js */

import { Tool } from "./Tool.js";
import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class MeasureLengthTool extends Tool
{
  constructor(options)
  {
    super({...{ 
            "title": bundle.get("MeasureLengthTool.title"), 
            "iconClass": "fa fa-ruler-horizontal",
            "position" : "right"
          }, ...options});    
    
    this._onMapClick = (event) => this.addPoint(event.lngLat);
    this.startData = {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [] }
    };
    this.data = {
      "type": "Feature",
      "geometry": { "type": "LineString", "coordinates": [] }
    };
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.getCanvas().style.cursor = "crosshair";
    this.updateLength();
    this.panel.show();

    map.addSource("measured_linestring_start", {
      type: 'geojson',
      data: this.startData
    });

    map.addLayer({
      "id": "measured_linestring_start",
      "type": 'circle',
      "source": "measured_linestring_start",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 4
      }
    });

    map.addSource("measured_linestring", {
      type: 'geojson',
      data: this.data
    });

    map.addLayer({
      "id": "measured_linestring",
      "type": 'line',
      "source": "measured_linestring",
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 4,
        "line-opacity": 0.5
      }
    });

    map.addLayer({
      "id": "measured_linestring_points",
      "type": 'circle',
      "source": "measured_linestring",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 3
      }
    });
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onMapClick);
    map.getCanvas().style.cursor = "grab";
    this.panel.hide();

    map.removeLayer("measured_linestring_start");
    map.removeSource("measured_linestring_start");

    map.removeLayer("measured_linestring");
    map.removeLayer("measured_linestring_points");
    map.removeSource("measured_linestring");
  }

  reactivate()
  {
    this.panel.show();
  }

  addPoint(lngLat)
  {
    const map = this.map;

    if (this.startData.geometry.coordinates.length === 0)
    {
      this.startData.geometry.coordinates = [lngLat.lng, lngLat.lat];
      map.getSource("measured_linestring_start").setData(this.startData);
    }

    this.data.geometry.coordinates.push([lngLat.lng, lngLat.lat]);
    map.getSource("measured_linestring").setData(this.data);

    this.updateLength();

    this.panel.show();
  }

  updateLength()
  {
    let length = 0;
    for (let i = 0; i < this.data.geometry.coordinates.length - 1; i++)
    {
      let c1 = this.data.geometry.coordinates[i];
      let c2 = this.data.geometry.coordinates[i + 1];

      let lngLat1 =  new maplibregl.LngLat(c1[0], c1[1]);
      let lngLat2 =  new maplibregl.LngLat(c2[0], c2[1]);
      length += lngLat1.distanceTo(lngLat2);
    }

    let units;
    if (length > 1000)
    {
      length /= 1000;
      units = "km";      
    }
    else
    {
      units = "m2";
    }
    
    let value = new Intl.NumberFormat('es-ES', 
     { minimumFractionDigits: 3, useGrouping: true })
     .format(length);    
    
    this.resultDiv.textContent = 
      bundle.get("MeasureLengthTool.length", value, units);
    this.resultDiv.className = "p-4 pl-1";
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
    this.panel.onHide = () => this.deactivateTool(this);

    const bodyDiv = this.panel.bodyDiv;

    const helpDiv = document.createElement("div");
    helpDiv.className = "p-1";
    helpDiv.textContent = bundle.get("MeasureLengthTool.help");
    bodyDiv.appendChild(helpDiv);    
    
    const buttonBar = document.createElement("div");
    buttonBar.className = "button_bar";
    bodyDiv.appendChild(buttonBar);

    const clearButton = document.createElement("button");
    clearButton.textContent = bundle.get("button.reset");
    clearButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.startData.geometry.coordinates = [];
      this.map.getSource("measured_linestring_start").setData(this.startData);

      this.data.geometry.coordinates = [];
      this.map.getSource("measured_linestring").setData(this.data);

      this.updateLength();
    });
    buttonBar.appendChild(clearButton);

    const undoButton = document.createElement("button");
    undoButton.textContent = bundle.get("button.undo");
    undoButton.addEventListener("click", (e) => {
      e.preventDefault();
      if (this.data.geometry.coordinates.length > 0)
      {
        this.data.geometry.coordinates.pop();
        this.map.getSource("measured_linestring").setData(this.data);

        if (this.data.geometry.coordinates.length === 0)
        {
          this.startData.geometry.coordinates = [];
          this.map.getSource("measured_linestring_start").setData(this.startData);
        }
        this.updateLength();
      }
    });
    buttonBar.appendChild(undoButton);

    this.resultDiv = document.createElement("div");
    bodyDiv.appendChild(this.resultDiv);
  }
}

export { MeasureLengthTool };