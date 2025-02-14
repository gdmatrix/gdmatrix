/* LegendControl.js */

import { Panel } from "../ui/Panel.js";
import { Bundle } from "../i18n/Bundle.js";

const bundle = Bundle.getBundle("main");

class LegendControl
{
  constructor(options)
  {
    this.options = {...{
        "position" : "right",
        "title" : bundle.get("LegendControl.title"),
        "iconClass" : "fa fa-layer-group"
      }, ...options};
  }

  createPanel(map)
  {
    this.panel = new Panel(map, this.options);
  }

  onAdd(map)
  {
    this.map = map;

    const div = document.createElement("div");
    this.div = div;
    div.innerHTML = `<button><span class="fa fa-layer-group"/></button>`;
    div.className = "maplibregl-ctrl maplibregl-ctrl-group flex align-items-center justify-content-center";
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
    this.populateTree();

    return div;
  }

  populateTree()
  {
    const style = this.map.getStyle();
    const legend = style?.metadata?.legend;
    const bodyDiv = this.panel.bodyDiv;
    if (legend)
    {
      this.titleDiv = legend.label || "Legend";
      if (legend.children && legend.children.length > 0)
      {
        const ul = document.createElement("ul");
        ul.className = "legend";
        bodyDiv.appendChild(ul);
        
        for (let childNode of legend.children)
        {
          this.populateNode(childNode, ul);
          childNode.parent = legend;
        }
        this.updateLegendStyle();
      }
    }
  }

  populateNode(node, ul)
  {
    const li = document.createElement("li");
    ul.appendChild(li);
    const liDiv = document.createElement("div");
    li.appendChild(liDiv);
    if (node.layerId)
    {
      li.className = "layer";
    }
    else
    {
      li.className = "group";

      if (node.children === undefined || node.children.length === 0)
      {
        li.style.display = "none";
        li.ariaHidden = "true";
      }

      const button = document.createElement("button");
      liDiv.appendChild(button);
      node.button = button;
      button.innerHTML = `<span class="pi pi-angle-right"></span>`;
      button.firstElementChild.style.transform = node.expanded ? 
        "rotate(90deg)" : "rotate(0deg)";

      if (node.mode === "block")
      {
        button.style.visibility = "hidden";
      }
      else
      {
        button.addEventListener("click", (event) =>
        {
          event.preventDefault();
          this.toogleNodeExpand(node);
        });
      }
    }
    const link = document.createElement("a");
    liDiv.appendChild(link);

    let icon = document.createElement("i"); // eye icon
    link.appendChild(icon);

    this.addNodeGraphicAndLabel(node, link);
    
    link.href = "#";
    node.link = link;
    link.addEventListener("click", (event) =>
    {
      event.preventDefault();
      if (node.layerId) // layer node
      {
        this.changeNodeVisibility(node, "toggle");
//        if (node.parent?.mode === "single")
//        {
//          this.changeSingleNodeVisibility(node);
//        }
//        else
//        {
//          this.changeNodeVisibility(node, "toggle");
//        }
      }
      else // group node
      {
        let groupVisible =
          node.link?.firstElementChild?.className === "pi pi-eye";
        if (node.mode === "single")
        {
          if (groupVisible) this.changeNodeVisibility(node, "none");
          else if (node.children.length > 0)
          {
            this.changeNodeVisibility(node.children[0], "visible");
          };
        }
        else
        {
          this.changeNodeVisibility(node, groupVisible ? "none" : "visible");
        }
//        if (node.parent?.mode === "single")
//        {
//          this.changeSingleNodeVisibility(node);
//        }
      }
      this.updateLegendStyle();
    });

    if (node.children && node.children.length > 0 && node.mode !== "block")
    {
      const subul = document.createElement("ul");
      node.ul = subul;
      node.ul.style.height = node.expanded ? "auto" : "0";

      li.appendChild(subul);
      for (let childNode of node.children)
      {
        this.populateNode(childNode, subul);
        childNode.parent = node;
      }
    }
  }

//  changeSingleNodeVisibility(node)
//  {
//    let visibleNode = null;
//    for (let childNode of node.parent.children)
//    {
//      if (childNode.link?.firstElementChild?.className === "pi pi-eye")
//      {
//        visibleNode = childNode;
//        break;
//      }
//    }
//    if (visibleNode)
//    {
//      this.changeNodeVisibility(visibleNode, "none");
//    }
//    if (visibleNode !== node)
//    {
//      this.changeNodeVisibility(node, "visible");
//    }
//  }

