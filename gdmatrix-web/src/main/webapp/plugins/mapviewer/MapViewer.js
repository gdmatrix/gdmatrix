/*
 * MapViewer.js
 *
 * Ajuntament de Sant Feliu de Llobregat - 2013
 * 
 */

var map;
var mode = "info"; // "info" or "edit"

var wmsUrls = new Array();
var layers = new Array();
var infoLayers = new Array();
var infoLayerCount = 0;
var formSeed;
var infoDividerPosition = 50;
var tileBuffer = 16;
var mapInfoContent;
var urlParameters = parseUrlParameters();
var lastLonLat = null;

newFormSeed();

var editControls = null;
var editingLayer = null;
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5;
// make OL compute scale according to WMS spec
OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;
// pink tile avoidance
OpenLayers.Util.onImageLoadErrorColor = "transparent";


var editingStyle =
{
  pointRadius: 6,
  strokeColor: "#A00000",
  strokeOpacity: 1,
  strokeWidth: 2,
  fillColor: "#FFA000",
  fillOpacity: 0.3
};

var editableStyle =
{
  pointRadius: 6,
  strokeColor: "#00C000",
  strokeOpacity: 0.2,
  strokeWidth: 2,
  fillOpacity: 0
};

var snapStyle =
{
  display: "none"
};

var selectStyle =
{
  pointRadius: 6,
  strokeColor: "#402080",
  strokeOpacity: 1,
  strokeWidth: 3,
  fillColor: "#A080E0",
  fillOpacity: 0.3
};

var searchStyle =
{
  pointRadius: 6,
  strokeColor: "#808020",
  strokeOpacity: 1,
  strokeWidth: 3,
  fillColor: "#606010",
  fillOpacity: 0.3
};

var markersStyle =
{
  pointRadius: 6,
  strokeColor: "#008000",
  strokeOpacity: 1,
  strokeWidth: 2,
  fillColor: "#F0F040",
  fillOpacity: 0.3
};

var editVertexStyle =
{
  pointRadius: 5,
  graphicName: "square",
  fillColor: "#D0D0D0",
  fillOpacity: 0.5,
  strokeWidth: 2,
  strokeOpacity: 1,
  strokeColor: "#333333"
};

var virtualVertexStyle =
{
  pointRadius: 4,
  graphicName: "cross",
  strokeWidth: 2,
  fillColor: "#000000",
  strokeOpacity: 1,
  strokeColor: "#000000"
};

var editingStyleMap = new OpenLayers.StyleMap({
    "default": editingStyle,
    "select": selectStyle,
    "vertex": editVertexStyle
}, {extendDefault: false});

var editableStyleMap = new OpenLayers.StyleMap({
    "default": editableStyle,
    "select": selectStyle,
    "vertex": editVertexStyle
}, {extendDefault: false});

var snapStyleMap = new OpenLayers.StyleMap({
    "default": snapStyle
}, {extendDefault: false});

