/* minimal profile */

import { GoHomeControl } from "../controls/GoHomeControl.js";
import { ZoomControl } from "../controls/ZoomControl.js";
import { LoadingIndicatorControl } from "../controls/LoadingIndicatorControl.js";

function init(map)
{
  const style = map.getStyle();

  if (style.metadata?.globeControlEnabled)
  {
    map.addControl(new maplibregl.GlobeControl());
  }
  
  map.addControl(new maplibregl.NavigationControl({
      visualizePitch: true,
      showZoom: true,
      showCompass: true
    })
  );

  map.addControl(new ZoomControl(), "bottom-left");

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