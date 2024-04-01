/* FeatureForm */

class FeatureForm
{
  constructor(feature)
  {
    this.feature = feature; // geojson feature;
    this.formSelector = null;
    this.priority = 0;
    this.service = null; // optional
    this.layerName = null; // optional
    this.forEdit = false;    

    this._div = document.createElement("div");
    this._rendered = false;
  }
  
  getElement()
  {
    return this._div;
  }

  setFormSelectorAndPriority(map)
  {
    const layerName = this.layerName;
    if (!layerName) return;

    const normLayerName = this.normalizeLayerName(layerName);

    let formSelector = null;
    let priority;
    
    const layerForms = map.getStyle().metadata?.layerForms;
    if (layerForms instanceof Array)
    {
      let i = 0;
      priority = layerForms.length;
      while (i < layerForms.length && formSelector === null)
      {
        let layerForm = layerForms[i];
        if (layerForm.layer === layerName || layerForm.layer === normLayerName)
        {
          formSelector = layerForm.formSelector;
          priority = i; 
        }
        else i++;
      }
    }
    else priority = 0;
    
    this.formSelector = formSelector || "form:" + normLayerName;
    this.priority = priority;
  }
  
  updateProperties(featureInfo)
  {
    const div = this._div;
    const definedProperties = featureInfo.properties;
    if (this.feature.properties === undefined)
    {
      this.feature.properties = {};
    }

    for (let definedProperty of definedProperties)
    {
      let propName = definedProperty.name;
      if (propName !== featureInfo.geometryColumn)
      {
        let input = div.querySelector("#" + propName);
        if (input)
        {
          this.feature.properties[propName] = input.value;
        }
      }
    }
  }

  async render(update = false)
  {
    if (!this._rendered || update)
    {
      const feature = this.feature;
      const forEdit = this.forEdit;
      const params = {
        entity: this.layerName,
        formseed: Math.random(),
        selector: this.formSelector,
        forEdit: forEdit,
        renderer: forEdit ?
          "org.santfeliu.web.servlet.form.EditableFormRenderer" :
          "org.santfeliu.web.servlet.form.ReadOnlyFormRenderer"
      };
      this.addProperties(params, feature.properties);

      const response = await fetch("/form?" + new URLSearchParams(params));
      this._div.innerHTML = await response.text();
      this._rendered = true;
    }
    return this;
  }

  addProperties(params, properties)
  {
    for (let name in properties)
    {
      let value = properties[name];
      if (value !== null && value !== undefined)
      {
        params[name] = value;
      }
    }
  }

  normalizeLayerName(layerName)
  {
    layerName = layerName.toUpperCase();
    const index = layerName.indexOf(":");
    return index > 0 ? layerName.substring(index + 1) : layerName;
  }
}

export { FeatureForm };
