/* GenericWfsFinder.js */

import { WfsFinder } from "./WfsFinder.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class GenericWfsFinder extends WfsFinder
{
  constructor(options)
  {
    super(options);
  }

  getTitle()
  {
    return bundle.get("GenericWfsFinder.title");
  }

  createFormFields()
  {
    return `
      <div class="formgrid grid">
        <div class="field col-12">
          <label for="wfs_layer_name">${bundle.get("GenericWfsFinder.layer")}:</label>
          <input id="wfs_layer_name" type="text" spellcheck="false" class="code w-full" />
        </div>
        <div class="field col-12">
          <label for="wfs_cql_filter">${bundle.get("GenericWfsFinder.filter")}:</label>
          <textarea id="wfs_cql_filter" spellcheck="false" class="code w-full"></textarea>
        </div>
        <div class="field col-12">
          <label for="wfs_output_expr">${bundle.get("GenericWfsFinder.outputExpression")}:</label>
          <textarea id="wfs_output_expr" spellcheck="false" class="code w-full">feature.id</textarea>
        </div>
        <div class="field col-12">
          <label for="wfs_sort_by">${bundle.get("GenericWfsFinder.sortBy")}:</label>
          <input id="wfs_sort_by" spellcheck="false" class="code w-full"></textarea>
        </div>
      </div>
      <div class="flex align-items-center">
        <input id="add_markers" type="checkbox" checked>
        <label for="add_markers">${bundle.get("GenericWfsFinder.addMarkers")}</label>
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
    this.outputExpression = new Function("feature", "return " + expr);
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
}

export { GenericWfsFinder };
