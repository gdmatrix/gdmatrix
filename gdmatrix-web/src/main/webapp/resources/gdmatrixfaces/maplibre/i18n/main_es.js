/* bundle main_es.js */

import { Bundle } from "../i18n/Bundle.js";

Bundle.getBundle("main").setTranslations("es", {
  "button.accept": "Aceptar",
  "button.cancel": "Cancelar",
  "button.find": "Buscar",
  "button.clear": "Limpiar",
  "button.reset": "Inicializar",
  "button.undo": "Deshacer",
  "button.export": "Exportar",
  "button.close": "Cerrar",
  "button.print": "Imprimir",

  "ExportAreaTool.title": "Exportar area",
  "ExportAreaTool.help": "Dibuja los vértices del area de exportación i pulsa el botón Exportar.",
  "ExportAreaTool.noLayersToExport": "Ninguna de las capas visibles es exportable.",

  "FindFeatureControl.title": "Buscar",
  "FindFeatureControl.finderLabel": "Selecciona buscador:",
  "FindFeatureControl.clearMarkers": "Limpiar marcadores anteriores",
  "FindFeatureControl.featureCount": (count) => count === 1 ? `${count} entitad encontrada` : `${count} entitades encontradas`,

  "GetFeatureInfoTool.title": "Obtener información",
  "GetFeatureInfoTool.help": "Pulsa sobre el mapa para obtener información de las entitades.",
  "GetFeatureInfoTool.noDataFound": "Datos no encontrados.",
  
  "GoHomeControl.title": "Vista inicial",
  
  "LegendControl.title": "Leyenda",
  
  "MapInfoControl.title": "Información del mapa",

  "MeasureAreaTool.title": "Medir área",
  "MeasureAreaTool.help": "Dibuja los vértices del área que quieres medir.",
  "MeasureAreaTool.area": (area, units) => `Área: ${area} ${units}.`,
  
  "MeasureLengthTool.title": "Medir longitud",
  "MeasureLengthTool.help": "Dibuja los vertices de la linea que quieres medir.",
  "MeasureLengthTool.length": (length, units) => `Longitud: ${length} ${units}.`,
  
  "ZoomControl.title": "Zoom",
  "ZoomControl.zoom": (zoom) => `Zoom: ${zoom}`,
  
  "PrintControl.title": "Imprimir",
  "PrintControl.reportName": "Plantilla",
  "PrintControl.reportTitle": "Título",
  "PrintControl.scale": "Escala de impresión",
  "PrintControl.currentWindow": "Ventana actual",
  
  "RefreshControl.title": "Refrescar",

  "WfsFinder.title": "Buscador WFS",
  "WfsFinder.layer": "Capa",
  "WfsFinder.filter": "Filtro (CQL)",
  "WfsFinder.outputExpression" : "Expresión de salida (JS)",
  "WfsFinder.sortBy" : "Ordenar por",
  "WfsFinder.addMarkers": "Añadir marcadores"  

});
