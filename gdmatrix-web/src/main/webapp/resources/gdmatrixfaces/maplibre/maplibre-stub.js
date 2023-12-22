/* maplibre-stub.js */

if (window.mapLibreControlsLoaded === undefined)
{
  class HomeButton
  {
    constructor(homePosition)
    {
      this.homePosition = homePosition;
    }

    onAdd(map)
    {
      const div = document.createElement("div");
      div.className = "maplibregl-ctrl maplibregl-ctrl-group";
      div.innerHTML = `<button><span class="fa fa-home"/></button>`;
      div.title = "Initial view";
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      div.addEventListener("click", (e) =>
      {
        e.preventDefault();
        map.flyTo(this.homePosition);
      });

      return div;
    }
  }

  class LoadingIndicator
  {
    constructor()
    {
      this.tileKeys = new Set();
      this.errorCount = 0;
    }

    onLoadingStarted(event)
    {
      let key = event?.tile?.tileID?.canonical?.key;
      if (key)
      {
        this.tileKeys.add(key);
        this.updateProgress();
      }
    }

    onLoadingCompleted(event)
    {
      let key = event?.tile?.tileID?.canonical?.key;
      if (key)
      {
        this.tileKeys.delete(key);
        this.updateProgress();
      }
    }

    onError(event)
    {
      let key = event?.tile?.tileID?.canonical?.key;
      if (key)
      {
        this.errorCount++;
        console.error(event.error.message + ": " + event.source.tiles);
        this.tileKeys.delete(key);
        this.updateProgress();
      }
    }

    updateProgress()
    {
      let pendingCount = this.tileKeys.size;
      this.div.textContent = pendingCount;
      if (pendingCount > 0)
      {
        this.div.classList.add("flash");
      }
      else
      {
        this.div.classList.remove("flash");
      }
      if (this.errorCount > 0)
      {
        this.div.style.color = "red";
      }
      else
      {
        this.div.style.color = "black";
      }
    }

    onAdd(map)
    {
      map.on("dataloading", (e) => this.onLoadingStarted(e));
      map.on("data", (e) => this.onLoadingCompleted(e));
      map.on("dataabort", (e) => this.onLoadingCompleted(e));
      map.on("error", (e) => this.onError(e));

      const div = document.createElement("div");
      this.div = div;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
      div.style.width = "29px";
      div.style.height = "29px";
      div.style.userSelect = "none";
      div.style.fontFamily = "var(--font-family)";
      div.title = "Pending tiles";

      this.updateProgress();
      return div;
    }
  }

  class Panel
  {
    constructor(containerId, title, iconClassName, insertTop = false)
    {
      this.container = containerId ?
        document.getElementById(containerId) : map.getContainer();
      this.title = title;
      this.iconClassName = iconClassName || "pi pi-info-circle";
      this.insertTop = insertTop;
      this.createPanel();
    }

    show()
    {
      this.panelDiv.style.display = "";
      this.container.classList.add("expanded");
      this.panelDiv.scrollIntoView({
        block: "start", inline: "start", behavior : "smooth" });
    }

    hide()
    {
      this.panelDiv.style.display = "none";
      let panels = this.container.getElementsByClassName("panel");
      let firstVisiblePanel = null;
      for (let panel of panels)
      {
        if (panel.style.display !== "none")
        {
          firstVisiblePanel = panel;
          break;
        }
      }
      if (firstVisiblePanel === null)
      {
        this.container.classList.remove("expanded");
      }
      else
      {
        firstVisiblePanel.scrollIntoView({ behavior : "smooth" });
      }
    }

    createPanel()
    {
      const panelDiv = document.createElement("div");
      this.panelDiv = panelDiv;
      panelDiv.className = "panel";
      panelDiv.style.display = "none";
      let firstPanel = this.container.firstElementChild.firstElementChild;
      if (firstPanel && this.insertTop)
      {
        this.container.firstElementChild.insertBefore(panelDiv, firstPanel);
      }
      else
      {
        this.container.firstElementChild.appendChild(panelDiv);
      }

      const headerDiv = document.createElement("div");
      this.headerDiv = headerDiv;
      headerDiv.className = "header flex-grow-0 flex p-1";
      panelDiv.appendChild(headerDiv);
      this.createHeader(headerDiv);

      const bodyDiv = document.createElement("div");
      this.bodyDiv = bodyDiv;
      bodyDiv.className = "body flex-grow-1 overflow-auto body";
      panelDiv.appendChild(bodyDiv);
    }

    createHeader(headerDiv)
    {
      const titleDiv = document.createElement("div");
      titleDiv.className = "flex-grow-1";
      headerDiv.appendChild(titleDiv);
      this.createTitle(titleDiv);

      const closeButton = document.createElement("button");
      closeButton.className = "panel_button flex-grow-0";
      closeButton.innerHTML = `<span class="pi pi-times-circle"></span>`;
      headerDiv.appendChild(closeButton);

      closeButton.addEventListener("click", (event) =>
      {
        event.preventDefault();
        this.hide();
      });
    }

    createTitle(titleDiv)
    {
      titleDiv.innerHTML = `<span class="${this.iconClassName} mr-1"></span> <span>${this.title}</span>`;
    }
  }

  class Search
  {
    constructor(containerId, insertTop)
    {
      this.panel = new Panel(containerId, "Search", "pi pi-search", insertTop);
    }

    onAdd(map)
    {
      this.map = map;

      const div = document.createElement("div");
      this.div = div;
      div.innerHTML = `<button><span class="pi pi-search"/></button>`;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
      div.title = this.title;
      div.style.width = "29px";
      div.style.height = "29px";
      div.style.fontFamily = "var(--font-family)";
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      div.addEventListener("click", (e) =>
      {
        e.preventDefault();
        this.panel.show();
      });

      return div;
    }
  }

  class Info
  {
    constructor(containerId, insertTop)
    {
      this.panel = new Panel(containerId, "Information", "pi pi-info-circle", insertTop);
      this.createBody();
    }

    createBody()
    {
      const bodyDiv = this.panel.bodyDiv;
      for (let i = 0; i < 20; i++)
      {
        let div = document.createElement("div");
        div.innerHTML = `<span>bla bla bla bla bla</span>`;
        bodyDiv.appendChild(div);
      }
    }

    onAdd(map)
    {
      this.map = map;

      const div = document.createElement("div");
      this.div = div;
      div.innerHTML = `<button><span class="pi pi-info-circle"/></button>`;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
      div.title = this.title;
      div.style.width = "29px";
      div.style.height = "29px";
      div.style.fontFamily = "var(--font-family)";
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      div.addEventListener("click", (e) =>
      {
        e.preventDefault();
        this.panel.show();
      });

      return div;
    }
  }

  class Legend
  {
    constructor(containerId, insertTop)
    {
      this.panel = new Panel(containerId, "Legend", "fa fa-layer-group", insertTop);
      this.createBody();
    }

    createBody(bodyDiv)
    {
      setTimeout(() => this.populateTree(), 0);
    }

    onAdd(map)
    {
      this.map = map;

      const div = document.createElement("div");
      this.div = div;
      div.innerHTML = `<button><span class="fa fa-layer-group"/></button>`;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
      div.title = this.title;
      div.style.width = "29px";
      div.style.height = "29px";
      div.style.fontFamily = "var(--font-family)";
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      div.addEventListener("click", (e) =>
      {
        e.preventDefault();
        this.panel.show();
      });

      return div;
    }

    populateTree()
    {
      const style = this.map.getStyle();
      const legend = style?.metadata?.legend;
      const bodyDiv = this.panel.bodyDiv;
      if (legend)
      {
        this.titleDiv = legend.label || "Legend";
        if (legend.children && legend.children.length > 0)
        {
          const ul = document.createElement("ul");
          ul.className = "legend";
          bodyDiv.appendChild(ul);

          for (let childNode of legend.children)
          {
            this.populateNode(childNode, ul);
          }
          this.updateLegendStyle();
        }
      }
    }

    populateNode(node, ul)
    {
      const li = document.createElement("li");
      ul.appendChild(li);
      const liDiv = document.createElement("div");
      li.appendChild(liDiv);
      if (node.layerId) // layer node
      {
        let graphic = this.getNodeGraphic(node);
        liDiv.appendChild(graphic);
      }
      else // group node
      {
        const button = document.createElement("button");
        liDiv.appendChild(button);
        node.button = button;
        button.innerHTML = `<span class="pi pi-angle-right"></span>`;

        if (node.mode === "block")
        {
          button.style.visibility = "hidden";
        }
        else
        {
          button.addEventListener("click", (event) =>
          {
            event.preventDefault();
            node.expanded = !node.expanded;
            this.updateListVisibility(node);
          });
        }
      }
      const link = document.createElement("a");
      liDiv.appendChild(link);

      link.href = "#";
      link.innerHTML = `<span></span> <span>${node.label}</span>`;
      node.link = link;
      link.addEventListener("click", (event) =>
      {
        event.preventDefault();
        if (node.layerId) // layer node
        {
          if (node.parent?.mode === "single")
          {
            let visibleNode = null;
            for (let childNode of node.parent.children)
            {
              if (childNode.link?.firstElementChild?.className === "pi pi-eye")
              {
                visibleNode = childNode;
                break;
              }
            }
            if (visibleNode)
            {
              this.changeNodeVisibility(visibleNode, "none");
            }
            if (visibleNode !== node)
            {
              this.changeNodeVisibility(node, "visible");
            }
          }
          else
          {
            this.changeNodeVisibility(node, "toggle");
          }
        }
        else // group node
        {
          let groupVisible =
            node.link?.firstElementChild?.className === "pi pi-eye";
          if (node.mode === "single")
          {
            if (groupVisible) this.changeNodeVisibility(node, "none");
            else if (node.children.length > 0)
            {
              this.changeNodeVisibility(node.children[0], "visible");
            };
          }
          else
          {
            this.changeNodeVisibility(node, groupVisible ? "none" : "visible");
          }
        }
        this.updateLegendStyle();
      });

      if (node.children && node.children.length > 0)
      {
        const subul = document.createElement("ul");
        node.ul = subul;

        li.appendChild(subul);
        for (let childNode of node.children)
        {
          this.populateNode(childNode, subul);
          childNode.parent = node;
        }
      }
      this.updateListVisibility(node);
    }

    getNodeGraphic(node)
    {
      let graphic = node.graphic || null;
      if (graphic === null ||
          graphic.startsWith("square:") ||
          graphic.startsWith("circle:"))
      {
        const span = document.createElement("span");
        span.className = "graphic";
        span.style.display = "inline-block";
        span.style.width = "16px";
        span.style.height = "16px";
        if (graphic === null)
        {
          span.style.backgroundColor = "transparent";
        }
        else if (graphic.startsWith("square:"))
        {
          let color = graphic.substring(7);
          span.style.backgroundColor = color;
          span.classList.add("square");
        }
        else if (graphic.startsWith("circle:"))
        {
          let color = graphic.substring(7);
          span.style.backgroundColor = color;
          span.classList.add("circle");
        }
        return span;
      }
      else if (graphic.startsWith("icon:"))
      {
        let img = document.createElement("img");
        img.className = "graphic";
        img.alt = "";
        img.src = "/documents/" + graphic.substring(5);
        return img;
      }
      else if (graphic.startsWith("image:"))
      {
        let img = document.createElement("img");
        img.style.display = "block";
        img.alt = "";
        img.src = "/documents/" + graphic.substring(6);
        return img;
      }
      else if (graphic === "auto")
      {
        let img = document.createElement("img");
        img.className = "graphic";
        img.alt = "";
        return img;
      }
    }

    updateListVisibility(node)
    {
      if (node.expanded && node.mode !== "block")
      {
        if (node.ul) node.ul.style.maxHeight = (node.children.length * 30) + "px";
        if (node.button) node.button.firstElementChild.style.transform = "rotate(90deg)";
      }
      else
      {
        if (node.ul) node.ul.style.maxHeight = "0";
        if (node.button) node.button.firstElementChild.style.transform = "rotate(0)";
      }
    }

    updateLegendStyle()
    {
      const map = this.map;
      const style = map.getStyle();
      const legend = style.metadata.legend;
      if (legend)
      {
        if (legend.children && legend.children.length > 0)
        {
          for (let childNode of legend.children)
          {
            this.updateNodeStyle(childNode);
          }
        }
      }
    }

    updateNodeStyle(node)
    {
      const map = this.map;
      const link = node.link;
      const layerId = node.layerId;
      const iconSpan = link.firstElementChild;
      const textSpan = iconSpan.nextElementSibling;
      let nodeVisible = false;

      if (layerId) // layer node
      {
        const layer = map.getLayer(layerId);
        if (layer.metadata === undefined) layer.metadata = {};
        nodeVisible = layer.metadata.visible !== false;
      }
      else // group node
      {
        for (let childNode of node.children)
        {
          let childVisible = this.updateNodeStyle(childNode);
          nodeVisible = nodeVisible || childVisible;
        }
      }

      if (nodeVisible === false)
      {
        textSpan.classList.add("hidden_layer");
        iconSpan.className = "pi pi-eye-slash";
      }
      else
      {
        textSpan.classList.remove("hidden_layer");
        iconSpan.className = "pi pi-eye";
      }
      return nodeVisible;
    }

    changeNodeVisibility(node, mode = "toggle", sourceSet = null)
    {
      const map = this.map;
      const layerId = node.layerId || null;
      if (sourceSet === null) sourceSet = new Set();

      if (layerId) // layer node
      {
        const layer = map.getLayer(layerId);
        if (layer.metadata === undefined) layer.metadata = {};

        if (mode === "visible")
        {
          layer.metadata.visible = true;
        }
        else if (mode === "none")
        {
          layer.metadata.visible = false;
        }
        else // toggle
        {
          if (layer.metadata.visible === false)
          {
            layer.metadata.visible = true;
          }
          else
          {
            layer.metadata.visible = false;
          }
        }
        if (layer.metadata.layers) // serviceParameters layer
        {
          const sourceId = layer.source;
          sourceSet.add(sourceId);
        }
        else
        {
          // apply visibility immediately
          map.setLayoutProperty(layerId, "visibility",
            layer.metadata.visible ? "visible" : "none");
        }
      }
      else // group node
      {
        for (let childNode of node.children)
        {
          this.changeNodeVisibility(childNode, mode, sourceSet);
        }
      }
      for (let sourceId of sourceSet)
      {
        const source = map.getSource(sourceId);
        const style = map.getStyle();
        let serviceParameters = style.metadata.serviceParameters[sourceId];
        let masterLayerId = serviceParameters.masterLayer;

        const sourceUrl = getSourceUrl(sourceId, style);
        if (sourceUrl) source.setTiles([sourceUrl]);

        const visibility = sourceUrl ? "visible" : "none";

        setTimeout(() =>
          map.setLayoutProperty(masterLayerId, "visibility", visibility), 100);
      }
    }
  }

  class Tool
  {
    static activeTool;

    constructor(iconClass, title)
    {
      this.iconClass = iconClass;
      this.title = title;
    }

    activate()
    {
    }

    deactivate()
    {
    }

    onAdd(map)
    {
      this.map = map;

      const div = document.createElement("div");
      this.div = div;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group";
      div.innerHTML = `<button><span class="${this.iconClass}"/></button>`;
      div.title = this.title;
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      div.addEventListener("click", (e) =>
      {
        e.preventDefault();
        if (Tool.activeTool !== this)
        {
          if (Tool.activeTool)
          {
            Tool.activeTool.deactivate();
            Tool.activeTool.div.classList.remove("active");
          }
          Tool.activeTool = this;
          this.div.classList.add("active");
          this.activate();
        }
      });
      return div;
    }
  }

  class GetFeatureInfo extends Tool
  {
    constructor(containerId, insertTop)
    {
      super("fa fa-arrow-pointer", "Get feature info");
      this.panel = new Panel(containerId, "Feature info", "pi pi-info-circle", insertTop);
      this.highlightCount = 0;
      this._onMapClick = (event) => this.getFeatureInfo(event.lngLat);
      this.createPanel();
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

    createPanel()
    {
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
          let layerId = layerData.layerId;
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
        const map = this.map;
        const dist = 20 / map.getZoom();

        if (info.geometryColumn)
        {
          let url = "/proxy?url=" + service.url + "&" +
            "request=GetFeature" +
            "&service=WFS" +
            "&version=2.0.0" +
            "&typeNames=" + layerName +
            "&srsName=EPSG:4326" +
            "&outputFormat=application/json" +
            "&cql_filter=" + "DISTANCE(" + info.geometryColumn +
              ",SRID=4326;POINT(" + lngLat.lng + " " + lngLat.lat + "))<" + dist;


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

  class MeasureLength extends Tool
  {
    constructor(containerId, insertTop)
    {
      super("fa fa-ruler-horizontal", "Measure length");
      this.panel = new Panel(containerId, "Measure length", "pi pi-info-circle", insertTop);
      this.createPanel();
      this._onMapClick = (event) => this.addPoint(event.lngLat);
      this.startData = {
        "type": "Feature",
        "geometry": { "type": "Point", "coordinates": [] }
      };
      this.data = {
        "type": "Feature",
        "geometry": { "type": "LineString", "coordinates": [] }
      };
    }

    activate()
    {
      const map = this.map;
      map.on("click", this._onMapClick);
      map.getCanvas().style.cursor = "crosshair";
      this.panel.show();

      map.addSource("measured_linestring_start", {
        type: 'geojson',
        data: this.startData
      });

      map.addLayer({
        "id": "measured_linestring_start",
        "type": 'circle',
        "source": "measured_linestring_start",
        "layout": {},
        "paint":
        {
          "circle-color": "#000000",
          "circle-radius": 4
        }
      });

      map.addSource("measured_linestring", {
        type: 'geojson',
        data: this.data
      });

      map.addLayer({
        "id": "measured_linestring",
        "type": 'line',
        "source": "measured_linestring",
        "layout": {},
        "paint":
        {
          "line-color": "#0000ff",
          "line-width": 4,
          "line-opacity": 0.5
        }
      });

      map.addLayer({
        "id": "measured_linestring_points",
        "type": 'circle',
        "source": "measured_linestring",
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

      map.removeLayer("measured_linestring_start");
      map.removeSource("measured_linestring_start");

      map.removeLayer("measured_linestring");
      map.removeLayer("measured_linestring_points");
      map.removeSource("measured_linestring");
    }

    addPoint(lngLat)
    {
      const map = this.map;

      if (this.startData.geometry.coordinates.length === 0)
      {
        this.startData.geometry.coordinates = [lngLat.lng, lngLat.lat];
        map.getSource("measured_linestring_start").setData(this.startData);
      }

      this.data.geometry.coordinates.push([lngLat.lng, lngLat.lat]);
      map.getSource("measured_linestring").setData(this.data);

      this.updateLength();

      this.panel.show();
    }

    updateLength()
    {
      let distance = 0;
      for (let i = 0; i < this.data.geometry.coordinates.length - 1; i++)
      {
        let c1 = this.data.geometry.coordinates[i];
        let c2 = this.data.geometry.coordinates[i + 1];

        let lngLat1 =  new maplibregl.LngLat(c1[0], c1[1]);
        let lngLat2 =  new maplibregl.LngLat(c2[0], c2[1]);
        distance += lngLat1.distanceTo(lngLat2);
      }
      this.resultDiv.textContent = "Length: " + distance.toFixed(3) + " m";
      this.resultDiv.className = "p-4";
    }

    createPanel()
    {
      const bodyDiv = this.panel.bodyDiv;
      const buttonBar = document.createElement("div");
      buttonBar.className = "button_bar";
      bodyDiv.appendChild(buttonBar);

      const clearButton = document.createElement("button");
      clearButton.textContent = "Reset";
      clearButton.addEventListener("click", (e) => {
        e.preventDefault();
        this.startData.geometry.coordinates = [];
        map.getSource("measured_linestring_start").setData(this.startData);

        this.data.geometry.coordinates = [];
        map.getSource("measured_linestring").setData(this.data);

        this.updateLength();
      });
      buttonBar.appendChild(clearButton);

      const undoButton = document.createElement("button");
      undoButton.textContent = "Undo";
      undoButton.addEventListener("click", (e) => {
        e.preventDefault();
        if (this.data.geometry.coordinates.length > 0)
        {
          this.data.geometry.coordinates.pop();
          map.getSource("measured_linestring").setData(this.data);

          if (this.data.geometry.coordinates.length === 0)
          {
            this.startData.geometry.coordinates = [];
            map.getSource("measured_linestring_start").setData(this.startData);
          }
          this.updateLength();
        }
      });
      buttonBar.appendChild(undoButton);

      this.resultDiv = document.createElement("div");
      bodyDiv.appendChild(this.resultDiv);
    }
  }

  class MeasureArea extends Tool
  {
    constructor(containerId, insertTop)
    {
      super("fa fa-ruler-combined", "Measure area");
      this.panel = new Panel(containerId, "Measure area", "pi pi-info-circle", insertTop);
      this.createPanel();
      this._onMapClick = (event) => this.addPoint(event.lngLat);
      this.startData = {
        "type": "Feature",
        "geometry": { "type": "Point", "coordinates": [] }
      };
      this.data = {
        "type": "Feature",
        "geometry": { "type": "Polygon", "coordinates": [[]] }
      };
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

      this.updateArea();

      this.panel.show();
    }

    updateArea()
    {
      let area = 0;
      const coordinates = this.data.geometry.coordinates[0];
      for (let i = 0; i < coordinates.length; i++)
      {
        let c1 = coordinates[i];
        let c2 = coordinates[(i + 1) % coordinates.length];

        let utm1 = toUtm(c1[1], c1[0], 7, 'ETRS89');
        let utm2 = toUtm(c2[1], c2[0], 7, 'ETRS89');

        let x1 = utm1.easting;
        let y1 = utm1.northing;

        let x2 = utm2.easting;
        let y2 = utm2.northing;

        area += (0.5 * (y1 + y2) * (x2 - x1));
      }

      area = Math.abs(area);

      this.resultDiv.textContent = "Area: " + area.toFixed(3) + " m2";
      this.resultDiv.className = "p-4";
    }

    createPanel()
    {
      const bodyDiv = this.panel.bodyDiv;
      const buttonBar = document.createElement("div");
      buttonBar.className = "button_bar";
      bodyDiv.appendChild(buttonBar);

      const clearButton = document.createElement("button");
      clearButton.textContent = "Reset";
      clearButton.addEventListener("click", (e) => {
        e.preventDefault();
        this.startData.geometry.coordinates = [];
        map.getSource("measured_polygon_start").setData(this.startData);

        this.data.geometry.coordinates[0] = [];
        map.getSource("measured_polygon").setData(this.data);

        this.updateArea();
      });
      buttonBar.appendChild(clearButton);

      const undoButton = document.createElement("button");
      undoButton.textContent = "Undo";
      undoButton.addEventListener("click", (e) => {
        e.preventDefault();
        if (this.data.geometry.coordinates[0].length > 0)
        {
          this.data.geometry.coordinates[0].pop();
          map.getSource("measured_polygon").setData(this.data);

          if (this.data.geometry.coordinates[0].length === 0)
          {
            this.startData.geometry.coordinates = [];
            map.getSource("measured_polygon_start").setData(this.startData);
          }
          this.updateArea();
        }
      });
      buttonBar.appendChild(undoButton);

      this.resultDiv = document.createElement("div");
      bodyDiv.appendChild(this.resultDiv);
    }
  }

  class ExportArea extends Tool
  {
    constructor(containerId, insertTop)
    {
      super("pi pi-download", "Export area");
      this.panel = new Panel(containerId, "Export area", "pi pi-info-circle", insertTop);
      this.createPanel();
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

    createPanel()
    {
      const bodyDiv = this.panel.bodyDiv;
      const buttonBar = document.createElement("div");
      buttonBar.className = "button_bar";
      bodyDiv.appendChild(buttonBar);

      const clearButton = document.createElement("button");
      clearButton.textContent = "Reset";
      clearButton.addEventListener("click", (e) => {
        e.preventDefault();
        this.startData.geometry.coordinates = [];
        map.getSource("measured_polygon_start").setData(this.startData);

        this.data.geometry.coordinates[0] = [];
        map.getSource("measured_polygon").setData(this.data);
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
          map.getSource("measured_polygon").setData(this.data);

          if (this.data.geometry.coordinates[0].length === 0)
          {
            this.startData.geometry.coordinates = [];
            map.getSource("measured_polygon_start").setData(this.startData);
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

  window.maplibreglx = {};
  maplibreglx.HomeButton = HomeButton;
  maplibreglx.LoadingIndicator = LoadingIndicator;
  maplibreglx.Search = Search;
  maplibreglx.Info = Info;
  maplibreglx.Legend = Legend;
  maplibreglx.GetFeatureInfo = GetFeatureInfo;
  maplibreglx.MeasureLength = MeasureLength;
  maplibreglx.MeasureArea = MeasureArea;
  maplibreglx.ExportArea = ExportArea;

  window.mapLibreControlsLoaded = true;
}

function extendArray(array, length)
{
  while (array.length < length) array.push("");
}

function getSourceUrl(sourceId, style)
{
  if (style.metadata.services === undefined) return null;
  if (style.metadata.serviceParameters === undefined) return null;

  let source = style.sources[sourceId];
  if (source === undefined) return null;

  let serviceParameters = style.metadata.serviceParameters[sourceId];
  if (serviceParameters === undefined) return null;

  let service = style.metadata.services[serviceParameters.service];
  if (service === undefined) return null;

  let layersArray = [];
  let stylesArray = [];
  let cqlFilterArray = [];
  if (serviceParameters.layers?.length > 0)
  {
    layersArray.push(...serviceParameters.layers.split(","));
  }
  if (serviceParameters.styles?.length > 0)
  {
    layersArray.push(...serviceParameters.styles.split(","));
  }
  if (serviceParameters.cqlFilter?.length > 0)
  {
    layersArray.push(...serviceParameters.cqlFilter.split(";"));
  }
  extendArray(stylesArray, layersArray.length);
  extendArray(cqlFilterArray, layersArray.length);

  for (let layer of style.layers)
  {
    if (layer.source === sourceId && layer.metadata)
    {
      if (serviceParameters.masterLayer === undefined)
      {
        serviceParameters.masterLayer = layer.id;
      }

      if (layer.metadata.visible)
      {
        if (layer.metadata.layers?.length > 0)
        {
          layersArray.push(...layer.metadata.layers.split(","));
        }
        if (layer.metadata.styles?.length > 0)
        {
          stylesArray.push(...layer.metadata.styles.split(","));
        }
        if (layer.metadata.cqlFilter?.length > 0)
        {
          cqlFilterArray.push(...layer.metadata.cqlFilter.split(";"));
        }
        extendArray(stylesArray, layersArray.length);
        extendArray(cqlFilterArray, layersArray.length);
      }
    }
  }

  if (layersArray.length === 0) return null;

  if (stylesArray.join("").trim().length === 0) stylesArray = [];
  if (cqlFilterArray.join("").trim().length === 0) cqlFilterArray = [];

  let serviceUrl = service.url;
  let urlParams;
  if (service.type === "wms")
  {
    let tileSize = source.tileSize || 256;
    urlParams = "SERVICE=WMS" +
    "&VERSION=1.1.1" +
    "&REQUEST=GetMap" +
    "&LAYERS=" + layersArray.join(",") +
    "&FORMAT=" + serviceParameters.format +
    "&TRANSPARENT=" + (serviceParameters.transparent ? "TRUE" : "FALSE") +
    "&TILES=true" +
    "&STYLES=" + stylesArray.join(",") +
    "&srs=EPSG:3857" +
    "&BBOX={bbox-epsg-3857}" +
    "&WIDTH=" + tileSize +
    "&HEIGHT=" + tileSize;
    if (serviceParameters.buffer !== undefined)
    {
      urlParams += "&BUFFER=" + serviceParameters.buffer;
    }
    if (cqlFilterArray.length > 0)
    {
      urlParams += "&cql_filter=" + cqlFilterArray.join(";");
    }
    if (serviceParameters.sldUrl)
    {
      urlParams += "&SLD=" + serviceParameters.sldUrl;
    }
  }
  else if (service.type === "wfs")
  {
    urlParams = "SERVICE=WFS" +
    "&VERSION=1.0.0" +
    "&REQUEST=GetFeature" +
    "&typeName=" + layersArray.join(",") +
    "&outputFormat=" + serviceParameters.format +
    "&srsName=EPSG:4326";
    if (cqlFilterArray.length > 0)
    {
      urlParams += "&cql_filter=" + cqlFilterArray.join(";");
    }
  }
  else return null;

  let url;
  if (service.useProxy)
  {
    url = "https://" + document.location.host + "/proxy?url=" +
          serviceUrl + "&" + urlParams;
  }
  else
  {
    url = serviceUrl + "?" + urlParams;
  }
  return url;
}

function initSources(style)
{
  for (let sourceId in style.sources)
  {
    let source = style.sources[sourceId];
    let url = getSourceUrl(sourceId, style);
    if (source.type === "raster")
    {
      if (url)
      {
        source.tiles = [url];
      }
      if (source.tileSize === undefined)
      {
        source.tileSize = 256;
      }
    }
    else if (source.type === "geojson")
    {
      if (url)
      {
        source.data = url;
      }
      delete source.tiles;
      delete source.bounds;
    }
  }
}

function initLayers(style)
{
  if (style.metadata.serviceParameters === undefined) return;

  for (let layer of style.layers)
  {
    if (layer.type === "raster")
    {
      let sourceId = layer.source;
      let serviceParameters = style.metadata.serviceParameters[sourceId];
      if (serviceParameters && serviceParameters.service)
      {
        if (layer.id === serviceParameters.masterLayer)
        {
          // master layer
          let visible = style.layers.some(
            l => l.source === sourceId && l.metadata?.visible !== false);
          layer.layout.visibility = visible ? "visible" : "none";
          layer.metadata.virtual = false;
        }
        else
        {
          // virtual layer
          layer.layout.visibility = "none";
          layer.metadata.virtual = true;
        }
      }

      let source = style.sources[sourceId];
      if (source?.tiles?.length === 0)
      {
        layer.layout.visibility = "none";
      }
    }
    else // not raster layer
    {
      if (layer.metadata?.visible === false)
      {
        layer.layout.visibility = "none";
      }
    }
  }
}

function maplibreInit(clientId, style)
{
  if (window.rtlPluginLoaded === undefined)
  {
    maplibregl.setRTLTextPlugin(
      'https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js'
    );
    window.rtlPluginLoaded = true;
  }

  initSources(style);
  initLayers(style);

  console.info("Map.style", style);

  const map = new maplibregl.Map({
    container: clientId,
    center: style.center,
    zoom: style.zoom,
    bearing: style.bearing,
    pitch: style.pitch,
    maxZoom: style.metadata.maxZoom,
    maxPitch: style.metadata.maxPitch,
    hash: style.metadata.hash || false,
    style: style,
    antialias: true
  });

  window.map = map;
  map.setPixelRatio(window.devicePixelRatio);

  map.addControl(new maplibregl.NavigationControl({
      visualizePitch: true,
      showZoom: true,
      showCompass: true
    })
  );

  map.addControl(new maplibregl.GeolocateControl({
    positionOptions: {
      enableHighAccuracy: true
    },
    trackUserLocation: true
  }));

  map.addControl(new maplibregl.ScaleControl({
    maxWidth: 80,
    unit: 'metric'
  }));

  map.addControl(new maplibreglx.HomeButton({
    center: style.center,
    zoom: style.zoom,
    bearing: style.bearing,
    pitch: style.pitch,
    speed: 1,
    curve: 1,
    easing(t) { return t; }
  }), "top-right");

  map.addControl(new maplibreglx.LoadingIndicator(), "top-right");

  map.addControl(new maplibreglx.Search("maplibre_left_container"), "top-left");

  map.addControl(new maplibreglx.GetFeatureInfo("maplibre_right_container", true), "top-left");

  map.addControl(new maplibreglx.MeasureLength("maplibre_right_container", true), "top-left");

  map.addControl(new maplibreglx.MeasureArea("maplibre_right_container", true), "top-left");

  map.addControl(new maplibreglx.ExportArea("maplibre_right_container", true), "top-left");

  map.addControl(new maplibreglx.Legend("maplibre_right_container", true), "bottom-right");

  map.addControl(new maplibreglx.Info("maplibre_right_container", true), "bottom-right");

  if (style.terrain)
  {
    const terrain = style.terrain;
    map.addControl(
      new maplibregl.TerrainControl({
        source: terrain.source,
        exaggeration: terrain.exaggeration
      })
    );
  }
}