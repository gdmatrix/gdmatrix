/* maplibre-stub.js */

MAPLIBRE_BASE_PATH = "/resources/gdmatrixfaces/maplibre/";
MAPLIBRE_PROFILES_PATH = MAPLIBRE_BASE_PATH + "profiles/";
MAPLIBRE_DEFAULT_PROFILE = "advanced";
MAPLIBRE_SCRIPTS_PATH = "/scripts/";

function maplibreInit(clientId, style, language)
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
    canvasContextAttributes: { 
      antialias: true,
      powerPreference: 'high-performance', 
      preserveDrawingBuffer: true
    },
    attributionControl: false
  });

  const _onRender = () => {
    map.resize();
    map.off("render", _onRender);
  };
  map.on("render", _onRender);
  
  const placeHolderImage = createPlaceHolderImage();
  
  map.on("styleimagemissing", (e) => {
    const imageId = e.id;

    map.addImage(imageId, placeHolderImage);
    
    loadActualImage(map, imageId);
  });
  
  let firstRender = true;
  
  map.on("styledata", (e) => {
    if (firstRender)
    {
      firstRender = false;
      const projectionType = style.metadata.projectionType || "mercator";
      map.setProjection({ type : projectionType });
      const background = style.metadata.backgroundStyle;
      if (background)
      {
        map.getContainer().style.background = background;
      }
    }
  });
    
  map.setPixelRatio(window.devicePixelRatio);
  map.getContainer().style.touchAction = "none";

  importControls(map, style, language);
  
  window.map = map;
}

function createPlaceHolderImage()
{
  const width = 2;
  const bytesPerPixel = 4;
  const data = new Uint8Array(width * width * bytesPerPixel);

  for (let x = 0; x < width; x++) 
  {
    for (let y = 0; y < width; y++) 
    {
      const offset = (y * width + x) * bytesPerPixel;
      data[offset + 0] = 0; // red
      data[offset + 1] = 0; // green
      data[offset + 2] = 0; // blue
      data[offset + 3] = 0; // alpha
    }
  }
  return { width, height: width, data };
}

async function loadActualImage(map, imageId)
{
  let url;
  let imageWidth = 32;
  let imageHeight = 32;

  if (imageId.indexOf("doc:") === 0)
  {
    // format: doc:<docId> || doc:<docId>/size || doc:<docId>/width/height
    // Examples: doc:129634, doc:13243/16, doc:2344245/32/64

    let docId = imageId.substring(4);
    let parts = docId.split("/");
    docId = parts[0];
    if (parts.length >= 2)
    {
      imageWidth = parseInt(parts[1]);
      if (parts.length >= 3)
      {
        imageHeight = parseInt(parts[2]);
      }
      else
      {
        imageHeight = imageWidth;
      }
    }
    url = "/documents/" + docId;
  }
  else
  {
    url = imageId;
  }
  
  const image = new Image(imageWidth, imageHeight);
  image.src = url;
  image.onload = () => {
    map.removeImage(imageId);
    map.addImage(imageId, image);
  };
}

async function importControls(map, style, language)
{
  const profile = style.metadata?.profile || MAPLIBRE_DEFAULT_PROFILE;

  const modulePaths = [MAPLIBRE_PROFILES_PATH + profile + ".js"];
  const scripts = style.metadata.scripts;
  if (scripts instanceof Array)
  {
    for (let scriptName of style.metadata.scripts)
    {
      modulePaths.push(MAPLIBRE_SCRIPTS_PATH + scriptName);
    }
  }

  const modules = [];

  for (let modulePath of modulePaths)
  {
    console.info(`importing module ${modulePath}`);
    modules.push(import(modulePath));
  }

  const loadedModules = await Promise.all(modules);

  const { Bundle } = await import(MAPLIBRE_BASE_PATH + "i18n/Bundle.js");
  Bundle.userLanguage = language;
  await Bundle.localizeBundles();

  console.info("init modules");
  for (let loadedModule of loadedModules)
  {
    if (loadedModule.init)
    {
      loadedModule.init(map);
    }
  }

  if (map.panelManager)
  {
    console.info("organize panels");
    map.panelManager.organizePanels();
  }
}

