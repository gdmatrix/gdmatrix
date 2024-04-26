/** TimeSliderControl.js **/

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class TimeSliderControl
{
  /* 
    options: 
      minDate: minimal accepted date.
      baseDate: base date of the slider.
      startDate: initial start date of the period.
      scales: object { "d": number of days, "w": number of weeks, "M": number of months, "y": number of years }.
      startDateVar: string, start date variable to change in the filters (using let), "startDate" by default.
      endDateVar: string, end date variable to change in the filters (using let), "endDate" by default.
      convertDate: function(date, isEndDate), returns the string value to set in the filter.
      enableForLayers: array of layer ids that enable this control when they are visible.
      firstDayOfWeek: number(0-6), first day of the week.
   */
  constructor(options)
  {
    this.options = options || {}; 
    this.minDate = this.roundDate(options.minDate || new Date());
    this.baseDate = this.roundDate(options.baseDate || this.minDate);
    this.startDate = new Date(this.baseDate);
    this.endDate = this.startDate;
    this.period = "d";

    if (options.firstDayOfWeek === undefined)
    {
      options.firstDayOfWeek = 1; // monday
    }

    if (options.scales === undefined)
    {
      options.scales = {
        "d": 30,
        "w": 16,
        "M": 12,
        "y": 10
      };
    }

    if (options.convertDate === undefined)
    {
      options.convertDate = (date, isEndDate) => {
        let day = ("0" + date.getDate()).slice(-2);
        let month = ("0" + (date.getMonth() + 1)).slice(-2);
        return date.getFullYear() + month + day + "000000";          
      };
    }
  }

  createSliderPanel()
  {    
    const div = document.createElement("div");
    this.div = div;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `
      <div class="flex flex-column p-1" style="font-family:var(--font-family)">
        <style>
          #ts_period {
            font-family: var(--font-family);
            font-size: 12px;border-color: var(--surface-border);
            border-radius: var(--border-radius);
            outline:none;
            border-style:solid;
            border-width:1px;
            background:var(--surface-ground);
            transition: background-color .2s,color .2s,border-color .2s,box-shadow .2s,opacity .2s;
          }
          #ts_period:focus
          {
            border-color: var(--primary-color);
            box-shadow: var(--focus-ring);
          }
          #ts_start_date, #ts_end_date
          {
            background-color: transparent;
          }
          #ts_range
          {
            appearance:none;
            -webkit-appearance: none;
            background:var(--surface-300);
            height:0.5rem;
            border-radius:var(--border-radius);
          }
          #ts_range::-webkit-slider-thumb
          {
            appearance: none;
            -webkit-appearance: none;
            background-color: var(--primary-color);
            border-radius: 50%;
            height: 1rem;
            width: 1rem;
          }
          #ts_range:focus::-webkit-slider-thumb
          {
            outline: 2px solid var(--surface-400);            
          }
        </style>
        <div class="flex align-items-center">
          <label for="ts_period" class="mr-1">${bundle.get("TimeSliderControl.period")}:</label>
          <select id="ts_period" class="flex-grow-1 text-color">
          </select>
        </div>
        <div class="flex mt-2">
          <span style="min-width:20px" class="code">&gt;=</span>
          <span id="ts_start_dow" class="ml-1 mr-1 code">dl.</span>
          <input id="ts_start_date" type="date" class="outline-none border-none text-color code"></input>
        </div>
        <div class="flex mt-2">
          <span style="min-width:20px" class="code">&lt;</span>
          <span id="ts_end_dow" class="ml-1 mr-1 code">dl.</span>
          <input id="ts_end_date" type="date" class="outline-none border-none text-color code"></input>
        </div>
        <input id="ts_range" type="range"
               class="outline-none border-radius mt-3 mb-2" value="0">
        </input>
      </div>
    `;
    this.periodElement = div.querySelector("#ts_period");
    this.startDoWElement = div.querySelector("#ts_start_dow");
    this.endDoWElement = div.querySelector("#ts_end_dow");
    this.startDateElement = div.querySelector("#ts_start_date");
    this.endDateElement = div.querySelector("#ts_end_date");
    this.rangeElement = div.querySelector("#ts_range");

    const scales = Object.keys(this.options.scales);
    scales.push("2");
    this.period = scales[0];
    
    for (let scale of scales)
    {
      let optionElement = document.createElement("option");
      optionElement.value = scale;
      optionElement.textContent = bundle.get("TimeSliderControl.period." + scale);
      this.periodElement.appendChild(optionElement);
    }

    this.periodElement.addEventListener("change", () => this.onPeriodChange());

    this.startDateElement.addEventListener("change", () => this.onStartDateChanged());
    this.startDateElement.addEventListener("keydown", (e) => 
    {
      if (e.code === "Enter") e.preventDefault();
    });
    this.endDateElement.addEventListener("change", () => this.onEndDateChanged());
    this.endDateElement.addEventListener("keydown", (e) => 
    {
      if (e.code === "Enter") e.preventDefault();
    });    
    this.rangeElement.addEventListener("input", () => this.onRangeChanged());

    this.updatePeriod();

    div.addEventListener("contextmenu", (e) => e.preventDefault());
    return div;
  }
  
  onPeriodChange()
  {
    this.period = this.periodElement.value;
    this.startDate = new Date(this.baseDate);
    this.updatePeriod();
    this.rangeElement.value = 0;
  }  
    
  onStartDateChanged()
  {
    let millis = Date.parse(this.startDateElement.value);   
    if (isNaN(millis))
    {
      this.startDate = new Date(this.baseDate);
    }
    else
    {
      this.startDate = this.roundDate(new Date(millis));
    }
    
    if (this.period === "d")
    {
      this.updatePeriod();
      const dayMillis = 24 * 60 * 60 * 1000;
      const days = (this.startDate.getTime() - this.baseDate.getTime()) / dayMillis;
      this.rangeElement.value = days;
    }
    else
    {
      this.period = "2";    
      this.periodElement.value = this.period;
      this.updatePeriod();
    }
  }
  
  onEndDateChanged()
  {
    let millis = Date.parse(this.endDateElement.value);   
    if (isNaN(millis))
    {
      this.endDate = new Date(this.baseDate);
    }
    else
    {
      this.endDate = this.roundDate(new Date(millis));
    }
    this.period = "2";
    this.updatePeriod();
    this.periodElement.value = this.period;    
  }

  onRangeChanged()
  {
    if (this.period === "d")
    {
      const days = parseInt(this.rangeElement.value);
      this.startDate = new Date(this.baseDate);
      this.startDate.setDate(this.startDate.getDate() + days);
      this.updatePeriod();
    }
    else if (this.period === "w")
    {
      const weeks = parseInt(this.rangeElement.value);
      this.startDate = new Date(this.baseDate);
      this.startDate.setDate(this.startDate.getDate() + 7 * weeks);
      this.updatePeriod();      
    }
    else if (this.period === "M")
    {
      const months = parseInt(this.rangeElement.value);
      this.startDate = new Date(this.baseDate);
      this.startDate.setMonth(this.startDate.getMonth() + months);
      this.updatePeriod();      
    }    
    else if (this.period === "y")
    {
      const years = parseInt(this.rangeElement.value);
      this.startDate = new Date(this.baseDate);
      this.startDate.setFullYear(this.startDate.getFullYear() + years);
      this.updatePeriod();      
    }
  }
  
  updatePeriod()
  {
    const period = this.period;
    if (this.startDate < this.minDate)
    {
      this.startDate = new Date(this.minDate);
    }
    
    if (period === "2")
    {
      if (this.startDate >= this.endDate)
      {
        this.endDate = new Date(this.startDate);
        this.endDate.setDate(this.endDate.getDate() + 1);
      }
      this.rangeElement.style.display = "none";
    }
    else
    {
      this.rangeElement.style.display = "";

      if (period === "d")
      {
        this.roundDate(this.startDate);
        this.endDate = new Date(this.startDate);
        this.endDate.setDate(this.endDate.getDate() + 1);        
        this.rangeElement.max = this.options.scales["d"];
      }
      else if (period === "w")
      {
        this.roundDate(this.startDate);
        let i = 0;
        const firstDayOfWeek = this.options.firstDayOfWeek;
        while (this.startDate.getDay() !== firstDayOfWeek && i < 7)
        {
          this.startDate.setDate(this.startDate.getDate() - 1);
          i++;
        }
        this.endDate = new Date(this.startDate);
        this.endDate.setDate(this.endDate.getDate() + 7);
        this.rangeElement.max = this.options.scales["w"];
      }
      else if (period === "M")
      {
        this.startDate.setDate(1);
        this.roundDate(this.startDate);
        this.endDate = new Date(this.startDate);
        this.endDate.setMonth(this.endDate.getMonth() + 1);
        this.rangeElement.max = this.options.scales["M"];
      }
      else if (period === "y")
      {
        this.startDate.setDate(1);
        this.startDate.setMonth(0);
        this.roundDate(this.startDate);
        this.endDate = new Date(this.startDate);
        this.endDate.setFullYear(this.endDate.getFullYear() + 1);
        this.rangeElement.max = this.options.scales["y"];
      }
    }
    this.updateDates();
    this.updateSourcesAndLayers();
  }
  
  updateDates()
  {
    const startDate = this.startDate;
    const endDate = this.endDate;

    this.startDateElement.value = this.getDateString(startDate);
    this.endDateElement.value = this.getDateString(endDate);
    this.startDoWElement.textContent = this.getDayOfWeek(startDate);
    this.endDoWElement.textContent = this.getDayOfWeek(endDate);    
  }

  updateSourcesAndLayers()
  {
    const map = this.map;
    let startDateString = this.options.convertDate(this.startDate, false);
    let endDateString = this.options.convertDate(this.endDate, true);
    
    console.info("start: " + startDateString, "end: " + endDateString);

    let sources = map.getStyle().sources;
    for(let sourceId in sources)
    {
      let source = sources[sourceId];
      if (source.type === "geojson" && source.filter)
      {
        source = map.getSource(sourceId);
        let filter = source.workerOptions.filter;
        if (this.updateFilter(filter, startDateString, endDateString))
        {
          source.updateData();
        }
      }
    }
    
    let layers = map.getStyle().layers;
    for (let layer of layers)
    {
      let layerId = layer.id;
      let filter = map.getFilter(layerId);

      if (this.updateFilter(filter, startDateString, endDateString))
      {
        map.setFilter(layerId, filter);
      }
    }
  }
  
  updateFilter(filter, startDateString, endDateString)
  {
    if (filter instanceof Array)
    {
      let startDateVar = this.options.startDateVar || "startDate";
      let endDateVar = this.options.endDateVar || "endDate";  
      
      if (filter.indexOf("let") !== 0) return false;

      let index1 = filter.indexOf(startDateVar);
      if (index1 !== -1) filter[index1 + 1] = startDateString;

      let index2 = filter.indexOf(endDateVar);
      if (index2 !== -1) filter[index2 + 1] = endDateString;

      if (index1 === -1 && index2 === -1) return false;
    }
    return true;
  }

  getDayOfWeek(date)
  {
    return date.toLocaleString(Bundle.userLanguage, { weekday:'short' });
  }
  
  getDateString(date)
  {
    let day = ("0" + date.getDate()).slice(-2);
    let month = ("0" + (date.getMonth() + 1)).slice(-2);

    return date.getFullYear() + "-" + month + "-" + day;    
  }
  
  roundDate(date)
  {
    date.setHours(0);
    date.setMinutes(0);
    date.setSeconds(0);
    date.setMilliseconds(0);    
    return date;
  }

  onIdle()
  {
    const enableForLayers = this.options.enableForLayers; 
    if (enableForLayers instanceof Array)
    {
      const map = this.map;
      let enabled = false;
      
      for (let layerId of enableForLayers)
      {
        let layer = map.getLayer(layerId);
        if (layer.metadata.visible)
        {
          enabled = true;
          break;
        }
      }
      this.div.style.display = enabled ? "" : "none";
    }
  }

  onAdd(map)
  {
    this.map = map;
    map.on("idle", () => this.onIdle());
    return this.createSliderPanel(map);
  }
}

export { TimeSliderControl };
