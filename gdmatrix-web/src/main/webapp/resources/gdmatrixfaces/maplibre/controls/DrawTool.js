/* DrawTool.js */

import { Tool } from "./Tool.js";
import { Panel } from "../ui/Panel.js";
import { FeatureForm } from "../ui/FeatureForm.js";
import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class DrawTool extends Tool
{    
  constructor(options)
  {
    super({...{
            "title": bundle.get("DrawTool.title"),
            "iconClass": "pi pi-pencil",
            "position" : "right"
          }, ...options});

    this.service = options?.service || {
      url: "https://gis.santfeliu.cat/geoserver/wfs",
      useProxy: true
    };

    this.directEditing = options?.directEditing || false;
    this.centerFeatures = options?.centerFeatures || false;
    this.centerZoom = options?.centerZoom || 20;
    this.onAcceptFeature = options?.onAcceptFeature || null;

    this.operations = options?.operations;
    if (!this.operations)
    {
       this.operations = [
        { 
          name: "edit", 
          label: bundle.get("DrawTool.edit"),
          help: bundle.get("DrawTool.edit.help")
        },
        { 
          name: "move", 
          label: bundle.get("DrawTool.move"), 
          help: bundle.get("DrawTool.move.help")
        },
        { 
          name: "rotate", 
          label: bundle.get("DrawTool.rotate"),
          help: bundle.get("DrawTool.rotate.help")
        },
        { 
          name: "copy", 
          label: bundle.get("DrawTool.copy"), 
          help: bundle.get("DrawTool.copy.help") 
        },
        { 
          name: "addPoint", 
          label: bundle.get("DrawTool.addPoint"),
          help: bundle.get("DrawTool.addPoint.help") 
        },
        { 
          name: "addLineString", 
          label: bundle.get("DrawTool.addLineString"), 
          help: bundle.get("DrawTool.addLineString.help") 
        },
        { 
          name: "addPolygon", 
          label: bundle.get("DrawTool.addPolygon"), 
          help: bundle.get("DrawTool.addPolygon.help") 
        },
        { 
          name: "delete", 
          label: bundle.get("DrawTool.delete"),
          help: bundle.get("DrawTool.delete.help")
        }
      ];      
    }
    this.operationIndex = 0; // index to operations array
    this.layerName = options?.layerName || null;
    this.cqlFilter = null;
    this.layers = options.layers || []; 
    /*
      where each layer has these properties:
      {
        "layerName": "<WFS_LAYER_NAME>",
        "label": "<label>",
        "cqlFilter": "<cqlFilter>",
        "refreshSources: [<sourceId>, ...]
      }
    */
    this.maxFeatures = options?.maxFeatures || 5000;
    this.tolerance = options?.tolerance === undefined ? 5 : options.tolerance;
    this.wfsVersion = "1.1.0";
    this.featureInfo = null;
    this.featureForm = null;
    this.geometryType = null;
    this.editingVertex = null;
    this.editingRing = null;
    this.moveLastPoint = null;
    this.rotateLastLngLat = null;

    this.editingLayer = {
      "type": "FeatureCollection",
      "features": []
    };

    this.coordinates = [[]]; // array of Ring : array of [lng, lat]

    this.point = {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [] }
    };

    this.lineString = {
      "type": "Feature",
      "geometry": { "type": "MultiLineString", "coordinates": this.coordinates }
    };

    this.polygon = {
      "type": "Feature",
      "geometry": { "type": "Polygon", "coordinates": this.coordinates }
    };

    this._onClick = (event) => this.onClick(event);
    this._onDoubleClick = (event) => this.onDoubleClick(event);
    this._onPointerUp = (event) => this.onPointerUp(event);
    this._onPointerDown = (event) => this.onPointerDown(event);
    this._onPointerMove = (event) => this.onPointerMove(event);
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onClick);
    map.on("dblclick", this._onDoubleClick);
    map.on("mousedown", this._onPointerDown);
    map.on("mouseup", this._onPointerUp);
    map.on("touchstart", this._onPointerDown);
    map.on("touchend", this._onPointerUp);

    map.doubleClickZoom.disable();

    map.getCanvas().style.cursor = "crosshair";
    this.panel.show();

    // sources

    map.addSource("editing_features", {
      type: 'geojson',
      data: this.editingLayer
    });

    map.addSource("editing_point", {
      type: 'geojson',
      data: this.point
    });

    map.addSource("editing_linestring", {
      type: 'geojson',
      data: this.lineString,
      tolerance: 0
    });

    map.addSource("editing_polygon", {
      type: 'geojson',
      data: this.polygon,
      tolerance: 0
    });

    // layers

    map.addLayer({
      "id": "editing_features_fill",
      "type": 'fill',
      "source": "editing_features",
      "layout": {},
      "paint":
      {
        "fill-color": ["match", ["get", "_ACTION_"],
           "insert", "#8080f0",
           "update", "#8080b0",
           "delete", "#f08000",
           "#00f000"],
        "fill-outline-color": ["match", ["get", "_ACTION_"],
            "delete",
            "rgba(0, 128, 0, 0.3)",
            "#008000"
          ],
        "fill-opacity": ["match", ["get", "_ACTION_"],
            "delete",
            0.2,
            0.5
          ]
      },
      "filter": ["in", ["geometry-type"], "Polygon"]
    });

    map.addLayer({
      "id": "editing_features_line",
      "type": 'line',
      "source": "editing_features",
      "layout": {},
      "paint":
      {
        "line-color": ["match", ["get", "_ACTION_"],
           "insert", "#8080f0",
           "update", "#0000b0",
           "delete", "#f08000",
           "#008000"],
        "line-width": 2,
        "line-opacity": ["match", ["get", "_ACTION_"],
            "delete",
            0.2,
            1.0
          ]
      },
      "filter": ["in", ["geometry-type"], "LineString"]
    });

    map.addLayer({
      "id": "editing_features_circle",
      "type": 'circle',
      "source": "editing_features",
      "layout": {},
      "paint":
      {
        "circle-color": ["match", ["get", "_ACTION_"],
           "insert", "#8080f0",
           "update", "#0000b0",
           "delete", "#f08000",
           "#008000"],
        "circle-radius": 4,
        "circle-opacity": ["match", ["get", "_ACTION_"],
            "delete",
            0.2,
            1.0
          ]
      },
      "filter": ["in", ["geometry-type"], "Point"]
    });

    // highlight layers

    map.addLayer({
      "id": "editing_polygon",
      "type": 'fill',
      "source": "editing_polygon",
      "layout": {},
      "paint":
      {
        "fill-color": "#0000ff",
        "fill-opacity": 0.1
      }
    });

    map.addLayer({
      "id": "editing_linestring",
      "type": 'line',
      "source": "editing_linestring",
      "layout": {},
      "paint":
      {
        "line-color": "#0000ff",
        "line-width": 2
      }
    });

    map.addLayer({
      "id": "editing_points",
      "type": 'circle',
      "source": "editing_linestring",
      "layout": {},
      "paint":
      {
        "circle-color": "#ffffff",
        "circle-radius": 2,
        "circle-stroke-color" : "#000000",
        "circle-stroke-width" : 2
      }
    });

    map.addLayer({
      "id": "editing_point",
      "type": 'circle',
      "source": "editing_point",
      "layout": {},
      "paint":
      {
        "circle-color": "#000000",
        "circle-radius": 5
      }
    });
    
    if (this.directEditing)
    {
      const elem = document.getElementById("layer_name");
      elem.selectedIndex = 1;
    }
    
    if (this.editingLayer.features.length === 0)
    {
      this.loadSelectedLayer();
    }
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onClick);
    map.off("dblclick", this._onDoubleClick);
    map.off("mousedown", this._onPointerDown);
    map.off("mouseup", this._onPointerUp);
    map.off("touchstart", this._onPointerDown);
    map.off("touchend", this._onPointerUp);
    map.doubleClickZoom.enable();

    map.getCanvas().style.cursor = "grab";
    this.panel.hide();

    map.removeLayer("editing_features_fill");
    map.removeLayer("editing_features_line");
    map.removeLayer("editing_features_circle");
    map.removeSource("editing_features");

    map.removeLayer("editing_polygon");
    map.removeSource("editing_polygon");

    map.removeLayer("editing_linestring");
    map.removeLayer("editing_points");
    map.removeSource("editing_linestring");

    map.removeLayer("editing_point");
    map.removeSource("editing_point");
  }

  reactivate()
  {
    this.panel.show();
  }

  getOperation()
  {
    return this.operations[this.operationIndex];
  }
  
  onClick(event)
  {
    if (!this.featureInfo) return;

    let operation = this.getOperation();
    if (operation.name === "addRectangle")
    {
      if (this.featureForm === null)
      {
        this.addFeature();
      }      
      this.addRectangle(event.lngLat);
    }
    else if (operation.name.startsWith("add"))
    {
      if (this.featureForm === null)
      {
        this.addFeature();
      }
      this.addPoint(event.lngLat);
    }
    else if (operation.name === "delete")
    {
      this.deleteFeature(event.point);
    }
    else    
    {
      if (this.featureForm === null)
      {
        this.editFeature(event.point);
      }
    }
  }

  onDoubleClick(event)
  {
    if (this.getOperation().name === "edit" && this.geometryType)
    {      
      this.insertOrRemovePoint(event);
    }
  }

  onPointerDown(event)
  {
    const map = this.map;
    const operation = this.getOperation();

    if (this.geometryType)
    {
      if (operation.name === "edit")
      {
        if (this.findVertex(event))
        {
          map.dragPan.disable();
          map.on("mousemove", this._onPointerMove);
          map.on("touchmove", this._onPointerMove);
        }
      }
      else if (operation.name === "move" || operation.name === "copy")
      {
        if (this.isOnEditingGeometry(event.point))
        {
          this.moveLastPoint = event.point;
          map.dragPan.disable();
          map.on("mousemove", this._onPointerMove);
          map.on("touchmove", this._onPointerMove);
        }
      }
      else if (operation.name === "rotate" && this.geometryType !== "Point")
      {
        if (this.isOnEditingGeometry(event.point))
        {
          this.rotateLastLngLat = [event.lngLat.lng, event.lngLat.lat];
          map.dragPan.disable();
          map.on("mousemove", this._onPointerMove);
          map.on("touchmove", this._onPointerMove);
        }
      }
    }
  }

  onPointerUp(event)
  {
    const map = this.map;

    this.editingVertex = null;
    this.editingRing = null;
    this.moveLastPoint = null;

    map.dragPan.enable();
    map.off("mousemove", this._onPointerMove);
    map.off("touchmove", this._onPointerMove);
  }

  onPointerMove(event)
  {
    const map = this.map;
    const operation = this.getOperation();

    if (event.originalEvent?.touches?.length > 1)
      return;

    if (operation.name === "edit")
    {
      if (this.editingVertex)
      {
        const ring = this.editingRing;
        const lngLat = event.lngLat;
        this.editingVertex[0] = lngLat.lng;
        this.editingVertex[1] = lngLat.lat;
        if (this.geometryType === "Polygon" &&
            this.editingVertex === ring[0])
        {
          const lastVertex = ring[ring.length - 1];
          lastVertex[0] = lngLat.lng;
          lastVertex[1] = lngLat.lat;
        }
        map.getSource("editing_point").setData(this.point);
        map.getSource("editing_linestring").setData(this.lineString);
        map.getSource("editing_polygon").setData(this.polygon);
      }
    }
    else if (operation.name === "move" || operation.name === "copy")
    {
      const coordinates = this.coordinates;
      const vx = event.point.x - this.moveLastPoint.x;
      const vy = event.point.y - this.moveLastPoint.y;
      for (let ring of coordinates)
      {
        for (let lngLat of ring)
        {
          let projected = map.project(lngLat);
          projected.x += vx;
          projected.y += vy;
          let newLngLat = map.unproject(projected);
          lngLat[0] = newLngLat.lng;
          lngLat[1] = newLngLat.lat;
        }
      }
      map.getSource("editing_point").setData(this.point);
      map.getSource("editing_linestring").setData(this.lineString);
      map.getSource("editing_polygon").setData(this.polygon);
      this.moveLastPoint = event.point;
    }
    else if (operation.name === "rotate")
    {
      const lngLat = [event.lngLat.lng, event.lngLat.lat];
      
      const getAngle = (lngLat1, lngLat2) => {
        const p1 = turf.toMercator(lngLat1);
        const p2 = turf.toMercator(lngLat2);
        const dx = p1[0] - p2[0];
        const dy = p1[1] - p2[1];
        return turf.radiansToDegrees(Math.atan2(dy, dx));
      };
      
      if (this.geometryType === "Polygon")
      {
        const poly = turf.polygon(this.coordinates);
        const centroid = turf.centroid(poly);
        const angle1 = getAngle(this.rotateLastLngLat, centroid.geometry.coordinates);
        const angle2 = getAngle(lngLat, centroid.geometry.coordinates);
        const angle = angle1 - angle2;
        let rotatedPoly = turf.transformRotate(poly, angle);
        this.coordinates = rotatedPoly.geometry.coordinates;
        this.lineString.geometry.coordinates = this.coordinates;
        this.polygon.geometry.coordinates = this.coordinates;
      }
      else if (this.geometryType === "LineString")
      {
        const poly = turf.multiLineString(this.coordinates);
        const centroid = turf.centroid(poly);
        const angle1 = getAngle(this.rotateLastLngLat, centroid.geometry.coordinates);
        const angle2 = getAngle(lngLat, centroid.geometry.coordinates);
        const angle = angle1 - angle2;
        let rotatedPoly = turf.transformRotate(poly, angle);
        this.coordinates = rotatedPoly.geometry.coordinates;        
        this.lineString.geometry.coordinates = this.coordinates;
      }
      map.getSource("editing_linestring").setData(this.lineString);
      map.getSource("editing_polygon").setData(this.polygon);
      this.rotateLastLngLat = lngLat;
    }
  }

  addFeature()
  {
    this.showFeatureForm();
    const operation = this.getOperation();
    if (operation.name === "addLineString" || operation.name === "addPolygon")
    {
      this.panel.undoButton.style.display = "";
    }
    this.panel.acceptButton.style.display = "";
    this.panel.cancelButton.style.display = "";
  }

  editFeature(point)
  {
    const feature = this.findFeatureByPoint(point);
    if (feature)
    {      
      this.showFeatureForm(feature);
      this.panel.acceptButton.style.display = "";
      this.panel.cancelButton.style.display = "";
      this.editGeometry(feature.geometry);
    }
    else
    {
      this.panel.formDiv.innerHTML = "";
    }
  }

  deleteFeature(point)
  {
    const feature = this.findFeatureByPoint(point);
    if (feature)
    {
      feature.properties = feature.properties || {}; // TODO: required?
      feature.properties._ACTION_ = "delete";
      this.map.getSource("editing_features").setData(this.editingLayer);
      this.panel.formDiv.innerHTML = "";

      if (this.directEditing) this.saveFeatures();
    }
  }

  acceptFeature()
  {
    const featureForm = this.featureForm;

    if (!featureForm) return;

    if (typeof this.onAcceptFeature === "function")
    {
      this.onAcceptFeature(featureForm);
    }

    const featureInfo = this.featureInfo;
    const idColumn = featureInfo.idColumn;
    const feature = featureForm.feature;
    const previousAction = feature.properties._ACTION_ || "none";

    if (this.isValidEditingGeometry())
    {
      this.featureForm.updateProperties(featureInfo);

      let action;

      let id = feature.properties[idColumn];
      if (id === undefined)
      {
        id = Math.round(Math.random() * 100000) + 100000;
        feature.properties[idColumn] = id;
        feature.geometry = this.createGeometry();
        this.editingLayer.features.push(feature);
        action = "insert";
      }
      else
      {
        id = feature.properties[idColumn];
        feature.geometry = this.createGeometry();
        const index = this.findFeatureIndexById(id);
        this.editingLayer.features[index] = feature;
        if (previousAction === "insert") action = "insert";
        else if (previousAction === "delete") action = "insert"; // TODO: may duplicate
        else action = "update";
      }

      if (id)
      {
        feature.properties._ACTION_ = action;
        this.map.getSource("editing_features").setData(this.editingLayer);
      }
      if (this.directEditing) this.saveFeatures();
    }
    this.cancelFeature();
  }

  cancelFeature()
  {
    this.resetGeometries();
    this.panel.formDiv.innerHTML = "";
    this.featureForm = null;
    this.panel.acceptButton.style.display = "none";
    this.panel.cancelButton.style.display = "none";
    this.panel.undoButton.style.display = "none";
  }

  changeOperation()
  {
    const map = this.map;
    const operation = this.getOperation();
    if (operation.name === "edit")
    {
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "move")
    {
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "rotate")
    {
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "copy")
    {
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "addPoint")
    {
      this.geometryType = "Point";
      this.updateLayerVisibility();
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "addLineString")
    {
      this.geometryType = "LineString";
      this.updateLayerVisibility();
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "addPolygon")
    {
      this.geometryType = "Polygon";
      this.updateLayerVisibility();
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "addRectangle")
    {
      this.geometryType = "Polygon";
      this.updateLayerVisibility();
      this.panel.helpDiv.textContent = operation.help;
    }
    else if (operation.name === "delete")
    {
      this.panel.helpDiv.textContent = operation.help;
    }
    this.panel.undoButton.style.display = "none";
    if (!this.geometryType || 
        operation.name.startsWith("add") ||
        operation.name === "delete")
    {
      this.panel.acceptButton.style.display = "none";
      this.panel.cancelButton.style.display = "none";
    }
    this.panel.formDiv.innerHTML = "";
  }

  showSaveResult(result)
  {
    if (result.error)
    {
      this.panel.formDiv.innerHTML = `
       <div style="color:#ff0000">ERROR: ${result.error}</div>
     `;
    }
    else
    {
      let messages = [];
      if (result.totalInserted > 0)
      {
        messages.push(`<div>${bundle.get("DrawTool.totalInserted", result.totalInserted)}</div>`);
      }
      if (result.totalUpdated > 0)
      {
        messages.push(`<div>${bundle.get("DrawTool.totalUpdated", result.totalUpdated)}</div>`);
      }
      if (result.totalDeleted > 0)
      {
        messages.push(`<div>${bundle.get("DrawTool.totalDeleted", result.totalDeleted)}</div>`);
      }      
      this.panel.formDiv.innerHTML = messages.join("");
    }
  }

  updateLayerVisibility()
  {
    const map = this.map;
    const geometryType = this.geometryType;
    const operation = this.getOperation();

    map.setPaintProperty("editing_points", "circle-stroke-color",
      operation.name === "edit" ? "#800000" : "#000000");

    map.setLayoutProperty("editing_linestring", "visibility",
      geometryType === "Point" ? "hidden" : "visible");

    map.setLayoutProperty("editing_points", "visibility",
      geometryType === "Point" ? "hidden" : "visible");

    map.setLayoutProperty("editing_polygon", "visibility",
      geometryType === "Polygon" ? "visible" : "hidden");
  }

  findFeatureByPoint(point)
  {
    let feature;
    const id = this.findFeatureIdByPoint(point);
    if (id)
    {
      const index = this.findFeatureIndexById(id);
      feature = this.editingLayer.features[index];
    }
    else feature = null;

    return feature;
  }

  findFeatureIdByPoint(point)
  {
    if (this.featureInfo)
    {
      const tolerance = this.tolerance;
      const bbox = [
        [point.x - tolerance, point.y - tolerance],
        [point.x + tolerance, point.y + tolerance]
      ];
      
      let features = this.map.queryRenderedFeatures(bbox);
      for (let feature of features)
      {
        if (feature.source === "editing_features")
        {
          const idColumn = this.featureInfo.idColumn;
          return feature.properties[idColumn];
        }
      }
    }
    return null;
  }

  findFeatureIndexById(id)
  {
    if (this.featureInfo)
    {
      const idColumn = this.featureInfo.idColumn;
      const features = this.editingLayer.features;
      for (let index = 0; index < features.length; index++)
      {
        let feature = features[index];
        if (feature.properties[idColumn] === id) return index;
      }
    }
    return -1;
  }

  // form methods

  showFeatureForm(feature)
  {
    if (feature === undefined)
    {
      feature = {
        "type": "Feature",
        "geometry" : null,
        "properties": {}
      };
    }
    else
    {
      feature = this.cloneJson(feature);
      const operation = this.getOperation();
      if (operation.name === "copy")
      {
        delete feature.properties[this.featureInfo.idColumn];
      }
    }
    this.featureForm = new FeatureForm(feature);
    this.featureForm.layerName = this.layerName;
    this.featureForm.forEdit = true;
    this.featureForm.setFormSelectorAndPriority(this.map);
    this.renderFeatureForm();
  }

  async renderFeatureForm()
  {
    const form = await this.featureForm.render();
    this.panel.formDiv.innerHTML = "";
    this.panel.formDiv.appendChild(form.getElement());
  }

  // geometry methods

  isValidEditingGeometry()
  {
    const geometryType = this.geometryType;
    const ring = this.coordinates[0];
    if (geometryType === "Point")
    {
      return ring.length === 1;
    }
    else if (geometryType === "LineString")
    {
      return ring.length >= 2;
    }
    else if (geometryType === "Polygon")
    {
      return ring.length >= 4;
    }
    return false;
  }

  resetGeometries()
  {
    const map = this.map;
    this.coordinates = [[]];

    this.point.geometry.coordinates = [];
    map.getSource("editing_point").setData(this.point);

    this.lineString.geometry.coordinates = this.coordinates;
    map.getSource("editing_linestring").setData(this.lineString);

    this.polygon.geometry.coordinates = this.coordinates;
    map.getSource("editing_polygon").setData(this.polygon);
  }

  createGeometry()
  {
    const geometryType = this.geometryType;
    let geometry;
    if (geometryType === "Point")
    {
      geometry = {
        "type" : "Point",
        "coordinates": [...this.coordinates[0][0]]
      };
    }
    else if (geometryType === "LineString")
    {
      geometry = {
        "type" : "LineString",
        "coordinates": this.cloneJson(this.coordinates[0])
      };
    }
    else if (geometryType === "Polygon")
    {
      geometry = {
        "type" : "Polygon",
        "coordinates": this.cloneJson(this.coordinates)
      };
    }
    return geometry;
  }

  editGeometry(geometry)
  {
    const map = this.map;
    const geometryType = geometry.type;
    this.geometryType = geometryType;
    if (geometryType === "Point")
    {
      this.coordinates = [[this.cloneJson(geometry.coordinates)]];
      this.point.geometry.coordinates = this.coordinates[0][0];
      map.getSource("editing_point").setData(this.point);
    }
    else if (geometryType === "LineString")
    {
      this.coordinates = [this.cloneJson(geometry.coordinates)];
      this.lineString.geometry.coordinates = this.coordinates;
      map.getSource("editing_linestring").setData(this.lineString);
    }
    else if (geometryType === "Polygon")
    {
      this.coordinates = this.cloneJson(geometry.coordinates);
      this.lineString.geometry.coordinates = this.coordinates;
      this.polygon.geometry.coordinates = this.coordinates;
      map.getSource("editing_linestring").setData(this.lineString);
      map.getSource("editing_polygon").setData(this.polygon);
    }
    this.updateLayerVisibility();
  }

  addRectangle(lngLat)
  {
    const map = this.map;
    const operation = this.getOperation();
    const halfWidth = 0.5 * (operation.width || 1);
    const halfHeight = 0.5 * (operation.height || 1);
    const point = turf.toMercator([lngLat.lng, lngLat.lat]);
    this.coordinates[0] = [];
    const ring = this.coordinates[0];

    const x = point[0];
    const y = point[1];
    ring.push(turf.toWgs84([x - halfWidth, y - halfHeight]));
    ring.push(turf.toWgs84([x + halfWidth, y - halfHeight]));
    ring.push(turf.toWgs84([x + halfWidth, y + halfHeight]));
    ring.push(turf.toWgs84([x - halfWidth, y + halfHeight]));
    ring.push(turf.toWgs84([x - halfWidth, y - halfHeight]));

    map.getSource("editing_polygon").setData(this.polygon);
    map.getSource("editing_linestring").setData(this.lineString);    
  }

  addPoint(lngLat)
  {
    const map = this.map;
    const ring = this.coordinates[0];
    const geometryType = this.geometryType;
    const point = [lngLat.lng, lngLat.lat];

    if (ring.length === 0)
    {
      this.point.geometry.coordinates = point;
      map.getSource("editing_point").setData(this.point);
    }

    if (geometryType === "Point")
    {
      if (ring.length === 0) ring.push(point);
    }
    else if (geometryType === "LineString")
    {
      ring.push(point);
      map.getSource("editing_linestring").setData(this.lineString);
    }
    else if (geometryType === "Polygon")
    {
      if (ring.length > 0) ring.pop();
      ring.push(point);
      ring.push(ring[0]);
      map.getSource("editing_polygon").setData(this.polygon);
      map.getSource("editing_linestring").setData(this.lineString);
    }
    this.panel.show();
  }

  removePoint()
  {
    const map = this.map;
    const ring = this.coordinates[0];
    const geometryType = this.geometryType;

    if (ring.length > 0)
    {
      ring.pop();
      if (geometryType === "Polygon")
      {
        ring.pop();
        if (ring.length > 0)
        {
          ring.push(ring[0]);
        }
        this.map.getSource("editing_polygon").setData(this.polygon);
        this.map.getSource("editing_linestring").setData(this.lineString);
      }
      else if (geometryType === "LineString")
      {
        map.getSource("editing_linestring").setData(this.lineString);
      }

      if (ring.length === 0)
      {
        this.point.geometry.coordinates = [];
        this.map.getSource("editing_point").setData(this.point);
      }
    }
  }

  findVertex(event)
  {
    const map = this.map;
    const point = event.point;
    const originalEvent = event.originalEvent;
    const margin = originalEvent instanceof MouseEvent ? 8 : 24;

    for (let ring of this.coordinates)
    {
      for (let lngLat of ring)
      {
        let projected = map.project(lngLat);

        let distance = projected.dist(point);
        if (distance <= margin)
        {
          this.editingVertex = lngLat;
          this.editingRing = ring;
          return true;
        }
      }
    }
    return false;
  }

  isOnEditingGeometry(point)
  {
    const map = this.map;

    const tolerance = this.tolerance;
    const bbox = [
      [point.x - tolerance, point.y - tolerance],
      [point.x + tolerance, point.y + tolerance]
    ];

    let features = map.queryRenderedFeatures(bbox);

    for (let feature of features)
    {
      if (feature.source === "editing_point" ||
          feature.source === "editing_linestring" ||
          feature.source === "editing_polygon")
      {
        return true;
      }
    }
    return false;
  }

  insertOrRemovePoint(event)
  {
    const map = this.map;
    const point0 = event.point;
    const originalEvent = event.originalEvent;
    const margin = originalEvent instanceof MouseEvent ? 8 : 24;
    const geometryType = this.geometryType;

    if (geometryType !== "LineString" && geometryType !== "Polygon") return;

    let editingRing = null;
    let removeIndex = -1;
    let insertIndex = -1;
    let insertDist = 10000000;

    for (let ring of this.coordinates)
    {
      for (let i = 0; i < ring.length - 1; i++)
      {
        let point1 = map.project(ring[i]);
        let point2 = map.project(ring[i + 1]);
        let result = this.distanceToEdge(point0, point1, point2, margin);
        if (result.position === "start")
        {
          editingRing = ring;
          removeIndex = i;
          break;
        }
        else if (result.position === "end")
        {
          editingRing = ring;
          removeIndex = i + 1;
          break;
        }
        else if (result.position === "middle")
        {
          let dist = result.distance;
          if (dist < insertDist)
          {
            editingRing = ring;
            insertIndex = i + 1;
            insertDist = dist;
          }
        }
      }
    }
    if (removeIndex !== -1)
    {
      if (geometryType === "LineString")
      {
        if (editingRing.length > 2)
        {
          editingRing.splice(removeIndex, 1);
        }
      }
      else // Polygon
      {
        if (this.coordinates[0] === editingRing) // exterior ring
        {
          if (editingRing.length > 4)
          {
            editingRing.splice(removeIndex, 1);
            if (removeIndex === 0)
            {
              editingRing[editingRing.length - 1] = editingRing[0];
            }            
          }
        }
        else // interior ring
        {
          if (editingRing.length > 4)
          {
            editingRing.splice(removeIndex, 1);
            if (removeIndex === 0)
            {
              editingRing[editingRing.length - 1] = editingRing[0];
            }            
          }
          else // remove hole
          {
            let ringIndex = this.coordinates.indexOf(editingRing);
            this.coordinates.splice(ringIndex, 1);
          }          
        }
      }
    }
    else if (insertIndex !== -1)
    {
      const lngLat = event.lngLat;
      editingRing.splice(insertIndex, 0, [lngLat.lng, lngLat.lat]);
    }

    if (removeIndex !== -1 || insertIndex !== -1)
    {
      map.getSource("editing_linestring").setData(this.lineString);
      if (geometryType === "Polygon")
      {
        map.getSource("editing_polygon").setData(this.polygon);
      }
    }
  }

  distanceToEdge(point0, point1, point2, margin)
  {
    const dist01 = point0.dist(point1);
    if (dist01 < margin) return { position: "start", distance: dist01 };

    const dist02 = point0.dist(point2);
    if (dist02 < margin) return { position: "end", distance: dist02 };

    const dist12 = point1.dist(point2);
    if (dist01 > dist12 || dist02 > dist12) return { position: "out" };

    const v10x = (point0.x - point1.x);
    const v10y = (point0.y - point1.y);

    const v12x = (point2.x - point1.x) / dist12;
    const v12y = (point2.y - point1.y) / dist12;

    const dot = v10x * v12x + v10y * v12y;
    const mx = point1.x + v12x * dot;
    const my = point1.y + v12y * dot;
    const dx = point0.x - mx;
    const dy = point0.y - my;
    const distance = Math.sqrt(dx * dx + dy * dy);

    return { position: "middle", distance: distance, dot: dot };
  }

  // utility methods

  cloneJson(object)
  {
    return JSON.parse(JSON.stringify(object));
  }

  // WFS methods

  async loadFeatures()
  {
    const map = this.map;
    
    if (!this.layerName || this.layerName.trim().length === 0)
    {
      this.editingLayer = { 
        "type": "FeatureCollection", 
        "features": [] 
      };
    }
    else
    {
      this.featureInfo = await FeatureTypeInspector.getInfo(
        this.service.url, this.layerName);

      let url = "/proxy?url=" + this.service.url + "&" +
        "request=GetFeature" +
        "&service=WFS" +
        "&version=2.0.0" +
        "&typeNames=" + this.layerName +
        "&srsName=EPSG:4326" +
        "&outputFormat=application/json" +
        "&exceptions=application/json" +
        "&count=" + this.maxFeatures;

      if (this.cqlFilter !== null)
      {
        url += "&cql_filter=" + encodeURIComponent(this.cqlFilter);
      }

      const response = await fetch(url);
      const json = await response.json();
      if (json.exceptions) throw json;

      this.editingLayer = json;
      for (let feature of this.editingLayer.features)
      {
        feature.properties._ACTION_ = "none";
      }
    }
    map.getSource("editing_features").setData(this.editingLayer);
  }

  async saveFeatures()
  {
    const service = this.service;
    const wfsVersion = this.wfsVersion;
    const namespace = this.featureInfo.namespace;
    const typeName = this.featureInfo.name;
    const idColumn =  this.featureInfo.idColumn;
    const geometryColumn = this.featureInfo.geometryColumn;

    let array = [];
    for (let feature of this.editingLayer.features)
    {
      let action = feature.properties._ACTION_;
      if (action === "insert")
      {
        let gmlProperties = this.insertPropertiesToGML(feature.properties);
        let gmlGeometry = this.geometryToGML(feature.geometry);
        let block  = `
          <wfs:Insert>
          <feature:${typeName} xmlns:feature="${namespace}">
            ${gmlProperties}
            <feature:${geometryColumn}>
              ${gmlGeometry}
            </feature:${geometryColumn}>
          </feature:${typeName}>
          </wfs:Insert>
        `;
        array.push(block);
      }
      else if (action === "update")
      {
        let id = feature.properties[idColumn];
        let gmlProperties = this.updatePropertiesToGML(feature.properties);
        let gmlGeometry = this.geometryToGML(feature.geometry);
        let block  = `
          <wfs:Update xmlns:feature="${namespace}" typeName="feature:${typeName}">
            ${gmlProperties}
            <wfs:Property>
              <wfs:Name>${geometryColumn}</wfs:Name>
              <wfs:Value>${gmlGeometry}</wfs:Value>
            </wfs:Property>
            <ogc:Filter>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>feature:${idColumn}</ogc:PropertyName>
                <ogc:Literal>${id}</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Filter>
          </wfs:Update>
        `;
        array.push(block);
      }
      else if (action === "delete")
      {
        let id = feature.properties[idColumn];
        let block  = `
          <wfs:Delete xmlns:feature="${namespace}" typeName="feature:${typeName}">
            <ogc:Filter>
              <ogc:PropertyIsEqualTo>
                <ogc:PropertyName>feature:${idColumn}</ogc:PropertyName>
                <ogc:Literal>${id}</ogc:Literal>
              </ogc:PropertyIsEqualTo>
            </ogc:Filter>
          </wfs:Delete>
        `;
        array.push(block);
      }
    }

    let request =
    `<wfs:Transaction xmlns:wfs="http://www.opengis.net/wfs"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xmlns:ogc="http://www.opengis.net/ogc"
                      service="WFS" version="${wfsVersion}"
                      xsi:schemaLocation="http://www.opengis.net/wfs http://schemas.opengis.net/wfs/${wfsVersion}/wfs.xsd">
      ${array.join("")}
    </wfs:Transaction>`;

    console.info(request);

    let response = await fetch("/proxy?url=" + service.url,
    {
      "method": "POST",
      "mode": "cors",
      "cache": "no-cache",
      "headers": { "Content-Type": "application/gml+xml" },
      "body": request
    });

    let responseText = await response.text();
    let result = this.parseTransactionResult(responseText);
    this.showSaveResult(result);

    await this.loadFeatures();
    
    this.updateSources(typeName);
  }

  insertPropertiesToGML(properties)
  {
    const array = [];
    const definedProperties = this.featureInfo.properties;
    for (let definedProperty of definedProperties)
    {
      let propName = definedProperty.name;
      if (propName !== this.featureInfo.geometryColumn)
      {
        let value = properties[propName];
        if (value !== undefined && value !== null)
        {
          array.push(`
          <feature:${propName}>${value}</feature:${propName}>
          `);
        }
      }
    }
    return array.join("");
  }

  updatePropertiesToGML(properties)
  {
    const array = [];
    const definedProperties = this.featureInfo.properties;
    for (let definedProperty of definedProperties)
    {
      let propName = definedProperty.name;
      if (propName !== this.featureInfo.geometryColumn &&
          propName !== this.featureInfo.idColumn)
      {
        let value = properties[propName];
        if (value !== undefined && value !== null)
        {
          array.push(`
          <wfs:Property>
            <wfs:Name>${propName}</wfs:Name>
            <wfs:Value>${value}</wfs:Value>
          </wfs:Property>
          `);
        }
      }
    }
    return array.join("");
  }

  geometryToGML(geometry)
  {
    const type = geometry.type;
    const array = [];

    if (type === "Point")
    {
      return `
        <gml:Point xmlns:gml="http://www.opengis.net/gml" srsName="EPSG:4326" srcDimension="2">
          <gml:pos>
            ${geometry.coordinates[0]} ${geometry.coordinates[1]}
          </gml:pos>
        </gml:Point>
      `;
    }
    else if (type === "LineString")
    {
      for (let coords of geometry.coordinates)
      {
        array.push(coords.join(" "));
      }
      const gmlCoordinates = array.join(" ");
      return `
        <gml:LineString xmlns:gml="http://www.opengis.net/gml" srsName="EPSG:4326" srcDimension="2">
          <gml:posList>
            ${gmlCoordinates}
          </gml:posList>
        </gml:LineString>
      `;
    }
    else if (type === "Polygon")
    {
      const rings = geometry.coordinates;
      for (let i = 0; i < rings.length; i++)
      {
        const coordsArray = [];
        let ring = rings[i];
        for (let coords of ring)
        {
          coordsArray.push(coords.join(" "));
        }
        const gmlCoordinates = coordsArray.join(" ");

        let gmlRing = `
          <gml:LinearRing>
            <gml:posList>
              ${gmlCoordinates}
            </gml:posList>
          </gml:LinearRing>
        `;
        if (i === 0)
        {
          array.push(`
          <gml:exterior>
          ${gmlRing}
          </gml:exterior>
          `);
        }
        else
        {
          array.push(`
          <gml:interior>
          ${gmlRing}
          </gml:interior>
          `);
        }
      }

      return `
        <gml:Polygon xmlns:gml="http://www.opengis.net/gml" srsName="EPSG:4326" srcDimension="2">
          ${array.join("")}
        </gml:Polygon>
      `;
    }
  }

  parseTransactionResult(responseText)
  {
    const result = {};
    const parser = new DOMParser();
    const xmlDoc = parser.parseFromString(responseText, "application/xml");
    const owsNS = "http://www.opengis.net/ows";
    const wfsNS = "http://www.opengis.net/wfs";

    let nodes;

    nodes = xmlDoc.getElementsByTagNameNS(owsNS, "ExceptionText");
    if (nodes.length > 0)
    {
      result.error = nodes[0].textContent;
    }

    nodes = xmlDoc.getElementsByTagNameNS(wfsNS, "totalInserted");
    if (nodes.length > 0)
    {
      result.totalInserted = parseInt(nodes[0].textContent);
    }
    nodes = xmlDoc.getElementsByTagNameNS(wfsNS, "totalUpdated");
    if (nodes.length > 0)
    {
      result.totalUpdated = parseInt(nodes[0].textContent);
    }
    nodes = xmlDoc.getElementsByTagNameNS(wfsNS, "totalDeleted");
    if (nodes.length > 0)
    {
      result.totalDeleted = parseInt(nodes[0].textContent);
    }
    return result;
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
    this.panel.onHide = () => this.deactivateTool(this);

    const bodyDiv = this.panel.bodyDiv;
    bodyDiv.innerHTML = `
      <div class="formgrid grid">
        <div class="field col-12 layer_selector">
        </div>
        <div class="field col-12 button_bar">
          <button class="load_features">${bundle.get("button.load")}</button>
          <button class="save_features">${bundle.get("button.save")}</button>
        </div>
        <div class="field col-12">
          <label for="operation">${bundle.get("DrawTool.operation")}:</label>
          <select id="operation" class="w-full">
          </select>
        </div>
        <div class="field col-12 help_message"></div>
        <div class="field col-12 button_bar">
          <button class="undo_vertex">${bundle.get("button.undo")}</button>
          <button class="accept_feature">${bundle.get("button.accept")}</button>
          <button class="cancel_feature">${bundle.get("button.cancel")}</button>
        </div>
        <div class="field col-12 feature_form">
        </div>
      </div>
    `;

    const layerSelector = bodyDiv.querySelector(".layer_selector");
    if (this.layers.length === 0)
    {
      layerSelector.innerHTML = `
        <label for="layer_name">${bundle.get("DrawTool.layerName")}:</label>
        <input type="text" id="layer_name" class="w-full" value="${this.layerName || ''}" /> 
      `;
    }
    else
    {
      layerSelector.innerHTML = `
        <label for="layer_name">${bundle.get("DrawTool.layerName")}:</label>
        <select id="layer_name" class="w-full" value="${this.layerName || ''}">
          <option></option>
        </select>
      `;
      let select = document.getElementById("layer_name");
      for (let layer of this.layers)
      {
        let option = document.createElement("option");
        option.value = layer.layerName;
        option.textContent = layer.label || layer.layerName;
        select.appendChild(option);
      }
      select.value = this.layerName || null;
      select.addEventListener("change", (e) => {
        e.preventDefault();
        this.loadSelectedLayer();
      });    
    }
    if (this.directEditing)
    {
      layerSelector.style.display = "none";
      const buttonBar = bodyDiv.querySelector(".button_bar");
      buttonBar.style.display = "none";
    }

    const loadButton = bodyDiv.querySelector(".load_features");
    loadButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.loadSelectedLayer();
    });

    const saveButton = bodyDiv.querySelector(".save_features");
    saveButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.acceptFeature();
      this.saveFeatures();
    });

    const operationSelect = bodyDiv.querySelector("#operation");
    for (let operation of this.operations)
    {
      let option = document.createElement("option");
      option.value = operation.name;
      option.textContent = operation.label;
      operationSelect.appendChild(option);
    }

    operationSelect.addEventListener("change", () => {
      this.operationIndex = operationSelect.selectedIndex;
      const operation = this.getOperation();
      if (["move", "rotate", "edit"].indexOf(operation.name) === -1)
      {
        this.acceptFeature();
      }
      this.changeOperation();
    });

    this.panel.undoButton = bodyDiv.querySelector(".undo_vertex");
    this.panel.undoButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.removePoint();
    });

    this.panel.acceptButton = bodyDiv.querySelector(".accept_feature");
    this.panel.acceptButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.acceptFeature();
    });

    this.panel.cancelButton = bodyDiv.querySelector(".cancel_feature");
    this.panel.cancelButton.addEventListener("click", (e) => {
      e.preventDefault();
      this.cancelFeature();
    });

    this.panel.helpDiv = bodyDiv.querySelector(".help_message");
    this.panel.formDiv = bodyDiv.querySelector(".feature_form");

    this.changeOperation();
  }
  
  async loadSelectedLayer()
  {
    const elem = document.getElementById("layer_name");
    this.layerName = elem.value;
    this.cqlFilter = null;
    if (typeof elem.selectedIndex === "number")
    {
      let index = elem.selectedIndex - 1;
      if (index >= 0)
      {
        this.cqlFilter = this.layers[index].cqlFilter || null;
      }
    }
    this.cancelFeature();
    await this.loadFeatures();
    if (this.centerFeatures) this.centerEditingFeatures();
  };
  
  centerEditingFeatures()
  {
    if (this.editingLayer.features.length > 0)
    {
      const map = this.map;
      const center = turf.center(this.editingLayer);
      map.flyTo({ center: center.geometry.coordinates, zoom: this.centerZoom });
    }
  }
  
  updateSources()
  {
    const map = this.map;
    const sources = map.getStyle().sources;
    const layers = this.layers;
    const seed = "_seed=" + Math.random();

    for (let layer of layers)
    {
      if (layer.layerName === this.layerName)
      {
        let refreshSources = layer.refreshSources || [];
        for (let sourceId of refreshSources)
        {
          const source = sources[sourceId];  
          let url = source.data;
          if (url.indexOf("?") === -1)
          {
            url += "?" + seed;
          }
          else
          {
            url += "&" + seed;
          }
          console.info("Refresh " + sourceId);
          map.getSource(sourceId).setData(url);
        }
      }
    }
  }
}

export { DrawTool };
