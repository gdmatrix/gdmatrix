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

function getSourceUrl(source, services)
{
  let serviceParameters = source.serviceParameters;
  if (serviceParameters === undefined) return null;

  let service = services[serviceParameters.service];
  if (service === undefined) return null;

  let serviceUrl = service.url;
  let urlParams;
  if (service.type === "wms")
  {
    let tileSize = source.tileSize || 256;
    urlParams = "SERVICE=WMS" +
    "&VERSION=1.1.1" +
    "&REQUEST=GetMap" +
    "&LAYERS=" + serviceParameters.layer +
    "&FORMAT=" + serviceParameters.format +
    "&TRANSPARENT=" + (serviceParameters.transparent ? "TRUE" : "FALSE") +
    "&TILES=true" +
    "&STYLES=" + (serviceParameters.styles ? serviceParameters.styles : "") +
    "&srs=EPSG:3857" +
    "&BBOX={bbox-epsg-3857}" +
    "&WIDTH=" + tileSize +
    "&HEIGHT=" + tileSize;
    if (serviceParameters.buffer !== undefined)
    {
      urlParams += "&BUFFER=" + serviceParameters.buffer;
    }
    if (serviceParameters.cqlFilter)
    {
      urlParams += "&CQL_FILTER=" + serviceParameters.cqlFilter;
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
    "&typeName=" + serviceParameters.layer +
    "&outputFormat=" + serviceParameters.format +
    "&srsName=EPSG:4326";
    if (serviceParameters.cqlFilter)
    {
      urlParams += "&cql_filter=" + serviceParameters.cqlFilter;
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

function maplibreInit(clientId, config)
{
  if (window.rtlPluginLoaded === undefined)
  {
    maplibregl.setRTLTextPlugin(
      'https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js'
    );
    window.rtlPluginLoaded = true;
  }

  const camera = config.camera;
  delete config.camera;

  const mapConfig = {
    container: clientId,
    center: camera.center,
    zoom: camera.zoom,
    bearing: camera.bearing,
    pitch: camera.pitch,
    hash: camera.hash,
    maxZoom: camera.maxZoom,
    maxPitch: camera.maxPitch,
    style: {
      version: 8,
      "glyphs": "https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf",
      sources: {},
      layers: []
    }
  };


  // process sources
  for (let sourceName in config.sources)
  {
    let source = config.sources[sourceName];
    if (source.type === "raster" || source.type === "raster-dem")
    {
      let url = getSourceUrl(source, config.services);
      if (url)
      {
        source.tiles.push(url);
        source.tileSize = source.tileSize || 256;
        delete source.serviceParameters;
        if (source.type === "raster-dem")
        {
          delete source.bounds;
        }
        mapConfig.style.sources[sourceName] = source;
      }
      else if (source.tiles && source.tiles.length > 0)
      {
        mapConfig.style.sources[sourceName] = source;
      }
      else if (source.url)
      {
        mapConfig.style.sources[sourceName] = source;
      }
    }
    else if (source.type === "vector")
    {
    }
    else if (source.type === "geojson")
    {
      let url = getSourceUrl(source, config.services);
      let geojsonSource = {
        type: "geojson"
      };
      if (url)
      {
        geojsonSource.data = url;
      }
      if (geojsonSource.data)
      {
        mapConfig.style.sources[sourceName] = geojsonSource;
      }
    }
  }

  if (config.terrain && config.terrain.source)
  {
    mapConfig.style.terrain = config.terrain;
  }

  // process layers
  for (let layer of config.layers)
  {
    if (layer.visible)
    {
      mapConfig.style.layers.push(layer);
    }
  }

  console.info("MapConfig", mapConfig);

  const map = new maplibregl.Map(mapConfig);

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
    center: camera.center,
    zoom: camera.zoom,
    bearing: camera.bearing,
    pitch: camera.pitch,
    speed: 1,
    curve: 1,
    easing(t) { return t; }
  }), "top-right");

  map.addControl(new LoadingIndicator(), "top-right");

  if (mapConfig.style.terrain)
  {
    const terrain = mapConfig.style.terrain;
    map.addControl(
      new maplibregl.TerrainControl({
        source: terrain.source,
        exaggeration: terrain.exaggeration
      })
    );
  }
}