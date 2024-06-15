/* ChartControl.js */

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";
import "../turf.js";

const bundle = Bundle.getBundle("main");

class ChartControl
{
  constructor(options)
  {
    this.options = options;
    this.layers = options.layers;
    this.parameters = options.parameters || [];
    this.currentSourceId = null;
    this.markers = {}; // for sourceId: [markers...]
    this.updatingMarkers = false;
  }

  async updateMarkers(recreate = false)
  {
    const map = this.map;
    if (this.updatingMarkers) return;
    
    this.updatingMarkers = true;
    this.setParametersFormEnabled(false);
    
    if (recreate)
    {
      for (let sourceId in this.markers)
      {
        let sourceMarkers = this.markers[sourceId];
        sourceMarkers.forEach(m => m.remove());
      }
      this.markers = {};
    }
    const markers = this.markers;

    let visibleLayers = 0;
    for (let layerSetup of this.layers)
    {
      let layerId = layerSetup.layerId;
      let layer = map.getLayer(layerId);
      let sourceId = layer.source;
     
      let source = map.getSource(sourceId);
      if (source.type === "geojson")
      {
        let sourceMarkers = markers[sourceId];        
        let visible = map.getLayoutProperty(layerId, "visibility") !== "none";
        if (map.getZoom() < layer.minzoom || map.getZoom() > layer.maxZoom)
        {
          visible = false;
        }
        
        if (visible)
        {
          visibleLayers++;
          if (sourceMarkers)
          {
            sourceMarkers.forEach(m => m.getElement().style.display = "");
          }
          else
          {
            const geojson = await source.getData();
            sourceMarkers = await this.createMarkers(geojson, layerSetup);
            markers[sourceId] = sourceMarkers;
          }
        }
        else
        {
          if (sourceMarkers)
          {
            sourceMarkers.forEach(m => m.getElement().style.display = "none");            
          }
        }
      }
    }
    this.setParametersFormVisible(visibleLayers > 0);
    this.setParametersFormEnabled(true);
    this.updatingMarkers = false;
  }
  
  async createMarkers(geojson, layerSetup)
  {
    const map = this.map;
    const chartType = layerSetup.chartType;
    let sourceMarkers = [];
        
    for (let feature of geojson.features)
    {
      const loadDiv = document.createElement("div");
      loadDiv.innerHTML = `<div style="width:24px;height:24px;border-radius:50%;background:rgba(240,240,240,0.8);color:black" 
        class="flex align-items-center justify-content-center">
        <i class="pi pi-spin pi-spinner"></i>
      </div>`;

      let marker = new maplibregl.Marker({ element : loadDiv });
      const centroid = turf.centroid(feature);
      marker.setLngLat(centroid.geometry.coordinates);
      marker.addTo(map);
      marker._feature = feature;
      sourceMarkers.push(marker);     
    }
    
    for (let marker of sourceMarkers)
    {
      const feature = marker._feature;
      const url = layerSetup.chartDataUrl(this.getParameterValues(), feature);
      const response = await fetch(url);
      const data = await response.json();
      let chart;
      switch (chartType)
      {
        case "donut":
          chart = this.createDonutChart(feature, data,
            layerSetup.radius, 
            layerSetup.holeFactor, 
            layerSetup.textSize,
            layerSetup.textColor,
            layerSetup.holeColor,
            layerSetup.holeOpacity,
            layerSetup.value);
          break;
        case "pie":
          chart = this.createDonutChart(feature, data, layerSetup.radius, 0);
          break;
      }
      if (chart)
      {
        marker.getElement().innerHTML = "";
        marker.getElement().appendChild(chart);
      }
    }
    return sourceMarkers;
  }

