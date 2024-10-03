/* codemirror-completion.js */

if (!window.MapLibreCompletion)
{
  class MapLibreCompletion
  {
    static maplibreFunctions = [
      ["array", "types-array"],
      ["boolean", "types-boolean"],
      ["collator", "types-collator"],
      ["format", "types-format"],
      ["image", "types-image"],
      ["literal", "types-literal"],
      ["number", "types-number"],
      ["number-format", "types-number-format"],
      ["object", "types-object"],
      ["string", "types-string"],
      ["to-boolean", "types-to-boolean"],
      ["to-color", "types-to-color"],
      ["to-number", "types-to-number"],
      ["to-string", "types-to-string"],
      ["typeof", "types-typeof"],
      "accumulated",
      "feature-state",
      "geometry-type",
      "id",
      "line-progress",
      "properties",
      "at",
      "get",
      "has",
      "in",
      "index-of",
      "length",
      "slice",
      "!",
      "!=",
      "<",
      "<=",
      "==",
      ">",
      ">=",
      "all",
      "any",
      "case",
      "coalesce",
      "match",
      "within",
      "interpolate",
      "interpolate-hcl",
      "interpolate-lab",
      "step",
      "let",
      "var",
      "concat",
      "downcase",
      "is-supported-script",
      "resolved-locale",
      "upcase",
      "rgb",
      "rgba",
      "to-rgba",
      "-",
      "*",
      "/",
      "%",
      "^",
      "+",
      "abs",
      "acos",
      "asin",
      "atan",
      "ceil",
      "cos",
      "distance",
      "e",
      "floor",
      "ln",
      "ln2",
      "log10",
      "log2",
      "max",
      "min",
      "pi",
      "round",
      "sin",
      "sqrt",
      "tan",
      "zoom",
      "heatmap-density"
    ];

    static maplibrePaint = {
      "background": [
        "background-color", 
        "background-opacity",
        "background-pattern"
      ],
      "fill": [
        "fill-antialias",
        "fill-color",
        "fill-opacity",
        "fill-outline-color",
        "fill-pattern",
        "fill-translate",
        "fill-translate-anchor"
      ],
      "line": [
        "line-blur",
        "line-color",
        "line-dasharray",
        "line-gap-width",
        "line-gradient",
        "line-offset",
        "line-opacity",
        "line-pattern",
        "line-translate",
        "line-translate-anchor",
        "line-width"
      ],
      "symbol": [
        "icon-color",
        "icon-halo-blur",
        "icon-halo-color",
        "icon-halo-width",
        "icon-opacity",
        "icon-translate",
        "icon-translate-anchor",
        "text-color",
        "text-halo-blur",
        "text-halo-color",
        "text-halo-width",
        "text-opacity",
        "text-translate",
        "text-translate-anchor"
      ],
      "raster": [
        "raster-brightness-max",
        "raster-brightness-min",
        "raster-contrast",
        "raster-fade-duration",
        "raster-hue-rotate",
        "raster-opacity",
        "raster-resampling",
        "raster-saturation"
      ],
      "circle": [
        "circle-blur",
        "circle-color",
        "circle-opacity",
        "circle-pitch-alignment",
        "circle-pitch-scale",
        "circle-radius",
        "circle-stroke-color",
        "circle-stroke-opacity",
        "circle-stroke-width",
        "circle-translate",
        "circle-translate-anchor"
      ],
      "fill-extrusion": [
        "fill-extrusion-base",
        "fill-extrusion-color",
        "fill-extrusion-height",
        "fill-extrusion-opacity",
        "fill-extrusion-pattern",
        "fill-extrusion-translate",
        "fill-extrusion-translate-anchor",
        "fill-extrusion-vertical-gradient"
      ],
      "heatmap": [
        "heatmap-color",
        "heatmap-intensity",
        "heatmap-opacity",
        "heatmap-radius",
        "heatmap-weight"
      ],
      "hillshade": [
        "hillshade-accent-color",
        "hillshade-exaggeration",
        "hillshade-highlight-color",
        "hillshade-illumination-anchor",
        "hillshade-illumination-direction",
        "hillshade-shadow-color"
      ]
    };

    static maplibreLayout = {
      "fill": [
        "fill-sort-key"
      ],
      "line": [
        "line-cap",
        "line-join",
        "line-miter-limit",
        "line-round-limit",
        "line-sort-key"
      ],
      "symbol": [
        "icon-allow-overlap",
        "icon-anchor",
        "icon-ignore-placement",
        "icon-image",
        "icon-keep-upright",
        "icon-offset",
        "icon-optional",
        "icon-overlap",
        "icon-padding",
        "icon-pitch-alignment",
        "icon-rotate",
        "icon-rotation-alignment",
        "icon-size",
        "icon-text-fit",
        "icon-text-fit-padding",
        "symbol-avoid-edges",
        "symbol-placement",
        "symbol-sort-key",
        "symbol-spacing",
        "symbol-z-order",
        "text-allow-overlap",
        "text-anchor",
        "text-field",
        "text-font",
        "text-allow-overlap",
        "text-ignore-placement",
        "text-justify",
        "text-keep-upright",
        "text-letter-spacing",
        "text-line-height",
        "text-max-angle",
        "text-max-width",
        "text-offset",
        "text-optional",
        "text-padding",
        "text-pitch-alignment",
        "text-radial-offset",
        "text-rotate",
        "text-rotation-alignment",
        "text-size",
        "text-transform",
        "text-variable-anchor",
        "text-writing-mode"
      ],
      "circle": [
        "circle-sort-key"
      ]
    };

    static getFunctionOptions()
    {
      let options = [];
      for (let fn of this.maplibreFunctions)
      {
        let functionName;
        let key;
        
        if (fn instanceof Array)
        {
          functionName = fn[0];
          key = fn[1];
        }  
        else
        {
          functionName = fn;
          key = fn;
        }
        
        let option = {
          "label": functionName, 
          "type": "function",
          "info": this.maplibreFnInfo(key) 
        };
        options.push(option);
      }
      return options;
    }

    static getPaintCompletion(layerType)
    {
      let paintOptions = [];
      let properties = this.maplibrePaint[layerType];
      if (!properties) return null;

      for (let property of properties)
      {
        let key = "paint-" + layerType + "-" + property;  
        let option = {
          "label": property, 
          "type": "constant", 
          "info": this.maplibreInfo(key) 
        };
        paintOptions.push(option);
      }
      return (context) => this.maplibreComplete(context, 
        this.getFunctionOptions(), paintOptions);
    }

    static getLayoutCompletion(layerType)
    {
      let layoutOptions = [];
      let properties = this.maplibreLayout[layerType];
      if (!properties) return null;

      for (let property of properties)
      {
        let key = "layout-" + layerType + "-" + property;  
        let option = {
          "label": property, 
          "type": "constant", 
          "info": this.maplibreInfo(key) 
        };
        layoutOptions.push(option);
      }
      return (context) => this.maplibreComplete(context, 
        this.getFunctionOptions(), layoutOptions);
    }

    static getFilterCompletion()
    {
      return (context) => this.maplibreComplete(context, this.getFunctionOptions());
    }

    static maplibreInfo(key)
    {
      let div = document.createElement("div");
      div.innerHTML = `<div><a href="https://maplibre.org/maplibre-style-spec/layers/#${key}" target="blank">+Info</a></div>`;
      return () => div;
    }

    static maplibreFnInfo(key)
    {
      let div = document.createElement("div");
      div.innerHTML = `<div><a href="https://maplibre.org/maplibre-style-spec/expressions/#${key}" target="blank">+Info</a></div>`;
      return () => div;
    }

    static maplibreComplete(context, functionOptions, propertyOptions)
    {
      if (!functionOptions) 
        return null;

      let word = context.matchBefore(/\[\"(\w|\-)*/); // functions
      if (word)
      {
        return {
          from: word.from + 2,
          options: functionOptions
        };
      };

      if (!propertyOptions) 
        return null;

      word = context.matchBefore(/\"(\w|\-)*/); // properties
      if (word === null) return null;
      
      let text = context.state.doc.toString();
      let index = word.from - 1;
      let auto = false;
      let indent = 0;
      while (index >= 0 && indent <= 2)
      {
        let ch = text[index];
        if (ch === '\n')
        {
          auto = true;
          break;
        }
        else if (ch === " ")
        {
          indent++;
          index--;
        }
        else if (ch === "\t")
        {
          indent += 2;
          index--;
        }
        else break;
      }

      if (!auto) return null;

      return {
        from: word.from + 1,
        options: propertyOptions
      };
    }
  }
  window.MapLibreCompletion = MapLibreCompletion;
}