function initSources(style)
{
  duplicateSources(style);

  for (let sourceId in style.sources)
  {
    let source = style.sources[sourceId];
    let url = getSourceUrl(sourceId, style);
    if (source.type === "vector")
    {
      if (source.url)
      {
        delete source.tiles;
      }
      else if (url)
      {
        source.tiles = [url];
      }
    }
    else if (source.type === "raster" || source.type === "raster-dem")
    {
      if (source.url)
      {
        delete source.tiles;
      }
      else if (url)
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
  for (let layer of style.layers)
  {
    if (layer.type === "raster")
    {
      let sourceId = layer.source;
      let source = style.sources[sourceId];
      if (source?.tiles?.length === 0)
      {
        layer.layout.visibility = "none";
        continue;
      }
      else if (style.metadata?.serviceParameters)
      {
        let serviceParameters = style.metadata.serviceParameters[sourceId];
        if (serviceParameters && serviceParameters.service)
        {
          if (layer.id === serviceParameters.masterLayer)
          {
            // master layer
            let visible = style.layers.some(
              l => l.source === sourceId && l.metadata?.visible !== false);
            layer.layout.visibility = visible ? "visible" : "none";
          }
          else
          {
            // virtual layer
            layer.layout.visibility = "none";
          }
          continue;
        }
      }
    }

    // general case
    if (layer.metadata?.visible === false)
    {
      layer.layout.visibility = "none";
    }
  }
}

function duplicateSources(style)
{
  if (style.metadata.services === undefined) return;
  if (style.metadata.serviceParameters === undefined) return;

  let prevSourceId = null;
  const lastSourceIds = {};

  for (let layer of style.layers)
  {
    let sourceId = layer.source;

    let serviceParameters = style.metadata.serviceParameters[sourceId];
    if (serviceParameters && layer.type === "raster")
    {
      if (serviceParameters.masterLayer === undefined)
      {
        serviceParameters.masterLayer = layer.id;
        prevSourceId = sourceId;
      }
      else
      {
        let lastSourceId = lastSourceIds[sourceId] || sourceId;
        if (lastSourceId !== prevSourceId) // create new source
        {
          let parts = lastSourceId.split("$");
          if (parts.length === 2)
          {
            lastSourceId = sourceId + "$" + (parseInt(parts[1]) + 1);
          }
          else
          {
            lastSourceId = lastSourceId + "$2";
          }
          style.sources[lastSourceId] = {...style.sources[sourceId]};
          style.metadata.serviceParameters[lastSourceId] = {...serviceParameters};
          style.metadata.serviceParameters[lastSourceId].masterLayer = layer.id;
          lastSourceIds[sourceId] = lastSourceId;
          prevSourceId = lastSourceId;
        }
        layer.source = lastSourceId;
      }
    }
    else
    {
      prevSourceId = sourceId;
    }
  }
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
    stylesArray.push(...serviceParameters.styles.split(","));
  }
  if (serviceParameters.cqlFilter?.length > 0)
  {
    cqlFilterArray.push(...serviceParameters.cqlFilter.split(";"));
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

  if (cqlFilterArray.length > 0)
  {
    cqlFilterArray.forEach((cqlFilter, index) => 
    {
      if (cqlFilter === "")
      {
        cqlFilterArray[index] = "1=1";
      }
    });
  }

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
      let cqlFilter = cqlFilterArray.join(";");
      urlParams += "&cql_filter=" + cqlFilter;
    }
    if (serviceParameters.sldUrl)
    {
      urlParams += "&SLD=" + serviceParameters.sldUrl;
    }
  }
  else if (service.type === "wfs")
  {
    urlParams = "service=WFS" +
    "&VERSION=1.0.0" +
    "&REQUEST=GetFeature" +
    "&typeName=" + layersArray.join(",") +
    "&outputFormat=" + serviceParameters.format +
    "&srsName=EPSG:4326";
    if (cqlFilterArray.length > 0)
    {
      let cqlFilter = cqlFilterArray.join(";");
      urlParams += "&cql_filter=" + cqlFilter;
    }
  }
  else if (service.type === "wmts")
  {
    let tileSize = source.tileSize || 256;
    urlParams = "SERVICE=WMTS" +
    "&REQUEST=GetTile" +
    "&VERSION=1.0.0" +
    "&HEIGHT=" + tileSize +
    "&WIDTH=" + tileSize +
    "&FORMAT=" + serviceParameters.format +
    "&STYLES=" + stylesArray.join(",") +
    "&LAYER=" + layersArray.join(",") +
    "&tileMatrixSet=EPSG:3587" +
    "&tileMatrix={z}" +
    "&tileRow={y}" +
    "&tileCol={x}";
  }
  else return null;

  let url;
  if (service.useProxy)
  {
    url = "https://" + document.location.host + "/proxy?url=" +
          serviceUrl + "&" + urlParams;
    if (service.type !== "wmts")
    {
      // enable cache if service type is not wmts
      url += "&_CHG=" + sourceId + "&_CHR=_seed";
    }
  }
  else
  {
    url = serviceUrl + "?" + urlParams;
  }  
  return url;
}
