/* FeatureFinder */

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class FeatureFinder
{
  constructor(options)
  {
    this.maxZoom = options?.maxZoom || 18;
    this.centerMarkerOnClick = options?.centerMarkerOnClick || false;
    this.formViewMode = options?.formViewMode || "panel"; // panel || popup
    this.showPopupFromList = options?.showPopupFromList || false;
  }

  onAdd(findFeatureControl)
  {
    this.findFeatureControl = findFeatureControl;
  }

  getTitle()
  {
    return "title";
  }

  populateForm(element)
  {
    // create form fields into elem
    if (this.createFormFields)
    {
      element.innerHTML = this.createFormFields();
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
  
  populateList(feature, element)
  {
    element.textContent = this.getFeatureLabel(feature);
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
    const marker = new maplibregl.Marker({ 
      color: this.getMarkerColor(feature) 
     });

    const element = marker.getElement();
    element.style.cursor = "pointer";

    return marker;
  }

  getSelectedMarker(feature)
  {
    const marker = new maplibregl.Marker({
      color: this.getSelectedMarkerColor(feature) 
    });

    const element = marker.getElement();
    element.style.cursor = "pointer";

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
}

export { FeatureFinder };