function initMapViewer()
{
  document.title = mapTitle;
  
  loadingUri = "/plugins/mapviewer/theme/" + cssTheme + "/img/loading.gif";

  var options = {
    controls: [],
    maxExtent: maxBounds,
    minScale: 50,
    maxScale: 20000000,
    maxResolution: 13.768162599531934,
    projection: srs,
    units: 'm',
    theme: "/plugins/mapviewer/theme/" + cssTheme + "/style.css?version=1"
  };
  
  var geodesic = srs == 'EPSG:4326';
  map = new OpenLayers.Map('map', options);

  var mapInfoElem = document.getElementById("infoContent");
  if (mapInfoElem)
  {
    mapInfoContent = mapInfoElem.innerHTML;
  }

  /* smallMap detection */
  var mapViewerDiv = document.getElementById("mapViewer");
  var mapViewerWidth = mapViewerDiv.clientWidth;
  var mapViewerHeight = mapViewerDiv.clientHeight;
  var smallMap = mapViewerWidth < 600 || mapViewerHeight < 400;
  if (!smallMap)
  {
    if (leftPanelVisible && searchEnabled) setLeftPanelVisible(true);
    if (rightPanelVisible) setRightPanelVisible(true);
  }

  var inFrame = !(window.self === window.top);

  createLayers();
  setDefaultBaseLayer();

  searchLayer = new OpenLayers.Layer.Vector("Search",
  {
    extractAttributes: true,
    styleMap: new OpenLayers.StyleMap({'default': searchStyle})
  });
  map.addLayer(searchLayer);

  selectionLayer = new OpenLayers.Layer.Vector("Selection",
  {
    extractAttributes: true,
    styleMap: new OpenLayers.StyleMap({'default': selectStyle})
  });
  map.addLayer(selectionLayer);

  pointLayer = new OpenLayers.Layer.Vector("Mark",
  {
    styleMap: new OpenLayers.Style({
      "externalGraphic" : baseUrl + "/plugins/mapviewer/img/clicked_point.gif",
      "graphicWidth": 7,
      "graphicHeight": 7
    }),
    displayInLayerSwitcher: false
  });
  map.addLayer(pointLayer);

  markersLayer = new OpenLayers.Layer.Markers("Markers");
  map.addLayer(markersLayer);

  updateGroupCheckBoxes();

  /*** CONTROLS ***/

  // build up all controls
  if (smallMap)
  {
    map.addControl(new OpenLayers.Control.PanZoom({
      position: new OpenLayers.Pixel(2, 46)
    }));
  }
  else
  {
    map.addControl(new OpenLayers.Control.PanZoomBar({
      position: new OpenLayers.Pixel(2, 46)
    }));
  }
  map.addControl(new OpenLayers.Control.Navigation(
    {dragPanOptions: {enableKinetic: isTouchDevice()},
     zoomWheelEnabled: !inFrame}
  ));
  map.addControl(new OpenLayers.Control.LoadingPanel());
  map.addControl(new OpenLayers.Control.MousePosition(
  {
    element: document.getElementById('location')
  }));
  var scaleElem = document.getElementById('scale');
  if (scaleElem != null)
  {
    map.addControl(new OpenLayers.Control.Scale(scaleElem));
  }
  if (!smallMap)
  {
    overviewMap = new OpenLayers.Control.OverviewMap(
    {
      maximized: false,
      autoPan:true,
      size: new OpenLayers.Size(200, 140)
    });
    map.addControl(overviewMap);
    overviewMap.element.childNodes[0].style.background = mapBackground;
  }
  map.addControl(new OpenLayers.Control.ScaleLine(
  {
    maxWidth: 200,
    geodesic: geodesic
  }));
  nav = new OpenLayers.Control.NavigationHistory();
  nav.previous.title = bundle["PreviousView"];
  nav.next.title = bundle["NextView"];
  map.addControl(nav);

  zoomExtent = new OpenLayers.Control.ZoomToMaxExtent(
  {
    title: bundle["ZoomExtent"]
  });
  
  measureLength = new OpenLayers.Control.DynamicMeasure(OpenLayers.Handler.Path,
  {
    title: bundle["MeasureLength"],
    persist: true,
    geodesic: geodesic,
    displayClass: "olControlMeasureLength",
    eventListeners:
    {
      activate: function (evt) {setHTML(bundle["MeasureLengthHelp"] + ": ");},
      measure: showMeasure,
      measurepartial: showMeasurePartial
    }
  });

  measureArea = new OpenLayers.Control.DynamicMeasure(OpenLayers.Handler.Polygon,
  {
    title: bundle["MeasureArea"],
    persist: true,
    geodesic: geodesic,
    displayClass: "olControlMeasureArea",
    eventListeners:
    {
      activate: function (evt) {setHTML(bundle["MeasureAreaHelp"] + ": ");},
      measure: showMeasure,
      measurepartial: showMeasurePartial
    }
  });

  exportArea = new OpenLayers.Control.DrawFeature(
    selectionLayer,
    OpenLayers.Handler.Polygon,
    {
      title: bundle["ExportArea"],
      persist: true,
      geodesic: geodesic,
      displayClass: "olControlExportArea",
      eventListeners:
      {
        activate: function (evt)
        {
          setMode("info");
          selectionLayer.destroyFeatures();
          setHTML(bundle["ExportAreaHelp"]);
        },
        featureadded: function (evt)
        {
          setHTML(bundle["ExportedLayers"] + ":");
          exportLayersInArea(evt.feature.geometry);
        },
        deactivate: function (evt)
        {
          selectionLayer.destroyFeatures();
        }
      }
    });
  exportArea.handler.callbacks.point = function(pt)
  {
    if (selectionLayer.features.length > 0)
    {
      selectionLayer.destroyFeatures();
      setHTML(bundle["ExportAreaHelp"]);
    }
  };

  picker = new OpenLayers.Control.WMSGetFeatureInfoEx({
    title: bundle["GetInfo"],
    layers: layers,
    queryVisible: true,
    eventListeners:
    {
      activate: function (evt)
      {
        setMode("info");
      },
      beforegetfeatureinfo: function(evt)
      {
        selectionLayer.destroyFeatures();
        pointLayer.destroyFeatures();
        var lonlat = map.getLonLatFromPixel(evt.xy);
        lastLonLat = lonlat;
        var point = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
        var selectedPoint = new OpenLayers.Feature.Vector(point);

        pointLayer.addFeatures([selectedPoint]);
        pointLayer.redraw();

        var loadingUrl = baseUrl + loadingUri;
        setHTML(bundle["GettingInfo"] + ":<br> " +
          lonlat.lon + ", " + lonlat.lat + 
         "<div id=\"infowait\"><img src=\"" + loadingUrl + "\"></div>");
      },
      getfeatureinfo: function(evt)
      {
        var waitElem = document.getElementById("infowait");
        waitElem.style.display = "none";
        if (evt.features && evt.features.length)
        {
          showInfoForms(evt.features);
        }
        else
        {
          appendHTML("<p>" + bundle["NothingSelected"] + "</p>");
        }
      },
      nogetfeatureinfo: function(evt)
      {
        setHTML("<p>" + bundle["NothingSelected"] + "</p>");
      }
    }
  });

  clearc = new OpenLayers.Control.Button(
  {
    title: bundle["ClearMarkers"],
    displayClass: "olClearMarkers",
    trigger: clearMarkers
  });

  printc = new OpenLayers.Control.Button(
  {
    title: bundle["Print"],
    displayClass: "olPrint",
    trigger: printMap
  });

  linkMap = new OpenLayers.Control.Button(
  {
    title: bundle["LinkMap"],
    displayClass: "olLinkMap",
    trigger: showMapLink
  });

  refresh = new OpenLayers.Control.Button(
  {
    title: bundle["Refresh"],
    displayClass: "olRefresh",
    trigger: refreshMap
  });

  search = new OpenLayers.Control.Button(
  {
    title: bundle["MapSearch"],
    displayClass: "olSearch",
    trigger: showSearch
  });

  mapInfo = new OpenLayers.Control.Button(
  {
    title: bundle["MapInfo"],
    displayClass: "olMapInfo",
    trigger: showMapInfo
  });

  toolBar = new OpenLayers.Control.Panel();
  toolBar.addControls([zoomExtent, nav.previous, nav.next, picker,
    clearc, measureLength, measureArea]);
  if (printMid !== null && printReports != null) toolBar.addControls([printc]);
  if (exportFormats.length > 0) toolBar.addControls([exportArea]);
  toolBar.addControls([linkMap, refresh, search, mapInfo]);
  map.addControl(toolBar);

  map.div.style.backgroundColor = mapBackground;

  // init snap & edit controls
  var snapLayers = getSnapLayers();
  for (var i = 0; i < layers.length; i++)
  {
    var layer = layers[i];
    if (layer.editable)
    {
      if (snapLayers.length > 0)
      {
        var snap = new OpenLayers.Control.Snapping({
          layer: layer.wfsLayer,
          targets: snapLayers,
          greedy: false
        });
        snap.events.register("beforesnap", snap,
          function(evt){return evt.layer.visibility;});
        snap.activate();
      }
      if (editingLayer == null) // set first editingLayer
      {
        editingLayer = layer.wfsLayer;
        layer.wfsLayer.styleMap = editingStyleMap;
        createEditControls();
      }
    }
  }

  picker.activate();

  map.zoomToExtent(maxBounds);

  map.events.on(
  {
    moveend: function(evt)
    {
      saveMapState();
    }
  });

  if (!initFeatureLocator())
  {
    restoreMapState();
  }

  // show feature area and length
  document.addEventListener('keyup', function(e)
  {
    if (e.altKey)
    {
      var feature = modifyFeature.feature;
      if (feature)
      {
        if (e.keyCode === 65)
        {
          alert("Area: " + feature.geometry.getArea());
        }
        else if (e.keyCode === 76)
        {
          alert("Length: " + feature.geometry.getLength());
        }
      }
    }
  });
}

