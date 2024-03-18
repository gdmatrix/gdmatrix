/* bundle main_ca.js */

import { Bundle } from "../i18n/Bundle.js";

Bundle.getBundle("main").setTranslations("ca", {
  "button.accept": "Accepta",
  "button.cancel": "Cancel·la",
  "button.find": "Cerca",
  "button.clear": "Neteja",
  "button.reset": "Inicialitza",
  "button.undo": "Desfer",
  "button.export": "Exporta",
  "button.close": "Tanca",
  "button.print": "Imprimir",

  "ExportAreaTool.title": "Exporta àrea",
  "ExportAreaTool.help": "Dibuixa els vèrtexs de l'àrea d'exportació i prem el botó Exporta.",
  "ExportAreaTool.noLayersToExport": "Cap de les capes visibles és exportable.",

  "FindFeatureControl.title": "Cerca",
  "FindFeatureControl.finderLabel": "Selecciona cercador:",
  "FindFeatureControl.clearMarkers": "Neteja marcadors anteriors",
  "FindFeatureControl.featureCount": (count) => count === 1 ? `${count} entitat trobada` : `${count} entitats trobades`,
  
  "GetFeatureInfoTool.title": "Obtenir informació",
  "GetFeatureInfoTool.help": "Prem sobre el mapa per obtenir informació de les entitats.",
  "GetFeatureInfoTool.noDataFound": "No s'han trobat dades.",
  
  "GoHomeControl.title": "Vista inicial",
  
  "LegendControl.title": "Llegenda",

  "MapInfoControl.title": "Informació del mapa",

  "MeasureAreaTool.title": "Mesura àrea",
  "MeasureAreaTool.help": "Dibuixa els vèrtexs de l'àrea que vols mesurar.",
  "MeasureAreaTool.area": (area, units) => `Àrea: ${area} ${units}.`,

  "MeasureLengthTool.title": "Mesura longitud",
  "MeasureLengthTool.help": "Dibuixa els vèrtexs de la línia que vols mesurar.",
  "MeasureLengthTool.length": (length, units) => `Longitud: ${length} ${units}.`,
  
  "ZoomControl.title": "Zoom",
  "ZoomControl.zoom": (zoom) => `Zoom: ${zoom}`,
  
  "PrintControl.title": "Imprimir",
  "PrintControl.printFormat": "Format d'impressió",
  "PrintControl.reportName": "Plantilla",
  "PrintControl.reportTitle": "Títol",
  "PrintControl.scale": "Escala d'impressió",
  "PrintControl.currentWindow": "Finestra actual",
  
  "RefreshControl.title": "Actualitza",

  "TimeSliderControl.period": "Periode",
  "TimeSliderControl.period.d": "1 dia",
  "TimeSliderControl.period.w": "1 setmana",
  "TimeSliderControl.period.M": "1 mes",
  "TimeSliderControl.period.y": "1 any",
  "TimeSliderControl.period.2": "entre 2 dates",  
  
  "GenericWfsFinder.title": "Cercador WFS",
  "GenericWfsFinder.layer": "Capa",
  "GenericWfsFinder.filter": "Filtre (CQL)",
  "GenericWfsFinder.outputExpression" : "Expressió de sortida (JS)",
  "GenericWfsFinder.sortBy" : "Ordena per",
  "GenericWfsFinder.addMarkers": "Afegeix marcadors"  

});
