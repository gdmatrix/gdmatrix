/* FindFeatureControl.js */

import { Panel } from "../ui/Panel.js";
import { FeatureForm } from "../ui/FeatureForm.js";
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
        "iconClass" : "pi pi-search",
        "paddingOffset": 32
      }, ...options};

    this.finders = [];
    this.activeFinder = null;
    this.data = {
      type: "FeatureCollection",
      features: []
    };
    this.markers = new Map(); // feature -> Marker
    this.selectedMarker = null;
  }

  createPanels(map)
  {
    const findPanel = new Panel(map, this.options);
    this.findPanel = findPanel;

    this.finderSelectorDiv = document.createElement("div");
    findPanel.bodyDiv.appendChild(this.finderSelectorDiv);

    this.formDiv = document.createElement("div");
    this.formDiv.className = "overflow-hidden";
    findPanel.bodyDiv.appendChild(this.formDiv);

    const clearDiv = document.createElement("div");
    findPanel.bodyDiv.appendChild(clearDiv);
    clearDiv.innerHTML = `<input id="clear_markers" type="checkbox" checked>
      <label for="clear_markers">${bundle.get("FindFeatureControl.clearMarkers")}</label>`;
    this.clearMarkersCheckbox = document.getElementById("clear_markers");
    clearDiv.className = "flex mb-3";

    this.buttonsDiv = document.createElement("div");
    this.buttonsDiv.className = "button_bar text-right";
    findPanel.bodyDiv.appendChild(this.buttonsDiv);

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
    findPanel.bodyDiv.appendChild(this.resultDiv);

    const infoPanel = new Panel(map, {
      title: bundle.get("FindFeatureControl.title"),
      position: "right"
    });
    this.infoPanel = infoPanel;
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

  addFinder(finder)
  {
    this.finders.push(finder);
    finder.onAdd(this);

    if (this.activeFinder === null) this.setActiveFinder(finder);
    this.div.style.display = "";

    this.updateFinderSelector();
  }

  updateFinderSelector()
  {
    this.finderSelectorDiv.innerHTML = "";
    const ul = document.createElement("ul");
    ul.className = "finder_selector";
    this.finderSelectorDiv.appendChild(ul);
    for (let finder of this.finders)
    {
      let li = document.createElement("li");
      ul.appendChild(li);
      let icon = document.createElement("i");
      icon.className = "pi pi-arrow-right mr-1";
      li.appendChild(icon);
      
      let anchor = document.createElement("a");
      anchor.href = "#";
      li.appendChild(anchor);
      anchor.textContent = finder.getTitle();
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
        this.setActiveFinder(finder);
        this.highlightFinderSelector();
      });
    }
    this.highlightFinderSelector();
  }
  
  highlightFinderSelector()
  {
    const ul = this.finderSelectorDiv.firstElementChild;
    const elems = ul.getElementsByClassName("selected");
    if (elems.length > 0) elems[0].classList.remove("selected");
    let index = this.finders.indexOf(this.activeFinder);
    let li = ul.children[index];
    li.classList.add("selected");
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
          let paddingOffset = this.options.paddingOffset || 0;
          let bbox = turf.bbox(this.data);
          let bounds = new maplibregl.LngLatBounds(
            [bbox[0], bbox[1]], [bbox[2], bbox[3]]);
          const panelManager = map.panelManager;
          this.map.fitBounds(bounds,
          {
            maxZoom: this.activeFinder.getMaxZoom(),
            padding: panelManager.getPadding(paddingOffset)
          });
        }
        this.listFeatures();
        if (this.clearMarkersCheckbox.checked) this.clearMarkers();
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
    this.infoPanel.hide();
  }

  listFeatures()
  {
    const finder = this.activeFinder;
    const features = this.data.features;
    const maxZoom = finder.getMaxZoom();

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
        const finder = this.activeFinder;
        this.selectFeature(feature, true, maxZoom);
      });
    }
  }

  addMarkers()
  {
    const map = this.map;
    const finder = this.activeFinder;
    const features = this.data.features;
    const maxZoom = finder.getMaxZoom();

    for (let feature of features)
    {
      let marker = finder.getMarker(feature);
      if (marker)
      {
        marker.addTo(map);
        this.markers.set(feature, marker);
        if (typeof marker.select === "function")
        {
          marker.getElement().addEventListener("click", (event) => 
          {
            event.stopPropagation();
            this.selectFeature(feature, false, maxZoom);
          });
        }
      }
    }
  }

  clearMarkers()
  {
    if (this.selectedMarker && this.selectedMarker.unselect)
    {
      this.selectedMarker.unselect();
      this.selectedMarker = null;
    }
    
    for (let marker of this.markers.values())
    {
      marker.remove();
    }
    this.markers.clear();
  }

  selectFeature(feature, center = false, maxZoom)
  {
    const map = this.map;

    if (center)
    {
      let paddingOffset = this.options.paddingOffset || 0;
      let bbox = turf.bbox(feature);
      let bounds = new maplibregl.LngLatBounds(
        [bbox[0], bbox[1]], [bbox[2], bbox[3]]);
      const panelManager = map.panelManager;
      map.fitBounds(bounds,
      {
        maxZoom: maxZoom || 18,
        padding: panelManager.getPadding(paddingOffset)
      });
    }
    
    if (this.selectedMarker && this.selectedMarker.unselect)
    {
      this.selectedMarker.unselect();
    }
    this.selectedMarker = this.markers.get(feature);
    if (this.selectedMarker && this.selectedMarker.select)
    {
      this.selectedMarker.select();
    }
  }

  async showForm(form)
  {
    this.infoPanel.bodyDiv.innerHTML =
      `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;

    form = await form.render();
    if (form)
    {
      this.infoPanel.bodyDiv.innerHTML = "";
      this.infoPanel.bodyDiv.appendChild(form.div);
      this.infoPanel.show();
    }
    else
    {
      this.infoPanel.bodyDiv.innerHTML = "";
    }
  }
  
  hideForm()
  {
    this.infoPanel.bodyDiv.innerHTML = "";
  }

  onAdd(map)
  {
    this.map = map;
    map.findFeatureControl = this;

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
      this.findPanel.show();
    });

    if (this.finders.length > 0)
    {
      this.setActiveFinder(this.finders[0]);
    }

    this.createPanels(map);

    return div;
  }
}

class FeatureFinder
{
  constructor(params)
  {
  }

  onAdd(findFeatureControl)
  {
    this.findFeatureControl = findFeatureControl;
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
    const control = this.findFeatureControl;
    const map = control.map;
    const centroid = turf.centroid(feature);
    const marker = new maplibregl.Marker({ color: this.getMarkerColor() })
     .setLngLat(centroid.geometry.coordinates);

    const element = marker.getElement();
    element.style.cursor = "pointer";

    const form = this.getForm(feature);

    marker.select = () => {
      marker.remove();
      marker.selectedMarker = this.getSelectedMarker(feature);
      marker.selectedMarker.addTo(map);      
      if (form) control.showForm(form);
    };

    marker.unselect = () => {
      marker.selectedMarker.remove();
      marker.addTo(map);
    };

    return marker;
  }

  getSelectedMarker(feature)
  {
    const centroid = turf.centroid(feature);
    const marker = new maplibregl.Marker({ color: this.getSelectedMarkerColor() })
     .setLngLat(centroid.geometry.coordinates);

    return marker;
  }

  getMarkerColor()
  {
    return "#3FB1CE";
  }

  getSelectedMarkerColor()
  {
    return "#FF0000";
  }

  getMaxZoom()
  {
    return 18;
  }

  getForm(feature)
  {
    return null;
  }
}

class WfsFinder extends FeatureFinder
{
  constructor(params)
  {
    super(params);

    this.service = params?.service || {
      url: "https://gis.santfeliu.cat/geoserver/wfs",
      useProxy: true
    };

    this.layerName = params?.layerName;
  }

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
    this.layerName = document.getElementById("layer_name").value;
    let cqlFilter = document.getElementById("cql_filter").value;
    if (this.service?.url && this.layerName)
    {
      return await this.findWfs(cqlFilter);
    }
    else
    {
      return [];
    }
  }

  async findWfs(cqlFilter, viewparams)
  {
    let url = "/proxy?url=" + this.service.url + "&" +
      "request=GetFeature" +
      "&service=WFS" +
      "&version=2.0.0" +
      "&typeNames=" + this.layerName +
      "&srsName=EPSG:4326" +
      "&outputFormat=application/json" +
      "&exceptions=application/json";

    if (cqlFilter)
    {
      url += "&cql_filter=" + cqlFilter;
    }

    if (viewparams)
    {
      url += "&viewparams=" + viewparams;
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

  getForm(feature)
  {
    const map = this.findFeatureControl.map;

    const service = this.service;
    const form = new FeatureForm(feature);
    form.service = service;
    form.layerName = this.layerName;
    form.setFormSelectorAndPriority(map);

    return form;
  }
}

export { FindFeatureControl, FeatureFinder, WfsFinder };