function initFeatureLocator()
{
  var doSearch = false;
  if (searchEnabled)
  {
    OpenLayers.FeatureLocator.map = map;
    OpenLayers.FeatureLocator.baseUrl = baseUrl;
    OpenLayers.FeatureLocator.language = language;
    OpenLayers.FeatureLocator.elemId = "leftPanel";
    OpenLayers.FeatureLocator.loadingUrl = baseUrl + loadingUri;
    OpenLayers.FeatureLocator.markersLayer = markersLayer;
    OpenLayers.FeatureLocator.searchLayer = searchLayer;
    OpenLayers.FeatureLocator.searchLabel = bundle["Search"];
    OpenLayers.FeatureLocator.clearSearchLabel = bundle["ClearSearch"];
    OpenLayers.FeatureLocator.searchSelectorLabel = bundle["SearchSelector"];
    OpenLayers.FeatureLocator.resultsMessage = bundle["SearchResults"];
    OpenLayers.FeatureLocator.noResultsMessage = bundle["NoResults"];
    OpenLayers.FeatureLocator.searchStarted =
    OpenLayers.FeatureLocator.resultsCleared =
    OpenLayers.FeatureLocator.formLoaded = function(activeLocator)
    {
      var searchLinkElem = document.getElementById("searchLinkBox");
      if (searchLinkElem)
      {
        searchLinkElem.innerHTML = ""; 
      }
    };
    OpenLayers.FeatureLocator.markerSelected = function(activeLocator, feature)
    {
      clearSelection();
      if (activeLocator.markerSelected)
      {
        activeLocator.markerSelected(feature);
      }
      showInfoForm(feature, null, false);
    };
    OpenLayers.FeatureLocator.searchCompleted = function(activeLocator, features)
    {
      if (features.length > 0 && activeLocator.parameters)
      {
        var facesPage = isPublic ? "go.faces" : "login.faces";
        var linkUrl = baseUrl + "/" + facesPage + "?xmid=" + currentMid +
          "&map_name=" + mapName +
          "&feature_locator=" + activeLocator.name;
        var parameters = activeLocator.parameters;
        for (var i = 0; i < parameters.length; i++)
        {
          var parameter = parameters[i];
          var elem = document.getElementById(parameter);
          if (elem)
          {
            var value;
            if (elem.type == 'checkbox')
            {
              value = elem.checked;
            }
            else value = encodeURIComponent(elem.value);
            linkUrl += "&_" + parameter + "=" + value;
          }
        }
        linkUrl += "&layer_visibility=" + getLayerVisibility();
        var searchLinkElem = document.getElementById("searchLinkBox");
        if (!searchLinkElem)
        {
          var leftPanelElem = document.getElementById("leftPanel");
          searchLinkElem = document.createElement("div");
          searchLinkElem.setAttribute("id", "searchLinkBox");
          searchLinkElem.setAttribute("class", "linkBox");
          leftPanelElem.appendChild(searchLinkElem);
        }
        searchLinkElem.innerHTML = '<a href="' + linkUrl +
          '" target="_blank" class="mapLink"><img src="' + baseUrl +
          '/plugins/mapviewer/img/link.png"><span>' +
          bundle["SearchLink"] + 
          ':</span></a><textarea alt="url" spellcheck="false" class="mapLink" aria-label="url">' +
          linkUrl + '</textarea>';
      }
    };
    OpenLayers.FeatureLocator.init();    
    var featureLocator = urlParameters["feature_locator"];
    doSearch = OpenLayers.FeatureLocator.activateLocatorByName(featureLocator);
    if (doSearch)
    {
      for (var paramName in urlParameters)
      {
        var firstChar = paramName.substr(0, 1);
        if (firstChar == '_' || firstChar == '@')
        {
          var elemName = paramName.substr(1);          
          var elem = document.getElementById(elemName);
          if (elem)
          {
            var elemValue = urlParameters[paramName];
            if (elem.type == 'checkbox')
              elem.checked = (elemValue == 'true');
            else elem.value = elemValue;
          }
        }
      }
      OpenLayers.FeatureLocator.search();
    }
    else
    {
      OpenLayers.FeatureLocator.activateLocator(0);
      if (searchOnLoad)
      {        
        OpenLayers.FeatureLocator.search();
        doSearch = true;
      }
    }
  }
  else
  {
    setLeftPanelVisible(false);
    var button = document.getElementById("toggleLeftPanel");
    if (button) button.style.display = "none";
  }
  return doSearch;
}

function createEditControls()
{
  if (editControls != null)
  {
    // unload previous controls
    for (var i = 0; i < editControls.length; i++)
    {
      var editControl = editControls[i];
      editControl.deactivate();
      editControl.destroy();
      OpenLayers.Util.removeItem(toolBar.controls, editControl);
    }
  }

  modifyFeature = new OpenLayers.Control.ModifyFeature(editingLayer,
  {
    title: bundle["ModifyFeatures"],
    vertexRenderIntent: "vertex",
    virtualStyle: virtualVertexStyle,
    displayClass: "olModifyFeature",
    standalone: false,
    eventListeners:
    {
      activate: function (evt)
      {
        setMode("edit");
      }
    }
  });
  modifyFeature.selectFeature = function(feature)
  {
    if (this.feature === feature ||
       (this.geometryTypes && OpenLayers.Util.indexOf(this.geometryTypes,
       feature.geometry.CLASS_NAME) == -1)) {
        return;
    }
    if (this.beforeSelectFeature(feature) !== false) {
        if (this.feature) {
            this.unselectFeature(this.feature);
        }
        this.feature = feature;
        this.layer.selectedFeatures.push(feature);
        this.layer.drawFeature(feature, 'select');
        this.modified = false;
        this.resetVertices();
        this.onModificationStart(this.feature);
        showInfoForm(feature, null, true);
        activateFormValidation();
    }
    // keep track of geometry modifications
    var modified = feature.modified;
    if (feature.geometry && !(modified && modified.geometry)) {
        this._originalGeometry = feature.geometry.clone();
    }
  };
  modifyFeature.unselectFeature = function(feature)
  {
    setFeatureAttributes(feature);setHTML("");
    this.layer.removeFeatures(this.vertices, {silent: true});
    this.vertices = [];
    this.layer.destroyFeatures(this.virtualVertices, {silent: true});
    this.virtualVertices = [];
    if(this.dragHandle) {
        this.layer.destroyFeatures([this.dragHandle], {silent: true});
        delete this.dragHandle;
    }
    if(this.radiusHandle) {
        this.layer.destroyFeatures([this.radiusHandle], {silent: true});
        delete this.radiusHandle;
    }
    this.layer.drawFeature(this.feature, 'default');
    this.feature = null;
    OpenLayers.Util.removeItem(this.layer.selectedFeatures, feature);
    this.onModificationEnd(feature);
    this.layer.events.triggerEvent("afterfeaturemodified", {
        feature: feature,
        modified: this.modified
    });
    this.modified = false;
  };

  modifyFeature.mode = OpenLayers.Control.ModifyFeature.RESHAPE |
   OpenLayers.Control.ModifyFeature.ROTATE |
   OpenLayers.Control.ModifyFeature.DRAG;

  drawPoint = new OpenLayers.Control.DrawFeatureEx(editingLayer,
    OpenLayers.Handler.Point,
    {
      title: bundle["DrawPoint"],
      displayClass: "olDrawPoint"
    }
   );

  drawPolyline = new OpenLayers.Control.DrawFeatureEx(editingLayer,
    OpenLayers.Handler.Path,
    {
      title: bundle["DrawPolyline"],
      displayClass: "olDrawPolyline"
    });

  drawPolygon = new OpenLayers.Control.DrawFeatureEx(editingLayer,
    OpenLayers.Handler.Polygon,
    {
      title: bundle["DrawPolygon"],
      displayClass: "olDrawPolygon",
      handlerOptions: {holeModifier: "altKey"}
    });

  deleteButton = new OpenLayers.Control.DeleteFeature(editingLayer,
  {
    title: bundle["DeleteFeatures"],
    displayClass: "olDeleteFeature"
  });

  var selectFeatures = new OpenLayers.Control.SelectFeature(editingLayer, 
  {
    title: bundle["EditAttributes"],
    displayClass: "olEditAttributes",
    box: true,
    multiple: false,
    clickout: true,
    multipleKey : 'shiftKey',
    toggleKey : 'altKey',
    onSelect: function(){showAttributesEditForm();},
    onUnselect: function(){showAttributesEditForm();}
  });

  selectFeatures.activate = function()
  {
    setMode('edit');
    OpenLayers.Control.SelectFeature.prototype.activate.call(this);
  };

  selectFeatures.deactivate = function()
  {
    OpenLayers.Control.SelectFeature.prototype.unselectAll.call(this);
    OpenLayers.Control.SelectFeature.prototype.deactivate.call(this);
  };

  function showAttributesEditForm()
  {
    var mapInfoElem = document.getElementById("infoContent");
    if (mapInfoElem)
    {
      mapInfoElem.innerHTML = "";
      setRightPanelVisible(true);
      var url = editingLayer.protocol.url;
      var featureType = editingLayer.protocol.featureType;
      var info = OpenLayers.FeatureTypeInfo.getInfo(url, featureType);
      var mergedAttributes = [];
      for (var i = 0; i < info.properties.length; i++)
      {
        var property = info.properties[i];
        if (property.type !== 'gml:GeometryPropertyType')
        {
          mergedAttributes.push({name: property.name, value: null, valueSet: false});
        }
      }
      var features = editingLayer.selectedFeatures;
      var multiValues = false;
      for (var i = 0; i < features.length; i++)
      {
        var attributes = features[i].attributes;
        for (var j = 0; j < mergedAttributes.length; j++)
        {
          var mergedAttribute = mergedAttributes[j];
          if (!attributes[mergedAttribute.name])
          {
            attributes[mergedAttribute.name] = "";
          }
          if (!mergedAttribute.valueSet)
          {
            mergedAttribute.value = attributes[mergedAttribute.name];
            mergedAttribute.valueSet = true;
          }
          else if (mergedAttribute.value !== attributes[mergedAttribute.name])
          {
            mergedAttribute.value = "*";
            multiValues = true;
          }
        }
      }
      var divCounter = document.createElement("div");
      divCounter.innerHTML = bundle["SelectCount"] + ": " + features.length;
      mapInfoElem.appendChild(divCounter);

      var tableElem = document.createElement("table");
      tableElem.className = "attrTable";
      mapInfoElem.appendChild(tableElem);
      for (var j = 0; j < mergedAttributes.length; j++)
      {
        var aid = "F_" + mergedAttributes[j].name;
        var rowElem = document.createElement("tr");
        tableElem.appendChild(rowElem);

        var tdNameElem = document.createElement("td");
        tdNameElem.innerHTML = mergedAttributes[j].name;
        rowElem.appendChild(tdNameElem);

        var tdValueElem = document.createElement("td");
        rowElem.appendChild(tdValueElem);

        var inputElem = document.createElement("input");
        inputElem.id =  aid;
        inputElem.name = mergedAttributes[j].name;
        inputElem.value = mergedAttributes[j].value;
        tdValueElem.appendChild(inputElem);
      }
      if (multiValues)
      {
        var divLabel = document.createElement("div");
        divLabel.innerHTML = "(*) " + bundle["MultipleValues"];
        mapInfoElem.appendChild(divLabel);
      }
      var onClick = function()
      {
        for (var i = 0; i < features.length; i++)
        {
          var feature = features[i];
          var attributes = feature.attributes;
          for (var j = 0; j < mergedAttributes.length; j++)
          {
            var aid = "F_" + mergedAttributes[j].name;
            var inputElem = document.getElementById(aid);
            if (inputElem && inputElem.value !== '*')
            {
              attributes[inputElem.name] = inputElem.value;
            }
          }
          feature.state = OpenLayers.State.UPDATE;
        }
      };
      var applyButton = document.createElement("input");
      applyButton.value = bundle["Apply"];
      applyButton.type = "button";
      applyButton.className = "button";
      applyButton.addEventListener('click', onClick);
      mapInfoElem.appendChild(applyButton);
    }
  }

  importFile = new OpenLayers.Control.Button(
  {
    title: bundle["ImportFile"],
    displayClass: "olImportFile",
    trigger: doImportFile
  });

  saveButton = new OpenLayers.Control.Button({
    title: bundle["SaveChanges"],
    trigger: function()
    {
      if (editingLayer != null)
      {
        editingLayer.strategies[1].save();        
      }
    },
    displayClass: "olControlSaveChanges"
   });

  undoButton = new OpenLayers.Control.Button({
    title: bundle["UndoChanges"],
    trigger: function()
    {
      refreshEditingLayer();
      setHTML("<div>" + bundle["ChangesUndone"] + "</div>");      
    },
    displayClass: "olControlUndoChanges"
   });

  editControls = [modifyFeature, drawPoint, drawPolyline, drawPolygon, 
    deleteButton, selectFeatures, importFile, undoButton, saveButton];
  toolBar.addControls(editControls);
  toolBar.redraw();
}

