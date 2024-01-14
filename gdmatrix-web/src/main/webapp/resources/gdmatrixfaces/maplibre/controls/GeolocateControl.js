/* GeolocateControl.js */

import { Panel } from "./Panel.js";

class GeolocateControl
{
  constructor(options)
  {
    this.title = "Geolocate";
    this.createPanel(options);
    this.geolocators = [];
    this.activeGeolocator = null;
    this.data = {
      type: "FeatureCollection",
      features: []
    };
  }

  addGeolocator(geolocator)
  {
    this.geolocators.push(geolocator);
    this.updateGeolocatorSelector();

    if (this.activeGeolocator === null) this.setActiveGeolocator(geolocator);
    this.div.style.display = "";
  }

  createPanel(options)
  {
    this.panel = new Panel(options.containerId,
      "Search", "pi pi-search", options.insertTop);

    this.geolocatorSelectorDiv = document.createElement("div");
    this.panel.bodyDiv.appendChild(this.geolocatorSelectorDiv);

    this.formDiv = document.createElement("div");
    this.formDiv.className = "overflow-hidden";
    this.panel.bodyDiv.appendChild(this.formDiv);

    this.buttonsDiv = document.createElement("div");
    this.buttonsDiv.className = "button_bar text-right";
    this.panel.bodyDiv.appendChild(this.buttonsDiv);

    this.findButton = document.createElement("button");
    this.findButton.textContent = "Search";
    this.findButton.addEventListener("click", (event) => {
      event.preventDefault();
      this.find();
    });
    this.buttonsDiv.appendChild(this.findButton);

    this.clearButton = document.createElement("button");
    this.clearButton.textContent = "Clear";
    this.clearButton.addEventListener("click", (event) => {
      event.preventDefault();
      this.clear();
    });
    this.buttonsDiv.appendChild(this.clearButton);

    this.resultDiv = document.createElement("div");
    this.resultDiv.className = "overflow-auto";
    this.panel.bodyDiv.appendChild(this.resultDiv);
  }

  initSourceAndLayers()
  {
    const map = this.map;

    map.addSource("geolocator_results", {
      type: 'geojson',
      data: this.data.features
    });

    map.addLayer({
      "id": "geolocator_points",
      "type": 'circle',
      "source": "geolocator_results",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 4
      },
      "filter": ["==", "$type", "Point"]
    });

