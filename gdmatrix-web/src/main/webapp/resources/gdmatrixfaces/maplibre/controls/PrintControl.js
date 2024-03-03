/* PrintControl */

import { Panel } from "../ui/Panel.js";
import { toUtm } from "../utm-latlng.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class PrintControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "left",
        "iconClass" : "pi pi-print",
        "title" : bundle.get("PrintControl.title"),
        "srs" : "EPSG:25831",
        "ellipsoid" : "ETRS89",
        "defaultPrintReport" : { reportName: "generic_a4_apaisat" }
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);

    const bodyDiv = this.panel.bodyDiv;

    const optionsDiv = document.createElement("div");
    this.optionsDiv = optionsDiv;
    optionsDiv.innerHTML = `
      <div class="formgrid grid">
        <div class="field col-12">
          <label for="print_format">${bundle.get("PrintControl.printFormat")}:</label>
          <select id="print_format" type="text">
            <option value="pdf">PDF</option>
            <option value="html">HTML</option>
          </select>
        </div>
        <div class="field col-12">
          <label for="print_report">${bundle.get("PrintControl.reportName")}:</label>
          <select id="print_report" type="text" class="w-full">
          </select>
        </div>
        <div class="field col-12">
          <label for="print_title">${bundle.get("PrintControl.reportTitle")}:</label>
          <input id="print_title" spellcheck="false" class="w-full"></input>
        </div>
        <div class="field col-12">
          <label for="print_scale">${bundle.get("PrintControl.scale")}:</label>
          <select id="print_scale">
            <option value="0">${bundle.get("PrintControl.currentWindow")}</option>
            <option value="25">1 : 25</option>
            <option value="50">1 : 50</option>
            <option value="100">1 : 100</option>
            <option value="200">1 : 200</option>
            <option value="250">1 : 250</option>
            <option value="500">1 : 500</option>
            <option value="1000">1 : 1000</option>
            <option value="2000">1 : 2000</option>
            <option value="5000">1 : 5000</option>
            <option value="10000">1 : 10000</option>
          </select>
        </div>
      </div>
    `;
    bodyDiv.appendChild(this.optionsDiv);

    this.reportNameSelect = optionsDiv.querySelector("#print_report");
    this.reportTitleElem = optionsDiv.querySelector("#print_title");
    this.scaleSelect = optionsDiv.querySelector("#print_scale");
    this.formatElem = optionsDiv.querySelector("#print_format");
    
    this.formatElem.addEventListener("change", () => {
      let format = this.formatElem.value;
      let div = this.reportNameSelect.parentElement;
      div.style.display = format === "html" ? "none" : "";

      div = this.scaleSelect.parentElement;
      div.style.display = format === "html" ? "none" : "";    
    });

    const style = map.getStyle();
    let printReports = style.metadata?.printReports;
    if (printReports === undefined) printReports = [];
    if (printReports.length === 0)
    {
      printReports.push(this.options.defaultPrintReport);
    }
    
    for (let printReport of printReports)
    {
      let option = document.createElement("option");
      option.value = printReport.reportName;
      option.textContent = printReport.label || printReport.reportName;
      this.reportNameSelect.appendChild(option);
    }

    this.buttonsDiv = document.createElement("div");
    this.buttonsDiv.className = "button_bar text-right";
    bodyDiv.appendChild(this.buttonsDiv);

    this.printButton = document.createElement("button");
    this.printButton.textContent = bundle.get("button.print");
    this.printButton.addEventListener("click", (event) => {
      event.preventDefault();
      
      if (this.formatElem.value === "pdf")
      {
        this.printPdf();      
      }
      else if (this.formatElem.value === "html")
      {
        this.printHtml();
      }
    });
    this.buttonsDiv.appendChild(this.printButton);
  }
  
  printPdf()
  {
    const map = this.map;
    const style = map.getStyle();
    const lastUtm = map.lastUtm;
    const layers = style.layers;
    const reportName = this.reportNameSelect.value;
    const reportTitle = this.reportTitleElem.value;
    const scale = this.scaleSelect.value;

    const bounds = map.getBounds();
    const sw = bounds.getSouthWest();
    const ne = bounds.getNorthEast();
    const ellipsoid = this.options.ellipsoid;

    let utm1 = toUtm(sw.lat, sw.lng, 7, ellipsoid);
    let utm2 = toUtm(ne.lat, ne.lng, 7, ellipsoid);
    const bbox = utm1.easting + "," + utm1.northing + "," +
     utm2.easting + "," + utm2.northing;

    const location = document.location;
    const port = location.port ? ":" + location.port : "";

    let visibility = "";
    for (let layer of layers)
    {
      visibility += layer.metadata?.visible ? "1" : "0";
    }

    let url = location.protocol + "//" + location.host + port
      + "/reports/" + reportName + ".pdf?"
      + "layer_visibility=" + visibility
      + "&map_name=" + style.name
      + "&scale=" + scale
      + "&bbox=" + bbox
      + "&srs=" + this.options.srs
      + "&bridges=geo";

    if (reportTitle)
    {
      url += "&title=" + reportTitle;
    }

    if (lastUtm)
    {
      url += "&last_x=" + lastUtm.easting + "&last_y=" + lastUtm.northing;
    }

    window.open(url, "_blank");
  }
  
  printHtml()
  {
    const map = this.map;
    const canvas = map.getCanvas();
    const imageURL = canvas.toDataURL("image/png");

    let reportTitle = this.reportTitleElem.value;
    if (!reportTitle) reportTitle = map.getStyle().name;

    const win = window.open("", "Print");
    win.document.body.innerHTML = `
      <style>
        .page {
         border: 1px solid black;
         display:inline-block;
        h1 {
          padding: 2px;
          margin:0;
          font-size: 16px;
          border-bottom: 1px solid #c0c0c0;
        }
      }
      </style>
      <div class="page">
        <h1>${reportTitle}</h1>
        <img src="${imageURL}"></img>
      </div>
    `;  
  }  

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="${this.options.iconClass}"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group";
    div.title = this.options.title;
    div.style.width = "29px";
    div.style.height = "29px";
    div.style.fontFamily = "var(--font-family)";
    div.addEventListener("contextmenu", (e) => e.preventDefault());
    div.addEventListener("click", (e) =>
    {
      e.preventDefault();
      this.panel.show();
    });

    this.createPanel(map);

    return div;
  }
}

export { PrintControl };
