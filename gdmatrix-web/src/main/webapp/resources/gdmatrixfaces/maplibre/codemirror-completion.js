/* codemirror-completion.js */

var maplibrePaint = {
  "background": [
    { label: '"background-color"', type: "text" },
    { label: '"background-opacity"', type: "text" },
    { label: '"background-pattern"', type: "text" }    
  ],
  "fill": [
    { label: '"fill-antialias"', type: "text" },
    { label: '"fill-color"', type: "text" },
    { label: '"fill-opacity"', type: "text" },
    { label: '"fill-outline-color"', type: "text" },
    { label: '"fill-pattern"', type: "text" },
    { label: '"fill-translate"', type: "text" },
    { label: '"fill-translate-anchor"', type: "text" }
  ],
  "line": [
    { label: '"line-blur"', type: "text" },
    { label: '"line-color"', type: "text" },
    { label: '"line-dasharray"', type: "text" },
    { label: '"line-gap-width"', type: "text" },
    { label: '"line-gradient"', type: "text" },
    { label: '"line-offset"', type: "text" },    
    { label: '"line-opacity"', type: "text" },    
    { label: '"line-pattern"', type: "text" },    
    { label: '"line-translate"', type: "text" },    
    { label: '"line-translate-anchor"', type: "text" },    
    { label: '"line-width"', type: "text" }
  ],
  "symbol": [
    { label: '"icon-color"', type: "text" },        
    { label: '"icon-halo-blur"', type: "text" },            
    { label: '"icon-halo-color"', type: "text" },            
    { label: '"icon-halo-width"', type: "text" },            
    { label: '"icon-opacity"', type: "text" },            
    { label: '"icon-translate"', type: "text" },            
    { label: '"icon-translate-anchor"', type: "text" },
    { label: '"text-color"', type: "text" },                
    { label: '"text-halo-blur"', type: "text" },                
    { label: '"text-halo-color"', type: "text" },                
    { label: '"text-halo-width"', type: "text" },    
    { label: '"text-opacity"', type: "text" },    
    { label: '"text-translate"', type: "text" },    
    { label: '"text-translate-anchor"', type: "text" }    
  ],
  "raster": [
    { label: '"raster-brightness-max"', type: "text" },
    { label: '"raster-brightness-min"', type: "text" },
    { label: '"raster-contrast"', type: "text" },    
    { label: '"raster-fade-duration"', type: "text" },    
    { label: '"raster-hue-rotate"', type: "text" },    
    { label: '"raster-opacity"', type: "text" },    
    { label: '"raster-resampling"', type: "text" },    
    { label: '"raster-saturation"', type: "text" }
  ],
  "circle": [
    { label: '"circle-blur"', type: "text" },
    { label: '"circle-color"', type: "text" },
    { label: '"circle-opacity"', type: "text" },
    { label: '"circle-pitch-alignment"', type: "text" },
    { label: '"circle-pitch-scale"', type: "text" },
    { label: '"circle-radius"', type: "text" },
    { label: '"circle-stroke-color"', type: "text" },
    { label: '"circle-stroke-opacity"', type: "text" },
    { label: '"circle-stroke-width"', type: "text" },    
    { label: '"circle-translate"', type: "text" },
    { label: '"circle-translate-anchor"', type: "text" }
  ],
  "fill-extrusion": [
    { label: '"fill-extrusion-base"', type: "text" },
    { label: '"fill-extrusion-color"', type: "text" },
    { label: '"fill-extrusion-height"', type: "text" },    
    { label: '"fill-extrusion-opacity"', type: "text" },    
    { label: '"fill-extrusion-pattern"', type: "text" },    
    { label: '"fill-extrusion-translate"', type: "text" },    
    { label: '"fill-extrusion-translate-anchor"', type: "text" },    
    { label: '"fill-extrusion-vertical-gradient"', type: "text" }
  ],
  "heatmap": [
    { label: '"heatmap-color"', type: "text" },    
    { label: '"heatmap-intensity"', type: "text" },
    { label: '"heatmap-opacity"', type: "text" },    
    { label: '"heatmap-radius"', type: "text" },    
    { label: '"heatmap-weight"', type: "text" }
  ],
  "hillshade": [
    { label: '"hillshade-accent-color"', type: "text" },    
    { label: '"hillshade-exaggeration"', type: "text" },    
    { label: '"hillshade-highlight-color"', type: "text" },    
    { label: '"hillshade-illumination-anchor"', type: "text" },        
    { label: '"hillshade-illumination-direction"', type: "text" },        
    { label: '"hillshade-shadow-color"', type: "text" }
  ]
};