    map.addLayer({
      "id": "geolocator_linestrings",
      "type": 'line',
      "source": "geolocator_results",
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 4,
        "line-opacity": 0.5
      },
      "filter": ["==", "$type", "LineString"]
    });

    map.addLayer({
      "id": "geolocator_polygons",
      "type": 'fill',
      "source": "geolocator_results",
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.2
      },
      "filter": ["==", "$type", "Polygon"]
    });
  }

  updateGeolocatorSelector()
  {
    this.geolocatorSelectorDiv.innerHTML = "";
    const ul = document.createElement("ul");
    this.geolocatorSelectorDiv.appendChild(ul);
    for (let geolocator of this.geolocators)
    {
      let li = document.createElement("li");
      ul.appendChild(li);
      let anchor = document.createElement("a");
      anchor.href = "#";
      li.appendChild(anchor);
      anchor.textContent = geolocator.getTitle();
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
        this.setActiveGeolocator(geolocator);
      });
    }
  }

  setActiveGeolocator(geolocator)
  {
    this.formDiv.innerHTML = "";
    this.resultDiv.innerHTML = "";
    this.activeGeolocator = geolocator;
    this.activeGeolocator.addFormFields(this.formDiv);
  }

  async find()
  {
    if (this.activeGeolocator)
    {
      this.resultDiv.innerHTML = `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;
      try
      {
        this.data.features = await this.activeGeolocator.find();
        this.map.getSource("geolocator_results").setData(this.data);
        if (this.data.features.length > 0)
        {
          let bbox = turf.bbox(this.data);
          console.info(bbox);
          let bounds = new maplibregl.LngLatBounds(
            [bbox[0], bbox[1]], [bbox[2], bbox[3]]);
          console.info(bounds);
          this.map.fitBounds(bounds, 
          {
            padding: 20
          });
        }
        this.listFeatures();
      }
      catch (ex)
      {
        console.error(ex);
        this.resultDiv.textContent = "" + ex;
      }
    }
  }

  clear()
  {
    this.data.features = [];
    this.map.getSource("geolocator_results").setData(this.data);
    this.setActiveGeolocator(this.activeGeolocator);
  }

  listFeatures()
  {
    const geolocator = this.activeGeolocator;
    this.resultDiv.innerHTML = "";
    const features = this.data.features;
    const ul = document.createElement("ul");
    ul.className = "pl-4";
    this.resultDiv.appendChild(ul);
    for (let feature of features)
    {
      let li = document.createElement("li");
      ul.appendChild(li);
      let anchor = document.createElement("a");
      anchor.href = "#";
      li.appendChild(anchor);
      anchor.textContent = geolocator.getFeatureLabel(feature);
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
        this.selectFeature(feature);
      });
    }
  }

  selectFeature(feature)
  {
    const map = this.map;
    
    let position = {
      center: turf.centroid(feature).geometry.coordinates,
      speed: 1,
      curve: 1,
      easing(t) { return t; }
    };
    map.flyTo(position);
  }

  onAdd(map)
  {
    this.map = map;
    map.geolocateControl = this;

    this.initSourceAndLayers();

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="pi pi-search"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.title = this.title;
    div.style.width = "29px";
    div.style.height = "29px";
    div.style.fontFamily = "var(--font-family)";
    div.style.display = "none";
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      this.panel.show();
    });

    if (this.geolocators.length > 0)
    {
      this.setActiveGeolocator(this.geolocators[0]);
    }

    return div;
  }
}

class Geolocator
{
  constructor(params)
  {
  }

  getTitle()
  {
    return "title";
  }

  addFormFields(elem)
  {
    // create form fields into elem
    if (this.createFormFields)
    {
      elem.innerHTML = this.createFormFields();
    }
  }

  createFormFields()
  {
    return `<input type="text" name="name" />`; // legacy method
  }

  async find()
  {
    return Promise.resolve([]); // geojson features
  }

  getFeatureLabel(feature)
  {
    return "?";
  }

  getMarker(feature)
  {
    return null;
  }
}

class WfsGeolocator extends Geolocator
{
  getTitle()
  {
    return "Generic WFS geolocator";
  }

  createFormFields()
  {
    return `
      <div class="formgrid grid">
        <div class="field col-12">
          <label for="layer_name">Layer:</label>
          <input id="layer_name" type="text" class="code ui-widget text-base text-color surface-overlay p-2 border-1 border-solid surface-border border-round appearance-none outline-none focus:border-primary w-full" />
        </div>
        <div class="field col-12">
          <label for="cql_filter">Filter:</label>
          <textarea id="cql_filter" class="code text-base text-color surface-overlay p-2 border-1 border-solid surface-border border-round appearance-none outline-none focus:border-primary w-full"></textarea>
        </div>
      </div>
    `;
  }

  async find()
  {
    let layerName = document.getElementById("layer_name").value;
    let cqlFilter = document.getElementById("cql_filter").value;
    if (layerName)
    {
      return this.findWfs("https://gis.santfeliu.cat/geoserver/wfs", 
        layerName, cqlFilter);
    }
    else
    {
      return Promise.resolve([]);
    }
  }

  async findWfs(serviceUrl, layerName, cqlFilter)
  {
    let url = "/proxy?url=" + serviceUrl + "&" +
      "request=GetFeature" +
      "&service=WFS" +
      "&version=2.0.0" +
      "&typeNames=" + layerName +
      "&srsName=EPSG:4326" +
      "&outputFormat=application/json";

    if (cqlFilter)
    {
      url += "&cql_filter=" + cqlFilter;
    }
    return fetch(url).then(response => response.json()).then(json => 
    {
      console.info(json);
      return json.features;
    });
  }

  getFeatureLabel(feature)
  {
    return JSON.stringify(feature.properties);
  }

  getMarker(feature)
  {
    return null;
  }
}

export { GeolocateControl, Geolocator, WfsGeolocator };

