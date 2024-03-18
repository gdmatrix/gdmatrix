/* bundle main.js */

import { Bundle } from "../i18n/Bundle.js";

Bundle.getBundle("main").setTranslations("", {
  "button.accept": "Accept",
  "button.cancel": "Cancel",
  "button.find": "Find",
  "button.clear": "Clear",
  "button.reset": "Reset",
  "button.undo": "Undo",
  "button.export": "Export",
  "button.close": "Close",
  "button.print": "Print",

  "ExportAreaTool.title": "Export area",
  "ExportAreaTool.help": "Draw the vertices of the export area and press the Export button.",
  "ExportAreaTool.noLayersToExport": "None of the visible layers can be exported.",
  
  "FindFeatureControl.title": "Find",
  "FindFeatureControl.finderLabel": "Select finder:",
  "FindFeatureControl.clearMarkers": "Clear previous markers",
  "FindFeatureControl.featureCount": (count) => count === 1 ? `${count} feature found:` : `${count} features found:`,
  
  "GetFeatureInfoTool.title": "Get feature information",
  "GetFeatureInfoTool.help": "Press on the map to obtain information about the features.",
  "GetFeatureInfoTool.noDataFound": "No data found.",
  
  "GoHomeControl.title": "Initial view",
  
  "LegendControl.title": "Legend",
  
  "MapInfoControl.title": "Map information",

  "MeasureAreaTool.title": "Measure area",
  "MeasureAreaTool.help": "Draw the vertices of the area you want to measure.",
  "MeasureAreaTool.area": (area, units) => `Area: ${area} ${units}.`,

  "MeasureLengthTool.title": "Measure length",
  "MeasureLengthTool.help": "Draw the vertices of the linestring you want to measure.",
  "MeasureLengthTool.length": (length, units) => `Length: ${length} ${units}.`,
  
  "ZoomControl.title": "Zoom",
  "ZoomControl.zoom": (zoom) => `Zoom: ${zoom}`,
  
  "PrintControl.title": "Print",
  "PrintControl.printFormat": "Print format",
  "PrintControl.reportName": "Template",
  "PrintControl.reportTitle": "Title",
  "PrintControl.scale": "Printing scale",
  "PrintControl.currentWindow": "Current window",
    
  "RefreshControl.title": "Refresh",
  
  "TimeSliderControl.period": "Period",
  "TimeSliderControl.period.d": "1 day",
  "TimeSliderControl.period.w": "1 week",
  "TimeSliderControl.period.M": "1 month",
  "TimeSliderControl.period.y": "1 year",
  "TimeSliderControl.period.2": "between 2 dates",
  
  "GenericWfsFinder.title": "WFS finder",
  "GenericWfsFinder.layer": "Layer",
  "GenericWfsFinder.filter": "Filter (CQL)",
  "GenericWfsFinder.outputExpression" : "Output expression (JS)",
  "GenericWfsFinder.sortBy" : "Sort by",
  "GenericWfsFinder.addMarkers": "Add markers"

});


  
