/* basic profile */

import { GoHomeControl } from "../controls/GoHomeControl.js";
import { LoadingIndicatorControl } from "../controls/LoadingIndicatorControl.js";
import { SearchControl } from "../controls/SearchControl.js";
import { LegendControl } from "../controls/LegendControl.js";
import { InfoControl } from "../controls/InfoControl.js";
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

  map.addControl(new SearchControl("maplibre_left_container"), "top-left");

  map.addControl(new GetFeatureInfoTool("maplibre_right_container", true), "top-left");

  map.addControl(new LegendControl("maplibre_right_container", true), "bottom-right");

  map.addControl(new InfoControl("maplibre_right_container", true), "bottom-right");
}

export { init };