/* FindFeatureControl.js */

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class FindFeatureControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : bundle.get("FindFeatureControl.title"),
        "iconClass" : "pi pi-search"
      }, ...options};

    this.finders = [];
    this.activeFinder = null;
    this.data = {
      type: "FeatureCollection",
      features: []
    };
    this.markers = [];
  }

  addFinder(finder)
  {
    this.finders.push(finder);
    this.updateFinderSelector();

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
    this.findButton.textContent = bundle.get("button.find");
    this.findButton.addEventListener("click", (event) => {
      event.preventDefault();
      this.find();
    });
    this.buttonsDiv.appendChild(this.findButton);

    this.clearButton = document.createElement("button");
    this.clearButton.textContent = bundle.get("button.clear");
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

  updateFinderSelector()
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
      const map = this.map;
      this.resultDiv.innerHTML = `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;
      try
      {
        this.data.features = await this.activeFinder.find();
        map.getSource("finder_results").setData(this.data);

        if (this.data.features.length > 0)
        {
          let bbox = turf.bbox(this.data);
          let bounds = new maplibregl.LngLatBounds(
            [bbox[0], bbox[1]], [bbox[2], bbox[3]]);
          const panelManager = map.panelManager;
          this.map.fitBounds(bounds,
          {
            maxZoom: this.activeFinder.getMaxZoom(),
            padding: panelManager.getPadding()
          });
        }
        this.listFeatures();
        this.clearMarkers();
        this.addMarkers();
      }
      catch (ex)
      {
        const ul = document.createElement("ul");
        ul.className = "error";
        this.resultDiv.innerHTML = "";
        this.resultDiv.append(ul);

        if (ex?.exceptions instanceof Array)
        {
          for (let iex of ex.exceptions)
          {
            let li = document.createElement("li");
            li.textContent = iex.text;
            ul.appendChild(li);
          }
        }
        else
        {
          ul.innerHTML = `<li>${String(ex)}</li>`;
        }
      }
    }
  }

  clear()
  {
    this.data.features = [];
    this.map.getSource("finder_results").setData(this.data);
    this.setActiveFinder(this.activeFinder);
    this.clearMarkers();
  }

  listFeatures()
  {
    const finder = this.activeFinder;
    const features = this.data.features;
    this.resultDiv.innerHTML = "";

    const countDiv = document.createElement("div");
    this.resultDiv.appendChild(countDiv);
    countDiv.className = "mt-2";
    countDiv.textContent = 
      bundle.get("FindFeatureControl.featureCount", features.length) +
      (features.length === 0 ? "." : ":");

    const ul = document.createElement("ul");
    ul.className = "finder_results";
    this.resultDiv.appendChild(ul);
    for (let feature of features)
    {
      let li = document.createElement("li");
      ul.appendChild(li);
      let iconUrl = finder.getListIconUrl(feature);
      if (iconUrl)
      {
        let img = document.createElement("img");
        img.src = iconUrl;
        img.alt = "";
        img.style.width = "16px";
        img.style.height = "16px";
        li.appendChild(img);
      }
      else
      {
        let icon = finder.getListIcon(feature) || "pi pi-map-marker";
        let span = document.createElement("i");
        span.className = icon;
        li.appendChild(span);
      }
      
      let anchor = document.createElement("a");
      anchor.href = "#";
      anchor.className = "pl-1";
      li.appendChild(anchor);
      anchor.textContent = finder.getFeatureLabel(feature);
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
        this.selectFeature(feature);
      });
    }
  }
  
  addMarkers()
  {
    const map = this.map;
    const finder = this.activeFinder;
    const features = this.data.features;

    for (let feature of features)
    {
      let marker = finder.getMarker(feature);
      if (marker)
      {
        marker.addTo(map);
        this.markers.push(marker);

        marker.getElement().addEventListener("click", (event) => 
        {
          event.preventDefault();
          this.selectFeature(feature, Math.round(map.getZoom()));
        });
        marker.getElement().style.cursor = "pointer";
      }
    }
  }
  
  clearMarkers()
  {
    for (let marker of this.markers)
    {
      marker.remove();
    }
    this.markers = [];
  }

  selectFeature(feature, maxZoom)
  {
    const map = this.map;
    if (maxZoom === undefined) maxZoom = this.activeFinder.getMaxZoom();
    
    let bbox = turf.bbox(feature);
    let bounds = new maplibregl.LngLatBounds(
      [bbox[0], bbox[1]], [bbox[2], bbox[3]]);
    const panelManager = map.panelManager;
    this.map.fitBounds(bounds,
    {
      maxZoom: maxZoom,
      padding: panelManager.getPadding()
    });
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

class FeatureFinder
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
    return []; // geojson features
  }

  getFeatureLabel(feature)
  {
    return "?";
  }

  getListIcon(feature)
  {
    return "pi pi-map-marker";
  }
  
  getListIconUrl(feature)
  {
    return null;
  }

  getMarker(feature)
  {
    let centroid = turf.centroid(feature);
    let marker = new maplibregl.Marker()
     .setLngLat(centroid.geometry.coordinates);
    
    return marker;
  }
    
  getMaxZoom()
  {
    return 18;
  }
}

class WfsFinder extends FeatureFinder
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
          <label for="layer_name">${bundle.get("FindFeatureControl.layer")}:</label>
          <input id="layer_name" type="text" class="code ui-widget text-base text-color surface-overlay p-2 border-1 border-solid surface-border border-round appearance-none outline-none focus:border-primary w-full" />
        </div>
        <div class="field col-12">
          <label for="cql_filter">${bundle.get("FindFeatureControl.filter")}:</label>
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
      return await this.findWfs("https://gis.santfeliu.cat/geoserver/wfs", 
        layerName, cqlFilter);
    }
    else
    {
      return [];
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
      "&outputFormat=application/json" +
      "&exceptions=application/json";

    if (cqlFilter)
    {
      url += "&cql_filter=" + cqlFilter;
    }
    const response = await fetch(url);
    const json = await response.json();
    if (json.exceptions) throw json;
    
    return json.features;
  }

  getFeatureLabel(feature)
  {
    return feature.id;
  }
}

export { FindFeatureControl, FeatureFinder, WfsFinder };

