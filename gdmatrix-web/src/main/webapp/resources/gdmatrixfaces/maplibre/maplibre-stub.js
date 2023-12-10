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
      this.pendingCount = 0;
    }

    loadingStarted(event)
    {
      if (event?.tile?.tileID?.canonical?.key)
      {
        this.pendingCount++;
        this.updateProgress();
      }
    }

    loadingCompleted(event)
    {
      if (event?.tile?.tileID?.canonical?.key)
      {
        this.pendingCount--;
        this.updateProgress();
      }
    }

    updateProgress()
    {
      this.div.textContent = this.pendingCount;
      if (this.pendingCount > 0)
      {
        this.div.classList.add("flash");
      }
      else
      {
        this.div.classList.remove("flash");
      }
    }

    onAdd(map)
    {
      map.on("dataloading", (e) => this.loadingStarted(e));
      map.on("data", (e) => this.loadingCompleted(e));
      map.on("dataabort", (e) => this.loadingCompleted(e));

      const div = document.createElement("div");
      this.div = div;
      div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
      div.style.width = "29px";
      div.style.height = "29px";
      div.style.fontFamily = "var(--font-family)";
      return div;
    }
  }

  window.HomeButton = HomeButton;
  window.LoadingIndicator = LoadingIndicator;
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
  console.info(url);
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
    style: style
  });
  window.map = map;

  map.addControl(
    new maplibregl.NavigationControl({
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

  map.addControl(new HomeButton({
    center: style.center,
    zoom: style.zoom,
    bearing: style.bearing,
    pitch: style.pitch,
    speed: 1,
    curve: 1,
    easing(t) { return t; }
  }), "top-right");

  map.addControl(new LoadingIndicator(), "top-right");

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