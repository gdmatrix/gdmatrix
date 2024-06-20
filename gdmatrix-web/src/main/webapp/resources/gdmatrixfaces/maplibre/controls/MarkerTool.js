/* MarkerTool.js */

import { Tool } from "./Tool.js";
import { Bundle } from "../i18n/Bundle.js";
import { toUtm, fromUtm } from "../utm-latlng.js";

const bundle = Bundle.getBundle("main");

class MarkerTool extends Tool
{
  constructor(options)
  {
    super({...{ 
            "title": bundle.get("MarkerTool.title"), 
            "iconClass": "pi pi-map-marker",
            "utm" : true,
            "widgetVar" : "geometry"
          }, ...options});
    
    this.markers = [];
    this._onMapClick = (event) => {
      if (event.originalEvent.srcElement.tagName !== "CANVAS") return;   
      const lngLat = event.lngLat;
      this.addMarker([lngLat.lng, lngLat.lat]);
      this.updateField();
    };
  }

  activate()
  {
    const map = this.map;
    map.on("click", this._onMapClick);
    map.getCanvas().style.cursor = "crosshair";
  }

  deactivate()
  {
    const map = this.map;
    map.off("click", this._onMapClick);
    map.getCanvas().style.cursor = "grab";
  }

  addMarker(point)
  {
    const map = this.map;   
    const marker = new maplibregl.Marker();
    marker.setLngLat(point);
    marker.getElement().addEventListener("click", (event) => 
    {
      event.preventDefault();
      this.removeMarker(marker);
      this.updateField();
    });
    marker.addTo(map);
    
    this.markers.push(marker);
  }
  
  removeMarker(marker)
  {
    let index = this.markers.indexOf(marker);
    this.markers.splice(index, 1);    
    marker.remove();
  }
  
  createMarkers(coordinates)
  {
    for (let point of coordinates)
    {
      this.addMarker(point);
    }
  }
  
  updateField()
  {
    let coordinates = [];
    this.markers.forEach(marker => {
      let lnglat = marker.getLngLat();
      let lng = lnglat.lng;
      let lat = lnglat.lat;
      if (this.options.utm)
      {
        const utm = toUtm(lat, lng, 31, 'ETRS89');
        coordinates.push([utm.easting, utm.northing]);
      }
      else
      {
        coordinates.push([lng, lat]);
      }
    });

    let value;
    if (coordinates.length > 0)
    {
      const geometry = { "type": "MultiPoint", "coordinates": coordinates };
      value = JSON.stringify(geometry);
    }
    else
    {
      value = null;
    }
    PF(this.options.widgetVar).jq.val(value);
  }
  
  onAdd(map)
  {    
    const div = super.onAdd(map);
    try
    {
      const widget = PF(this.options.widgetVar);
      if (widget)
      {
        const value = widget.jq.val();
        if (value)
        {
          const geometry = JSON.parse(value);
          let coordinates = geometry.coordinates;
          if (this.options.utm)
          {
            let cnvCoordinates = [];
            for (let point of geometry.coordinates)
            {
              let easting = point[0];
              let northing = point[1];
              let latLng = fromUtm(easting, northing, 31, 'T', 'ETRS89');
              cnvCoordinates.push([latLng.longitude, latLng.latitude]);
              coordinates = cnvCoordinates;
            }
          }
          this.createMarkers(coordinates);
        }
      }
    }
    catch (ex)
    {
      console.error(ex);
    }
    return div;
  }  
}

export { MarkerTool };
