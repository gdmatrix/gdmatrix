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
  "button.load": "Carrega",
  "button.save": "Desa",

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

  "PopupOnHoverTool.title": "Obtenir informació",  
  
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

  "DrawTool.title": "Dibuixa",
  "DrawTool.layerName": "Capa a editar",
  "DrawTool.operation": "Operació",
  "DrawTool.edit": "Editar",
  "DrawTool.move": "Moure",
  "DrawTool.rotate": "Rotar",
  "DrawTool.copy": "Copiar",
  "DrawTool.addPoint": "Afegir punt",
  "DrawTool.addLineString": "Afegir línia",
  "DrawTool.addPolygon": "Afegir polígon",
  "DrawTool.delete": "Esborrar",
  "DrawTool.edit.help": "Fes clic a l'entitat i arrossega els seus vèrtexs. Doble clic per afegir o esborrar un vèrtex.",
  "DrawTool.move.help": "Fes clic a l'entitat i arrossega-la des d'un punt interior.",
  "DrawTool.rotate.help": "Fes clic a l'entitat i rota-la arrossegant horitzontalment des d'un punt interior.",
  "DrawTool.copy.help": "Fes clic a l'entitat i arrossega-la des d'un punt interior.",
  "DrawTool.addPoint.help": "Fes clic per situar el punt.",
  "DrawTool.addLineString.help": "Fes successius clics per dibuixar els vèrtexs de la línia.",
  "DrawTool.addPolygon.help": "Fes successius clics per dibuixar els vèrtexs del polígon.",
  "DrawTool.delete.help": "Fes clic a l'entitat que vols esborrar.",
  "DrawTool.totalInserted": (num) => num === 1 ? "1 element afegit." : `${num} elements afegits.`,
  "DrawTool.totalUpdated": (num) => num === 1 ? "1 element modificat." : `${num} elements modificats.`,
  "DrawTool.totalDeleted": (num) => num === 1 ? "1 element esborrat." : `${num} elements esborrats.`,  

  "TimeSliderControl.period": "Periode",
  "TimeSliderControl.period.d": "1 dia",
  "TimeSliderControl.period.w": "1 setmana",
  "TimeSliderControl.period.M": "1 mes",
  "TimeSliderControl.period.y": "1 any",
  "TimeSliderControl.period.2": "entre 2 dates",

  "SimulateRouteControl.view" : "Vista",
  "SimulateRouteControl.view.manual" : "Manual",
  "SimulateRouteControl.view.centered" : "Centrada",
  "SimulateRouteControl.view.navigation" : "Navegació",  
  "SimulateRouteControl.start" : "Inicia ruta",
  "SimulateRouteControl.pause" : "Pausa ruta",
  "SimulateRouteControl.showProfile" : "Mostra perfil",

  "ChartControl.loading" : "Carregant",  
  "ChartControl.noData" : "No hi ha dades.",
  
  "GenericWfsFinder.title": "Cercador WFS",
  "GenericWfsFinder.layer": "Capa",
  "GenericWfsFinder.filter": "Filtre (CQL)",
  "GenericWfsFinder.outputExpression" : "Expressió de sortida (JS)",
  "GenericWfsFinder.sortBy" : "Ordena per",
  "GenericWfsFinder.addMarkers": "Afegeix marcadors",
  
  "SnapshotControl.title": "Crea captura del mapa",
  
  "PickupTool.title": "Recollida d'elements"

});
