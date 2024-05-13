/* RefreshControl.js */

import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class RefreshControl
{
  constructor(options)
  {
    this.defaultRefreshInterval = options?.defaultRefreshInterval || 0; // seconds
    this.refreshIntervalMap = options?.refreshIntervalMap || {}; // { "sourceId": <seconds | 0>, ...}
    this.lastRefreshTimeMap = {}; // { "sourceId": millis, ...}

    // value for refreshInterval = x:
    // x < 0: never refresh
    // x == 0: only refresh manually (pressing button).
    // x > 0: refresh automatically every x seconds
  }

  onAdd(map)
  {
    this.map = map;
    const div = document.createElement("div");
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `<button><span class="pi pi-refresh"/></button>`;
    div.title = bundle.get("RefreshControl.title");
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      this.refreshSources(false);
    });

    this.initLastRefreshTimes();

    const update = () => {      
      this.refreshSources(true);
      if (document.body.contains(map.getCanvas()))
      {
        setTimeout(update, 1000);
      }
    };

    if (this.isTimerRequired())
    {
      update();
    }
    return div;
  }

  refreshSources(auto = false)
  {
    const map = this.map;
    const sources = map.getStyle().sources;
    const seed = "_seed=" + Math.random();

    for (let sourceId in sources)
    {
      if (this.isRefreshRequired(sourceId, auto))
      {
        let source = sources[sourceId];
        if (source.type === "geojson" && typeof source.data === "string")
        {
          let url = source.data;
          if (url.indexOf("?") === -1)
          {
            url += "?" + seed;
          }
          else
          {
            url += "&" + seed;
          }
          console.info("Refresh " + sourceId);
          map.getSource(sourceId).setData(url);
          this.lastRefreshTimeMap[sourceId] = Date.now();
        }
      }
    }
  }
  
  isRefreshRequired(sourceId, auto)
  {
    const seconds = this.getRefreshInterval(sourceId);

    if (auto)
    {
      if (seconds <= 0) return false;

      const lastRefreshTime = this.lastRefreshTimeMap[sourceId];
      const millis = Date.now();
      return (millis - lastRefreshTime) > seconds * 1000;
    }
    else // manual
    {
      return seconds >= 0;
    }
  }

  initLastRefreshTimes()
  {
    const sources = map.getStyle().sources;

    const millis = Date.now();
    for (let sourceId in sources)
    {
      this.lastRefreshTimeMap[sourceId] = millis;
    }
  }

  isTimerRequired()
  {
    const sources = map.getStyle().sources;

    for (let sourceId in sources)
    {
      if (this.getRefreshInterval(sourceId) > 0) return true;
    }    
    return false;
  }
  
  getRefreshInterval(sourceId)
  {
    let seconds = this.refreshIntervalMap[sourceId];
    if (seconds === undefined) seconds = this.defaultRefreshInterval;
    return seconds;    
  }
}

export { RefreshControl };


