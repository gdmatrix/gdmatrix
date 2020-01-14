package org.santfeliu.misc.mapviewer.util;

import java.net.URLEncoder;
import org.santfeliu.util.net.HttpClient;

/**
 *
 * @author realor
 */
public class MapImageGenerator
{
  private String layers;
  private String styles;
  private String ogcServerUrl;
  private String searchLayer;
  private double bufferSize = 100;
  private String srs = "EPSG:4326";
  private String format = "image/png";

  public String getOgcServerUrl()
  {
    return ogcServerUrl;
  }

  public void setOgcServerUrl(String ogcServerUrl)
  {
    this.ogcServerUrl = ogcServerUrl;
  }

  public String getLayers()
  {
    return layers;
  }

  public void setLayers(String layers)
  {
    this.layers = layers;
  }

  public String getStyles()
  {
    return styles;
  }

  public void setStyles(String styles)
  {
    this.styles = styles;
  }

  public String getSearchLayer()
  {
    return searchLayer;
  }

  public void setSearchLayer(String searchLayer)
  {
    this.searchLayer = searchLayer;
  }

  public double getBufferSize()
  {
    return bufferSize;
  }

  public void setBufferSize(double bufferSize)
  {
    this.bufferSize = bufferSize;
  }

  public String getSrs()
  {
    return srs;
  }

  public void setSrs(String srs)
  {
    this.srs = srs;
  }

  public String getFormat()
  {
    return format;
  }

  public void setFormat(String format)
  {
    this.format = format;
  }

  public String getImageUri(String cqlFilter, int size)
  {
    try
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append(ogcServerUrl);
      buffer.append("?service=WFS&version=1.0.0&request=GetFeature&typeName=");
      buffer.append(searchLayer);
      buffer.append("&outputFormat=text/xml;%20subtype=gml/3.1.1");
      buffer.append("&maxFeatures=1");
      buffer.append("&cql_filter=");
      buffer.append(URLEncoder.encode(cqlFilter, "UTF-8"));
      String urlString = buffer.toString();
      HttpClient client = new HttpClient();
      client.setURL(urlString);
      client.doGet();
      String gml = client.getContentAsString();

      // IMPORTANT: assume searchLayer has fields XPOS and YPOS.
      // TODO: parse GML, and get geometry centroid
      double x = getFeatureAttribute(gml, "XPOS");
      double y = getFeatureAttribute(gml, "YPOS");
      double offset = bufferSize / 2.0;
      double xmin = x - offset;
      double xmax = x + offset;
      double ymin = y - offset;
      double ymax = y + offset;
      // build WMS request
      buffer.setLength(0);
      buffer.append("proxy?url=");
      buffer.append(ogcServerUrl);
      buffer.append("&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap");
      buffer.append("&LAYERS=");
      buffer.append(layers);
      if (styles != null)
      {
        buffer.append("&STYLES=");
        buffer.append(styles);
      }
      buffer.append("&SRS=");
      buffer.append(URLEncoder.encode(srs, "UTF-8"));
      buffer.append("&BBOX=");
      buffer.append(xmin);
      buffer.append(",");
      buffer.append(ymin);
      buffer.append(",");
      buffer.append(xmax);
      buffer.append(",");
      buffer.append(ymax);
      buffer.append("&WIDTH=");
      buffer.append(size);
      buffer.append("&HEIGHT=");
      buffer.append(size);
      buffer.append("&FORMAT=");
      buffer.append(format);
      return buffer.toString();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }

  private double getFeatureAttribute(String gml, String fieldName)
    throws Exception
  {
    int index = gml.indexOf(fieldName + ">");
    if (index != -1)
    {
      int index2 = gml.indexOf("<", index);
      String value = gml.substring(index + fieldName.length() + 1, index2);
      return Double.parseDouble(value);
    }
    throw new Exception();
  }

  public static void main(String[] args)
  {
    try
    {
      MapImageGenerator gen = new MapImageGenerator();
      gen.setOgcServerUrl("http://xxx.yyy.zzz.www:pppp/geoserver/sf/ows");
      gen.setLayers("sf:cadastre,sf:SEMAFORS"); // layer list
      gen.setSearchLayer("sf:CERCA_FINCA");
      gen.setBufferSize(100);
      gen.setSrs("EPSG:23031");
      gen.setFormat("image/png");
      String imageUri = gen.getImageUri("RF like '02169%'", 300);
      System.out.println("http://localhost/" + imageUri);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
