/* bundle main_ca.js */

import { Bundle } from "../i18n/Bundle.js";

Bundle.getBundle("main").setTranslations("ca", {
  "button.accept" : "Accepta",
  "button.cancel" : "Cancel·la",
  "button.find" : "Cerca",
  "button.clear" : "Neteja",
  "button.reset" : "Inicialitza",
  "button.undo" : "Desfer",
  "button.export" : "Exporta",

  "ExportAreaTool.title": "Exporta àrea",
  "ExportAreaTool.help": "Dibuixa els vèrtexs de l'àrea d'exportació i prem el botó Exporta.",

  "FindControl.title": "Cerca",
  "FindControl.layer": "Capa",
  "FindControl.filter": "Filtre",
  "FindControl.featureCount": (count) => count === 1 ? `${count} entitat trobada` : `${count} entitats trobades`,
  
  "GetFeatureInfoTool.title" : "Obtenir informació",
  "GetFeatureInfoTool.help" : "Prem sobre el mapa per obtenir informació de les entitats.",
  "GetFeatureInfoTool.noDataFound" : "No s'han trobat dades.",
  
  "GoHomeControl.title" : "Vista inicial",
  
  "LegendControl.title" : "Llegenda",

  "MapInfoControl.title" : "Informació del mapa",

  "MeasureAreaTool.title" : "Mesura àrea",
  "MeasureAreaTool.help" : "Dibuixa els vèrtexs de l'àrea que vols mesurar.",
  "MeasureAreaTool.area" : (area, units) => `Àrea: ${area} ${units}.`,

  "MeasureLengthTool.title" : "Mesura longitud",
  "MeasureLengthTool.help" : "Dibuixa els vèrtexs de la línia que vols mesurar.",
  "MeasureLengthTool.length" : (length, units) => `Longitud: ${length} ${units}.`
});
