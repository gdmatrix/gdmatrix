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
    const layerForms = map.getStyle().metadata?.layerForms;
    if (!layerForms) return;

    const layerName = this.layerName;
    if (!layerName) return;
    
    const normLayerName = this.normalizeLayerName(layerName);
 
    let formSelector = null;
    let priority;
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

  async render(update = false)
  {
    if (!this._rendered || update)
    {
      const feature = this.feature;
      const forEdit = this.forEdit;
      const properties = feature.properties;
      const params = {
        entity: this.layerName,
        formseed: Math.random(),
        selector: this.formSelector,
        forEdit: forEdit,
        renderer: forEdit ?
          "org.santfeliu.web.servlet.form.EditableFormRenderer" :
          "org.santfeliu.web.servlet.form.ReadOnlyFormRenderer",
        ...properties
      };

      const response = await fetch("/form?" + new URLSearchParams(params));
      this._div.innerHTML = await response.text();
      this._rendered = true;
    }
    return this;
  }

  normalizeLayerName(layerName)
  {
    layerName = layerName.toUpperCase();
    const index = layerName.indexOf(":");
    return index > 0 ? layerName.substring(index + 1) : layerName;
  }
}

export { FeatureForm };
