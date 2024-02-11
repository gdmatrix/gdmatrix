/* FeatureFinder */

import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class FeatureFinder
{
  constructor(params)
  {
  }

  onAdd(findFeatureControl)
  {
    this.findFeatureControl = findFeatureControl;
  }

  getTitle()
  {
    return "title";
  }

  addFormFields(elem)
  {
    // create form fields into elem
    if (this.createFormFields)
    {
      elem.innerHTML = this.createFormFields();
    }
  }

  createFormFields()
  {
    return `<input type="text" name="name" />`; // legacy method
  }

  async find()
  {
    return []; // geojson features
  }

  getFeatureLabel(feature)
  {
    return "?";
  }

  getListIcon(feature)
  {
    return "pi pi-map-marker";
  }

  getListIconUrl(feature)
  {
    return null;
  }

  getMarker(feature)
  {
    const control = this.findFeatureControl;
    const map = control.map;
    const centroid = turf.centroid(feature);
    const marker = new maplibregl.Marker({ 
      color: this.getMarkerColor(feature) 
     }).setLngLat(centroid.geometry.coordinates);

    const element = marker.getElement();
    element.style.cursor = "pointer";

    const form = this.getForm(feature);

    marker.select = () => {
      marker.remove();
      marker.selectedMarker = this.getSelectedMarker(feature);
      marker.selectedMarker.addTo(map);      
      if (form) control.showForm(form);
    };

    marker.unselect = () => {
      marker.selectedMarker.remove();
      marker.addTo(map);
    };

    return marker;
  }

  getSelectedMarker(feature)
  {
    const centroid = turf.centroid(feature);
    const marker = new maplibregl.Marker({ 
      color: this.getSelectedMarkerColor(feature) 
    }).setLngLat(centroid.geometry.coordinates);

    return marker;
  }

  getMarkerColor(feature)
  {
    return "#3FB1CE";
  }

  getSelectedMarkerColor(feature)
  {
    return "#FF0000";
  }

  getForm(feature)
  {
    return null;
  }
  
  getMaxZoom()
  {
    return 18;
  }
}

export { FeatureFinder };