function doImportFile()
{
  deactivateControls();
  modifyFeature.activate();

  var inputElem = document.createElement("input");
  var openFile = function(event) 
  {
    var input = event.target;
    var file = input.files[0];
    document.body.removeChild(inputElem);
    var reader = new FileReader();
    reader.onload = function()
    {
      var parser = new DxfParser();
      try
      {
        var features = [];
        var dxf = parser.parseSync(reader.result);
        var entities = dxf.entities;
        for (var i = 0; i < entities.length; i++)
        {
          var entity = entities[i];
          var geometry = createGeometryFromEntity(entity);
          if (geometry !== null)
          {
            var feature = new OpenLayers.Feature.Vector(geometry, {});
            features.push(feature);
          }
        }
        if (features.length > 0)
        {
          editingLayer.addFeatures(features);
          for (var i = 0; i < features.length; i++)
          {
            features[i].state = OpenLayers.State.INSERT;
          }
        }
      }
      catch (err)
      {
        return console.error(err.stack);
      }
    };
    reader.readAsText(file);
  };
  inputElem.type = "file";
  inputElem.addEventListener("change", openFile);  
  document.body.appendChild(inputElem);
  inputElem.click();
}

function createGeometryFromEntity(entity)
{
  if (entity.type === 'LWPOLYLINE')
  {
    console.info(entity);
    var points = [];
    for (var j = 0; j < entity.vertices.length; j++)
    {
      var vertex = entity.vertices[j];
      var point = new OpenLayers.Geometry.Point(vertex.x, vertex.y);
      points.push(point);
    }
    if (points[0].equals(points[points.length - 1])) // Polygon
    {
      var ring = new OpenLayers.Geometry.LinearRing(points);
      return new OpenLayers.Geometry.Polygon([ring]);
    }
    else // LineString
    {
      return new OpenLayers.Geometry.LineString(points);      
    }
  }
  else if (entity.type === 'POINT')
  {
    return new OpenLayers.Geometry.Point(
      entity.position.x, entity.position.y);
  }
  else
  {
    console.info(entity);
  }
  return null;
}

/**** Layer management functions ****/

function addLayer(label, serviceIndex, name, base, group, sldUrl, style, format,
  filter, visible, locatable, editable, snap, independent, opacity, transparent)
{
  var layer =
  {
    label: label,
    url: baseUrl + "/proxy?url=" + wmsUrls[serviceIndex],
    name: name,
    base: base,
    group: group,
    sldUrl: sldUrl,
    style: style,
    format: format,
    filter: filter,
    visible: visible,
    locatable: locatable,
    editable: editable,
    snap: snap,
    independent: independent,
    opacity: opacity,
    transparent : transparent,
    wmsLayer: null,
    wfsLayer: null
  };
  layers.push(layer);
}

