/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.webapp.modules.geo.pdfgen;

import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.maplibre.model.Layer;
import org.santfeliu.faces.maplibre.model.Style;
import org.santfeliu.pdfgen.PdfGenerator;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.security.util.URLCredentialsCipher;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.webapp.modules.geo.io.MapStore.MapDocument;
import org.santfeliu.webapp.modules.geo.io.SldStore;
import org.santfeliu.webapp.modules.geo.metadata.Service;
import org.santfeliu.webapp.modules.geo.metadata.ServiceParameters;
import org.santfeliu.webapp.modules.geo.metadata.StyleMetadata;
import static org.santfeliu.webapp.modules.geo.pdfgen.MapContext.*;
/**
 *
 * @author realor
 */
public class MapViewNode extends MapRectNode
{
  private double svgDPI = 90.0; // TODO: detect dpi from svg (Inkscape: 90 dpi)

  public MapViewNode(String argument)
  {
    super(argument);
  }

  @Override
  public void primitivePaint(Graphics2D g2d)
  {
    try
    {
      System.out.println("\n\n>>>> MapViewNode paint");
      PdfGenerator gen = PdfGenerator.getCurrentInstance();
      Map context = gen.getContext();
      MapContext.init(context);

      MapDocument map = (MapDocument)context.get(MAP);
      Bounds mapBounds = (Bounds)context.get(BOUNDS);
      Style style = map.getStyle();

      String layerVisibility = (String)context.get(LAYER_VISIBILITY);
      Credentials credentials = (Credentials)context.get(CREDENTIALS);

      double scale = (Double)gen.getContext().get(SCALE);

      Rectangle2D svgBounds = getBounds();
      double svgWidth = svgBounds.getWidth();
      double svgHeight = svgBounds.getHeight();

      // adjust aspect ratio
      mapBounds = mapBounds.getAdjusted(svgWidth, svgHeight);

      if (scale != 0)
      {
        // adjust scale
        System.out.println("mapWidth: " + svgWidth);
        double areaWidth = (svgWidth / svgDPI) * 0.0254; // meters
        System.out.println("areaWidth: " + areaWidth);
        double nw = areaWidth * scale;
        double scaleFactor = nw / mapBounds.getWidth();
        System.out.println("scaleFactor: " + scaleFactor);
        mapBounds = mapBounds.getScaled(scaleFactor);
      }
      gen.getContext().put(BOUNDS, mapBounds);

      List<Layer> layers = style.getLayers();

      for (int i = 0; i < layers.size(); i++)
      {
        Layer layer = layers.get(i);
        boolean visible = false;
        if (i < layerVisibility.length())
        {
          visible = layerVisibility.charAt(i) == '1';
        }
        if (visible)
        {
          drawLayer((PdfGraphics2D)g2d, style, layer, mapBounds, gen, credentials);
        }
      }

      // draw highlight
      String lastX = (String)context.get(LAST_X);
      String lastY = (String)context.get(LAST_Y);
      String serviceId = (String)context.get(HILIGHT_SERVICE);
      String hilightLayer = (String)context.get(HILIGHT_LAYER);
      String hilightGeometry = (String)context.get(HILIGHT_GEOMETRY);

      if (lastX != null && lastY != null && serviceId != null &&
          hilightLayer != null && hilightGeometry != null)
      {
        String hilightStyle = (String)context.get(HILIGHT_STYLE);

        drawHighlight((PdfGraphics2D)g2d, map, serviceId,
          hilightLayer, hilightStyle, hilightGeometry,
          lastX, lastY, mapBounds, gen, credentials);
      }

      localPaint(g2d);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void drawLayer(PdfGraphics2D g2d, Style style, Layer layer,
    Bounds mapBounds, PdfGenerator gen, Credentials credentials)
  {
    try
    {
      System.out.println(">> Processing layer id: " + layer.getId());
      System.out.println(">> label: " + layer.getLabel());

      Rectangle2D svgBounds = getBounds();
      double svgWidth = svgBounds.getWidth();
      double svgHeight = svgBounds.getHeight();

      String sourceId = layer.getSource();

      StyleMetadata styleMetadata = new StyleMetadata(style);

      ServiceParameters serviceParameters =
        styleMetadata.getServiceParameters(sourceId, false);
      if (serviceParameters == null) return;

      String serviceId = serviceParameters.getService();
      Service service = styleMetadata.getService(serviceId, false);
      if (service == null) return;

      String sld = serviceParameters.getSldName();

      List<String> layerList = new ArrayList<>();
      List<String> cqlFilterList = new ArrayList<>();
      List<String> styleList = new ArrayList<>();

      mergeLayer(serviceParameters, layer, layerList, styleList, cqlFilterList);

      if (layerList.isEmpty()) return;

      String layers = String.join(",", layerList);
      String styles = String.join(",", styleList);
      String cqlFilters = String.join(";", cqlFilterList);

      if (styles.replace(",", " ").trim().length() == 0) styles = null;
      if (cqlFilters.replace(";", " ").trim().length() == 0) cqlFilters = null;

      System.out.println("Layers: " + layers);
      System.out.println("Styles: " + styles);
      System.out.println("CqlFilters: " + cqlFilters);

      String srs = (String)gen.getContext().get(SRS);

      String url = getWmsUrl(service.getUrl(), srs,
        sld, layers, styles, cqlFilters,
        mapBounds, "application/pdf", svgWidth, svgHeight, credentials);

      System.out.println("url:" + url);

      addPdfLayer(url, g2d, gen);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void mergeLayer(ServiceParameters serviceParameters, Layer layer,
    List<String> layerList, List<String> styleList, List<String> cqlFilterList)
  {
    String[] parts;

    if (!StringUtils.isBlank(serviceParameters.getLayers()))
    {
      parts = serviceParameters.getLayers().split(",");
      layerList.addAll(Arrays.asList(parts));
    }

    if (!StringUtils.isBlank(serviceParameters.getStyles()))
    {
      parts = serviceParameters.getStyles().split(",");
      styleList.addAll(Arrays.asList(parts));
      while (styleList.size() < layerList.size()) styleList.add("");
    }

    if (!StringUtils.isBlank(serviceParameters.getCqlFilter()))
    {
      parts = serviceParameters.getCqlFilter().split(";");
      cqlFilterList.addAll(Arrays.asList(parts));
      while (cqlFilterList.size() < layerList.size()) cqlFilterList.add("1=1");
    }

    if (!StringUtils.isBlank(layer.getLayers()))
    {
      parts = layer.getLayers().split(",");
      layerList.addAll(Arrays.asList(parts));
    }

    if (!StringUtils.isBlank(layer.getStyles()))
    {
      parts = layer.getStyles().split(",");
      styleList.addAll(Arrays.asList(parts));
      while (styleList.size() < layerList.size()) styleList.add("");
    }

    if (!StringUtils.isBlank(layer.getCqlFilter()))
    {
      parts = layer.getCqlFilter().split(",");
      cqlFilterList.addAll(Arrays.asList(parts));
      while (cqlFilterList.size() < layerList.size()) cqlFilterList.add("1=1");
    }
  }

  private void drawHighlight(PdfGraphics2D g2d, MapDocument map,
    String serviceId, String highlightLayer,
    String highlightStyle, String geometryColumn,
    String lastX, String lastY, Bounds mapBounds,
    PdfGenerator gen, Credentials credentials)
  {
    try
    {
      Rectangle2D svgBounds = getBounds();
      double svgWidth = svgBounds.getWidth();
      double svgHeight = svgBounds.getHeight();
      String cqlFilter =
        "contains(" + geometryColumn +
        ", point(" + lastX + " " + lastY + "))";

      Style style = map.getStyle();

      StyleMetadata styleMetadata = new StyleMetadata(style);

      Service service = styleMetadata.getService(serviceId, false);
      if (service == null) return;

      String serviceUrl = service.getUrl();
      String srs = (String)gen.getContext().get(SRS);

      String url = getWmsUrl(serviceUrl, srs, null, highlightLayer,
        highlightStyle, cqlFilter, mapBounds,
        "application/pdf", svgWidth, svgHeight, credentials);
      addPdfLayer(url, g2d, gen);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  @Override
  protected void localPrimitivePaint(Graphics2D g2d, Rectangle2D bounds)
  {
    PdfGenerator pdfgen = PdfGenerator.getCurrentInstance();
    Map context = pdfgen.getContext();

    String lastX = (String)context.get(LAST_X);
    String lastY = (String)context.get(LAST_Y);
    double boxWidth = bounds.getWidth();
    double boxHeight = bounds.getHeight();

    Color color = g2d.getColor();
    g2d.setColor(Color.BLACK);
    if (lastX != null && lastY != null)
    {
      try
      {
        double x = Double.parseDouble(lastX);
        double y = Double.parseDouble(lastY);

        Bounds mapBounds = (Bounds)context.get(BOUNDS);

        mapBounds = mapBounds.getAdjusted(boxWidth, boxHeight);
        int relativeX =
          (int)((x - mapBounds.getMinX()) * (boxWidth / mapBounds.getWidth()));
        int relativeY =
          (int)(boxHeight - ((y - mapBounds.getMinY()) * boxHeight / mapBounds.getHeight()));
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_ROUND, 0.5f));
        g2d.drawLine(relativeX - 4 , relativeY - 4, relativeX + 4 , relativeY + 4);
        g2d.drawLine(relativeX - 4 , relativeY + 4, relativeX + 4 , relativeY - 4);
        g2d.setStroke(oldStroke);
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
    g2d.setColor(Color.BLACK);
    Stroke stroke = g2d.getStroke();
    g2d.setStroke(new BasicStroke(1));
    g2d.drawRect(0, 0,
      (int)Math.round(boxWidth),
      (int)Math.round(boxHeight));
    g2d.setColor(color);
    g2d.setStroke(stroke);
  }

  private void addPdfLayer(String url, PdfGraphics2D g2d, PdfGenerator pdfgen)
    throws Exception
  {
    Rectangle2D svgBounds = getBounds();
    double x = svgBounds.getX();
    double y = svgBounds.getY();
    double svgWidth = svgBounds.getWidth();
    double svgHeight = svgBounds.getHeight();

    Document document = pdfgen.getDocument();

    PdfReader reader = new PdfReader(new URL(url));

    PdfContentByte cb = ((PdfGraphics2D)g2d).getContent();
    PdfWriter writer = cb.getPdfWriter();
    PdfImportedPage layerPage = writer.getImportedPage(reader, 1);
    AffineTransform tr = new AffineTransform();

    double factor = 72.0 / svgDPI;

    Rectangle pdfPageBounds = document.getPageSize();
    double ox = factor * x;
    double oy = pdfPageBounds.getHeight() - factor * (y + svgHeight);

    Rectangle pdfLayerBounds = layerPage.getBoundingBox();
    double sx = factor * svgWidth / pdfLayerBounds.getWidth();
    double sy = factor * svgHeight / pdfLayerBounds.getHeight();

    tr.translate(ox, oy);
    tr.scale(sx, sy);
    double[] matrix = new double[6];
    tr.getMatrix(matrix);
    cb.addTemplate(layerPage,
      (float)matrix[0], (float)matrix[1], (float)matrix[2],
      (float)matrix[3], (float)matrix[4], (float)matrix[5]);
  }

  private String getWmsUrl(String serviceUrl, String srs,
    String sld, String layerNames, String styleNames, String cqlFilter,
    Bounds mapBounds, String format, double mapWidth, double mapHeight,
    Credentials credentials)
  {
    // connect via proxy servlet
    String port = MatrixConfig.getProperty("org.santfeliu.web.defaultPort");
    String contextPath = MatrixConfig.getProperty("contextPath");
    String url = "http://localhost:" + port + contextPath  +
     "/proxy?url=" + serviceUrl +
     "&service=WMS&version=1.1.0&request=GetMap&layers=" + layerNames +
     "&bbox=" + mapBounds +
     "&width=" + (int)Math.round(mapWidth) +
     "&height=" + (int)Math.round(mapHeight) + "&srs=" + srs +
     "&TRANSPARENT=true" +
     "&format=" + format;
    if (!StringUtils.isBlank(styleNames))
    {
      url += "&styles=" + styleNames;
    }
    if (!StringUtils.isBlank(sld))
    {
      SldStore sldStore = new SldStore();
      String sldUrl = sldStore.getSldUrl(sld);
      url += "&sld=" + sldUrl;
    }
    if (!StringUtils.isBlank(cqlFilter))
    {
      try
      {
        url += "&CQL_FILTER=" + URLEncoder.encode(cqlFilter, "UTF-8");
      }
      catch (Exception ex)
      {
      }
    }
    if (credentials != null)
    {
      URLCredentialsCipher urlCredentialsCipher =
        SecurityUtils.getURLCredentialsCipher();
      url = urlCredentialsCipher.putCredentials(url, credentials);
    }
    System.out.println("URL: " + url);
    return url;
  }

  public static void main(String[] args)
  {
    try
    {
      PdfGenerator gen = PdfGenerator.getCurrentInstance();
      gen.registerBridge(MapRectElementBridge.class);
      gen.registerBridge(MapTextElementBridge.class);

      MatrixConfig.setProperty("org.santfeliu.web.defaultPort", "80");
      MatrixConfig.setProperty("contextPath", "");

      Map context = gen.getContext();
      context.put("title", "Guia d'entitats");
      context.put("map_name", "planejament");
      context.put("bbox", "420181.21738675,4581632.2185804,420296.41818475,4581698.3702992");
      gen.open(new FileOutputStream("c:/users/realor/Desktop/a4v.pdf"));
      gen.addPage(new File("c:/users/realor/Desktop/a4v.svg"));
      //gen.addPage(new File("./samples/dibujo2.svg"));
      gen.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
