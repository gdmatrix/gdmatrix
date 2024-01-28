/* FeatureForm */

class FeatureForm
{
  constructor(map, service, layerName, feature)
  {
    this.map = map;
    this.service = service;
    this.layerName = layerName;
    this.feature = feature; // geojson feature;
    this.initFormSelectorAndPriority();
  }

  async render(forEdit = false)
  {
    const feature = this.feature;
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
    this.html = await response.text();
    
    return this;
  }
  
  initFormSelectorAndPriority()
  {
    const map = this.map;
    const layerForms = map.getStyle().metadata?.layerForms;
    const layerName = this.layerName;
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
  
  normalizeLayerName(layerName)
  {
    layerName = layerName.toUpperCase();
    const index = layerName.indexOf(":");
    return index > 0 ? layerName.substring(index + 1) : layerName;
  }
}

export { FeatureForm };
