/* basic profile */

import { GoHomeControl } from "../controls/GoHomeControl.js";
import { LoadingIndicatorControl } from "../controls/LoadingIndicatorControl.js";
import { GeolocateControl } from "../controls/GeolocateControl.js";
import { LegendControl } from "../controls/LegendControl.js";
import { MapInfoControl } from "../controls/MapInfoControl.js";
import { GetFeatureInfoTool } from "../controls/GetFeatureInfoTool.js";

function init(map)
{
  const style = map.getStyle();
  
  map.addControl(new maplibregl.NavigationControl({
      visualizePitch: true,
      showZoom: true,
      showCompass: true
    })
  );

  map.addControl(new maplibregl.ScaleControl({
    maxWidth: 80,
    unit: 'metric'
  }));

  map.addControl(new GoHomeControl({
    center: style.center,
    zoom: style.zoom,
    bearing: style.bearing,
    pitch: style.pitch,
    speed: 1,
    curve: 1,
    easing(t) { return t; }
  }), "top-right");

  map.addControl(new LoadingIndicatorControl(), "top-right");

  map.addControl(new GeolocateControl({
    containerId: "maplibre_left_container", 
    insertTop: false
  }), "top-left");

  map.addControl(new GetFeatureInfoTool({
    containerId: "maplibre_right_container", 
    insertTop: true
  }), "top-left");

  map.addControl(new MeasureLengthTool({
    containerId: "maplibre_right_container", 
    insertTop: true}
  ), "top-left");

  map.addControl(new MeasureAreaTool({
    containerId: "maplibre_right_container", 
    insertTop: true}
  ), "top-left");
}

export { init };