  onAdd(map)
  {
    this.map = map;
    const div = document.createElement("div");
    this.div = div;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `
      <div class="flex flex-column p-1" style="font-family:var(--font-family);min-width:146px">
        <style>
          .param_input {
            font-family: var(--font-family);
            font-size: 12px;border-color: var(--surface-border);
            border-radius: var(--border-radius);
            outline:none;
            border-style:solid;
            border-width:1px;
            background:var(--surface-ground);
            transition: background-color .2s,color .2s,border-color .2s,box-shadow .2s,opacity .2s;
          }
          .param_input:focus
          {
            border-color: var(--primary-color);
            box-shadow: var(--focus-ring);
          }
        </style>
        <div class="flex align-items-center"><i class="pi pi-chart-pie mr-1"></i> <strong>${this.options.title}</strong></div>
      </div>
    `;
    this.createParametersForm();
    
    map.on("idle", () => this.updateMarkers());
    map.on("zoomend", () => this.updateMarkers());

    this.createPanel(map);

    return div;
  }
  
  createParametersForm()
  {
    const formDiv = this.div.firstElementChild;

    for (let parameter of this.parameters)
    {
      let name = parameter.name;
      let label = parameter.label || name;
      let values = parameter.values;
      const labelElem = document.createElement("label");
      labelElem.textContent = label + ":";
      labelElem.className = "mt-1";
      labelElem.htmlFor = name;
      formDiv.appendChild(labelElem);
      if (values instanceof Array)
      {
        const selectElem = document.createElement("select");
        selectElem.className = "param_input";
        selectElem.id = name;
        formDiv.appendChild(selectElem);
        for (let value of values)
        {
          let optionElem = document.createElement("option");
          optionElem.value = typeof value === "string" ? value : value[0];
          optionElem.textContent = typeof value === "string" ? value : value[1];
          selectElem.appendChild(optionElem);
        }
        selectElem.addEventListener("change", () => this.updateMarkers(true));
      }
      else
      {
        const inputElem = document.createElement("input");
        inputElem.className = "param_input";
        inputElem.id = name;
      }
    }
  }
  
  getParameterValues()
  {
    const parameters = {};
    for (let parameter of this.parameters)
    {
      const name = parameter.name;
      const inputElem = document.getElementById(name);
      if (inputElem)
      {
        const value = inputElem.value;
        parameters[name] = value;
      }
    }
    return parameters;
  }
  
  setParametersFormEnabled(enabled)
  {
    for (let parameter of this.parameters)
    {
      const name = parameter.name;
      const inputElem = document.getElementById(name);
      if (inputElem)
      {
        inputElem.disabled = !enabled;
      }
    }    
  }

  setParametersFormVisible(visible)
  {
    this.div.style.display = visible ? "" : "none";
  }

  createDonutChart(feature, data, r = 32, f = 0.5, 
    textSize = 11, textColor = "#000000", holeColor = "#ffffff", 
    holeOpacity = 1, value)
  {
    const offsets = [];
    let total = 0; // sum of all data[*].value
    for (let i = 0; i < data.length; i++)
    {
      offsets.push(total);
      total += data[i].value;
    }
    const r0 = Math.round(r * f);
    const r1 = Math.round(r * 0.9);
    const w = r * 2;

    let html =
    `<div>
      <svg width="${w}" height="${w}" viewbox="0 0 ${w} ${w}" 
        text-anchor="middle" style="font: ${textSize}px; display: block">`;

    for (let i = 0; i < data.length; i++)
    {
      html +="<g>";
      html += "<title>" + data[i].label + "</title>";
      html += this.donutSegment(
              offsets[i] / total,
              (offsets[i] + data[i].value) / total,
              r,
              r0,
              data[i].color,
              data[i].label
              );
      html += this.donutSegment(
              offsets[i] / total,
              (offsets[i] + data[i].value) / total,
              r,
              r1,
              data[i].color,
              data[i].label
              );
      html += "</g>";
    }

    if (f > 0)
    {
      if (typeof value !== "function")
      {
        value = () => total.toLocaleString();
      }
      
      html += `
          <circle cx="${r}" cy="${r}" r="${r0}" 
            fill="${holeColor}" fill-opacity="${holeOpacity}" 
            stroke="#000000" stroke-width="0" />
          
          <text dominant-baseline="central" 
            fill="${textColor}"
            transform="translate(${r}, ${r})">${value(data)}</text>`;
    }
        
    html += `
      </svg>
    </div>`;

    const el = document.createElement('div');
    el.innerHTML = html;
    
    let sectors = el.getElementsByTagName("g");
    for (let i = 0; i < sectors.length; i++)
    {
      let sector = sectors[i];
      let path = sector.getElementsByTagName("path")[1];
      sector.style.cursor = "pointer";
      sector.addEventListener("click", () => this.showData(feature, data[i].id));
      sector.addEventListener("mouseover", () => 
      { path.setAttribute("fill", "#000000"); });
      sector.addEventListener("mouseout", () => 
      { path.setAttribute("fill", data[i].color); });
    }
    
    if (f > 0)
    {
      let circle = el.getElementsByTagName("circle")[0];
      circle.style.cursor = "pointer";
      circle.addEventListener("click", () => this.showData(feature, ""));

      circle.addEventListener("mouseover", () => 
      { circle.setAttribute("stroke-width", "2"); });
      circle.addEventListener("mouseout", () => 
      { circle.setAttribute("stroke-width", "0"); });

      let text = el.getElementsByTagName("text")[0];
      text.style.cursor = "pointer";
      text.addEventListener("click", () => this.showData(feature, ""));

      text.addEventListener("mouseover", () => 
      { circle.setAttribute("stroke-width", "2"); });
      text.addEventListener("mouseout", () => 
      { circle.setAttribute("stroke-width", "0"); });
    }
    
    return el.firstChild;
  }
  
