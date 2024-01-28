/* basic profile */

import { init as initMinimal } from "./minimal.js";
import { FindFeatureControl, WfsFinder } from "../controls/FindFeatureControl.js";
import { GetFeatureInfoTool } from "../controls/GetFeatureInfoTool.js";
import { LegendControl } from "../controls/LegendControl.js";
import { MapInfoControl } from "../controls/MapInfoControl.js";

function init(map)
{
  initMinimal(map);
  
  const style = map.getStyle();

  map.addControl(new FindFeatureControl({
    position: "left", 
    insertTop: false
  }), "top-left");
  
  map.addControl(new LegendControl({
    position: "right", 
    insertTop: true}
  ), "bottom-right");

  map.addControl(new MapInfoControl({
    position: "right", 
    insertTop: true}
  ), "bottom-right");
  
  map.addControl(new GetFeatureInfoTool({
    position: "right", 
    insertTop: true
  }), "top-left");  
  
  map.findControl.addFinder(new WfsFinder());  
}

export { init };