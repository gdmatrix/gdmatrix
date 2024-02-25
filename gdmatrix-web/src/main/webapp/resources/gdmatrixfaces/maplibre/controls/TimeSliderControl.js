/** TimeSliderControl.js **/

class TimeSliderControl
{
  /* 
    options: 
      days: number, number of days of the slider
      variable: string, variable name to change in the filters (using let)
      layers: array[string], identifiers of the layers to filter
      convertISODate: function(isoDate), returns the value to set in the filter
  */
  constructor(options)
  {
    this.options = options; 
    this.startDate = new Date();
      
    if (options.convertISODate === undefined)
    {
      options.convertISODate = (isoDate) => isoDate.substring(0, 4) + 
        isoDate.substring(5, 7) + isoDate.substring(8, 10) + "000000";
    }
  }

  createSliderPanel()
  {
    const millisPerDay = 24 * 60 * 60 * 1000;
    const days = this.options.days || 28;
    const div = document.createElement("div");
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `
      <div class="flex flex-column">
        <input id="ts_date" type="date" id="selected_date"></input>
        <input id="ts_range" type="range" value="0" min="0" max="${days}"></input>
      </div>
    `;
    const dateElement = div.querySelector("#ts_date");
    const rangeElement = div.querySelector("#ts_range");

    dateElement.style.outline = "none";
    dateElement.style.border = "none";
    dateElement.style.borderRadius = "var(--border-radius)";
    let date = this.startDate;
    dateElement.value = date.toISOString().substring(0, 10);
    rangeElement.style.outline = "none";

    rangeElement.addEventListener("input", (e) => 
    {
      let days = Number(rangeElement.value);
      let date = new Date(this.startDate.getTime() + days * millisPerDay);
      dateElement.value = date.toISOString().substring(0, 10);
      this.updateLayers(dateElement.value);
    });

    dateElement.addEventListener("change", (e) =>
    {
      let delta = Date.parse(dateElement.value) - this.startDate.getTime();
      let deltaDays = delta / millisPerDay;
      rangeElement.value = deltaDays;
      this.updateLayers(dateElement.value);
    });

    div.addEventListener("contextmenu", (e) => e.preventDefault());
    return div;
  }

  updateLayers(isoDate)
  {
    const map = this.map;
    let sdate = this.options.convertISODate(isoDate);
    let variable = this.options.variable || "date";
    let layerIds = this.options.layers || [];

    for (let layerId of layerIds)
    {
      let filter = map.getFilter(layerId);
      if (filter)
      {
        let index = filter.indexOf(variable);
        if (index !== -1) filter[index + 1] = sdate;
        map.setFilter(layerId, filter);

        let visibility = map.getLayoutProperty(layerId, 'visibility');
        if (visibility === "none")
        {
          map.setLayoutProperty(layerId, 'visibility', 'visible');
          map.getLayer(layerId).metadata.visible = true;
        }
      }
    }
  }

  onAdd(map)
  {
    this.map = map;
    this.updateLayers(new Date().toISOString());
    return this.createSliderPanel(map);
  }
}

export { TimeSliderControl };
