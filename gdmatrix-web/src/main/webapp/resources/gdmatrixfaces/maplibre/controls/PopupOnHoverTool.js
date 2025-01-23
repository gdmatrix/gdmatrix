/* PopupOnHoverTool.js */

import { Tool } from "./Tool.js";
import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class PopupOnHoverTool extends Tool
{
  constructor(options)
  {
    super({...{
            "title": bundle.get("PopupOnHoverTool.title"),
            "iconClass": "fa-solid fa-message",
            "position" : "right"
          }, ...options}); 
    this.layers = options?.layers || {}; //{ "layerId" : contentFn(feature), ... }
    this.popupOptions = options?.popupOptions || {
      closeButton: false,
      closeOnClick: false
    };
    this.enterHandlers = {};
    this.leaveHandlers = {};
  }
  
  activate()
  {    
    const layers = this.layers;
    const enterHandlers = this.enterHandlers;
    const leaveHandlers = this.leaveHandlers;
    const layerIds = Object.keys(layers);

    for (let layerId of layerIds)
    {
      this.map.on("mouseenter", layerId, enterHandlers[layerId]);
      if (!this.popupOptions.closeButton)
      {
        this.map.on("mouseleave", layerId, leaveHandlers[layerId]);
      }
    }
  }
  
  deactivate()
  {
    const layers = this.layers;
    const enterHandlers = this.enterHandlers;
    const leaveHandlers = this.leaveHandlers;
    const layerIds = Object.keys(layers);

    for (let layerId of layerIds)
    {
      this.map.off("mouseenter", layerId, enterHandlers[layerId]);
      if (!this.popupOptions.closeButton)
      {
        this.map.off("mouseleave", layerId, leaveHandlers[layerId]);
      }
    }
  }
 
  onAdd(map)
  {
    const div = super.onAdd(map);
 
    console.info(this.popupOptions);
    const popup = new maplibregl.Popup(this.popupOptions);

    const layers = this.layers;
    const enterHandlers = this.enterHandlers;
    const leaveHandlers = this.leaveHandlers;
    const layerIds = Object.keys(layers);

    for (let layerId of layerIds)
    {
      enterHandlers[layerId] = event => 
      {
        map.getCanvas().style.cursor = "pointer";

        const feature = event.features[0];
        let coordinates;
        if (feature.geometry.type === "Point")
        {
          coordinates = turf.centroid(feature).geometry.coordinates;
        }
        else
        {          
          coordinates = event.lngLat;
        }
        
        let content = this.layers[layerId];
        let popupContent;
        if (typeof content === "function")
          popupContent = content(feature);
        else if (!content)
          popupContent = "<pre style='word-wrap:break-word;white-space:pre-wrap;'>" + JSON.stringify(feature.properties, null, 2) + "</pre>";
        else
          popupContent = String(content);

        while (Math.abs(event.lngLat.lng - coordinates[0]) > 180) 
        {
          coordinates[0] += event.lngLat.lng > coordinates[0] ? 360 : -360;
        }
        popup.setLngLat(coordinates).setHTML(popupContent).addTo(map);
      };

      leaveHandlers[layerId] = () => 
      {
        map.getCanvas().style.cursor = "";
        popup.remove();    
      };    
    }
    return div;
  }
}

export { PopupOnHoverTool };



