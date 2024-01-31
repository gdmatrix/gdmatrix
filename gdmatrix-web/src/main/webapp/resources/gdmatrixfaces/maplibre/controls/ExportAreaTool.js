/* ExportAreaTool.js */

import { Tool } from "./Tool.js";
import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class ExportAreaTool extends Tool
{
  constructor(options)
  {
    super({...{ 
            "title": bundle.get("ExportAreaTool.title"), 
            "iconClass": "pi pi-download",
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

    this.formats = [
      ["GEOJSON", "application/json", "geojson"],
      ["GML3", "gml3", "xml"],
      ["EXCEL", "excel", "xlsx"],
      ["CSV", "text/csv", "csv"],
      ["SHAPE", "SHAPE-ZIP", "zip"],       
      ["DXF", "DXF", "dxf"],
      ["DXF-ZIP", "DXF-ZIP", "zip"],
      ["KML", "application/vnd.google-earth.kml+xml", "kml"]
    ];
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.getCanvas().style.cursor = "crosshair";
    this.panel.show();

    // sources

    map.addSource("export_polygon_start", {
      type: 'geojson',
      data: this.startPoint
    });

    map.addSource("export_linestring", {
      type: 'geojson',
      data: this.linestring
    });

    map.addSource("export_polygon", {
      type: 'geojson',
      data: this.polygon
    });
    
    // layers
    
    map.addLayer({
      "id": "export_polygon",
      "type": 'fill',
      "source": "export_polygon",
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.1
      }
    });    

    map.addLayer({
      "id": "export_linestring",
      "type": 'line',
      "source": "export_linestring",
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 2
      }
    });

    map.addLayer({
      "id": "export_polygon_points",
      "type": 'circle',
      "source": "export_linestring",
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
      "id": "export_polygon_start",
      "type": 'circle',
      "source": "export_polygon_start",
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

    map.removeLayer("export_polygon");
    map.removeLayer("export_polygon_points");
    map.removeSource("export_polygon");

    map.removeLayer("export_linestring");
    map.removeSource("export_linestring");  

    map.removeLayer("export_polygon_start");
    map.removeSource("export_polygon_start");
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
      map.getSource("export_polygon_start").setData(this.startPoint);
    }    
    
    if (points.length > 0) points.pop();
    points.push(point);
    points.push(points[0]);
      
    this.polygon.geometry.coordinates[0] = points;
    this.linestring.geometry.coordinates = points;

    map.getSource("export_polygon").setData(this.polygon);
    map.getSource("export_linestring").setData(this.linestring);

    this.panel.show();
    this.resultDiv.innerHTML = "";
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
      this.map.getSource("export_polygon").setData(this.polygon);
      this.map.getSource("export_linestring").setData(this.linestring);

      if (points.length === 0)
      {
        this.startPoint.geometry.coordinates = [];
        this.map.getSource("export_polygon_start").setData(this.startPoint);
      }
    }    
  }
  
  clearLayers()
  {
    this.points = [];
    
    this.startPoint.geometry.coordinates = [];
    this.map.getSource("export_polygon_start").setData(this.startPoint);

    this.polygon.geometry.coordinates[0] = [];
    this.map.getSource("export_polygon").setData(this.polygon);

    this.linestring.geometry.coordinates = [];
    this.map.getSource("export_linestring").setData(this.linestring);
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
    this.panel.onHide = () => this.deactivateTool(this);

    const bodyDiv = this.panel.bodyDiv;

    const helpDiv = document.createElement("div");
    helpDiv.className = "p-1";
    helpDiv.textContent = bundle.get("ExportAreaTool.help");
    bodyDiv.appendChild(helpDiv);    
    
    const buttonBar = document.createElement("div");
    buttonBar.className = "button_bar";
    bodyDiv.appendChild(buttonBar);

    const clearButton = document.createElement("button");
    clearButton.textContent = bundle.get("button.reset");
    clearButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.clearLayers();      
      this.resultDiv.innerHTML = "";
    });
    buttonBar.appendChild(clearButton);

    const undoButton = document.createElement("button");
    undoButton.textContent = bundle.get("button.undo");
    undoButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.removePoint();
      this.resultDiv.innerHTML = "";
    });
    buttonBar.appendChild(undoButton);

    const exportButton = document.createElement("button");
    exportButton.textContent = bundle.get("button.export");
    exportButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.exportLayers();
    });
    buttonBar.appendChild(exportButton);

    this.resultDiv = document.createElement("div");
    bodyDiv.appendChild(this.resultDiv);
  }

  exportLayers()
  {
    const map = this.map;
    const points = this.points;
    const resultDiv = this.resultDiv;
    resultDiv.innerHTML = "";
    
    if (points.length < 4) return;
    
    const services = map.getStyle().metadata?.services;
    const serviceParameters = map.getStyle().metadata?.serviceParameters;
    if (services === undefined) return;
    if (serviceParameters === undefined) return;

    let promises = [];

    for (let layer of map.getStyle().layers)
    {
      if (layer.metadata?.visible && layer.metadata?.locatable)
      {
        let sourceId = layer.source;
        let params = serviceParameters[sourceId];
        if (params)
        {
          let serviceId = params.service;
          if (serviceId)
          {
            let service = services[serviceId];
            let layerNameArray = [];
            if (layer.metadata?.layers)
            {
              layerNameArray.push(...layer.metadata.layers.split(","));
            }
            if (params.layers)
            {
              layerNameArray.push(...params.layers.split(","));
            }
            for (let layerName of layerNameArray)
            {
              promises.push(FeatureTypeInspector.getInfo(service.url, layerName)
                .then(info =>
                {
                  return {
                    serviceUrl: service.url, 
                    layerId : layer.id,
                    layerName: layerName, 
                    layerLabel: layer.metadata?.label,
                    geometryColumn: info.geometryColumn 
                  };
                }));
            }
          }
        }
      }
    }
    Promise.all(promises).then(layerInfo => this.showLinks(layerInfo));
  }

  showLinks(layerInfo)
  {
    let cqlPolygon = "SRID=4326;POLYGON((";
    let ring = this.polygon.geometry.coordinates[0];
    if (ring.length < 3) return;

    for (let i = 0; i < ring.length; i++)
    {
      let point = ring[i];
      if (i > 0) cqlPolygon += ", ";
      cqlPolygon += point[0] + " " + point[1];
    }
    cqlPolygon += ", " + ring[0][0] + " "  + ring[0][1]; // add first point
    cqlPolygon += "))";

    const formats = this.formats;

    const resultDiv = this.resultDiv;
    resultDiv.innerHTML = "";

    for (let info of layerInfo)
    {
      if (info.geometryColumn === null) continue;

      let serviceUrl = info.serviceUrl;
      let layerId = info.layerId;
      let layerName = info.layerName;
      let layerLabel = info.layerLabel || layerId || layerName;
      let cqlFilter = "INTERSECTS(" + info.geometryColumn +
        ", " + cqlPolygon + ")";


      let layerDiv = document.createElement("div");
      layerDiv.className = "pt-2";
      layerDiv.innerHTML = `<div class="mb-2"><b>${layerLabel}</b> (${layerName}):</div>`;
      resultDiv.appendChild(layerDiv);

      for (let format of formats)
      {
        let url = "/proxy?url=" + serviceUrl +
          "&service=WFS&version=2.0.0 " +
          "&request=GetFeature" + 
          "&typeName=" + layerName + 
          "&outputFormat=" + format[1] +
          "&exceptions=application/json" +
          "&srsName=EPSG:25831" + 
          "&cql_filter=" + encodeURIComponent(cqlFilter);
        let anchor = document.createElement("a");
        anchor.target= "_blank";
        anchor.href = url;
        anchor.className = "export_link";
        anchor.download = layerId + "." + format[2];
        
        let icon = document.createElement("i");
        icon.className = "pi pi-download mr-1";
        anchor.append(icon);

        let span = document.createElement("span");
        span.textContent = format[0];
        anchor.append(span);
        
        layerDiv.append(anchor);
      }
    }
  }
}

export { ExportAreaTool };