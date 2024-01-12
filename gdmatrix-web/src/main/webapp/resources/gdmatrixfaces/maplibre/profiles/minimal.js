/* minimal profile */

import { GoHomeControl } from "../controls/GoHomeControl.js";
import { LoadingIndicatorControl } from "../controls/LoadingIndicatorControl.js";

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
}

export { init };