  donutSegment(start, end, r, r0, color)
  {
    if (end - start === 1)
      end -= 0.00001;
    const a0 = 2 * Math.PI * (start - 0.25);
    const a1 = 2 * Math.PI * (end - 0.25);
    const x0 = Math.cos(a0),
            y0 = Math.sin(a0);
    const x1 = Math.cos(a1),
            y1 = Math.sin(a1);
    const largeArc = end - start > 0.5 ? 1 : 0;

    return [
      '<path d="M',
      r + r0 * x0,
      r + r0 * y0,
      'L',
      r + r * x0,
      r + r * y0,
      'A',
      r,
      r,
      0,
      largeArc,
      1,
      r + r * x1,
      r + r * y1,
      'L',
      r + r0 * x1,
      r + r0 * y1,
      'A',
      r0,
      r0,
      0,
      largeArc,
      0,
      r + r0 * x0,
      r + r0 * y0,
      `" fill="${color}" />`
    ].join(' ');
  }

  async showData(feature, id)
  {
    this.panel.show();  
    this.panel.bodyDiv.innerHTML = `<i class="pi pi-spin pi-spinner"></i> ${bundle.get("ChartControl.loading")}...`;

    let layerSetup = this.layers[0]; // TODO
    
    const url = layerSetup.tableDataUrl(this.getParameterValues(), feature, id);
    
    const response = await fetch(url);
    const json = await response.json();
    
    let html;
    if (json.length === 0)
    {
      html = `<div>${bundle.get("ChartControl.noData")}</div>`;
    }
    else
    {
      html = `
      <style>
        table.chart_data
        {
          border-spacing: 0;
          border-collapse: collapse;
          border: 1px solid var(--surface-400);
        }
        table.chart_data td        
        {
          border-top: 1px solid var(--surface-300);
          border-right: 1px solid var(--surface-200);
        }
        table.chart_data td:last-child
        {
          border-right:none;
        }        
      </style>
      <div style="width:100%;overflow:auto;"><table class="chart_data"><tr>`;
      for (let col of layerSetup.tableColumns)
      {
        html += `<th>${col.label}</th>`;
      }
      html += "</tr>";
      for (let row of json)
      {
        html += "<tr>";
        for (let col of layerSetup.tableColumns)
        {
          let styleClass = col.class || "text-left";
          let value = row[col.name];
          if (typeof col.format === "function")
          {
            value = col.format(value);
          }
          html += `<td class="${styleClass}">${value}</td>`;
        }
        html += "</tr>";
      }
      html += "</table></div>";
    }
    this.panel.bodyDiv.innerHTML = html;
  }
  
  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
  }
}

export { ChartControl };


