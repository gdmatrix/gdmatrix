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
    this.currentSourceId = null;
    this.markers = {}; // for sourceId: [markers...]
  }

  async updateMarkers()
  {
    const map = this.map;
    const markers = this.markers;
    
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
  }
  
  async createMarkers(geojson, layerSetup)
  {
    console.info(layerSetup);
    const map = this.map;
    const chartType = layerSetup.chartType;
    let sourceMarkers = [];
    
    for (let feature of geojson.features)
    {
      const url = layerSetup.chartDataUrl(feature);
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
        let marker = new maplibregl.Marker({ element : chart });
        const centroid = turf.centroid(feature);
        marker.setLngLat(centroid.geometry.coordinates);
        marker.addTo(map);
        sourceMarkers.push(marker);
      }
    }
    return sourceMarkers;
  }

  onAdd(map)
  {
    this.map = map;
    const div = document.createElement("div");
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.innerHTML = `<button><span class="pi pi-chart-pie"/></button>`;
    div.title = bundle.get("ChartControl.title");
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      console.info("chart");
    });

    map.on("idle", () => this.updateMarkers());
    map.on("zoomend", () => this.updateMarkers());
    
    this.createPanel(map);

    return div;
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
    let layerSetup = this.layers[0]; // TODO
    
    const url = layerSetup.tableDataUrl(feature, id);
    
    const response = await fetch(url);
    const json = await response.json();
    console.info(json);
        
    let html = `<table><tr>`;
    for (let col of layerSetup.tableLabels)
    {
      html += `<th>${col}</th>`;
    }
    html += "</tr>";
    for (let row of json)
    {
      html += "<tr>";
      for (let col of layerSetup.tableColumns)
      {
        html += `<td>${row[col]}</td>`;     
      }
      html += "</tr>";
    }
    html += "</table>";

    this.panel.bodyDiv.innerHTML = html;
    this.panel.show();  
  }
  
  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
  }
}

export { ChartControl };


