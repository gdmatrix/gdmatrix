/* FeatureFinder.js */

class FeatureFinder
{
  constructor(options)
  {
    this.name = options?.name || this.constructor.name;
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
    return this.name;
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
    const markerOptions = {};
    let element;

    if (this.isCustomMarker(feature)) // custom marker
    {
      element = document.createElement("div");
      element.className = "marker";
      this.populateMarker(feature, element);
      markerOptions.element = element;
    }
    else // default marker
    {
      markerOptions.color = this.getMarkerColor(feature);
    }
    
    const marker = new maplibregl.Marker(markerOptions);
    element = marker.getElement();
    element.style.cursor = "pointer";

    return marker;
  }

  selectMarker(marker, feature)
  {
    if (this.isCustomMarker(feature))
    {
      marker.addClassName("selected");
    }
    else
    {
      this.changeMarkerColor(marker, this.getSelectedMarkerColor(feature));      
    }
  }

  unselectMarker(marker, feature)
  {
    if (this.isCustomMarker(feature))
    {
      marker.removeClassName("selected");
    }
    else
    {
      this.changeMarkerColor(marker, this.getMarkerColor(feature));      
    }
  }
  
  isCustomMarker(feature)
  {
    return false;
  }

  populateMarker(feature, element)
  {
  }

  changeMarkerColor(marker, color)
  {
    // only for default markers
    let markerElement = marker.getElement();
    let fillElements = markerElement.querySelectorAll(
      'svg g[fill="' + marker._color + '"]');
    if (fillElements.length > 0)
    {
      fillElements[0].setAttribute("fill", color);
      marker._color = color;
    }
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