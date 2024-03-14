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
      scales: object { "d": number of days, "w": number of weeks, "M": number of months }
      startDateVar: string, start date variable to change in the filters (using let)
      endDateVar: string, end date variable to change in the filters (using let)
      convertDate: function(date), returns the string value to set in the filter
      firstDayOfWeek: number(0-6), first day of week
  */
  constructor(options)
  {
    this.options = options || {}; 
    this.minDate = this.roundDate(options.minDate || new Date());
    this.baseDate = this.roundDate(options.baseDate || new Date());
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
        "M": 12        
      };
    }

    if (options.convertDate === undefined)
    {
      options.convertDate = (date) => {
        let day = ("0" + date.getDate()).slice(-2);
        let month = ("0" + (date.getMonth() + 1)).slice(-2);
        return date.getFullYear() + month + day + "000000";          
      };
    }
  }

  createSliderPanel()
  {    
    const div = document.createElement("div");
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
            background:var(--surface-300);
            height:8px;
            border-radius:var(--border-radius);
          }
          #ts_range:focus
          {
            background:var(--surface-400);
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
               class="outline-none border-radius mt-2" value="0">
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
    }
    this.updateDates();
    this.updateLayers();
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

  updateLayers()
  {
    const map = this.map;
    let startDateString = this.options.convertDate(this.startDate);
    let endDateString = this.options.convertDate(this.endDate);
    
    let startDateVar = this.options.startDateVar || "startDate";
    let endDateVar = this.options.endDateVar || "endDate";
    
    let layers = map.getStyle().layers;

    for (let layer of layers)
    {
      let layerId = layer.id;
      let filter = map.getFilter(layerId);
      if (filter instanceof Array)
      {
        if (filter.indexOf("let") !== 0) continue;
        
        let index1 = filter.indexOf(startDateVar);
        if (index1 !== -1) filter[index1 + 1] = startDateString;

        let index2 = filter.indexOf(endDateVar);
        if (index2 !== -1) filter[index2 + 1] = endDateString;
        
        if (index1 === -1 && index2 === -1) continue;
        
        map.setFilter(layerId, filter);
      }
    }
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

  onAdd(map)
  {
    this.map = map;
    this.updateLayers(new Date().toISOString());
    return this.createSliderPanel(map);
  }
}

export { TimeSliderControl };