  addNodeGraphicAndLabel(node, link)
  {
    const labelSpan = document.createElement("span");
    labelSpan.textContent = node.label;
    
    let graphic = node.graphic || null;
    if (graphic === null)
    {
      link.appendChild(labelSpan);
      link.classList.add("align-items-center");
    }
    else if (graphic.startsWith("square:") ||
             graphic.startsWith("circle:"))
    {
      const graphicSpan = document.createElement("span");
      graphicSpan.className = "graphic";
      graphicSpan.style.display = "inline-block";
      graphicSpan.style.width = "16pt";
      graphicSpan.style.height = "16pt";
      if (graphic.startsWith("square:"))
      {
        let color = graphic.substring(7);
        graphicSpan.style.backgroundColor = color;
        graphicSpan.classList.add("square");
      }
      else if (graphic.startsWith("circle:"))
      {
        let color = graphic.substring(7);
        graphicSpan.style.backgroundColor = color;
        graphicSpan.classList.add("circle");
      }
      link.appendChild(graphicSpan);
      link.appendChild(labelSpan);
      link.classList.add("align-items-center");
    }
    else if (graphic.startsWith("icon:"))
    {
      let imageDef = graphic.substring(5);
      let parts = imageDef.split("/");
      let docId = parts[0];

      let img = document.createElement("img");     
      img.className = "graphic";
      img.alt = "";
      img.src = "/documents/" + docId;

      if (parts.length >= 2)
      {
        let imageWidth = parseInt(parts[1]);
        let imageHeight;
        if (parts.length >= 3)
        {
          imageHeight = parseInt(parts[2]);
        }
        else
        {
          imageHeight = imageWidth;
        }
        img.style.width = imageWidth + "pt";
        img.style.height = imageHeight + "pt";
      }
      link.appendChild(img);
      link.appendChild(labelSpan);
      link.classList.add("align-items-center");
    }
    else if (graphic.startsWith("image:"))
    {
      let imageDef = graphic.substring(6);
      let parts = imageDef.split("/");
      let docId = parts[0];

      let img = document.createElement("img");
      img.className = "block pt-1";
      img.alt = "";
      img.src = "/documents/" + docId;

      if (parts.length >= 2)
      {
        let imageWidth = parseInt(parts[1]);
        let imageHeight;
        if (parts.length >= 3)
        {
          imageHeight = parseInt(parts[2]);
        }
        else
        {
          imageHeight = imageWidth;
        }
        img.style.width = imageWidth + "pt";
        img.style.height = imageHeight + "pt";
      }

      const div = document.createElement("div");
      div.appendChild(labelSpan);
      div.appendChild(img);
      link.appendChild(div);
      link.classList.add("align-items-start");
    }
    else if (graphic.startsWith("url:"))
    {
      let url = graphic.substring(4);
      let img = document.createElement("img");
      img.className = "block pt-1";
      img.alt = "";
      img.src = url;

      const div = document.createElement("div");
      div.appendChild(labelSpan);
      div.classList.add("url");
      div.appendChild(img);
      link.appendChild(div);
      link.classList.add("align-items-start");    
    }
    else if (graphic === "auto")
    {
      let img = document.createElement("img");
      img.className = "block pt-1";
      img.alt = "";
      // img.src = ... get url from service
      
      const div = document.createElement("div");
      div.appendChild(labelSpan);
      div.appendChild(img);
      link.appendChild(div);      
      link.classList.add("align-items-start");
    }
  }
  
  toogleNodeExpand(node)
  {
    const ul = node.ul;
    const button = node.button;
    const height = Math.max(ul.offsetHeight, ul.scrollHeight);

    node.expanded = !node.expanded;

    if (node.expanded) // expand
    {
      if (button) button.firstElementChild.style.transform = "rotate(90deg)";
      this.animateList(ul, 0, height, Math.round(height / 20));
    }
    else // collapse
    {
      if (button) button.firstElementChild.style.transform = "rotate(0)";
      this.animateList(ul, height, 0, -Math.round(height / 20));
    }
  }
  