var maplibreLayout = {
  "fill": [
    { label: '"fill-sort-key"', type: "text" }
  ],
  "line": [
    { label: '"line-cap"', type: "text" },
    { label: '"line-join"', type: "text" },
    { label: '"line-miter-limit"', type: "text" },
    { label: '"line-round-limit"', type: "text" },    
    { label: '"line-sort-key"', type: "text" }
  ],
  "symbol": [
    { label: '"icon-allow-overlap"', type: "text" },
    { label: '"icon-anchor"', type: "text" },
    { label: '"icon-ignore-placement"', type: "text" },
    { label: '"icon-image"', type: "text" },
    { label: '"icon-keep-upright"', type: "text" },    
    { label: '"icon-offset"', type: "text" },    
    { label: '"icon-optional"', type: "text" },    
    { label: '"icon-overlap"', type: "text" },    
    { label: '"icon-padding"', type: "text" },    
    { label: '"icon-pitch-alignment"', type: "text" },    
    { label: '"icon-rotate"', type: "text" },    
    { label: '"icon-rotation-alignment"', type: "text" },    
    { label: '"icon-size"', type: "text" },    
    { label: '"icon-text-fit"', type: "text" },    
    { label: '"icon-text-fit-padding"', type: "text" },    
    { label: '"symbol-avoid-edges"', type: "text" },    
    { label: '"symbol-placement"', type: "text" },    
    { label: '"symbol-sort-key"', type: "text" },        
    { label: '"symbol-spacing"', type: "text" },        
    { label: '"symbol-z-order"', type: "text" },        
    { label: '"text-allow-overlap"', type: "text" },        
    { label: '"text-anchor"', type: "text" },            
    { label: '"text-field"', type: "text" },            
    { label: '"text-font"', type: "text" },            
    { label: '"text-allow-overlap"', type: "text" },    
    { label: '"text-ignore-placement"', type: "text" },        
    { label: '"text-justify"', type: "text" },        
    { label: '"text-keep-upright"', type: "text" },        
    { label: '"text-letter-spacing"', type: "text" },        
    { label: '"text-line-height"', type: "text" },        
    { label: '"text-max-angle"', type: "text" },        
    { label: '"text-max-width"', type: "text" },        
    { label: '"text-offset"', type: "text" },        
    { label: '"text-optional"', type: "text" },        
    { label: '"text-padding"', type: "text" },        
    { label: '"text-pitch-alignment"', type: "text" },        
    { label: '"text-radial-offset"', type: "text" },        
    { label: '"text-rotate"', type: "text" },        
    { label: '"text-rotation-alignment"', type: "text" },        
    { label: '"text-size"', type: "text" },        
    { label: '"text-transform"', type: "text" },        
    { label: '"text-variable-anchor"', type: "text" },        
    { label: '"text-writing-mode"', type: "text" }          
  ],
  "circle": [
    { label: '"circle-sort-key"', type: "text" }
  ]
};

function getPaintCompletion(layerType)
{
  return (context) => maplibreComplete(context, maplibrePaint[layerType]);
}

function getLayoutCompletion(layerType)
{
  return (context) => maplibreComplete(context, maplibreLayout[layerType]);
}

function getFilterCompletion()
{
  return null;
}

function maplibreComplete(context, options) 
{
  if (options === undefined) return null;
  let word = context.matchBefore(/\"(\w|\-)*/);
  if (word === null) return null;

  let text = context.state.doc.toString();
  let index = word.from - 1;
  let auto = false;
  while (index >= 0)
  {
    let ch = text[index];
    if (ch === '\n')
    {
      auto = true;
      break;
    }
    else if (ch === " " || ch === "\t")
    {
      index--;
    }
    else break;
  }

  if (!auto) return null;

  return {
    from: word.from,
    options: options
  };
}    