function createLayers()
{
  var layer;
  var i;
  var wmsLayer;
  var mergeContext = {};

  // wms layers
  for (i = 0; i < layers.length; i++)
  {
    layer = layers[i];
    if (!canMergeLayer(layer, mergeContext))
    {
      layer.wmsLayer = createWmsLayer(layer);
      wmsLayer = updateWmsLayer(i - 1);
      if (wmsLayer != null) map.addLayer(wmsLayer);
    }
  }
  wmsLayer = updateWmsLayer(layers.length - 1);
  if (wmsLayer != null) map.addLayer(wmsLayer);

  var saveEventListeners =
  {
    'start': function(event)
    {
      var feature = editControls[0].feature;
      if (feature != null) setFeatureAttributes(feature);
      setHTML(bundle["SavingFeatures"] + "...");
    },
    'success': function(event)
    {
      refreshWmsLayers();
      refreshEditingLayer();
      setHTML("<div>" + bundle["SaveResults"] + ":</div>");
      appendHTML("<span style=\"color:#008000\">" +
        getSaveMessage(event) + "</span>");
    },
    'fail': function(event)
    {
      setHTML("<div>" + bundle["ChangesNotSaved"] + ":</div>");
      appendHTML("<span style=\"color:red\">" +
        getErrorMessage(event) + "</span>");
    }
  };

  // wfs layers
  for (i = 0; i < layers.length; i++)
  {
    layer = layers[i];
    if (!layer.base && (layer.editable || layer.snap))
    {
      var saveStrategy = new OpenLayers.Strategy.Save();
      saveStrategy.events.on(saveEventListeners);

      var wfsLayer = new OpenLayers.Layer.Vector(layer.label,
      {
        extractAttributes: true,
        styleMap: layer.editable ? editableStyleMap : snapStyleMap,
        strategies: [new OpenLayers.Strategy.EBBOX(), saveStrategy],
        protocol: OpenLayers.Protocol.WFS.fromWMSLayer(createWmsLayer(layer))
      });
      wfsLayer.setVisibility(false);      
      layer.wfsLayer = wfsLayer;
      map.addLayer(wfsLayer);
    }
  }
}

function getSnapLayers()
{
  var snapLayers = new Array();
  for (var i = 0; i < layers.length; i++)
  {
    var layer = layers[i];
    if (layer.snap) snapLayers.push(layer.wfsLayer);
  }
  return snapLayers;
}

function canMergeLayer(layer, mergeContext)
{
  if (mergeContext.field === undefined)
  {
    if (layer.filter === mergeContext.filter) mergeContext.field = 'F';
    else if (layer.name === mergeContext.layerName) mergeContext.field = 'L';
  }

  var merge = !layer.base && !layer.independent &&
   layer.url === mergeContext.url &&
   layer.sldUrl === mergeContext.sldUrl &&
   layer.format === mergeContext.format &&
   ((mergeContext.field === 'F' && layer.filter === mergeContext.filter) ||
    (mergeContext.field === 'L' && layer.name === mergeContext.layerName));

  mergeContext.url = layer.url;
  mergeContext.sldUrl = layer.sldUrl;
  mergeContext.format = layer.format;
  mergeContext.layerName = layer.name;
  mergeContext.filter = layer.filter;
  if (!merge)
  {
    delete mergeContext.field;
  }
  return merge;
}

function createWmsLayer(layer)
{
  var params =
  {
    layers: layer.name,
    format: layer.format,
    transparent: layer.transparent && layer.format !== 'image/jpeg'
  };
  if (layer.format !== 'image/jpeg')
  {
    params["tiles"] = true;
    params["tilesOrigin"] = maxBounds.left + ',' + maxBounds.bottom;
  }
  if (layer.sldUrl !== null && layer.sldUrl.length > 0)
  {
    params["SLD"] = layer.sldUrl;
  }
  if (layer.style !== null && layer.style.length > 0)
  {
    params["STYLES"] = layer.style;
  }
  else
  {
    params["STYLES"] = "";    
  }
  if (layer.filter !== null && layer.filter.length > 0)
  {
    params["CQL_FILTER"] = layer.filter;
  }
  params["_CHG"] = getGroupName(layer.name);
  params["_CHR"] = "_OLSALT";
  params["_CHF"] = "image"; // only cache images
  params["buffer"] = "" + tileBuffer;

  var options =
  {
    buffer: 0,
    transitionEffect: layer.base ? "resize" : "",
    displayOutsideMaxExtent: true,
    isBaseLayer: layer.base,
    visibility: layer.visible,
    yx : {srs : false}
  };

  return new OpenLayers.Layer.WMS(layer.label, layer.url, params, options);
}

function updateWmsLayer(layerId)
{
  var i = layerId;
  while (i >= 0 && layers[i].wmsLayer == null) i--;  
  if (i == -1) return null;

  var firstLayer = layers[i]; // first layer in group
  var wmsLayer = firstLayer.wmsLayer;

  if (wmsLayer.base || // is base
      i == layers.length - 1 || // last layer
      layers[i + 1].wmsLayer != null) // next is different
  {
    // independent layer
    wmsLayer.setVisibility(firstLayer.visible);
  }
  else
  {
    // grouped layer
    var layerNames = "";
    var styleArray = new Array();
    var filterArray = new Array();

    if (firstLayer.visible)
    {
      layerNames += firstLayer.name;
      styleArray.push(firstLayer.style == null ? '' : firstLayer.style);
      if (firstLayer.filter != null) filterArray.push(firstLayer.filter);
    }

    var j = i + 1;
    while (j < layers.length && layers[j].wmsLayer == null)
    {
      var nextLayer = layers[j];
      if (nextLayer.visible)
      {
        if ((',' + layerNames + ',').indexOf(',' + nextLayer.name + ',') == -1)
        {
          if (layerNames.length > 0) layerNames += ",";
          layerNames += nextLayer.name;
          styleArray.push(nextLayer.style == null ? '' : nextLayer.style);
        }
        if (nextLayer.filter != null) filterArray.push(nextLayer.filter);
      }
      j++;
    }
    if (layerNames.length == 0)
    {
      wmsLayer.setVisibility(false);
    }
    else
    {
      wmsLayer.mergeNewParams({
        LAYERS: layerNames,
        STYLES: styleArray.join(","),
        CQL_FILTER: filterArray.length == 0 ? 
          null : joinFilter(filterArray),
        "_CHG": getGroupName(layerNames),
        "_CHR": "_OLSALT",
        "_CHF": "image", // only cache images
        "buffer": "" + tileBuffer
      });
      wmsLayer.setVisibility(true);
    }
  }

  if (self.console != undefined)
  {
    self.console.info("Udpate layer " + i + ": " + wmsLayer.params['LAYERS'] +
      " STYLES=" + wmsLayer.params['STYLES'] +
      " SLD=" + wmsLayer.params['SLD'] + 
      " CQL_FILTER=" + wmsLayer.params['CQL_FILTER'] +
      " Visible:" + wmsLayer.visibility);
  }
  wmsLayer.setOpacity(firstLayer.opacity);
  
  return wmsLayer;
}

function setDefaultBaseLayer()
{
  var index = 0;
  var found = false;
  while (index < layers.length && !found)
  {
    var layer = layers[index];
    if (layer.base && layer.visible)
    {
      map.setBaseLayer(layer.wmsLayer);
      found = true;
    }
    index++;
  }
}