  animateList(ul, height, endHeight, step)
  { 
    ul.style.height = height + "px";
    height += step;

    if (step > 0)
    {
      if (height >= endHeight)
      {
        ul.style.height = "auto";
        return;
      }
    }
    else // step < 0
    {
      if (height <= 0)
      {
        ul.style.height = "0";        
        return;
      }
    }
    setTimeout(() => this.animateList(ul, height, endHeight, step), 10);
  }

  updateLegendStyle()
  {
    const map = this.map;
    const style = map.getStyle();
    const legend = style.metadata.legend;
    if (legend)
    {
      if (legend.children && legend.children.length > 0)
      {
        for (let childNode of legend.children)
        {
          this.updateNodeStyle(childNode);
        }
      }
    }
  }

  updateNodeStyle(node)
  {
    const map = this.map;
    const layerId = node.layerId;
    let nodeVisible = false;

    if (layerId) // layer node
    {
      const layer = map.getLayer(layerId);
      if (layer.metadata === undefined) layer.metadata = {};
      nodeVisible = layer.metadata.visible !== false;
    }
    else // group node
    {
      if (node.children)
      {
        for (let childNode of node.children)
        {
          let childVisible = this.updateNodeStyle(childNode);
          nodeVisible = nodeVisible || childVisible;
        }
      }
    }
    
    const link = node.link;    
    if (link === undefined) return nodeVisible;
    
    const iconSpan = link.firstElementChild;

    if (nodeVisible === false)
    {
      link.classList.add("hidden_layer");
      iconSpan.className = "pi pi-eye-slash";
    }
    else
    {
      link.classList.remove("hidden_layer");
      iconSpan.className = "pi pi-eye";
    }
    return nodeVisible;
  }

  changeNodeVisibility(node, mode = "toggle", sourceSet = null)
  {
    const map = this.map;
    const layerId = node.layerId || null;
    if (sourceSet === null) sourceSet = new Set();

    if (layerId) // layer node
    {
      const layer = map.getLayer(layerId);
      if (layer.metadata === undefined) layer.metadata = {};

      if (mode === "visible")
      {
        layer.metadata.visible = true;
        this.hideSingleLayers(node);
      }
      else if (mode === "none")
      {
        layer.metadata.visible = false;
      }
      else // toggle
      {
        if (layer.metadata.visible === false)
        {
          layer.metadata.visible = true;
          this.hideSingleLayers(node);
        }
        else
        {
          layer.metadata.visible = false;
        }
      }
      if (layer.metadata.layers) // serviceParameters layer
      {
        const sourceId = layer.source;
        sourceSet.add(sourceId);
      }
      else
      {
        // apply visibility immediately
        map.setLayoutProperty(layerId, "visibility",
          layer.metadata.visible ? "visible" : "none");
      }
    }
    else // group node
    {
      for (let childNode of node.children)
      {
        this.changeNodeVisibility(childNode, mode, sourceSet);
      }
    }
    for (let sourceId of sourceSet)
    {
      const source = map.getSource(sourceId);
      const style = map.getStyle();
      let serviceParameters = style.metadata.serviceParameters[sourceId];
      let masterLayerId = serviceParameters.masterLayer;

      const sourceUrl = getSourceUrl(sourceId, style);
      if (sourceUrl) source.setTiles([sourceUrl]);

      const visibility = sourceUrl ? "visible" : "none";

      setTimeout(() =>
        map.setLayoutProperty(masterLayerId, "visibility", visibility), 100);
    }   
  }
  
  hideSingleLayers(node)
  {
    if (node.parent)
    {
      if (node.parent.mode === "single")
      {
        for (let childNode of node.parent.children)
        {
          if (childNode !== node)
          {
            this.changeNodeVisibility(childNode, "none");
          }
        }
      }
      this.hideSingleLayers(node.parent);
    }
  }
}

export { LegendControl };