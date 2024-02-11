import { FeatureFinder } from "./FeatureFinder.js";
import { FeatureForm } from "../ui/FeatureForm.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class WfsFinder extends FeatureFinder
{
  constructor(params)
  {
    super(params);

    this.service = params?.service || {
      url: "https://gis.santfeliu.cat/geoserver/wfs",
      useProxy: true
    };

    this.layerName = params?.layerName;
    this.outputProperties = null;
    this.addMarkers = true;
  }

  getTitle()
  {
    return "Generic WFS finder";
  }

  createFormFields()
  {
    return `
      <div class="formgrid grid">
        <div class="field col-12">
          <label for="wfs_layer_name">${bundle.get("FindFeatureControl.layer")}:</label>
          <input id="wfs_layer_name" type="text" spellcheck="false" class="code w-full" />
        </div>
        <div class="field col-12">
          <label for="wfs_cql_filter">${bundle.get("FindFeatureControl.filter")}:</label>
          <textarea id="wfs_cql_filter" spellcheck="false" class="code w-full"></textarea>
        </div>
        <div class="field col-12">
          <label for="wfs_output_expr">${bundle.get("FindFeatureControl.outputExpression")}:</label>
          <textarea id="wfs_output_expr" spellcheck="false" class="code w-full">feature.id</textarea>
        </div>
        <div class="field col-12">
          <label for="wfs_sort_by">${bundle.get("FindFeatureControl.sortBy")}:</label>
          <input id="wfs_sort_by" spellcheck="false" class="code w-full"></textarea>
        </div>
      </div>
      <div class="flex align-items-center">
        <input id="add_markers" type="checkbox" checked>
        <label for="add_markers">${bundle.get("FindFeatureControl.addMarkers")}</label>
      </div>
    `;
  }

  async find()
  {
    this.layerName = document.getElementById("wfs_layer_name").value;
    let cqlFilter = document.getElementById("wfs_cql_filter").value;
    let sortBy = document.getElementById("wfs_sort_by").value;
    let expr = document.getElementById("wfs_output_expr").value;
    if (expr === null || expr.trim().length === 0)
    {
      expr = "feature.id";
      document.getElementById("wfs_output_expr").value = expr;
    }
    this.labelExpression = new Function("feature", "return " + expr);
    this.addMarkers = document.getElementById("add_markers").checked;

    if (this.service?.url && this.layerName)
    {
      return await this.findWfs(cqlFilter, null, sortBy);
    }
    else
    {
      return [];
    }
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
      url += "&cql_filter=" + cqlFilter;
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
    if (this.labelExpression)
    {
      return this.labelExpression(feature);
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
