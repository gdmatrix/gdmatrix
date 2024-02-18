/* WfsFinder.js */

import { FeatureFinder } from "./FeatureFinder.js";
import { FeatureForm } from "../ui/FeatureForm.js";

class WfsFinder extends FeatureFinder
{
  constructor(options)
  {
    super(options);

    this.service = options?.service || {
      url: "https://gis.santfeliu.cat/geoserver/wfs",
      useProxy: true
    };

    this.layerName = options?.layerName;
    this.outputExpression = options?.outputExpression || null;
    this.addMarkers = options && options.addMarkers === false ? false : true;
  }

  async findWfs(cqlFilter, viewparams, sortBy)
  {
    let url = "/proxy?url=" + this.service.url + "&" +
      "request=GetFeature" +
      "&service=WFS" +
      "&version=2.0.0" +
      "&typeNames=" + this.layerName +
      "&srsName=EPSG:4326" +
      "&outputFormat=application/json" +
      "&exceptions=application/json";

    if (cqlFilter)
    {
      url += "&cql_filter=" + encodeURIComponent(cqlFilter);
    }

    if (viewparams)
    {
      url += "&viewparams=" + viewparams;
    }

    if (sortBy)
    {
      url += "&sortBy=" + sortBy;
    }

    const response = await fetch(url);
    const json = await response.json();
    if (json.exceptions) throw json;

    return json.features;
  }

  getFeatureLabel(feature)
  {
    if (this.outputExpression)
    {
      return this.outputExpression(feature);
    }
    else
    {
      return feature.id;
    }
  }

  getMarker(feature)
  {
    return this.addMarkers ? super.getMarker(feature) : null;    
  }

  getForm(feature)
  {
    const map = this.findFeatureControl.map;

    const service = this.service;
    const form = new FeatureForm(feature);
    form.service = service;
    form.layerName = this.layerName;
    form.setFormSelectorAndPriority(map);

    return form;
  }
}

export { WfsFinder };
