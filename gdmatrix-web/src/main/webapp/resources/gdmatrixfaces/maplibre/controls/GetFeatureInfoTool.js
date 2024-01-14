/* GetFeatureInfoTool.js */

import { Panel } from "./Panel.js";
import { Tool } from "./Tool.js";

class GetFeatureInfoTool extends Tool
{
  constructor(options)
  {
    super("fa fa-arrow-pointer", "Get feature info");
    this.createPanel(options);

    this.highlightCount = 0;
    this._onMapClick = (event) => this.getFeatureInfo(event.lngLat);
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.getCanvas().style.cursor = "crosshair";
    this.panel.show();
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onMapClick);
    map.getCanvas().style.cursor = "grab";
    this.headerDiv.innerHTML = "";
    this.infoDiv.innerHTML = "";
    this.clearHighlight();
    this.panel.hide();
  }

  createPanel(options)
  {
    this.panel = new Panel(options.containerId, 
      "Feature info", "pi pi-info-circle", options.insertTop);

    this.headerDiv = document.createElement("div");
    this.panel.bodyDiv.appendChild(this.headerDiv);

    this.infoDiv = document.createElement("div");
    this.panel.bodyDiv.appendChild(this.infoDiv);
  }

  showInfo(data)
  {
    const map = this.map;
    const infoDiv = this.infoDiv;
    infoDiv.innerHTML = "";

    const ul = document.createElement("ul");
    ul.className = "feature_info";
    infoDiv.appendChild(ul);

    let featureCount = 0;
    for (let layerData of data)
    {
      if (layerData)
      {
        let geojson = layerData.geojson;
        let features = geojson.features;
        if (features && features.length > 0)
        {
          for (let feature of features)
          {
            featureCount++;
            const li = document.createElement("li");
            ul.appendChild(li);

            const liDiv = document.createElement("div");
            let featureId = feature.id;
            let index = featureId.lastIndexOf(".");
            if (index !== -1) featureId = featureId.substring(0, index);

            liDiv.textContent = featureId;
            li.appendChild(liDiv);

            const subUl = document.createElement("ul");
            li.appendChild(subUl);

            for (let propertyName in feature.properties)
            {
              let propLi = document.createElement("li");
              propLi.textContent = propertyName + ": " + feature.properties[propertyName];
              subUl.appendChild(propLi);
            }
          }
        }
      }
    }
    if (featureCount === 0)
    {
      infoDiv.innerHTML = `<div class="pt-4 pb-4">No data found.</div>`;
    }
    this.panel.show();
  }

  clearHighlight()
  {
    const map = this.map;
    for (let i = 0; i < this.highlightCount; i++)
    {
      map.removeLayer("highlight_" + i + "_point");
      map.removeLayer("highlight_" + i + "_linestring");
      map.removeLayer("highlight_" + i + "_polygon");
      map.removeSource("highlight_" + i);
    }
    this.highlightCount = 0;
  }

  addPointer(lngLat)
  {
    const map = this.map;

    let i = this.highlightCount++;

    map.addSource("highlight_" + i,
    {
      type: 'geojson',
      data: {
        "type": "Feature",
        "properties" : {},
        "geometry" : { "type" : "Point", "coordinates" : [lngLat.lng, lngLat.lat]}
      }
    });

    map.addLayer({
      "id": "highlight_" + i + "_point",
      "type": 'circle',
      "source": "highlight_" + i,
      "layout": {},
      "paint":
      {
        "circle-radius": 3,
        "circle-color": "#000000"
      }
    });
  }

  highlight(geojson)
  {
    const map = this.map;

    let i = this.highlightCount++;

    map.addSource("highlight_" + i,
    {
      type: 'geojson',
      data: geojson
    });

    map.addLayer({
      "id": "highlight_" + i + "_point",
      "type": 'circle',
      "source": "highlight_" + i,
      "layout": {},
      "paint":
      {
        "circle-opacity": 0,
        "circle-radius": 6,
        "circle-stroke-width": 2,
        "circle-stroke-color": "blue"
      },
      "filter": ['==', '$type', 'Point']
    });

    map.addLayer({
      "id": "highlight_" + i + "_linestring",
      "type": 'line',
      "source": "highlight_" + i,
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 4,
        "line-opacity": 0.5
      },
      "filter": ["any", ['==', '$type', 'LineString'], ['==', '$type', 'Polygon']]
    });

    map.addLayer({
      "id": "highlight_" + i + "_polygon",
      "type": 'fill',
      "source": "highlight_" + i,
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.2
      },
      "filter": ['==', '$type', 'Polygon']
    });
  }

  async getFeatureInfo(lngLat)
  {
    const map = this.map;

    this.panel.show();

    this.clearHighlight();
    this.addPointer(lngLat);

    const headerDiv = this.headerDiv;
    headerDiv.innerHTML = "";
    const lngLatDiv = document.createElement("div");
    headerDiv.appendChild(lngLatDiv);
    lngLatDiv.innerHTML = `<div>Lon: ${lngLat.lng}</div>
                           <div>Lat: ${lngLat.lat}</div>`;

    if (toUtm)
    {
      const utm = toUtm(lngLat.lat, lngLat.lng, 7, 'ETRS89');
      const utmDiv = document.createElement("div");
      headerDiv.appendChild(utmDiv);
      utmDiv.innerHTML = `<div>UTM-x: ${utm.easting}</div>
                          <div>UTM-y: ${utm.northing}</div>`;
    }

    this.infoDiv.innerHTML = `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;

    const services = map.getStyle().metadata?.services;
    const serviceParameters = map.getStyle().metadata?.serviceParameters;
    if (services === undefined) return;
    if (serviceParameters === undefined) return;

    const promises = [];
    const layers = map.getStyle().layers;
    for (let lay of layers)
    {
      let layer = map.getLayer(lay.id);
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
            let cqlFilterArray = [];
            if (layer.metadata?.layers)
            {
              layerNameArray.push(...layer.metadata.layers.split(","));
            }
            if (layer.metadata?.cqlFilter)
            {
              cqlFilterArray.push(...layer.metadata.cqlFilter.split(";"));
            }
            extendArray(cqlFilterArray, layerNameArray.length);

            if (params.layers)
            {
              layerNameArray.push(...params.layers.split(","));
            }
            if (params.cqlFilter)
            {
              cqlFilterArray.push(...params.cqlFilter.split(";"));
            }
            extendArray(cqlFilterArray, layerNameArray.length);

            if (layerNameArray.length > 0)
            {
              for (let i = 0; i < layerNameArray.length; i++)
              {
                let layerName = layerNameArray[i];
                let cqlFilter = cqlFilterArray[i];
                let promise = this.getFeatures(
                  lngLat, layer.id, service, layerName, cqlFilter);
                promises.push(promise);
              }
            }
          }
        }
      }
    }
    let data = await Promise.all(promises);
    this.showInfo(data);
  }

  async getFeatures(lngLat, layerId, service, layerName, cqlFilter = "")
  {
    return FeatureTypeInspector.getInfo(service.url, layerName).then(info =>
    {
      if (info.geometryColumn)
      {
        const map = this.map;
        const isSurface = info.geometryType.indexOf("Surface") !== -1;
        const zoom = map.getZoom();
        const dist = isSurface ? 0 : Math.pow(2, 18 - zoom);

        let url = "/proxy?url=" + service.url + "&" +
          "request=GetFeature" +
          "&service=WFS" +
          "&version=2.0.0" +
          "&typeNames=" + layerName +
          "&srsName=EPSG:4326" +
          "&outputFormat=application/json" +
          "&cql_filter=" + "DISTANCE(" + info.geometryColumn +
            ",SRID=4326;POINT(" + lngLat.lng + " " + lngLat.lat + "))<=" + dist;

        if (cqlFilter && cqlFilter.trim().length > 0)
        {
          url += " and " + cqlFilter;
        }

        return fetch(url).then(response => response.json()).then(geojson =>
        {
          let layer = map.getLayer(layerId);
          if (layer.metadata.highlight)
          {
            this.highlight(geojson);
          }
          return {
            "layerId": layerId,
            "geojson": geojson
          };
        })
        .catch(error => error);
      }
      else
      {
        return Promise.resolve();
      }
    });
  }
}

export { GetFeatureInfoTool };