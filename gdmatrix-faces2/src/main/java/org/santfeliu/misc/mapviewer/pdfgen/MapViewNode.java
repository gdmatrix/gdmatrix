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
package org.santfeliu.misc.mapviewer.pdfgen;

import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
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
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.misc.mapviewer.Bounds;
import org.santfeliu.misc.mapviewer.Map.Layer;
import org.santfeliu.misc.mapviewer.MapDocument;
import org.santfeliu.misc.mapviewer.SLDStore;
import org.santfeliu.misc.mapviewer.ServiceCache;
import org.santfeliu.misc.mapviewer.ServiceCapabilities;
import org.santfeliu.misc.mapviewer.util.LayerMerger;
import org.santfeliu.pdfgen.PdfGenerator;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.security.util.URLCredentialsCipher;
import org.santfeliu.util.MatrixConfig;

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
      PdfGenerator pdfgen = PdfGenerator.getCurrentInstance();
      Map context = pdfgen.getContext();
      MapContext.init(context);

      MapDocument map = (MapDocument)context.get(MapContext.MAP);
      Bounds mapBounds = (Bounds)context.get(MapContext.BOUNDS);
      List<Layer> layers = map.getLayers();
      String layerVisibility = (String)context.get(MapContext.LAYER_VISIBILITY);
      Credentials credentials = (Credentials)context.get(MapContext.CREDENTIALS);

      double scale = (Double)pdfgen.getContext().get(MapContext.SCALE);

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
      pdfgen.getContext().put(MapContext.BOUNDS, mapBounds);

      LayerMerger merger = new LayerMerger();
      for (int i = 0; i < layers.size(); i++)
      {
        Layer layer = layers.get(i);
        boolean visible = false;
        if (layerVisibility == null)
        {
          visible = layer.isVisible(); // default visibility
        }
        else if (i < layerVisibility.length())
        {
          visible = layerVisibility.charAt(i) == '1';
        }
        if (visible)
        {
          if (!merger.merge(layer))
          {
            drawLayer((PdfGraphics2D)g2d, map, merger, mapBounds, pdfgen,
              credentials);
            merger.reset();
            merger.merge(layer);
          }
        }
      }
      drawLayer((PdfGraphics2D)g2d, map, merger, mapBounds, pdfgen,
        credentials);

      // draw hilight
      String lastX = (String)context.get(MapContext.LAST_X);
      String lastY = (String)context.get(MapContext.LAST_Y);
      String hilightLayer = (String)context.get(MapContext.HILIGHT_LAYER);
      String hilightGeometry = (String)context.get(MapContext.HILIGHT_GEOMETRY);
      if (lastX != null && lastY != null && 
          hilightLayer != null && hilightGeometry != null)
      {
        String hilightStyle = (String)context.get(MapContext.HILIGHT_STYLE);        
        drawHilightLayer((PdfGraphics2D)g2d, map, 
          hilightLayer, hilightStyle, hilightGeometry,
          lastX, lastY, mapBounds, pdfgen, credentials);
      }
      localPaint(g2d);
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void drawLayer(PdfGraphics2D g2d, 
    MapDocument map, LayerMerger merger, Bounds mapBounds,
    PdfGenerator pdfgen, Credentials credentials)
  {
    try
    {
      Rectangle2D svgBounds = getBounds();
      double svgWidth = svgBounds.getWidth();
      double svgHeight = svgBounds.getHeight();

      String serviceUrl = merger.getServiceUrl();
      ServiceCapabilities capabilities =
        ServiceCache.getServiceCapabilities(serviceUrl, false, credentials);
      List<String> formats = capabilities.getRequest().getGetMap().getFormats();
      if (formats.indexOf("application/pdf") != -1)
      {
        String url = getWmsUrl(serviceUrl, map.getSrs(), merger, mapBounds,
         "application/pdf", svgWidth, svgHeight, credentials);
        addPdfLayer(url, g2d, pdfgen);
      }
      else if (formats.indexOf("image/png") != -1)
      {
        String url = getWmsUrl(serviceUrl, map.getSrs(), merger, mapBounds,
         "image/png", svgWidth, svgHeight, credentials);
        addImageLayer(url, g2d, pdfgen);
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  private void drawHilightLayer(PdfGraphics2D g2d, MapDocument map, 
    String hilightLayer, String hilightStyle, String hilightGeometry,
    String lastX, String lastY, Bounds mapBounds,
    PdfGenerator pdfgen, Credentials credentials)
  {
    try
    {
      Rectangle2D svgBounds = getBounds();
      double svgWidth = svgBounds.getWidth();
      double svgHeight = svgBounds.getHeight();
      String cqlFilter =
        "contains(" + hilightGeometry +
        ", point(" + lastX + " " + lastY + "))";

      String serviceUrl = map.getServices().get(0).getUrl();
      ServiceCapabilities capabilities =
        ServiceCache.getServiceCapabilities(serviceUrl, false, credentials);
      List<String> formats = capabilities.getRequest().getGetMap().getFormats();
      if (formats.indexOf("application/pdf") != -1)
      {
        String url = getWmsUrl(serviceUrl, map.getSrs(), hilightLayer,
          hilightStyle, null, cqlFilter, mapBounds,
          "application/pdf", svgWidth, svgHeight, credentials);
        addPdfLayer(url, g2d, pdfgen);
      }
      else if (formats.indexOf("image/png") != -1)
      {
        String url = getWmsUrl(serviceUrl, map.getSrs(), hilightLayer,
          hilightStyle, null, cqlFilter, mapBounds,
          "image/png", svgWidth, svgHeight, credentials);
        addImageLayer(url, g2d, pdfgen);
      }
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

    String lastX = (String)context.get(MapContext.LAST_X);
    String lastY = (String)context.get(MapContext.LAST_Y);
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

        Bounds mapBounds = (Bounds)context.get(MapContext.BOUNDS);
        
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

  private void addImageLayer(String url, PdfGraphics2D g2d, PdfGenerator pdfgen)
    throws Exception
  {
    Rectangle2D svgBounds = getBounds();
    double x = svgBounds.getX();
    double y = svgBounds.getY();
    double svgWidth = svgBounds.getWidth();
    double svgHeight = svgBounds.getHeight();

    double factor = 72.0 / svgDPI;

    Document document = pdfgen.getDocument();

    Rectangle pdfPageBounds = document.getPageSize();

    double ox = factor * x;
    double oy = pdfPageBounds.getHeight() - factor * (y + svgHeight);
    double imageWidth = factor * svgWidth;
    double imageHeight = factor * svgHeight;

    Image image = Image.getInstance(new URL(url));
    image.setAbsolutePosition((float)ox, (float)oy);
    image.scaleToFit((float)imageWidth, (float)imageHeight);

    document.add(image);
  }

  private String getWmsUrl(String serviceUrl, String srs,
    LayerMerger merger, Bounds mapBounds, String format,
    double mapWidth, double mapHeight, Credentials credentials)
  {
    return getWmsUrl(serviceUrl, srs, merger.getLayerNames(),
      merger.getStyleNames(), merger.getSld(), merger.getCqlFilter(),
      mapBounds, format, mapWidth, mapHeight, credentials);
  }

  private String getWmsUrl(String serviceUrl, String srs,
    String layerNames, String styleNames, String sld, String cqlFilter,
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
      String sldUrl = SLDStore.getSldURL(sld);
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
      PdfGenerator.registerBridge(MapRectElementBridge.class);
      PdfGenerator.registerBridge(MapTextElementBridge.class);

      MatrixConfig.setProperty("org.santfeliu.web.defaultPort", "80");
      MatrixConfig.setProperty("contextPath", "");

      PdfGenerator gen = new PdfGenerator();
      Map context = gen.getContext();
      context.put("title", "Guia d'entitats");
      context.put("map_name", "entitats_guia");
      context.put("bbox", "420181.21738675,4581632.2185804,420296.41818475,4581698.3702992");
      gen.open(new FileOutputStream("c:/a4v.pdf"));
      gen.addPage(new File("c:/a4v.svg"));
      //gen.addPage(new File("./samples/dibujo2.svg"));
      gen.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
