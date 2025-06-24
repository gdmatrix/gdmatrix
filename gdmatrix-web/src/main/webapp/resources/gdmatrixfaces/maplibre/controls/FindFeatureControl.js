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
        "iconClass" : "pi pi-search",
        "paddingOffset": 32,
        "selectedFinderIconClass": "pi pi-arrow-right",
        "unselectedFinderIconClass": "pi pi-minus",
        "popup" : {}
      }, ...options};

    this.finders = [];
    this.activeFinder = null;
    this.data = {
      type: "FeatureCollection",
      features: []
    };
    this.markers = new Map(); // feature -> Marker
    this.forms = new Map(); // feature -> Form
    this.selectedFeature = null;
    this.selectedMarker = null;
  }

  createPanels(map)
  {
    const findPanel = new Panel(map, this.options);
    this.findPanel = findPanel;

    this.finderLabelDiv = document.createElement("div");
    findPanel.bodyDiv.appendChild(this.finderLabelDiv);
    this.finderLabelDiv.textContent = bundle.get("FindFeatureControl.finderLabel");

    this.finderSelectorDiv = document.createElement("div");
    findPanel.bodyDiv.appendChild(this.finderSelectorDiv);
    this.finderSelectorDiv.className = "finder_selector";

    this.formDiv = document.createElement("div");
    findPanel.bodyDiv.appendChild(this.formDiv);

    const clearDiv = document.createElement("div");
    findPanel.bodyDiv.appendChild(clearDiv);
    clearDiv.innerHTML = `<input id="clear_markers" type="checkbox" checked>
      <label for="clear_markers">${bundle.get("FindFeatureControl.clearMarkers")}</label>`;
    this.clearMarkersCheckbox = document.getElementById("clear_markers");
    clearDiv.className = "flex align-items-center mb-3";

    this.buttonsDiv = document.createElement("div");
    this.buttonsDiv.className = "button_bar text-right";
    findPanel.bodyDiv.appendChild(this.buttonsDiv);

    this.findButton = document.createElement("button");
    this.findButton.textContent = bundle.get("button.find");
    this.findButton.addEventListener("click", (event) => {
      event.preventDefault();
      this.find();
      this.updateUrl();
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
  
  createPopup()
  {
    this.removePopup();
    const popup = new maplibregl.Popup(this.options.popup);
    this.popup = popup;
    return popup;
  }
  
  removePopup()
  {
    if (this.popup) this.popup.remove();
    this.popup = null;    
  }

  initSourceAndLayers()
  {
    const map = this.map;

    map.addSource("finder_results", {
      type: 'geojson',
      data: this.data
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
      "filter": ["==", ["geometry-type"], "Point"]
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
      "filter": ["==", ["geometry-type"], "LineString"]
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
      "filter": ["==", ["geometry-type"], "Polygon"]
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
    this.finderSelectorDiv.appendChild(ul);
    for (let finder of this.finders)
    {
      let li = document.createElement("li");
      ul.appendChild(li);
      let icon = document.createElement("i");
      icon.className = this.options.unselectedFinderIconClass;
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
    let li;
    let icon;
    const elems = ul.getElementsByClassName("selected");
    if (elems.length > 0)
    {
      li = elems[0];
      li.classList.remove("selected");
      icon = li.firstElementChild;
      icon.className = this.options.unselectedFinderIconClass;
    }
    let index = this.finders.indexOf(this.activeFinder);
    li = ul.children[index];
    li.classList.add("selected");
    icon = li.firstElementChild;
    icon.className = this.options.selectedFinderIconClass;
  }

  setActiveFinder(finder)
  {
    this.formDiv.innerHTML = "";
    this.resultDiv.innerHTML = "";
    this.activeFinder = finder;
    this.activeFinder.populateForm(this.formDiv);
  }

  async find()
  {
    if (this.activeFinder)
    {
      const map = this.map;
      this.resultDiv.innerHTML = `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;
      try
      {
        if (this.clearMarkersCheckbox.checked) 
        {
          this.clearMarkers();
          this.clearForms();
        }
        this.removePopup();
        
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
            maxZoom: this.activeFinder.maxZoom,
            padding: panelManager.getPadding(paddingOffset)
          });
        }
        this.listFeatures();
        this.addMarkers();
        this.addForms();        
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
      finder.populateList(feature, anchor);
      anchor.addEventListener("click", (event) => {
        event.preventDefault();
        this.selectFeature(feature, finder, "list");
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
        this.markers.set(feature, marker);

        const centroid = turf.centroid(feature);
        marker.setLngLat(centroid.geometry.coordinates);
        marker.getElement().addEventListener("click", (event) => {
          event.stopPropagation();
          this.selectFeature(feature, finder, "marker");
        });
        marker.addTo(map);
      }
    }
  }

  addForms()
  {
    const map = this.map;
    const finder = this.activeFinder;
    const features = this.data.features;

    for (let feature of features)
    {
      let form = finder.getForm(feature);
      if (form)
      {
        this.forms.set(feature, form);
      }
    }
  }
  
  clear()
  {
    this.data.features = [];
    this.map.getSource("finder_results").setData(this.data);
    this.setActiveFinder(this.activeFinder);
    this.clearMarkers();
    this.clearForms();
    this.infoPanel.hide();
    this.removePopup();
  }  

  clearMarkers()
  {
    if (this.selectedMarker)
    {
      this.selectedMarker.remove();
      this.selectedMarker = null;
      this.selectedFeature = null;
    }

    for (let marker of this.markers.values())
    {
      marker.remove();
    }
    this.markers.clear();
  }
  
  clearForms()
  {
    this.forms.clear();
  }
  
  paramsToForm(params)
  {
    const inputs = this.formDiv.querySelectorAll("input");
    for (const input of inputs)
    {
      let value = params.get(input.id);
      if (value !== null)
      {
        input.value = value;
      }
    }

    const selects = this.formDiv.querySelectorAll("select");
    for (const select of selects)
    {
      let value = params.get(select.id);
      if (value !== null)
      {
        select.value = value;
      }
    }
  }
  
  paramsFromForm(params)
  {
    const inputs = this.formDiv.querySelectorAll("input");
    for (const input of inputs)
    {
      let value = input.value;
      if (value)
      {
        params.set(input.id, value);
      }
    }

    const selects = this.formDiv.querySelectorAll("select");
    for (const select of selects)
    {
      let value = select.value;
      if (value)
      {
        params.set(select.id, value);
      }
    }
  }  
  

  selectFeature(feature, finder, source)
  {
    const map = this.map;

    if (source === "list" || finder.centerMarkerOnClick)
    {
      let paddingOffset = this.options.paddingOffset || 0;
      let bbox = turf.bbox(feature);
      let bounds = new maplibregl.LngLatBounds(
        [bbox[0], bbox[1]], [bbox[2], bbox[3]]);
      const panelManager = map.panelManager;
      map.fitBounds(bounds,
      {
        maxZoom: finder.maxZoom || 18,
        padding: panelManager.getPadding(paddingOffset)
      });
    }

    if (this.selectedMarker)
    {
      finder.unselectMarker(this.selectedMarker, this.selectedFeature);
    }

    this.selectedFeature = feature;
    this.selectedMarker = this.markers.get(feature);
    
    if (this.selectedMarker)
    {
      this.selectedMarker.remove();      
      finder.selectMarker(this.selectedMarker, this.selectedFeature);
      this.selectedMarker.addTo(map);            
    }
    
    this.removePopup();
    
    this.infoPanel.bodyDiv.innerHTML = "";
    
    if (source === "marker" || 
        finder.formViewMode === "panel" || 
        finder.showPopupFromList)
    {
      let form = this.forms.get(feature);
      if (form)
      {
        this.showForm(form, finder.formViewMode);
      }
    }
  }

  async showForm(form, formViewMode)
  {
    const map = this.map;

    if (formViewMode === "panel")
    {
      this.infoPanel.bodyDiv.innerHTML =
        `<span class="pi pi-spin pi-spinner pt-4 pb-4" />`;

      form = await form.render();
      if (form)
      {
        this.infoPanel.bodyDiv.innerHTML = "";
        this.infoPanel.bodyDiv.appendChild(form.getElement());
        this.infoPanel.show();
      }
      else
      {
        this.infoPanel.bodyDiv.innerHTML = "";
      }
    }
    else if (formViewMode === "popup")
    {
      form = await form.render();
      if (form)
      {
        const popup = this.createPopup();
        const feature = form.feature;
        const centroid = turf.centroid(feature);
        popup.setLngLat(centroid.geometry.coordinates);
        popup.setDOMContent(form.getElement());
        popup.addTo(map);
      }
    }
  }
  
  onLoad()
  {
    if (this.finders.length === 0) return;
    
    let selectedFinder = this.finders[0];

    const params = new URLSearchParams(document.location.search);
    const finderName = params.get("finder");

    if (finderName)
    {
      for (const finder of this.finders)
      {
        if (finder.name === finderName)
        {
          selectedFinder = finder;
          break;
        }
      }
    }
    this.setActiveFinder(selectedFinder);
    this.paramsToForm(params);
    if (finderName) this.find();
  }
  
  updateUrl()
  {
    const baseUrl = document.location.protocol + "//" + 
                    document.location.hostname + 
                    document.location.pathname;
            
    const params = new URLSearchParams(document.location.search);
    params.set("finder", this.activeFinder.name);
    this.paramsFromForm(params);
    
    let url = baseUrl + "?" + params.toString();
    if (document.location.hash) url += document.location.hash;
    
    window.history.replaceState({}, '', url);
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

    this.createPanels(map);
    
    map.on("load", () => this.onLoad());

    return div;
  }
}

export { FindFeatureControl };

