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
  "button.load": "Cargar",
  "button.save": "Guardar",

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
  
  "PopupOnHoverTool.title": "Obtener información",    
  
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
  "PrintControl.printFormat": "Formato de impresión",
  "PrintControl.reportName": "Plantilla",
  "PrintControl.reportTitle": "Título",
  "PrintControl.scale": "Escala de impresión",
  "PrintControl.currentWindow": "Ventana actual",
  
  "RefreshControl.title": "Refrescar",
  
  "DrawTool.title": "Dibujar",
  "DrawTool.layerName": "Capa a editar",
  "DrawTool.operation": "Operación",
  "DrawTool.edit": "Editar",
  "DrawTool.move": "Mover",
  "DrawTool.rotate": "Rotar",
  "DrawTool.copy": "Copiar",
  "DrawTool.addPoint": "Añadir punto",
  "DrawTool.addLineString": "Añadir linea",
  "DrawTool.addPolygon": "Añadir polígono",
  "DrawTool.delete": "Borrar",
  "DrawTool.edit.help": "Haz clic en la entidad y arrastra sus vértices. Doble clic para añadir o borrar un vértice.",
  "DrawTool.move.help": "Haz clic en la entidad y arrástrala desde un punto interior.",
  "DrawTool.rotate.help": "Haz clic en la entidad y rótala arrastrando horizontalmente desde un punto interior.",
  "DrawTool.copy.help": "Haz clic en la entidad y arrástrala desde un punto interior.",
  "DrawTool.addPoint.help": "Haz clic para situar el punto.",
  "DrawTool.addLineString.help": "Haz sucesivos clics para dibujar los vértices de la línea.",
  "DrawTool.addPolygon.help": "Haz sucesivos clics para dibujar los vértices del polígono.",
  "DrawTool.delete.help": "Haz clic en la entidad que deseas borrar.",
  "DrawTool.totalInserted": (num) => num === 1 ? "1 elemento añadido." : `${num} elementos añadidos.`,
  "DrawTool.totalUpdated": (num) => num === 1 ? "1 elemento modificado." : `${num} elementos modificados.`,
  "DrawTool.totalDeleted": (num) => num === 1 ? "1 elemento borrado." : `${num} elementos borrados.`,  

  "TimeSliderControl.period": "Periodo",
  "TimeSliderControl.period.d": "1 día",
  "TimeSliderControl.period.w": "1 semana",
  "TimeSliderControl.period.M": "1 mes",
  "TimeSliderControl.period.y": "1 año",
  "TimeSliderControl.period.2": "entre 2 fechas",

  "SimulateRouteControl.view" : "Vista",
  "SimulateRouteControl.view.manual" : "Manual",
  "SimulateRouteControl.view.centered" : "Centrada",
  "SimulateRouteControl.view.navigation" : "Navegación",  
  "SimulateRouteControl.start" : "Iniciar ruta",
  "SimulateRouteControl.pause" : "Pausar ruta",
  "SimulateRouteControl.showProfile" : "Mostrar perfil",
  
  "ChartControl.loading" : "Cargando",    
  "ChartControl.noData" : "No hay datos.",
  
  "GenericWfsFinder.title": "Buscador WFS",
  "GenericWfsFinder.layer": "Capa",
  "GenericWfsFinder.filter": "Filtro (CQL)",
  "GenericWfsFinder.outputExpression" : "Expresión de salida (JS)",
  "GenericWfsFinder.sortBy" : "Ordenar por",
  "GenericWfsFinder.addMarkers": "Añadir marcadores",
  
  "SnapshotControl.title": "Crear captura del mapa",
  
  "PickupTool.title": "Recogida de elementos"

});
