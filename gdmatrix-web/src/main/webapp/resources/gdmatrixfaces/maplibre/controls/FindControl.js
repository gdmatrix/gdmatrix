/* FindControl.js */

import { Panel } from "../ui/Panel.js";

class FindControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : "Find",
        "iconClass" : "pi pi-search"
      }, ...options};

    this.finders = [];
    this.activeFinder = null;
    this.data = {
      type: "FeatureCollection",
      features: []
    };
  }

  addFinder(finder)
  {
    this.finders.push(finder);
    this.updateGeolocatorSelector();

    if (this.activeFinder === null) this.setActiveFinder(finder);
    this.div.style.display = "";
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);

    this.finderSelectorDiv = document.createElement("div");
    this.panel.bodyDiv.appendChild(this.finderSelectorDiv);

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

    map.addSource("finder_results", {
      type: 'geojson',
      data: this.data.features
    });

    map.addLayer({
      "id": "finder_points",
      "type": 'circle',
      "source": "finder_results",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 4
      },
      "filter": ["==", "$type", "Point"]
    });

    map.addLayer({
      "id": "finder_linestrings",
      "type": 'line',
      "source": "finder_results",
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
      "id": "finder_polygons",
      "type": 'fill',
      "source": "finder_results",
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
    this.finderSelectorDiv.innerHTML = "";
    const ul = document.createElement("ul");
    this.finderSelectorDiv.appendChild(ul);
    for (let finder of this.finders)
    {
      let li = document.createElement("li");
      ul.appendChild(li);
      let anchor = document.createElement("a");
      anchor.href = "#";
      li.appendChild(anchor);
      anchor.textContent = finder.getTitle();
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
        this.setActiveFinder(finder);
      });
    }
  }

  setActiveFinder(finder)
  {
    this.formDiv.innerHTML = "";
    this.resultDiv.innerHTML = "";
    this.activeFinder = finder;
    this.activeFinder.addFormFields(this.formDiv);
  }

  async find()
  {
    if (this.activeFinder)
    {
      this.resultDiv.innerHTML = `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;
      try
      {
        this.data.features = await this.activeFinder.find();
        this.map.getSource("finder_results").setData(this.data);
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
    this.map.getSource("finder_results").setData(this.data);
    this.setActiveFinder(this.activeFinder);
  }

  listFeatures()
  {
    const finder = this.activeFinder;
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
      anchor.textContent = finder.getFeatureLabel(feature);
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
    map.findControl = this;

    this.initSourceAndLayers();

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="${this.options.iconClass}"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.title = this.options.title;
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

    if (this.finders.length > 0)
    {
      this.setActiveFinder(this.finders[0]);
    }
    
    this.createPanel(map);

    return div;
  }
}

class Finder
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

class WfsFinder extends Finder
{
  getTitle()
  {
    return "Generic WFS finder";
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

export { FindControl, Finder, WfsFinder };

