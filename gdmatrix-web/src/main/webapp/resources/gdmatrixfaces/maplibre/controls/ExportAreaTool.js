/* ExportArea */

import { Panel } from "./Panel.js";
import { Tool } from "./Tool.js";

class ExportAreaTool extends Tool
{
  constructor(containerId, insertTop)
  {
    super("pi pi-download", "Export area");
    this.createPanel(containerId, insertTop);
    
    this._onMapClick = (event) => this.addPoint(event.lngLat);
    this.startData = {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [] }
    };
    this.data = {
      "type": "Feature",
      "geometry": { "type": "Polygon", "coordinates": [[]] }
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

    map.addSource("measured_polygon_start", {
      type: 'geojson',
      data: this.startData
    });

    map.addLayer({
      "id": "measured_polygon_start",
      "type": 'circle',
      "source": "measured_polygon_start",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 4
      }
    });

    map.addSource("measured_polygon", {
      type: 'geojson',
      data: this.data
    });

    map.addLayer({
      "id": "measured_polygon",
      "type": 'fill',
      "source": "measured_polygon",
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.2
      }
    });

    map.addLayer({
      "id": "measured_polygon_points",
      "type": 'circle',
      "source": "measured_polygon",
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

    map.removeLayer("measured_polygon_start");
    map.removeSource("measured_polygon_start");

    map.removeLayer("measured_polygon");
    map.removeLayer("measured_polygon_points");
    map.removeSource("measured_polygon");
  }

  addPoint(lngLat)
  {
    const map = this.map;

    if (this.startData.geometry.coordinates.length === 0)
    {
      this.startData.geometry.coordinates = [lngLat.lng, lngLat.lat];
      map.getSource("measured_polygon_start").setData(this.startData);
    }

    this.data.geometry.coordinates[0].push([lngLat.lng, lngLat.lat]);
    map.getSource("measured_polygon").setData(this.data);

    this.panel.show();
    this.resultDiv.innerHTML = "";
  }

  createPanel(containerId, insertTop)
  {
    this.panel = new Panel(containerId, 
      "Export area", "pi pi-info-circle", insertTop);

    const bodyDiv = this.panel.bodyDiv;
    const buttonBar = document.createElement("div");
    buttonBar.className = "button_bar";
    bodyDiv.appendChild(buttonBar);

    const clearButton = document.createElement("button");
    clearButton.textContent = "Reset";
    clearButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.startData.geometry.coordinates = [];
      this.map.getSource("measured_polygon_start").setData(this.startData);

      this.data.geometry.coordinates[0] = [];
      this.map.getSource("measured_polygon").setData(this.data);
      this.resultDiv.innerHTML = "";
    });
    buttonBar.appendChild(clearButton);

    const undoButton = document.createElement("button");
    undoButton.textContent = "Undo";
    undoButton.addEventListener("click", (e) => {
      e.preventDefault();
      if (this.data.geometry.coordinates[0].length > 0)
      {
        this.data.geometry.coordinates[0].pop();
        this.map.getSource("measured_polygon").setData(this.data);

        if (this.data.geometry.coordinates[0].length === 0)
        {
          this.startData.geometry.coordinates = [];
          this.map.getSource("measured_polygon_start").setData(this.startData);
        }
      }
      this.resultDiv.innerHTML = "";
    });
    buttonBar.appendChild(undoButton);

    const exportButton = document.createElement("button");
    exportButton.textContent = "Export";
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
    let ring = this.data.geometry.coordinates[0];
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
          "&service=WFS&version=2.0.0&request=GetFeature&typeName=" +
            layerName + "&outputFormat=" + format[1] +
            "&srsName=EPSG:25831" +
            "&cql_filter=" + encodeURIComponent(cqlFilter);
        let anchor = document.createElement("a");
        anchor.target= "_blank";
        anchor.href = url;
        anchor.className = "export_link";
        anchor.download = layerId + "." + format[2];
        anchor.textContent = format[0];
        layerDiv.append(anchor);
      }
    }
  }
}

export { ExportAreaTool };