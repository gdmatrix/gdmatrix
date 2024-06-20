/* MeasureAreaTool.js */

import { Tool } from "./Tool.js";
import { Panel } from "../ui/Panel.js";
import { toUtm } from "../utm-latlng.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class MeasureAreaTool extends Tool
{
  constructor(options)
  {
    super({...{ 
            "title": bundle.get("MeasureAreaTool.title"), 
            "iconClass": "fa fa-ruler-combined",
            "position" : "right"
          }, ...options});
    
    this._onMapClick = (event) => this.addPoint(event.lngLat);

    this.points = [];

    this.startPoint = {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [] }
    };

    this.polygon = {
      "type": "Feature",
      "geometry": { "type": "Polygon", "coordinates": [[]] }
    };

    this.linestring = {
      "type": "Feature",
      "geometry": { "type": "LineString", "coordinates": [] }
    };  
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.getCanvas().style.cursor = "crosshair";
    this.updateArea();
    this.panel.show();

    // sources

    map.addSource("measured_polygon_start", {
      type: 'geojson',
      data: this.startPoint
    });

    map.addSource("measured_linestring", {
      type: 'geojson',
      data: this.linestring
    });

    map.addSource("measured_polygon", {
      type: 'geojson',
      data: this.polygon
    });
    
    // layers
    
    map.addLayer({
      "id": "measured_polygon",
      "type": 'fill',
      "source": "measured_polygon",
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.1
      }
    });    

    map.addLayer({
      "id": "measured_linestring",
      "type": 'line',
      "source": "measured_linestring",
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 2
      }
    });

    map.addLayer({
      "id": "measured_polygon_points",
      "type": 'circle',
      "source": "measured_linestring",
      "layout": {},
      "paint":
      {
        "circle-color": "#ffffff",
        "circle-radius": 2,
        "circle-stroke-color" : "#000000",
        "circle-stroke-width" : 2
      }
    });

    map.addLayer({
      "id": "measured_polygon_start",
      "type": 'circle',
      "source": "measured_polygon_start",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 5
      }
    });
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onMapClick);
    map.getCanvas().style.cursor = "grab";
    this.panel.hide();

    map.removeLayer("measured_polygon");
    map.removeLayer("measured_polygon_points");
    map.removeSource("measured_polygon");

    map.removeLayer("measured_linestring");
    map.removeSource("measured_linestring");  

    map.removeLayer("measured_polygon_start");
    map.removeSource("measured_polygon_start");
  }

  reactivate()
  {
    this.panel.show();
  }

  addPoint(lngLat)
  {
    const map = this.map;
    const points = this.points;
    
    const point = [lngLat.lng, lngLat.lat];

    if (points.length === 0)
    {
      this.startPoint.geometry.coordinates = point;
      map.getSource("measured_polygon_start").setData(this.startPoint);
    }    
    
    if (points.length > 0) points.pop();
    points.push(point);
    points.push(points[0]);
      
    this.polygon.geometry.coordinates[0] = points;
    this.linestring.geometry.coordinates = points;

    map.getSource("measured_polygon").setData(this.polygon);
    map.getSource("measured_linestring").setData(this.linestring);

    this.updateArea();

    this.panel.show();
  }
  
  removePoint()
  {
    const map = this.map;
    const points = this.points;
    
    if (points.length > 0)
    {
      points.pop();
      points.pop();

      if (points.length > 0)
      {
        points.push(points[0]);
        this.polygon.geometry.coordinates[0] = points;
        this.linestring.geometry.coordinates = points;
      }
      else
      {
        this.polygon.geometry.coordinates[0] = [];        
        this.linestring.geometry.coordinates = [];        
      }
      this.map.getSource("measured_polygon").setData(this.polygon);
      this.map.getSource("measured_linestring").setData(this.linestring);

      if (points.length === 0)
      {
        this.startPoint.geometry.coordinates = [];
        this.map.getSource("measured_polygon_start").setData(this.startPoint);
      }
    }    
  }
  
  clearLayers()
  {
    this.points = [];
    
    this.startPoint.geometry.coordinates = [];
    this.map.getSource("measured_polygon_start").setData(this.startPoint);

    this.polygon.geometry.coordinates[0] = [];
    this.map.getSource("measured_polygon").setData(this.polygon);

    this.linestring.geometry.coordinates = [];
    this.map.getSource("measured_linestring").setData(this.linestring);
  }  

  updateArea()
  {
    let area = 0;
    const coordinates = this.points;
    for (let i = 0; i < coordinates.length - 1; i++)
    {
      let c1 = coordinates[i];
      let c2 = coordinates[i + 1];

      let utm1 = toUtm(c1[1], c1[0], 31, 'ETRS89');
      let utm2 = toUtm(c2[1], c2[0], 31, 'ETRS89');

      let x1 = utm1.easting;
      let y1 = utm1.northing;

      let x2 = utm2.easting;
      let y2 = utm2.northing;

      area += (0.5 * (y1 + y2) * (x2 - x1));
    }

    let units;
    area = Math.abs(area);
    if (area > 1000000)
    {
      area /= 1000000;
      units = "km2";
    }
    else if (area > 10000)
    {
      area /= 10000;
      units = "ha";
    }
    else
    {
      units = "m2";
    }

    let value = new Intl.NumberFormat('es-ES', 
     { minimumFractionDigits: 3, useGrouping: true })
     .format(area);    

    this.resultDiv.textContent = 
      bundle.get("MeasureAreaTool.area", value, units);
    this.resultDiv.className = "p-4 pl-1";
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
    this.panel.onHide = () => this.deactivateTool(this);

    const bodyDiv = this.panel.bodyDiv;

    const helpDiv = document.createElement("div");
    helpDiv.className = "p-1";
    helpDiv.textContent = bundle.get("MeasureAreaTool.help");
    bodyDiv.appendChild(helpDiv);    
    
    const buttonBar = document.createElement("div");
    buttonBar.className = "button_bar";
    bodyDiv.appendChild(buttonBar);

    const clearButton = document.createElement("button");
    clearButton.textContent = bundle.get("button.reset");
    clearButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.clearLayers();
      this.updateArea();
    });
    buttonBar.appendChild(clearButton);

    const undoButton = document.createElement("button");
    undoButton.textContent = bundle.get("button.undo");
    undoButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.removePoint();
      this.updateArea();
    });
    buttonBar.appendChild(undoButton);

    this.resultDiv = document.createElement("div");
    bodyDiv.appendChild(this.resultDiv);
  }
}

export { MeasureAreaTool };
