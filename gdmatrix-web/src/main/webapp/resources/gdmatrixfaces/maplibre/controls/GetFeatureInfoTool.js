/* GetFeatureInfoTool.js */

import { Tool } from "./Tool.js";
import { Panel } from "../ui/Panel.js";
import { FeatureForm } from "../ui/FeatureForm.js";
import { toUtm } from "../utm-latlng.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class GetFeatureInfoTool extends Tool
{
  constructor(options)
  {
    super({...{ 
            "title": bundle.get("GetFeatureInfoTool.title"), 
            "iconClass": "fa fa-arrow-pointer",
            "position" : "right"
          }, ...options});

    this.highlightCount = 0;
    this.inProgress = false;
    this.tolerance = options?.tolerance === undefined ? 5 : options.tolerance;
    this._onMapClick = (event) => {
      this.getFeatureInfo(event.point, event.lngLat);
    };    
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
    this.headerDiv.innerHTML = bundle.get("GetFeatureInfoTool.help");
    this.infoDiv.innerHTML = "";
    this.clearHighlight();
    this.panel.hide();
    this.setLastUtm(null);
  }

  reactivate()
  {
    this.panel.show();
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
    this.panel.onHide = () => this.deactivateTool(this);

    const bodyDiv = this.panel.bodyDiv;

    this.headerDiv = document.createElement("div");
    bodyDiv.appendChild(this.headerDiv);
    this.headerDiv.textContent = bundle.get("GetFeatureInfoTool.help");    

    this.infoDiv = document.createElement("div");
    this.infoDiv.className = "custom_form";
    bodyDiv.appendChild(this.infoDiv);
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
  
  setLastUtm(utm)
  {
    const map = this.map;
    if (utm)
    {
      map.lastUtm = utm;
    }
    else
    {
      map.lastUtm = null;
    }
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
      "filter": ['==', ["geometry-type"], 'Point']
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
      "filter": ["any", ['==', ["geometry-type"], 'LineString'], ['==', ["geometry-type"], 'Polygon']]
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
      "filter": ['==', ["geometry-type"], 'Polygon']
    });
  }

  async getFeatureInfo(point, lngLat)
  {
    if (this.inProgress) return; // operation in progress

    this.inProgress = true;
    try
    {
      const map = this.map;

      this.panel.show();

      this.clearHighlight();
      this.addPointer(lngLat);

      const headerDiv = this.headerDiv;
      headerDiv.className = "pl-2";
      headerDiv.innerHTML = "";
      const lngLatDiv = document.createElement("div");
      headerDiv.appendChild(lngLatDiv);
      lngLatDiv.innerHTML = `Lon/Lat: ${lngLat.lng.toFixed(5)}, ${lngLat.lat.toFixed(5)}`;

      const utm = toUtm(lngLat.lat, lngLat.lng, 7, 'ETRS89');
      this.setLastUtm(utm);
      const utmDiv = document.createElement("div");
      headerDiv.appendChild(utmDiv);
      utmDiv.innerHTML = `UTM: ${utm.easting.toFixed(3)}, ${utm.northing.toFixed(3)}`;

      this.infoDiv.innerHTML = `<span class="pi pi-spin pi-spinner p-2" />`;

      const services = map.getStyle().metadata?.services;
      const serviceParameters = map.getStyle().metadata?.serviceParameters;
      if (services === undefined) return;
      if (serviceParameters === undefined) return;

      const formPromises = [];

      const tolerance = this.tolerance;
      const bbox = [
        [point.x - tolerance, point.y - tolerance],
        [point.x + tolerance, point.y + tolerance]
      ];
      
      const features = this.map.queryRenderedFeatures(bbox);
      const geojson = { "type": "FeatureCollection", "features": [] };
      for (let feat of features)
      {
        let layer = map.getLayer(feat.layer.id);
        if (layer.metadata?.locatable)
        {
          let feature = {
            type: "Feature",
            geometry: feat.geometry, 
            properties: feat.properties 
          };
          
          let sourceId = layer.source;
          let params = serviceParameters[sourceId];
          let service = params?.service ? services[params.service] : null;
          let layerName = params?.layers ? params.layers : layer.id;

          // highlight
          if (layer.metadata.highlight)
          {
            geojson.features.push(feature);
          }

          const form = new FeatureForm(feature);
          form.service = service;
          form.layerName = layerName;
          form.setFormSelectorAndPriority(map);
          formPromises.push(form.render());       
        }
      }
      if (geojson.features.length > 0)
      {
        this.highlight(geojson);        
      }      

      const layers = map.getStyle().layers;
      for (let lay of layers)
      {
        let layer = map.getLayer(lay.id);
        if (layer.metadata?.visible && layer.metadata?.locatable)
        {
          let sourceId = layer.source;
          let source = map.getSource(sourceId);
          let params = serviceParameters[sourceId];
          if (params && source.type === "raster")
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

                  formPromises.push(this.getForms(
                    lngLat, layer, service, layerName, cqlFilter));
                }
              }
            }
          }
        }
      }
      let forms = (await Promise.all(formPromises)).flat();
      this.showForms(forms);
    }
    catch (ex)
    {
      console.error(ex);
    }
    finally
    {
      this.inProgress = false;
    }
  }
  
  async getForms(lngLat, layer, service, layerName, cqlFilter = "")
  {
    const map = this.map;
    const selectionDistance = layer?.metadata?.selection_distance || 4;
    
    const geojson = await this.getFeatures(lngLat, service, layerName, 
      cqlFilter, selectionDistance);
      
    const formPromises = [];
    
    if (geojson && geojson.features)
    {
      // highlight
      if (layer.metadata.highlight)
      {
        this.highlight(geojson);
      }

      // get forms
      let features = geojson.features;
      if (features && features.length > 0)
      {
        for (let feature of features)
        {
          const form = new FeatureForm(feature);
          form.service = service;
          form.layerName = layerName;
          form.setFormSelectorAndPriority(map);
          formPromises.push(form.render());
        }
      }
    }

    const forms = await Promise.all(formPromises);
    return forms;
  }

  async getFeatures(lngLat, service, layerName, cqlFilter = "", selectionDistance)
  {
    try
    {      
      const info = await FeatureTypeInspector.getInfo(service.url, layerName);
      if (info?.geometryColumn)
      {
        const map = this.map;
        const isSurface = info.geometryType.indexOf("Surface") !== -1;
        const zoom = map.getZoom();
        const dist = isSurface ?
          0 : (selectionDistance * Math.pow(2, 18 - zoom)) / 4;

        let url = "/proxy?url=" + service.url + "&" +
          "request=GetFeature" +
          "&service=WFS" +
          "&version=2.0.0" +
          "&typeNames=" + layerName +
          "&srsName=EPSG:4326" +
          "&outputFormat=application/json" +
          "&exceptions=application/json" +
          "&cql_filter=" + info.geometryColumn + " IS NOT NULL AND DISTANCE(" + 
          info.geometryColumn + ",SRID=4326;POINT(" + 
          lngLat.lng + " " + lngLat.lat + "))<=" + dist;

        if (cqlFilter && cqlFilter.trim().length > 0)
        {
          url += " and " + cqlFilter;
        }

        const response = await fetch(url);
        const geojson = await response.json();
        return geojson;
      }
      else
      {
        return {};
      }
    }
    catch (ex)
    {
      return { error: ex };
    }
  }

  showForms(forms)
  {
    const map = this.map;
    const infoDiv = this.infoDiv;

    const ul = document.createElement("ul");
    ul.className = "feature_info";
    infoDiv.appendChild(ul);

    if (forms.length === 0)
    {
      infoDiv.innerHTML = `<div class="pt-4 pb-4">${bundle.get("GetFeatureInfoTool.noDataFound")}</div>`;
    }
    else
    {
      forms.sort((a, b) => a.priority - b.priority);
      infoDiv.innerHTML = "";
      for (let form of forms)
      {
        infoDiv.appendChild(form.getElement());
      }
    }
    this.panel.show();
  }
}

export { GetFeatureInfoTool };