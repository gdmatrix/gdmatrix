/** SimulateRouteControl.js **/

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class SimulateRouteControl
{
  constructor(options)
  {
    this.options = options || {};

    this.point = {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [] },
      "properties": { "bearing" : 0 }
    };

    this.routeLayerIds = this.options.layers || [];
    this.routeViews = this.options.views || ["manual", "centered", "navigation"];
    this.directionImage = this.options.directionImage || "route_direction";
    this.speed = this.options.speed || 50; // km/h
    this.initialZoom = this.options.initialZoom || 17;

    this.routeCoordinates = {};
    this.turfLine = null;
    this.routeLength = 0;
    this.distance = 0;
    this.time0 = 0;
    this.state = "stop";
    this.currentRouteLayerId = null;
    this.manualCamera = false;

    this._onPointerDown = (event) => this.onPointerDown(event);
    this._onPointerUp = (event) => this.onPointerUp(event);
    this._onWheel = (event) => this.onWheel(event);
  }

  createPanel()
  {
    const div = document.createElement("div");
    this.div = div;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `
      <div class="flex flex-column p-1" style="font-family:var(--font-family);min-width:146px">
        <style>
          #route_view {
            font-family: var(--font-family);
            font-size: 12px;border-color: var(--surface-border);
            border-radius: var(--border-radius);
            outline:none;
            border-style:solid;
            border-width:1px;
            background:var(--surface-ground);
            transition: background-color .2s,color .2s,border-color .2s,box-shadow .2s,opacity .2s;
          }
          #route_view:focus
          {
            border-color: var(--primary-color);
            box-shadow: var(--focus-ring);
          }
          #route_range
          {
            appearance:none;
            -webkit-appearance: none;
            background:var(--surface-300);
            height:0.5rem;
            border-radius:var(--border-radius);
          }
          #route_range::-webkit-slider-thumb
          {
            appearance: none;
            -webkit-appearance: none;
            background-color: var(--primary-color);
            border-radius: 50%;
            height: 1rem;
            width: 1rem;
          }
          #route_range:focus::-webkit-slider-thumb
          {
            outline: 2px solid var(--surface-400);
          }
        </style>
        <div class="flex align-items-center">
          <div id="route_distance" class="code flex-grow-1 text-center" style="font-size:15px"></div>
          <button id="play_button" class="pi pi-play border-none" title="${bundle.get("SimulateRouteControl.start")}" />
          <button id="pause_button" class="pi pi-pause border-none" title="${bundle.get("SimulateRouteControl.pause")}" />
        </div>
        <input id="route_range" type="range"
               class="outline-none border-radius mt-3 mb-3" value="0"
               min="0" max="1000">
        </input>
        <div class="flex">
          <label for="route_view">${bundle.get("SimulateRouteControl.view")}:</label>
          <select id="route_view" class="ml-1 flex-grow-1"></select>
        </div>
      </div>
    `;
    this.distanceElement = div.querySelector("#route_distance");
    this.playButton = div.querySelector("#play_button");
    this.pauseButton = div.querySelector("#pause_button");
    this.routeRange = div.querySelector("#route_range");
    this.routeViewElement = div.querySelector("#route_view");
    
    if (this.map.getTerrain())
    {
      const profileDiv = document.createElement("div");
      profileDiv.className = "flex mt-2";
      profileDiv.innerHTML = `
        <input id="show_profile" type="checkbox" />
        <label for="show_profile">${bundle.get("SimulateRouteControl.showProfile")}</label>
      `;
      this.routeRange.parentElement.appendChild(profileDiv);
      this.profileCheckbox = profileDiv.querySelector("#show_profile");
      this.profileCheckbox.addEventListener("change", () => {
        if (this.profileCheckbox.checked)
        {
          this.profilePanel.show();
        }
        else
        {
          this.profilePanel.hide();          
        }        
      });
    }

    if (this.routeViews.length === 0) this.routeViews = ["manual"];

    for (let routeView of this.routeViews)
    {
      let optionElement = document.createElement("option");
      optionElement.value = routeView;
      optionElement.textContent = 
        bundle.get("SimulateRouteControl.view." + routeView);
      this.routeViewElement.appendChild(optionElement);        
    }

    this.updateRouteDistance();
    this.onIdle();

    this.playButton.addEventListener("click",
      (e) => { e.preventDefault(); this.onPlay(); });
    this.pauseButton.addEventListener("click",
      (e) => { e.preventDefault(); this.onPause(); });
    this.routeRange.addEventListener("input", (e) => {
      this.goToDistance(this.routeRange.value);
    });
    this.routeRange.addEventListener("change", (e) => {
      this.centerToPosition();
    });

    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.style.display = "none";
    return div;
  }
  
  createProfilePanel(map)
  {
    this.profilePanel = new Panel(map, this.options);

    this.profilePanel.onShow = () => this.renderProfile();    
    this.profilePanel.onHide = () => 
    {
      this.profileCheckbox.checked = false;
    };
    this.profilePanel.onResize = () =>
    {
      this.renderProfile();
    };
  }

  renderProfile()
  {
    const map = this.map;

    if (!map.getTerrain() || !this.profileCheckbox.checked) return;

    const cssWidth = this.profilePanel.bodyDiv.clientWidth - 10;
    const cssHeight = 150;
    
    const pixelRatio = window.devicePixelRatio || 1;
    const width = cssWidth * pixelRatio;
    const height = cssHeight * pixelRatio;
    
    this.profilePanel.bodyDiv.innerHTML = `
       <canvas id="profile_canvas" width="${width}" height="${height}" style="border:1px solid gray;width:${cssWidth}px;height:${cssHeight}px"></canvas>
    `;

    if (this.turfLine === null) return;
    
    const canvas = this.profilePanel.bodyDiv.querySelector("#profile_canvas");
        
    let elevations = [];
    let minElevation = 1000000;
    let maxElevation = -1000000;
    let step = 4 * pixelRatio;
    const marginLeft = 40 * pixelRatio;
    const marginTop = 10 * pixelRatio;
    const marginBottom = 10 * pixelRatio;
    const pointRadius = 5 * pixelRatio;
    const borderSize = 2 * pixelRatio;
    const lineWidth = 2 * pixelRatio;
    const profileWidth = width - marginLeft;
    const kmPerPixel = this.routeLength / profileWidth;
    const pixelsPerKm = 1 / kmPerPixel;
        
    for (let i = 0; i < profileWidth + step; i += step)
    {
      if (i > profileWidth) i = profileWidth; 
      
      const distKm = i * kmPerPixel;
      const coords = turf.along(this.turfLine, distKm,
        { units: "kilometers"}).geometry.coordinates;
      const elevation = map.queryTerrainElevation(coords) + map.transform.elevation;
      
      elevations.push(elevation);
      if (elevation < minElevation) minElevation = elevation;
      if (elevation > maxElevation) maxElevation = elevation;      
    }
       
    const deltaElevation = maxElevation - minElevation;
    
    const getElevationHeight = 
      (elevation) => height - marginBottom - (((height - marginTop - marginBottom) * (elevation - minElevation)) / deltaElevation);
        
    const ctx = canvas.getContext("2d");

    // draw profile area
    ctx.setLineDash([]);
    ctx.beginPath();
    ctx.fillStyle = "#c3af60";
    ctx.moveTo(marginLeft, getElevationHeight(elevations[0]));
    for (let j = 1; j < elevations.length; j++)
    {
      ctx.lineTo(marginLeft + step * j, getElevationHeight(elevations[j]));
    }
    ctx.lineTo(width, height);
    ctx.lineTo(marginLeft, height);
    ctx.fill();

    // draw profile line
    ctx.beginPath();
    ctx.strokeStyle = "#604020";
    ctx.lineWidth = lineWidth;
    ctx.moveTo(marginLeft, getElevationHeight(elevations[0]));
    for (let j = 1; j < elevations.length; j++)
    {
      ctx.lineTo(marginLeft + step * j, getElevationHeight(elevations[j]));
    }
    ctx.stroke();

    // vertical lines (0.1 Km)
    ctx.setLineDash([2 * pixelRatio, 2 * pixelRatio]);
    ctx.strokeStyle = 'rgba(0,0,0,0.2)';
    ctx.lineWidth = 0.5 * lineWidth;
    for (let km = 0; km < this.routeLength; km += 0.1)
    {
      let j = Math.round(km * pixelsPerKm);
      ctx.beginPath();
      ctx.moveTo(j + marginLeft, 0);
      ctx.lineTo(j + marginLeft, height);
      ctx.stroke();
    }

    // horizontal lines
    let meterDivision;
    if (deltaElevation > 500) meterDivision = 100;
    else if (deltaElevation > 300) meterDivision = 50;
    else if (deltaElevation > 100) meterDivision = 20;
    else if (deltaElevation > 50) meterDivision = 10;
    else meterDivision = 5;
        
    ctx.fillStyle = '#000000';
    ctx.font = Math.round(12 * pixelRatio) + "px monospace";
    const startElevation = Math.round(minElevation / meterDivision) * meterDivision;
    for (let m = startElevation; m < maxElevation; m += meterDivision)
    {
      let h = Math.round(getElevationHeight(m));
      ctx.beginPath();
      ctx.moveTo(0, h);
      ctx.lineTo(width, h);
      ctx.stroke();
      ctx.fillText(m + "m", 2 * pixelRatio, h - 2 * pixelRatio);
    }

    // draw cursor
    const currentCoords = turf.along(this.turfLine, this.distance,
        { units: "kilometers"}).geometry.coordinates;
    const currentElevation = map.queryTerrainElevation(currentCoords) + map.transform.elevation;
    
    let x = marginLeft + this.distance / kmPerPixel;
    let y = getElevationHeight(currentElevation);

    ctx.setLineDash([]);
    ctx.beginPath();
    ctx.fillStyle = "#c0c0c0";
    ctx.arc(x, y, (pointRadius + borderSize), 0, 2 * Math.PI);
    ctx.fill();        
    
    ctx.beginPath();
    ctx.fillStyle = "#000000";
    ctx.arc(x, y, pointRadius, 0, 2 * Math.PI);
    ctx.fill();    
  }

  onPlay()
  {
    const map = this.map;

    if (this.state === "run")
    {
      return;
    }
    else if (this.state === "pause")
    {
      this.resumeAnimation();
    }
    else if (this.state === "stop")
    {
      this.startAnimation();
    }    
  }

  onPause()
  {
    if (this.state === "run")
    {
      this.pauseAnimation();
    }
  }

  onIdle()
  {
    const map = this.map;
    if (this.currentRouteLayerId === null ||
        map.getLayoutProperty(this.currentRouteLayerId, "visibility") === "none")
    {
      this.updateRoute();
      this.stopAnimation();
    }
    this.renderProfile();
  }

  onPointerDown(event)
  {
    this.map.stop();
    this.manualCamera = true;
  }

  onPointerUp(event)
  {
    this.manualCamera = false;
  }

  onWheel(event)
  {
    this.map.stop();
    this.manualCamera = true;
    if (this.wheelTimer)
    {
      clearTimeout(this.wheelTimer);
    }
    this.wheelTimer = setTimeout(() => { this.manualCamera = false; }, 500);
  }

  startAnimation()
  {
    if (this.initRoute())
    {
      this.updateRouteDistance();
      this.updateRoutePosition();
      this.div.style.display = "block";
      map.setLayoutProperty("position_in_route", "visibility", "visible");
      this.time0 = window.performance.now();
      this.state = "run";
      this.listenEvents();
      window.requestAnimationFrame(() => this.animate());    
    }
  }
  
  pauseAnimation()
  {
    this.state = "pause";
    this.unlistenEvents();
  }

  resumeAnimation()
  {
    this.state = "run";
    this.listenEvents();

    this.time0 = window.performance.now();
    window.requestAnimationFrame(() => this.animate());    
  }

  stopAnimation()
  {
    this.state = "stop";
    this.unlistenEvents();
  }
  
  listenEvents()
  {
    const map = this.map;
    map.on("mousedown", this._onPointerDown);
    map.on("mouseup", this._onPointerUp);
    map.on("touchstart", this._onPointerDown);
    map.on("touchend", this._onPointerUp);    
    map.on("wheel", this._onWheel);    
  }
  
  unlistenEvents()
  {
    const map = this.map;
    map.off("mousedown", this._onPointerDown);
    map.off("mouseup", this._onPointerUp);
    map.off("touchstart", this._onPointerDown);
    map.off("touchend", this._onPointerUp);    
    map.off("wheel", this._onWheel);    
  }

  updateRoute()
  {
    const map = this.map;

    if (this.initRoute())
    {
      this.updateRouteDistance();
      this.updateRoutePosition();
      this.div.style.display = "block";
      map.setLayoutProperty("position_in_route", "visibility", "visible");
      this.renderProfile();
      this.map.flyTo({ 
        center: this.point.geometry.coordinates, 
        zoom: this.initialZoom 
      });
    }
    else if (this.currentRouteLayerId)
    {
      this.distance = 0;
      this.currentRouteLayerId = null;
      this.div.style.display = "none";
      this.turfLine = null;
      map.setLayoutProperty("position_in_route", "visibility", "none");
    }
    this.routeRange.value = 0;
  }

  initRoute()
  {
    const map = this.map;
    for (let layerId in this.routeCoordinates)
    {
      if (map.getLayoutProperty(layerId, "visibility") !== "none")
      {
        let coords = this.routeCoordinates[layerId];
        this.turfLine = turf.lineString(coords);
        this.routeLength = turf.length(this.turfLine, { units : "kilometers" });
        this.distance = 0;
        this.currentRouteLayerId = layerId;
        return true;
      }
    }
    return false;
  }

  animate()
  {
    if (this.state !== "run") return;

    if (this.currentRouteLayerId === null ||
        map.getLayoutProperty(this.currentRouteLayerId, "visibility") === "none")
    {
      this.stopAnimation();
      return;
    }

    if (this.distance < this.routeLength)
    {
      let time1 = window.performance.now();
      let ellapsedHours = (time1 - this.time0) / (1000 * 3600);
      this.time0 = time1;

      this.distance += this.speed * ellapsedHours;

      this.updateRoutePosition();
      this.updateRouteDistance();

      let position = this.point.geometry.coordinates;
      let bearing = this.point.properties.bearing;

      if (!this.manualCamera)
      {
        let routeView = this.routeViewElement.value;
        
        if (routeView === "centered")
        {
          this.map.easeTo({ center: position, duration: 100 });
        }
        else if (routeView === "navigation")
        {
          this.map.easeTo({
            center: position,
            bearing: bearing,
            pitch: 70,
            duration: 100
          });
        }
      }

      this.routeRange.value = 1000 * (this.distance / this.routeLength);
      this.renderProfile();
      setTimeout(() => this.animate(), 20);
    }
    else
    {
      this.stopAnimation();
    }    
  }

  goToDistance(value)
  {
    if (this.state === "stop")
    {
      if (this.initRoute())
      {
        this.pauseAnimation();
      }
      else return;
    }
    this.distance = this.routeLength * value / 1000;
    this.updateRoutePosition();
    this.updateRouteDistance();
    this.renderProfile();
  }

  centerToPosition()
  {
    let position = this.point.geometry.coordinates;
    this.map.flyTo({ center: position, duration: 1000 });
  }

  updateRouteDistance()
  {
    this.distanceElement.textContent = "km " + this.distance.toFixed(3);
  }

  updateRoutePosition()
  {
    const map = this.map;

    let position1 = turf.along(this.turfLine, this.distance,
      { units: "kilometers"}).geometry.coordinates;

    this.point.geometry.coordinates = position1;

    let bearing;
    if (this.distance < this.routeLength - 0.001)
    {
      let position2 = turf.along(this.turfLine, this.distance + 0.001,
        { units: "kilometers"}).geometry.coordinates;
      bearing = turf.bearing(position1, position2);
    }
    else // last meter
    {
      let position0 = turf.along(this.turfLine, this.distance - 0.001,
        { units: "kilometers"}).geometry.coordinates;
      bearing = turf.bearing(position0, position1);
    }
    this.point.properties.bearing = bearing;

    map.getSource("position_in_route").setData(this.point);
  }

  async loadAllRouteCoordinates()
  {
    let promises = [];
    for (let layerId of this.routeLayerIds)
    {
      promises.push(this.loadRouteCoordinates(layerId));
    }
    await Promise.all(promises);
    this.updateRoute();
  }

  async loadRouteCoordinates(layerId)
  {
    const map = this.map;
    const style = map.getStyle();

    let layer = map.getLayer(layerId);
    if (!layer) 
    {
      console.warn(`Layer ${layerId} not found.`);
      return;
    }
    
    let sourceId = layer.source;

    const url = getSourceUrl(sourceId, style);

    const response = await fetch(url);
    const json = await response.json();
    if (json.exceptions) throw json;

    let features = json.features;

    let coords = this.mergeCoordinates(features);
    if (coords.length > 0)
    {
      this.routeCoordinates[layerId] = coords;
    }
  }

  mergeCoordinates(features)
  {
    let lines = [];

    for (let feature of features)
    {
      if (feature.geometry.type === "LineString")
      {
        lines.push(feature.geometry.coordinates);
      }
      else if (feature.geometry.type === "MultiLineString")
      {
        for (let part of feature.geometry.coordinates)
        {
          lines.push(part);
        }
      }
    }

    if (lines.length === 0) return [];

    let coords = lines.pop();

    while (lines.length > 0)
    {
      let removed = 0;
      for (let line of [...lines])
      {
        let nextCoords = line;
        if (this.sameCoords(coords[coords.length - 1], nextCoords[0]))
        {
          coords = [...coords, ...nextCoords];
          lines.splice(lines.indexOf(line), 1);
          removed++;
        }
        else if (this.sameCoords(nextCoords[nextCoords.length - 1], coords[0]))
        {
          coords = [...nextCoords, ...coords];
          lines.splice(lines.indexOf(line), 1);
          removed++;
        }
      }
      if (removed === 0) break;
    }
    return coords;
  }

  sameCoords(coords1, coords2)
  {
    let p1 = turf.point(coords1);
    let p2 = turf.point(coords2);

    let dist = turf.distance(p1, p2, { units: "kilometers" });

    return dist < 0.001;
  }
  
  addDefaultDirectionImage(map)
  {
    const svg = `
      <svg
         xmlns="http://www.w3.org/2000/svg"
         xmlns:svg="http://www.w3.org/2000/svg"
         width="64"
         height="64"
         viewBox="0 0 16.933333 16.933333"
         version="1.1">
        <g>
          <circle
             style="fill:#0010ee;stroke-width:0.26458333;stroke:#000000;stroke-opacity:1;stroke-dasharray:none"
             cx="8.4666662"
             cy="8.4666662"
             r="7.4083333" />
          <path
             style="fill:#ffffff;stroke:#000000;stroke-width:0.264583px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1;fill-opacity:1"
             d="M 8.4666666,3.175 4.2333333,12.7 8.4666666,10.583333 12.7,12.7 Z" />
        </g>
      </svg>
    `;
    
    const svgImage = new Image(64, 64);
    svgImage.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
    svgImage.onload = () => 
    {
      map.addImage('route_direction', svgImage);
    };    
  }

  onAdd(map)
  {
    this.map = map;

    map.addSource("position_in_route", {
      type: 'geojson',
      data: this.point
    });

    map.addLayer({
      "id": "position_in_route",
      "type": 'symbol',
      "source": "position_in_route",
      "layout":
      {
        "icon-image": this.directionImage, //"doc:5272712/64",
        "icon-size": 0.5,
        "icon-rotate": ["get", "bearing"],
        "icon-rotation-alignment": "map",
        "icon-allow-overlap": true,
        "icon-ignore-placement": true
      }
    });

    this.loadAllRouteCoordinates();

    this.addDefaultDirectionImage(map);

    map.on("idle", () => this.onIdle());

    this.createProfilePanel(map);

    return this.createPanel(map);
  }
}

export { SimulateRouteControl };