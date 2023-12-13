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
    }

    show()
    {
      this.panelDiv.style.display = "";
      this.container.classList.add("expanded");
      this.panelDiv.scrollIntoView({ behavior : "smooth" });
    }

    hide()
    {
      this.panelDiv.style.display = "none";
      let panels = this.container.getElementsByClassName("panel");
      let visiblePanel = false;
      for (let panel of panels)
      {
        if (panel.style.display !== "none")
        {
          visiblePanel = true;
          break;
        }
      }
      if (!visiblePanel)
      {
        this.container.classList.remove("expanded");
      }
    }

    onAdd(map)
    {
      this.map = map;
      this.createPanel();

      const div = document.createElement("div");
      this.div = div;
      div.innerHTML = `<button><span class="${this.iconClassName}"/></button>`;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
      div.title = this.title;
      div.style.width = "29px";
      div.style.height = "29px";
      div.style.fontFamily = "var(--font-family)";
      div.addEventListener("contextmenu", (e) => e.preventDefault());
      div.addEventListener("click", (e) =>
      {
        e.preventDefault();
        this.show();
      });
      
      return div;
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
      this.createBody(bodyDiv);
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

    createBody(bodyDiv)
    {
    }

    createTitle(titleDiv)
    {
      titleDiv.innerHTML = `<span class="${this.iconClassName} mr-1"></span> <span>${this.title}</span>`;
    }
  }

  class SearchPanel extends Panel
  {
    constructor(containerId, insertTop)
    {
      super(containerId, "Search", "pi pi-search", insertTop);
    }
  }
  
  class InfoPanel extends Panel
  {
    constructor(containerId, insertTop)
    {
      super(containerId, "Information", "pi pi-info-circle", insertTop);
    }

    createBody(bodyDiv)
    {
      for (let i = 0; i < 20; i++)
      {
        let div = document.createElement("div");
        div.innerHTML = `<span>bla bla bla bla bla</span>`;
        bodyDiv.appendChild(div);
      }
    }  
  }

  class LegendPanel extends Panel
  {
    constructor(containerId, insertTop)
    {
      super(containerId, "Legend", "fa fa-layer-group", insertTop);
    }
    
    createBody(bodyDiv)
    {
      setTimeout(() => this.populateTree(), 0);
    }
    
    populateTree()
    {
      const style = this.map.getStyle();
      const legend = style.metadata.legend;
      if (legend)
      {
        this.titleDiv = legend.label;
        if (legend.children && legend.children.length > 0)
        {
          const ul = document.createElement("ul");
          ul.className = "legend";
          this.bodyDiv.appendChild(ul);

          for (let childNode of legend.children)
          {
            this.populateNode(childNode, ul);
            this.updateLinks(childNode);
          }
        }       
      }
    }
    
    populateNode(node, ul)
    {
      const li = document.createElement("li");
      ul.appendChild(li);
      const liDiv = document.createElement("div");
      liDiv.innerHTML = `<a href="#"><span></span> <span>${node.label}</span></a>`;
      const link = liDiv.firstElementChild;
      node.link = link;
      link.addEventListener("click", (event) => 
      {
        event.preventDefault();
        this.toggleNodeVisibility(node);
        this.updateLinks(node);
      });
      li.appendChild(liDiv);
      
      if (node.children && node.children.length > 0)
      {
        const subul = document.createElement("ul");
        li.appendChild(subul);
        for (let childNode of node.children)
        {
          this.populateNode(childNode, subul);  
        }
      }      
    }
    
    updateLinks(node)
    {
      const link = node.link;
      const layerId = node.layerId;
      
      if (layerId)
      {
        const iconSpan = link.firstElementChild;
        const textSpan = iconSpan.nextElementSibling;

        const layer = this.map.getLayer(layerId);
        if (layer?.metadata?.visible === false)
        {
          textSpan.classList.add("hidden_layer");
          iconSpan.className = "pi pi-eye-slash";
        }
        else
        {
          textSpan.classList.remove("hidden_layer");
          iconSpan.className = "pi pi-eye";        
        }
      }
      else // group
      {
        for (let childNode of node.children)
        {
          this.updateLinks(childNode);
        }        
      }
    }
    
    toggleNodeVisibility(node, sourceSet = null)
    {
      const map = this.map;
      const layerId = node.layerId || null;
      if (sourceSet === null) sourceSet = new Set();
      
      if (layerId)
      {
        const layer = map.getLayer(layerId);
        const sourceId = layer.source;
        console.info("tooggle layer: " + layerId + " source:" + sourceId);

        if (layer.metadata?.visible === false)
        {
          layer.metadata.visible = true;
        }
        else
        {
          layer.metadata.visible = false;        
        }

        if (layer?.metadata?.layers)
        {
          sourceSet.add(sourceId);
        }
        else
        {
          layer.layout.visibility = layer.metadata.visible ? "visible" : "none";
        }
      }
      else // group node
      {
        for (let childNode of node.children)
        {
          this.toggleNodeVisibility(childNode, sourceSet);
        }
      }
      for (let sourceId of sourceSet)
      {
        let source = map.getSource(sourceId);
        let serviceParameters = map.getStyle().metadata.serviceParameters[sourceId];
        let masterLayer = map.getLayer(serviceParameters.masterLayer);
        if (masterLayer.layout === undefined) masterLayer.layout = {};

        const sourceUrl = getSourceUrl(sourceId, map.getStyle());
        if (sourceUrl === null)
        {
          masterLayer.layout.visibility = "none";          
        }
        else
        {
          source.setTiles([sourceUrl]);
          masterLayer.layout.visibility = "visible";
        }        
      }
    }
  }

  window.maplibreglx = {};
  maplibreglx.HomeButton = HomeButton;
  maplibreglx.LoadingIndicator = LoadingIndicator;
  maplibreglx.SearchPanel = SearchPanel;
  maplibreglx.InfoPanel = InfoPanel;
  maplibreglx.LegendPanel = LegendPanel;
  window.mapLibreControlsLoaded = true;
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

  const extendArray = (array, length) =>
  {
    while (array.length < length) array.push("");
  };

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
    if (url !== null)
    {
      source.tiles = [url];
    }
    if (source.type === "raster" && source.tileSize === undefined)
    {
      source.tileSize = 256;
    }
  }
}

function initLayers(style)
{
  if (style.metadata.services === undefined) return;
  if (style.metadata.serviceParameters === undefined) return;

  for (let layer of style.layers)
  {
    let sourceId = layer.source;
    let serviceParameters = style.metadata.serviceParameters[sourceId];
    if (serviceParameters && serviceParameters.service)
    {
      let layerCount = serviceParameters.layerCount || 0;
      layer.layout.visibility = layerCount === 0 ? "visible" : "none";
      layer.metadata.virtual = layerCount > 0;
      serviceParameters.layerCount = layerCount + 1;

      let source = style.sources[sourceId];
      if (source.tiles.length === 0)
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

  map.addControl(new maplibreglx.SearchPanel("maplibre_left_container"), "top-left");

  map.addControl(new maplibreglx.InfoPanel("maplibre_left_container"), "top-left");

  map.addControl(new maplibreglx.LegendPanel("maplibre_right_container", true), "bottom-right");

  map.addControl(new maplibreglx.InfoPanel("maplibre_right_container", true), "bottom-right");

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