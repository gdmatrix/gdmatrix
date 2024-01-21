/* bundle main.js */

import { Bundle } from "../i18n/Bundle.js";

Bundle.getBundle("main").setTranslations("", {
  "button.accept" : "Accept",
  "button.cancel" : "Cancel",
  "button.find" : "Find",
  "button.clear" : "Clear",
  "button.reset" : "Reset",
  "button.undo" : "Undo",
  "button.export" : "Export",

  "ExportAreaTool.title": "Export area",
  "ExportAreaTool.help": "Draw the vertices of the export area and press the Export button.",
  
  "FindControl.title": "Find",
  "FindControl.layer": "Layer",
  "FindControl.filter": "Filter",
  "FindControl.featureCount": (count) => count === 1 ? `${count} feature found:` : `${count} features found:`,
  
  "GetFeatureInfoTool.title" : "Get feature information",
  "GetFeatureInfoTool.help" : "Press on the map to obtain information about the features.",
  "GetFeatureInfoTool.noDataFound" : "No data found.",
  
  "GoHomeControl.title" : "Initial view",
  
  "LegendControl.title" : "Legend",
  
  "MapInfoControl.title" : "Map information",

  "MeasureAreaTool.title" : "Measure area",
  "MeasureAreaTool.help" : "Draw the vertices of the area you want to measure.",
  "MeasureAreaTool.area" : (area, units) => `Area: ${area} ${units}.`,

  "MeasureLengthTool.title" : "Measure length",
  "MeasureLengthTool.help" : "Draw the vertices of the linestring you want to measure.",
  "MeasureLengthTool.length" : (length, units) => `Length: ${length} ${units}.`
  
});


  
