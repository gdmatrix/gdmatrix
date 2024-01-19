/* advanced profile */

import { init as initBasic } from "./basic.js";
import { MeasureLengthTool } from "../controls/MeasureLengthTool.js";
import { MeasureAreaTool } from "../controls/MeasureAreaTool.js";
import { ExportAreaTool } from "../controls/ExportAreaTool.js";

function init(map)
{
  initBasic(map);
  
  const style = map.getStyle();

  map.addControl(new MeasureLengthTool({
    position: "right", 
    insertTop: true}
  ), "top-left");

  map.addControl(new MeasureAreaTool({
    position: "right", 
    insertTop: true}
  ), "top-left");

  map.addControl(new ExportAreaTool({
    position: "right", 
    insertTop: true}
  ), "top-left");

  map.addControl(new maplibregl.GeolocateControl({
    positionOptions: {
      enableHighAccuracy: true
    },
    trackUserLocation: true
  }));

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

export { init };