function getGroupName(layerNames)
{
  var groupName = "";
  for (var i = 0; i < layerNames.length && i < 64; i++)
  {
    var ch = layerNames.charAt(i).toUpperCase();
    if ((ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9'))
      groupName += ch;
  }
  return groupName;
}

function switchGroup(groupName)
{
  var layer;
  var i;
  var checkBox = document.getElementById("check_group_" + groupName);
  var visible = checkBox.checked;
  checkBox.className = visible ? "all" : "none";

  var wmsLayersToUpdate = new Array();
  for (i = 0; i < layers.length; i++)
  {
    layer = layers[i];
    if (layer.group == groupName)
    {
      if (layer.visible != visible) // visibility change
      {
        layer.visible = visible;
        var layerCheckBox = document.getElementById("layer_" + i);
        if (layerCheckBox)
        {
          layerCheckBox.checked = visible;

          var j = i;
          while (j >= 0 && layers[j].wmsLayer == null) j--;
          wmsLayersToUpdate[j] = true;

          var wfsLayer = layer.wfsLayer;
          if (wfsLayer != null) wfsLayer.setVisibility(visible && mode == "edit");
        }
      }
    }
  }
  for (i = 0; i < wmsLayersToUpdate.length; i++)
  {
    if (wmsLayersToUpdate[i] === true) updateWmsLayer(i);
  }
}

function switchLayer(layerId)
{
  var layerElem = document.getElementById("layer_" + layerId);
  var visible = layerElem.checked;
  var layer = layers[layerId];
  layer.visible = visible;
  updateWmsLayer(layerId);
  updateGroupCheckBoxes(layer.group);
  var wfsLayer = layer.wfsLayer;
  if (wfsLayer != null) wfsLayer.setVisibility(visible && mode == "edit");
}

function updateGroupCheckBoxes(groupName)
{
  var groupMaps = {};
  var groupMap;
  for (var i = 0; i < layers.length; i++)
  {
    var layer = layers[i];
    if (!layer.base && layer.group != null)
    {
      if (groupName == undefined || layer.group == groupName)
      {
        groupMap = groupMaps[layer.group];
        if (groupMap == undefined)
        {
          groupMap = {count:0, visible:0};
          groupMaps[layer.group] = groupMap;
        }
        groupMap.count++;
        if (layer.visible) groupMap.visible++;
      }
    }
  }

  for (groupName in groupMaps)
  {
    groupMap = groupMaps[groupName];
    var checkBox = document.getElementById("check_group_" + groupName);
    if (checkBox != null)
    {
      checkBox.checked = groupMap.visible > 0;
      // update background color
      if (groupMap.visible == 0)
      {
        checkBox.className = "none";
      }
      else if (groupMap.visible < groupMap.count)
      {
        checkBox.className = "some";
      }
      else
      {
        checkBox.className = "all";
      }
    }
  }
}

function setEditingLayer(layerId)
{
  var wfsLayer = layers[layerId].wfsLayer;
  if (wfsLayer != null)
  {
    if (wfsLayer != editingLayer || mode == 'info')
    {
      editingLayer.styleMap = editableStyleMap;
      editingLayer.redraw(true);
      editingLayer = wfsLayer;
      editingLayer.styleMap = editingStyleMap;
      editingLayer.redraw(true);
      var elem = document.getElementById("layer_" + layerId);
      elem.checked = true;
      layers[layerId].visible = true;
      updateWmsLayer(layerId);
      createEditControls();
      deactivateControls();
      editControls[0].activate();
      setMode("edit");
    }
  }
}

function deactivateControls()
{
  for (var i = 0; i < toolBar.controls.length; i++)
  {
    var control = toolBar.controls[i];
    control.deactivate();
  }
}

function setMode(m)
{
  var elem;
  mode = m;
  if (mode == "edit")
  {
    selectionLayer.destroyFeatures();
    pointLayer.destroyFeatures();
  }
  for (var i = 0; i < layers.length; i++)
  {
    var layer = layers[i];
    if (layer.wfsLayer != null)
    {
      var wfsLayer = layers[i].wfsLayer;
      elem = document.getElementById("layer_" + i);
      if (elem != null)
      {
        var visible = mode == "edit" ? elem.checked : false;
        wfsLayer.setVisibility(visible);
      }
      elem = document.getElementById("editlayer_" + i);
      if (elem != null)
      {
        elem.className = (mode == 'edit' && wfsLayer === editingLayer) ?
          "editing" : (layer.editable ? "editable" : "noeditable");
      }
    }
  }
}

function refreshWmsLayers()
{
  for (var i = 0; i < layers.length; i++)
  {
    var wmsLayer = layers[i].wmsLayer;
    if (wmsLayer != null) wmsLayer.redraw(true);
  }
}

function refreshWfsLayers()
{
  for (var i = 0; i < layers.length; i++)
  {
    var wfsLayer = layers[i].wfsLayer;
    if (wfsLayer != null)
    {
      wfsLayer.refresh({force:true});
      wfsLayer.redraw(true);
    }
  }
}

function refreshEditingLayer()
{
  if (mode == 'edit')
  {
    editingLayer.destroyFeatures();
    editingLayer.refresh({force:true});
    editingLayer.redraw(true);
    // reactivate current active control
    for (i = 0; i < editControls.length; i++)
    {
      if (editControls[i].active)
      {
        editControls[i].deactivate();
        editControls[i].activate();
      }
    }
  }
}

function refreshMap()
{
  setHTML(bundle["MapUpdated"]);
  refreshWmsLayers();
  refreshWfsLayers();
}

function showSearch()
{
  setLeftPanelVisible(true);
}

function showMapInfo()
{
  setRightPanelVisible(true);
  var mapInfoElem = document.getElementById("infoContent");
  if (mapInfoElem)
  {
    mapInfoElem.innerHTML = mapInfoContent;
  }
}

function setBaseLayer(index)
{
  for (var i = 0; i < layers.length; i++)
  {
    if (layers[i].base) layers[i].visible = false;
  }
  layers[index].visible = true;
  var wmsLayer = layers[index].wmsLayer;
  map.setBaseLayer(wmsLayer);
  wmsLayer.setVisibility(true);
}


// other functions

function getFeatureType(feature)
{
  var featureType = null;
  if (feature.layer != null && feature.layer.protocol != null)
  {
    featureType = feature.layer.protocol.featureType;
  }
  else
  {
    var fid = feature.fid;
    if (fid != null)
    {
      var index = fid.indexOf(".");
      featureType = fid.substring(0, index);
    }
  }
  return featureType;
}

function getDefaultEntitySelector(entity)
{
  var selector = null;
  var infoLayer = infoLayers[entity];
  if (infoLayer)
  {
    selector = infoLayer.formSelector;
  }
  if (selector == null) selector = "form:" + entity;
  return selector;
}

function setHTML(text)
{
  document.getElementById('infoContent').innerHTML = text;
}

function appendHTML(text)
{
  var previousText = document.getElementById('infoContent').innerHTML;
  document.getElementById('infoContent').innerHTML = previousText + text;
}

function clearMarkers()
{
  clearSelection();
  OpenLayers.FeatureLocator.clear();
  lastLonLat = null;
}

function clearSelection()
{
  setHTML("");
  if (selectionLayer)
  {
    selectionLayer.destroyFeatures();
    selectionLayer.redraw();
  }
  if (pointLayer)
  {
    pointLayer.destroyFeatures();
    pointLayer.redraw();
  }
  measureLength.cancel();
  measureArea.cancel();
}

function showInfoForms(features)
{
  var feature, i;
  if (infoLayerCount == 0)
  {
    for (i = 0; i < features.length; i++)
    {
      feature = features[i];
      selectionLayer.addFeatures([feature]);
      appendHTML(getInfoForm(feature, null, false));
    }
  }
  else
  {
    var formArray = new Array(infoLayerCount);
    for (i = 0; i < features.length; i++)
    {
      feature = features[i];
      var featureType = getFeatureType(feature);
      try
      {
        var infoLayer = infoLayers[featureType];
        var index = infoLayer.index;
        var form = getInfoForm(feature, infoLayer.formSelector, false);
        if (formArray[index] == null) formArray[index] = form;
        else formArray[index] += form;
        if (infoLayer.hilight) selectionLayer.addFeatures([feature]);
      }
      catch (e)
      {
      }
    }
    var haveInfo = false;
    for (i = 0; i < infoLayerCount; i++)
    {
      if (formArray[i] != null)
      {
        appendHTML(formArray[i]);
        haveInfo = true;
      }
    }
    if (!haveInfo)
    {
      appendHTML("<p>" + bundle["NothingSelected"] + "</p>");
    }
  }
  selectionLayer.redraw();
}

function showInfoForm(feature, selector, forEdit)
{
  var loadingUrl = baseUrl + loadingUri;
  setHTML("<div id=\"infowait\"><img src=\"" + loadingUrl + "\"></div>");
  getInfoForm(feature, selector, forEdit, function(form) {setHTML(form);});
}

function getInfoForm(feature, selector, forEdit, callback)
{
  var entity = getFeatureType(feature);
  var getParams = {};
  var attributes = feature.attributes;
  for (var name in attributes)
  {
    var value = attributes[name];
    getParams[name] = value;
  }
  if (selector == null) selector = getDefaultEntitySelector(entity);
  getParams["entity"] = entity;
  getParams["formseed"] = formSeed;
  getParams["selector"] = selector;
  getParams["foredit"] = forEdit;
  getParams["renderer"] = forEdit ?
    "org.santfeliu.web.servlet.form.EditableFormRenderer" :
    "org.santfeliu.web.servlet.form.ReadOnlyFormRenderer";

  var request;
  if (callback === undefined)
  {
    request = OpenLayers.Request.GET(
    {
      url: baseUrl + "/form",
      params: getParams,
      async: false
    });
    return request.responseText;
  }
  else
  {
    request = OpenLayers.Request.GET(
    {
      url: baseUrl + "/form",
      params: getParams,
      async: true,
      callback: function(request) {callback(request.responseText);}
    });
    return null;
  }
}

function activateFormValidation()
{
  var featureType = editingLayer.protocol.featureType;
  var elems = document.forms[0].elements;
  for (var i = 0; i < elems.length; i++)
  {
    var elem = elems[i];
    var name = elem.name;
    if (name.indexOf(featureType + ".") == 0)
    {
      var format = elem.getAttribute("format");
      elem.removeAttribute("format");

      var listener = null;
      if (format === "number")
      {
        listener = function(evt)
        {
          evt = evt || window.event;
          var el = evt.target || evt.srcElement;
          checkNumber(el);
        };
      }
      // else other formats

      if (listener != null)
      {
        if (elem.addEventListener)
        {
          elem.addEventListener("keyup", listener, false);
          checkNumber(elem);
        }
        else if (elem.attachEvent)
        {
          elem.attachEvent("onkeyup", listener);
          checkNumber(elem);
        }
      }
    }
  }
}

function checkNumber(elem)
{
  var value = elem.value;
  if ((value - 0) == value && value.length > 0)
  {
    elem.style.color = "#000000";
  }
  else
  {
    elem.style.color = "#FF0000";
  }
  return false;
}

function setFeatureAttributes(feature)
{
  var attributes = feature.attributes;
  var elems = document.forms[0].elements;
  var featureType = editingLayer.protocol.featureType;
  for (var i = 0; i < elems.length; i++)
  {
    var elem = elems[i];
    var name = elem.name;
    if (name.indexOf(featureType + ".") == 0)
    {
      var attrName = name.substring(featureType.length + 1);
      var oldValue = attributes[attrName];
      if (oldValue != null) oldValue = "" + oldValue;
      var newValue = elem.value;
      if (oldValue != newValue)
      {
        if (newValue == "") newValue = null;
        attributes[attrName] = newValue;
        if (feature.fid == null)
        {
          feature.state = OpenLayers.State.INSERT;
        }
        else
        {
          feature.state = OpenLayers.State.UPDATE;
        }
      }
    }
  }
}

function showMeasure(evt)
{
  var value = Math.round(evt.measure * 1000) / 1000;
  var result = value + " " + evt.units + (evt.order == 2 ? "2" : "");
  appendHTML("<div>" + bundle["MeasureTotal"] + ": " + result + "</div>");
}

function showMeasurePartial(evt)
{
  if (evt.measure > 0 && evt.order == 1)
  {
    var value = Math.round(evt.measure * 1000) / 1000;
    var result = value + " " + evt.units + (evt.order == 2 ? "2" : "");
    appendHTML("<div>" + bundle["MeasurePartial"] + ": " + result + "</div>");
  }
}

function addInfoLayer(name, formSelector, hilight)
{
  infoLayers[name] =
  {
    "index": infoLayerCount,
    "formSelector" : formSelector,
    "hilight" : hilight
  };
  infoLayerCount++;
}

function getSaveMessage(event)
{
  var totalInserted = 0, totalUpdated = 0, totalDeleted = 0;
  var responseText = event.response.priv.responseText;
  
  var index = responseText.indexOf("totalInserted>");
  if (index != -1)
  {
    var index2 = responseText.indexOf("<", index + 14);
    if (index2 != -1)
    {
      totalInserted = responseText.substring(index + 14, index2);
    }
  }
  index = responseText.indexOf("totalUpdated>");
  if (index != -1)
  {
    index2 = responseText.indexOf("<", index + 13);
    if (index2 != -1)
    {
      totalUpdated = responseText.substring(index + 13, index2);
    }
  }
  index = responseText.indexOf("totalDeleted>");
  if (index != -1)
  {
    index2 = responseText.indexOf("<", index + 13);
    if (index2 != -1)
    {
      totalDeleted = responseText.substring(index + 13, index2);
    }
  }
  var message = 
    "<div>" + bundle["InsertedFeatures"] + ": " + totalInserted + "</div>" +
    "<div>" + bundle["UpdatedFeatures"] + ": " + totalUpdated + "</div>" +
    "<div>" + bundle["DeletedFeatures"] + ": " + totalDeleted + "</div>";
  return message;
}

function getErrorMessage(event)
{
  var responseText = event.response.priv.responseText;
  var index = responseText.indexOf("ExceptionText>");
  var error = "";
  if (index !== -1)
  {
    var index2 = responseText.indexOf("<", index + 14);
    if (index2 !== -1)
    {
      error = responseText.substring(index + 14, index2);
    }
  }
  return error;
}

function exportLayersInArea(polygon)
{
  var cqlPolygon = "POLYGON((";
  var ring = polygon.components[0];
  for (var i = 0; i < ring.components.length; i++)
  {
    var point = ring.components[i];
    if (i > 0) cqlPolygon += ", ";
    cqlPolygon += point.x + " " + point.y;
  }
  cqlPolygon += "))";

  for (i = 0; i < layers.length; i++)
  {
    var layer = layers[i];
    if (!layer.base && layer.visible && layer.locatable)
    {
      var serviceUrl = layer.url;
      var layerNameArray = layer.name.split(",");
      var layerFilter = layer.filter;
      for (var l = 0; l < layerNameArray.length; l++)
      {
        var layerName = layerNameArray[l];
        var featureInfo =
          OpenLayers.FeatureTypeInfo.getInfo(serviceUrl, layerName);
        if (featureInfo)
        {
          var buffer = '<div class="exportBlock"><div class="exportLayer">' +
            layer.label + " (" + layerName + "):</div>";
          var filter = "INTERSECTS(" + featureInfo.geometryName +
            ", " + cqlPolygon + ")";
          if (layerFilter != null && layerFilter.length > 0)
          {
            filter += " AND " + layerFilter;
          }
          buffer += '<div class="exportFormats">';
          for (var f = 0; f < exportFormats.length; f++)
          {
            var format = exportFormats[f];
            var url = serviceUrl +
              "&service=WFS&version=1.0.0&request=GetFeature&typeName=" +
              layerName + "&outputFormat=" + format +
              "&srsName=" + srs +
              "&cql_filter=" + encodeURIComponent(filter);
            buffer += '<a href="' + url + '" target="_blank">' +
              format + '</a> ';
          }
          buffer += "</div></div>";
          appendHTML(buffer);
        }
      }
    }
  }
}

function printMap()
{
  var printUrl = baseUrl + "/go.faces?xmid=" + printMid;
  if (printReports != null)
  {
    printUrl += "&reports=" + printReports;
  }
  if (lastLonLat != null)
  {
    printUrl += "&last_x=" + lastLonLat.lon + "&last_y=" + lastLonLat.lat;
  }
  printUrl += "&map_name=" + mapName +
    "&bbox=" + map.getExtent() +
    "&layer_visibility=" + getLayerVisibility();
  
  window.open(printUrl);
}

function showMapLink()
{
  setRightPanelVisible(true);

  var lon = map.getCenter().lon;
  var lat = map.getCenter().lat;
  var zoom = map.getZoom();

  var facesPage = isPublic ? "go.faces" : "login.faces";
  var linkUrl = baseUrl + "/" + facesPage + "?xmid=" + currentMid +
    "&map_name=" + mapName + "&map_lon=" + lon + "&map_lat=" + lat +
    "&map_zoom=" + zoom + "&layer_visibility=" + getLayerVisibility();

  setHTML('<div class="linkBox">' +
    '<a href="' + linkUrl + '" target="_blank" class="mapLink">' +
    '<img src="' +  baseUrl + '/plugins/mapviewer/img/link.png"><span>' +
    bundle["MapLink"] + 
    ':</span></a><textarea alt="url" spellcheck="false" class="mapLink" aria-label="url">' +
    linkUrl + '</textarea></div>');
}

function getLayerVisibility()
{
  var layerVisibility = "";
  for (var i = 0; i < layers.length; i++)
  {
    var layer = layers[i];
    if (layer.visible) layerVisibility += "1";
    else layerVisibility += "0";
  }
  return layerVisibility;
}

function saveMapState()
{
  var lon = map.getCenter().lon;
  var lat = map.getCenter().lat;
  var zoom = map.getZoom();
  setCookie("map_name", mapName);
  setCookie("map_lon", lon);
  setCookie("map_lat", lat);
  setCookie("map_zoom", zoom);
  setCookie("map_extent", "" + map.getExtent());
}

function restoreMapState()
{
  var paramString = window.location.search;
  if (paramString.indexOf("zoomextent") == -1) // restore last view
  {
    var name = urlParameters["map_name"];
    if (!name) name = getCookie("map_name");
    var lon = urlParameters["map_lon"];
    if (!lon) lon = getCookie("map_lon");
    var lat = urlParameters["map_lat"];
    if (!lat) lat = getCookie("map_lat");
    var zoom = urlParameters["map_zoom"];
    if (!zoom) zoom = getCookie("map_zoom");
    if (lon != null && lat != null && zoom != null && name == mapName)
    {
      map.setCenter(new OpenLayers.LonLat(lon, lat), zoom);
    }
  }
}

function setCookie(c_name, value)
{
  var exdate = new Date();
  exdate.setDate(exdate.getDate() + 3);
  var c_value = escape(value) + "; expires=" + exdate.toUTCString() + "; path=/";
  document.cookie = c_name + "=" + c_value;
}

function getCookie(c_name)
{
  var cks = document.cookie.split(";");
  var value = null;
  for (i = 0; i < cks.length; i++)
  {
    var cki = cks[i];
    var index = cki.indexOf("=");
    var name = cki.substr(0, index);
    name = name.replace(/^\s+|\s+$/g,"");
    if (name == c_name)
    {
      value = cki.substr(index + 1);
    }    
  }
  return value;
}

function isTouchDevice()
{
  return !!('ontouchstart' in window) // works on most browsers
      || !!('onmsgesturechange' in window); // works on ie10
}

function getInternetExplorerVersion()
{
  var rv = -1; // Return value assumes failure.
  if (navigator.appName == 'Microsoft Internet Explorer')
  {
    var ua = navigator.userAgent;
    var re  = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
    if (re.exec(ua) != null)
      rv = parseFloat(RegExp.$1);
  }
  return rv;
}

function groupClicked(groupName, button)
{
  button.className = button.className == 'expand' ? 'collapse' : 'expand';
  var elem = document.getElementById("group_" + groupName);
  if (elem.style.display == "none")
  {
    elem.style.display = "block";
  }
  else
  {
    elem.style.display = "none";
  }
}

function toggleLeftPanel()
{
  var leftPanel = document.getElementById("leftPanel");
  setLeftPanelVisible(leftPanel.style.display != 'block');
}

function setLeftPanelVisible(visible)
{
  var button = document.getElementById("toggleLeftPanelButton");
  var leftPanel = document.getElementById("leftPanel");
  var mapPanel = document.getElementById("map");
  if (visible) // show
  {
    leftPanel.style.display = 'block';
    leftPanel.style.width = leftPanelWidth;
    mapPanel.style.left = leftPanelWidth;
    button.src = "/plugins/mapviewer/img/left-panel-minimize.png";
  }
  else // hide
  {
    leftPanel.style.display = 'none';
    mapPanel.style.left = 0;
    button.src = "/plugins/mapviewer/img/left-panel-maximize.png";
  }
  map.updateSize();
}

function toggleRightPanel()
{
  var rightPanel = document.getElementById("rightPanel");
  setRightPanelVisible(rightPanel.style.display != 'block');
}

function setRightPanelVisible(visible)
{
  var button = document.getElementById("toggleRightPanelButton");
  var rightPanel = document.getElementById("rightPanel");
  var mapPanel = document.getElementById("map");
  if (visible) // show
  {
    rightPanel.style.display = 'block';
    rightPanel.style.width = rightPanelWidth;
    mapPanel.style.right = rightPanelWidth;
    button.src = "/plugins/mapviewer/img/right-panel-minimize.png";
  }
  else // hide
  {
    rightPanel.style.display = 'none';
    mapPanel.style.right = 0;
    button.src = "/plugins/mapviewer/img/right-panel-maximize.png";
  }
  map.updateSize();
}

function infoDividerUp()
{
  if (infoDividerPosition >= 50) infoDividerPosition -= 50;
  updateDivider();
}

function infoDividerDown()
{
  if (infoDividerPosition <= 50) infoDividerPosition += 50;
  updateDivider();
}

function updateDivider()
{
  var infoElem = document.getElementById("info");
  var legendElem = document.getElementById("legend");
  if (infoDividerPosition == 0)
  {
    infoElem.style.bottom = "100%";
    legendElem.style.top = "0%";
  }
  else if (infoDividerPosition == 50)
  {
    infoElem.style.bottom = "50%";
    legendElem.style.top = "50%";
  }
  else if (infoDividerPosition == 100)
  {
    infoElem.style.bottom = "0%";
    legendElem.style.top = "100%";
  }
}


function newFormSeed()
{
  formSeed = Math.floor(Math.random() * 100000000);
}

// load
if (document.addEventListener)
{
  document.addEventListener("DOMContentLoaded", initMapViewer, false);
}
else if (/WebKit/i.test(navigator.userAgent))
{
  var _timer = setInterval(function()
  {
    if (/loaded|complete/.test(document.readyState)) {
      initMapViewer(); // call the onload handler
    }
  }, 10);
}
else
{
  window.onload = initMapViewer;
}